package com.neo.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class StringRandom {
	public static void main(String[] args) {
		
		try {
			
			/*
			int numberThread = 200;
			// Random random = new Random();
			// System.out.println("Int random value: "+random.nextInt(10));
			//: 1911221707044471
			while (true) {
				for (int i = 0; i < numberThread; i++) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							String value = StringRandom.getRandomCode("",8);
							if (myKey.get(value) != null) {
								System.err.println(value + " existing");
								return;
							}
							myKey.put(value, value);
						}
					}).start();
				}
				Thread.sleep(100);
				System.out.println("-----> Total key: " + myKey.size());
			}
			*/
			
			String session = getRandomCode("",18);
			
			System.out.println("-----> session:" + session);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	static ConcurrentHashMap<String, String> myKey = new ConcurrentHashMap<String, String>();
	
	private static String arrChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456780".toLowerCase();
	private static String arrInt = "0123456789";//012345678901234567890123456789
	private static int length_char = arrChar.length();
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
	
	//Ham nay xu ly tra ra 1 string Random voi so ky tu la count.
	public static String getRandomStr(int count) {
		StringBuilder str = new StringBuilder();
		try{
			Random rd = new Random();
			for (int i =0; i < count; i ++) {
				str.append(arrChar.charAt(rd.nextInt(length_char)));
			}
			
		}catch(Exception e) {
			str.append(getRandomStr2(count));
			e.printStackTrace();
		}
		return str.toString();
	}
	
	public static String getRandomStr2(int count) {
		StringBuilder str = new StringBuilder();
		try{
			Random rd = new Random();
			for (int i =0; i < count; i ++) {
				int index = rd.nextInt(length_char) - 1;
				if (index<0) index =0;
				str.append(arrChar.charAt(index));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}
	
	public static String getTimeId(Date d) {
		try{
			return sdf.format(d);
		}catch(Exception e) {
			return "";
		}
	}
	
	//Ham xu ly tra ra id gom co: time(yyMMddHHmmss) + random string. 
	public static String getTimeStrId(Date d, int length) {
		StringBuilder str = new StringBuilder();
		try{
			str.append(sdf.format(d)).append(getRandomStr(length));
		}catch(Exception e){
			str.append(getRandomStr(length));
			e.printStackTrace();
		}
		return str.toString();
	}
	
	public static String getTimeStrId(String code, Date d, int length) {
		StringBuilder str = new StringBuilder(code);
		try {
			str.append(sdf.format(d)).append(getRandomStr(length));
		} catch (Exception e){
			str.append(getRandomStr(length));
			e.printStackTrace();
		}
		return str.toString();
	}
	
	public static String getTimeIntId (String code, Date d, int length) {
		StringBuilder str = new StringBuilder(code);
		try{
			str.append(sdf.format(d)).append(getRandomCode("",length));
		}catch(Exception e) {
			str.append(getRandomStr(length));
			e.printStackTrace();
		}
		return str.toString();
	}
	
	//Ham tra ra 1 so Random theo Id de lam ma giao dich:
	public static String getRandomCode(String prefix, int length) {
		
		StringBuilder str = new StringBuilder(prefix);
		try {
			Random rd = new Random();
			for (int i=0; i < length; i ++) {
				str.append(arrInt.charAt(rd.nextInt(10)));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}
}