package com.neo.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Common {
	
	public static Date getDate(Date date, int addDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, addDate);
		date.setTime(calendar.getTimeInMillis());
		return date;
	}
	
	public static String getDateToString() {
		return null;
	}
	
	public static String getDateTimeToString() {
		Calendar calendar = Calendar.getInstance();
		StringBuilder dateTime = new StringBuilder();
		dateTime.append(calendar.get(Calendar.DAY_OF_MONTH));
		dateTime.append(calendar.get(Calendar.MONTH));
		dateTime.append(calendar.get(Calendar.YEAR));
		dateTime.append("_");
		return dateTime.toString();
	}
	public static boolean inPeriod(String period) {
		List<List<Calendar>> list = new ArrayList<List<Calendar>>();
		String[] listPeriod = period.split(",");
		for(int i=0;i<listPeriod.length;i++) {
			String[] time = listPeriod[i].split("-");
			String[] startTime = time[0].split(":");
			Calendar start = Calendar.getInstance();
			start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
			start.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
			start.add(Calendar.MINUTE, -1);
			String[] endTime = time[1].split(":");
			Calendar end = Calendar.getInstance();
			end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
			end.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
			end.add(Calendar.MINUTE, 1);
			List<Calendar> calendars = new ArrayList<Calendar>();
			calendars.add(start);
			calendars.add(end);
			list.add(calendars);
		}
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < list.size(); i++) {
			List<Calendar> tmp = list.get(i);
			
			if(tmp.get(0).before(calendar)&&tmp.get(1).after(calendar))
			{
				return true;
			}
		}
		return false;
	}

}
