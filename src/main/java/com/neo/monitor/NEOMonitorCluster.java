package com.neo.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.neo.module.bo.ModuleBo;
import com.neo.module.service.ModuleService;

public class NEOMonitorCluster {

	@Autowired
	@Qualifier("listModule")
	private List<ModuleBo> jobs;

	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;

	/**
	 * lưu trữ những modul client đang kết nối đến
	 */
	@Autowired
	@Qualifier("mapJobSocket")
	private ConcurrentHashMap<ModuleBo, SocketChannel> map;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	@Qualifier("schedulerFactory")
	private SchedulerFactoryBean scheduler;
	
	@Autowired
	@Qualifier("bigData")
	private CronTriggerFactoryBean bigData;

	@Autowired
	@Qualifier("jobBigData")
	private JobDetailFactoryBean jobBigData;

	@Autowired
	@Qualifier("retry")
	private ConcurrentHashMap<ModuleBo, SocketChannel> retry;

	@Autowired
	@Qualifier("thisModule")
	private ModuleBo moduleBo;// sử dụng một bean để lưu trữ lại thông tin của bản thân job này

	@Autowired
	private ApplicationContext context;// sử dụng để trong trường hợp không tạo được db thì tắt job

	private final Logger logger = LoggerFactory.getLogger(NEOMonitorCluster.class);

	public void run() throws IOException {
		try {
			jobs = moduleService.getAllModule();
		} catch (Exception e) {
			logger.info("exception {}", e);
			int exitValue = SpringApplication.exit(context);
			System.exit(exitValue);
		}
		String serverName = pro.getString("module.name").trim();

		ModuleBo server = null;
		for (ModuleBo moduleBo : jobs) {
			if (serverName.trim().equals(moduleBo.getModuleName().trim())) {
				server = moduleBo;
				this.moduleBo.setId(moduleBo.getId());
				this.moduleBo.setIp(moduleBo.getIp());
				this.moduleBo.setIsMaster(moduleBo.getIsMaster());
				this.moduleBo.setModuleGroup(moduleBo.getModuleGroup());
				this.moduleBo.setModuleName(moduleBo.getModuleName());
				this.moduleBo.setPort(moduleBo.getPort());
				this.moduleBo.setState(moduleBo.getState());
				this.moduleBo.setStartDate(moduleBo.getStartDate());
				break;
			}
		}
		if (server == null) {
			logger.info("Shut Down module : {} because module do not exist in data base, NEOMonitorCluster 116",
					pro.getString("module.name"));
			int exitValue = SpringApplication.exit(context);
			System.exit(exitValue);
		}
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		try {
			logger.info("Create ServerSocketChannel : {}", pro.getString("module.name"));
			serverSocketChannel.socket().bind(new InetSocketAddress(server.getPort().intValue()));// tạo server cho
		} catch (Exception e) {
			logger.info("Shut Down module : {}  {}", pro.getString("module.name"));
			int exitValue = SpringApplication.exit(context);
			System.exit(exitValue);
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						logger.info("Server waiting connect.....");
						SocketChannel socketChannel = serverSocketChannel.accept();
						logger.info("One module connected ");
						socketChannel.socket().setKeepAlive(true);
						SocketChannel socketChannelTmp = null;
						InputStream in = socketChannel.socket().getInputStream();
						OutputStream out = socketChannel.socket().getOutputStream();
						byte tmp[] = new byte[100];
						String jobNameClient = null;
						try {
							in.read(tmp);
							// server gửi thông tin của nó cho client
							out.write(moduleBo.getModuleName().getBytes());
							jobNameClient = new String(tmp);
						} catch (Exception e) {
							jobNameClient = "";
						}
						logger.info("module connected {}", jobNameClient);
						for (Map.Entry<ModuleBo, SocketChannel> entry : map.entrySet()) {
							if (jobNameClient.trim().indexOf(entry.getKey().getModuleName().trim()) != -1) {
								socketChannelTmp = entry.getValue();
								if (entry.getKey().getState() == 0L && moduleBo.getIsMaster() == 1) {
									ModuleBo tmpm = entry.getKey();
									tmpm.setState(1L);
									moduleService.updateModule(tmpm);
									logger.info("update database");
								}
							}
						}
						if (socketChannelTmp == null) {
							ModuleBo moduleBotmp = moduleService.getModule(jobNameClient.trim());
							if (moduleBotmp != null) {
								logger.info("add module {} in map", moduleBotmp.getModuleName());
								moduleBotmp.setState(1L);
								map.put(moduleBotmp, socketChannel);
							} else {
								logger.info("close socket");
								socketChannel.close();
							}
							if (moduleBo.getIsMaster() == 1 && moduleBotmp != null) {
								logger.info("update modul conected {}", moduleBotmp.getModuleName());
								moduleService.updateModule(moduleBotmp);
								if (moduleBo.getState() == 0) {
									moduleBo.setState(1L);
									moduleService.updateModule(moduleBo);
								}
								logger.info("update database");
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();

				}

			}
		}).start();

		getAllClient(jobs);

		if (moduleBo.getIsMaster() == 1) {
			addJobMaster();
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				ExecutorService executor = Executors.newFixedThreadPool(20);
				List<String> checked = new ArrayList<String>();// lưu trữ thông tin các job đã xác nhận
				while (true) {
					if (!retry.isEmpty()) {// thực hiện thêm những module retry ở lần chạy trước vào map
						updateRetry();
						logger.info("Add Success list module retry in list servers can connect, map size : {}",
								map.size());
					}
					@SuppressWarnings("unused")
					ModuleBo tmp = null;
					List<ModuleBo> jobsTmp = new ArrayList<ModuleBo>();// lưu trữ các modul không retry được
					for (Map.Entry<ModuleBo, SocketChannel> entry : map.entrySet()) {
						ModuleBo jobBoTmp = null;
						SocketChannel obChannel = entry.getValue();

						OutputStream out = null;
						InputStream in = null;
						@SuppressWarnings("unused")
						ModuleBo jobtmp = null;
						try {
							out = obChannel.socket().getOutputStream();
							in = obChannel.socket().getInputStream();
							out.write(moduleBo.getModuleName().getBytes());// gửi thông tin của client cho server
							out.flush();
							// xác nhận thông tin
							if (!isChecked(entry.getKey().getModuleName(), checked)) {
								checked.add(entry.getKey().getModuleName());
								byte serverInfor[] = new byte[100];
								in.read(serverInfor);
								String info = new String(serverInfor);
								logger.info("information respon from server  : {}", info);
								if (info.indexOf(entry.getKey().getModuleName().trim()) != -1) {
									// xác nhận thông tin
									logger.info("successful confirmation  : {}", info);
									InetSocketAddress address = new InetSocketAddress(entry.getKey().getIp(),
											entry.getKey().getPort().intValue());
									SocketChannel socketChannel = null;
									try {
										socketChannel = SocketChannel.open(address);
										OutputStream out1 = socketChannel.socket().getOutputStream();
										out1.write(pro.getString("module.name").trim().getBytes());
										out1.flush();
										entry.setValue(socketChannel);
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									jobBoTmp = entry.getKey();
									jobBoTmp.setState(0L);
									jobsTmp.add(jobBoTmp);// thêm vào danh sách các job không còn kết nối được
									logger.info(" information Job {} do not exist", info);
								}
							} else {
								@SuppressWarnings("unused")
								Future<Boolean> future = executor.submit(new SocketReadChannel(obChannel));
							}

						} catch (Exception e) {
							logger.info("Disconnect with module : {}", entry.getKey().getModuleName());
							// e.printStackTrace();
							jobBoTmp = entry.getKey();
							SocketChannel socketChannel = retry(jobBoTmp);// lấy ra kết qủa sau khi retry
							if (socketChannel != null) {// retry thành công
								retry.put(jobBoTmp, socketChannel);
								logger.info("Retry success add one element in retry map, map size : {}", retry.size());

							} else {
								jobBoTmp.setState(0L);
								jobsTmp.add(jobBoTmp);
								logger.info("Retry not success add module {} element in list",
										jobBoTmp.getModuleName());

							}
						}
					}
					// không loại bỏ các job vẫn còn kết nối với data base

					removeModuleDisconnetDb(jobsTmp, moduleService.getNumberConnectTion(jobsTmp));

					if (!jobsTmp.isEmpty()) {// xóa danh sách các modul retry không thành công ra khỏi map

						for (ModuleBo moduleBo : jobsTmp) {
							map.remove(moduleBo);
							checked.remove(moduleBo.getModuleName());
							logger.info("Retry not success remove module {} in map", moduleBo.getModuleName());
						}
						// xóa các job ra khỏi list đã kiểm tra

					}
					if (moduleBo.getIsMaster() == 1) {// nếu nó là master nó có quyền được cập nhật
						if (!jobsTmp.isEmpty()) {
							// comment
							moduleService.updateAll(moduleBo, jobsTmp);
							String proc = pro.getString("sub.sql.redistribute");
							// extendService.redistributeModuleDisconnect(jobsTmp, map, moduleBo, proc);
							logger.info("master update db 279");
						}
					} else {
						Long idMaster = checkMasterInListRetryFail(jobsTmp);
						if (idMaster != null) {
							logger.info("Master in list Retry not success");

							if (checkUpdateMaster()) {// nếu nó được cập nhật là master thì nó cập nhật lại chính nó là
														// master và cập nhật lại toàn bộ những thằng đã chết trong DB
								logger.info(" this module update is  Master ({})", moduleBo.getModuleName());
								//moduleService.updateMaster(map, moduleBo);
								// thực hiện phân phối lại toàn bộ dữ liệu cho các job còn sống
								String proc = pro.getString("sub.sql.redistribute");
								// extendService.redistributeModuleDisconnect(jobsTmp, map, moduleBo, proc);
								moduleBo.setIsMaster(1L);
								// commnent
								moduleService.updateAll(moduleBo, jobsTmp);
								logger.info("master update db 296");
								addJobMaster();

							} else { // nếu nó không được phép cập nhật làm master thì nó cập nhật lại thằng được
										// chọn làm master
								Long idMaster1 = getMasterCurrent();

								for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
									if (map.getKey().getId() == idMaster1) {
										map.getKey().setIsMaster(1L);
										logger.info("update module {} is master", map.getKey().getModuleName());
									}
								}
							}

						}
					}
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * create list socket module active and conect servers
	 * 
	 * @param map  map các client sẽ được request
	 * @param jobs danh sách job được lấy ra từ Db
	 * 
	 */
	public void getAllClient(List<ModuleBo> jobs) {

		List<ModuleBo> listJob = new ArrayList<>();

		for (ModuleBo moduleBo : jobs) {
			if (pro.getString("module.name").trim().equals(moduleBo.getModuleName()))
				continue;// không call đến chính job đó
			InetSocketAddress address = new InetSocketAddress(moduleBo.getIp(), moduleBo.getPort().intValue());
			SocketChannel socketChannel = null;
			try {
				socketChannel = SocketChannel.open(address);
				OutputStream out = socketChannel.socket().getOutputStream();
				out.write(pro.getString("module.name").trim().getBytes());
				out.flush();
				map.put(moduleBo, socketChannel);
			} catch (IOException e) {
				logger.info("Do not conect module {} ", moduleBo.getModuleName());
				// lưu lại danh sách các job mất kết nối
				moduleBo.setState(0L);
				listJob.add(moduleBo);
			}
		}

		// không loại bỏ các job vẫn còn kết nối với data base
		removeModuleDisconnetDb(listJob, moduleService.getNumberConnectTion(listJob));
		// hoàn tất kết nối kiểm tra cập nhật master
		// kiểm tra trong các mất kết nối có master không
//		Long idMaster = checkMasterInListRetryFail(listJob);
//		if (idMaster != null) {
//			logger.info("Master in list Retry not success");
//			// nếu có kiểm tra xem bản thân thằng này có được cập nhật monitor không
//			if (checkUpdateMaster()) {
//				logger.info("module {} is update Master", moduleBo.getModuleName());
//				// nếu được cập nhật thì cập nhật lại để quét
//				moduleService.updateMaster(map, moduleBo);
//				addJobMaster();
//
//			}
//		}
		Boolean update = updateMaster();
		if (update == true && moduleBo.getIsMaster() == 1L) {
			//commnent
			moduleService.updateAll(moduleBo,listJob);
			logger.info("master update db 374");
		}else {
			if(moduleBo.getIsMaster()==1) {
				moduleBo.setState(1L);
				moduleService.updateAll(moduleBo,listJob);
				logger.info("master update 379");
				update = true;
			}
		}
		if (moduleBo.getIsMaster() == 1L && checkUpdateMaster() && !update) {
			// commnent
			int k = moduleService.updateAll(moduleBo, listJob);
			logger.info("master update db 385");
		}
		if (map.isEmpty() && !update) {
			int k = moduleService.updateAll(moduleBo, listJob);
			logger.info("master update db 389");
		}

	}

	/**
	 * retry connection server five times
	 * 
	 * @param job
	 * @return
	 */
	public SocketChannel retry(ModuleBo job) {
		InetSocketAddress address = new InetSocketAddress(job.getIp(), job.getPort().intValue());
		SocketChannel socketChannel = null;
		for (int i = 0; i < 5; i++) {
			try {
				logger.info("retry time {}", i);
				Thread.sleep(150);
				socketChannel = SocketChannel.open(address);
				OutputStream out = socketChannel.socket().getOutputStream();
				out.write(moduleBo.getModuleName().getBytes());
				out.flush();
				InputStream in = socketChannel.socket().getInputStream();
				byte tmp[] = new byte[100];
				in.read(tmp);
				String s = new String(tmp);
				if (!s.trim().equals(job.getModuleName().trim())) {
					socketChannel = null;
				} else {
					break;
				}
			} catch (Exception e) {
				socketChannel = null;
				logger.info("retry not success");
			}
		}
		return socketChannel;

	}

	/**
	 * delete element in map retry exists in map after add map retry in map
	 * 
	 */
	public void updateRetry() {

		List<ModuleBo> jobstmp = new ArrayList<ModuleBo>();
		for (Map.Entry<ModuleBo, SocketChannel> entryRetry : retry.entrySet()) {
			for (Map.Entry<ModuleBo, SocketChannel> entryMap : map.entrySet()) {
				if (entryRetry.getKey().getId() == entryMap.getKey().getId()) {
					jobstmp.add(entryRetry.getKey());
				}
			}
		}
		for (ModuleBo moduleBo : jobstmp) {
			map.remove(moduleBo);
		}

		map.putAll(retry);
		retry.clear();

	}

	/**
	 * check exists module master in list retry fail
	 * 
	 * @param jobsTmp list module retry fail
	 * @return
	 */
	public Long checkMasterInListRetryFail(List<ModuleBo> jobsTmp) {

		for (ModuleBo moduleBo : jobsTmp) {
			if (moduleBo.getIsMaster() == 1) {
				moduleBo.setIsMaster(0L);
				return 1L;
			}
		}
		return null;
	}

	/**
	 * check this module update master
	 * 
	 * @return
	 */
	public Boolean checkUpdateMaster() {
		for (Map.Entry<ModuleBo, SocketChannel> entryMap : map.entrySet()) {
			if (entryMap.getKey().getId() < moduleBo.getId()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * remove modules disconnect database
	 * 
	 * @param moduleBos
	 * @param objects
	 */
	public void removeModuleDisconnetDb(List<ModuleBo> moduleBos, List<Map<String, String>> map) {
		List<ModuleBo> list = new ArrayList<ModuleBo>();
		if (!map.isEmpty()) {
			for (ModuleBo moduleBo : moduleBos) {
				for (Map<String, String> object : map) {
					if (object.get(moduleBo.getModuleName()) != null) {
						list.add(moduleBo);
					}
				}
			}
			for (ModuleBo moduleBo : list) {
				moduleBos.remove(moduleBo);
			}
		}
	}

	public Boolean isChecked(String moduleName, List<String> list) {
		for (String s : list) {
			if (s.trim().equals(moduleName.trim())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * update master
	 */
	public Boolean updateMaster() {
		if (!checkExistMaster()) {//kiểm tra sự tồn tại của master trong danh sách đã load ra
			jobs.get(0).setIsMaster(1L);
			if (jobs.get(0).getId() == moduleBo.getId()) {
				moduleBo.setIsMaster(1L);
			}
			return true;
		} else {
			if (checkDuplicateMaster()) {
				fixDuplicateMaster();
				return true;
			}
			return false;

		}

	}

	public Boolean checkExistMaster() {
		for (ModuleBo moduleBo : jobs) {
			if (moduleBo.getIsMaster() == 1)
				return true;
		}
		return false;
	}

	public void fixDuplicateMaster() {
		int i = 0;
		for (ModuleBo moduleBo : jobs) {
			if (moduleBo.getIsMaster() == 1) {
				break;
			}
			i++;
		}
		for (int j = i + 1; j < jobs.size(); j++) {
			jobs.get(j).setIsMaster(0L);
		}
		for (ModuleBo moduleBos : jobs) {
			if(moduleBo.getId() == moduleBos.getId()) {
				moduleBo.setIsMaster(moduleBos.getIsMaster());
			}
		}
	}

	public Boolean checkDuplicateMaster() {
		int i = 0;
		for (ModuleBo moduleBo : jobs) {
			if (moduleBo.getState() == 1)
				i++;
		}
		if (i > 1)
			return true;
		return false;
	}

	public void addJobMaster() {
		Scheduler sc1 = scheduler.getScheduler();
		try {
			sc1.start();
			if (sc1.checkExists(jobBigData.getObject().getKey())) {
				logger.info("jobBigData already exist, delete old jobBigData");
				sc1.deleteJob(jobBigData.getObject().getKey());
			}

			logger.info("Add job jobBigData in master");
			sc1.scheduleJob(jobBigData.getObject(), bigData.getObject());

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public Long getMasterCurrent() {
		Long id = moduleBo.getId();
		for (Map.Entry<ModuleBo, SocketChannel> map : map.entrySet()) {
			if (map.getKey().getId() < id) {
				id = map.getKey().getId();
			}
		}
		return id;
	}

}
