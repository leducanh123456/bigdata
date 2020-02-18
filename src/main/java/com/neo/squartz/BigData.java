package com.neo.squartz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.bigdata.service.BigDataService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class BigData extends QuartzJobBean{
	
	private BigDataService bigDataService;

	private PropertiesConfiguration pro;

	private final Logger loggerRun = LoggerFactory.getLogger(BigData.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		loggerRun.info("Start job big data");
		long startTime = System.nanoTime();
		int i = 1;
		while (true) {
			String sql = pro.getString("groupone.sub.sql" + i);
			if (sql == null)
				break;
			String fileName = pro.getString("groupone.sub.sql" + i + ".file.name");
			String size = pro.getString("groupone.sub.sql" + i + ".max.line");
			if (size != null) {
				List<Object> dataInDay = bigDataService.listBigData(sql);
				writeListBigData(dataInDay, fileName, size);
			} else {
				List<Object> dataInDay = bigDataService.listBigData(sql);
				writeListBigData(dataInDay, fileName);
			}
			i++;
		}
		loggerRun.info("Time run big data : {} nano second",(System.nanoTime() - startTime));
	}

	public BigDataService getBigDataService() {
		return bigDataService;
	}

	public void setBigDataService(BigDataService bigDataService) {
		this.bigDataService = bigDataService;
	}

	

	public PropertiesConfiguration getPro() {
		return pro;
	}

	public void setPro(PropertiesConfiguration pro) {
		this.pro = pro;
	}

	public void writeListBigData(List<Object> dataInDays, String... paramStrings) {
		if (paramStrings.length == 2) {
			writeFileHasSize(dataInDays, paramStrings[0], Integer.parseInt(paramStrings[1].trim()));
		} else if (paramStrings.length == 1) {
			writeFile(dataInDays, paramStrings[0]);
		}
	}

	public void writeFileHasSize(List<Object> dataInDays, String fileName, Integer numberReord) {
		if (dataInDays.isEmpty())
			return;
		PrintWriter printWriter = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy");
		String strDate = formatter.format(new Date());
		int i = 1;
		int part = 0;
		while (true) {
			if (i == dataInDays.size() && dataInDays.size() != 1)
				break;
			File file = new File(
					pro.getString("link.file.bigdata") + strDate + fileName + part + ".txt");
			if (!file.exists()) {
				System.out.println("tạo file" + file.getPath());
				try {
					file.createNewFile();
				} catch (IOException e) {
					loggerRun.info("exception BigData {}", e);
				}
			}
			try {
				printWriter = new PrintWriter(file);
				while (true) {
					String tmp = (String) dataInDays.get(i - 1);
					printWriter.print(tmp);
					printWriter.println();
					if (i % numberReord == 0 || i == dataInDays.size()) {
						if (i < dataInDays.size()) {
							i++;
						}
						part++;
						break;
					}
					i++;
				}
			} catch (FileNotFoundException e) {
				loggerRun.info("exception BigData {}", e);
			} finally {
				if (printWriter != null) {
					printWriter.flush();
					printWriter.close();
				}
			}
			if (dataInDays.size() == 1)
				break;
		}
	}

	public void writeFile(List<Object> dataInDays, String fileName) {
		if (dataInDays.isEmpty())
			return;
		PrintWriter printWriter = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy");
		String strDate = formatter.format(new Date());
		File file = new File(pro.getString("link.file.bigdata") + strDate + fileName + ".txt");
		if (!file.exists()) {
			System.out.println("tạo file" + file.getPath());
			try {
				file.createNewFile();
			} catch (IOException e) {
				loggerRun.info("exception BigData {}", e);
			}
		}
		try {
			printWriter = new PrintWriter(file);
			for (Object object : dataInDays) {
				String elemnt = object.toString();
				printWriter.print(elemnt);
				printWriter.println();
			}
			printWriter.flush();
		} catch (Exception e) {
			loggerRun.info("exception BigData {}", e);
			printWriter.flush();
			printWriter.close();
		}
	}
}
