package com.neo.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author cimit
 * @Date 08-01-2020
 *
 *       Utils.java
 */
public class Utils {
	public static List<String> getListParamName(String sql) {
		int currentIndex = 0;
		List<String> listParam = new ArrayList<>();
		while (sql.indexOf(":", currentIndex) != -1) {
			int startIndexKey = sql.indexOf(":", currentIndex);
			int endKey = sql.indexOf(" ", startIndexKey);

			String key = null;
			if (endKey == -1) {
				key = sql.substring(startIndexKey + 1);
			} else {
				key = sql.substring(startIndexKey + 1, endKey);
			}
			currentIndex = startIndexKey + 1;
			listParam.add(key.toUpperCase());
		}
		return listParam;
	}

	public static Map<String, String> toMap(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();

		Map<String, String> row = new LinkedHashMap<String, String>();
		int count = meta.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < count; i++) {
				int columnNumber = i + 1;
				// use column label to get the name as it also handled SQL SELECT aliases
				String columnName;
				try {
					columnName = meta.getColumnLabel(columnNumber);
				} catch (SQLException e) {
					columnName = meta.getColumnName(columnNumber);
				}
				// use index based which should be faster
				int columnType = meta.getColumnType(columnNumber);
				if (columnType == Types.CLOB || columnType == Types.BLOB) {
					row.put(columnName, rs.getString(columnNumber));
				} else {
					row.put(columnName, rs.getString(columnNumber));
				}
			}
		}
		// rs.close();
		return row;
	}

	public static String convertTimeUnit(long value) {
		if (value / Math.pow(10, 9) < 1) {
			if (value / Math.pow(10, 6) < 1) {
				if (value / Math.pow(10, 3) < 1) {
					return value + " nanoseconds";
				} else
					return String.format("%.2f microseconds", value / Math.pow(10, 3));
				// return TimeUnit.MICROSECONDS.convert(value, TimeUnit.NANOSECONDS) + "
				// microseconds";
			} else
				return String.format("%.2f milliseconds", value / Math.pow(10, 6));
			// return TimeUnit.MILLISECONDS.convert(value, TimeUnit.NANOSECONDS) + "
			// milliseconds";
		} else
			return String.format("%.2f seconds", value / Math.pow(10, 9));
		// return TimeUnit.SECONDS.convert(value, TimeUnit.NANOSECONDS) + " seconds";
	}

	/**
	 * 
	 * @param value = startTime = System.nanoTime()
	 * @return
	 */
	public static String estimateTime(long startTime) {
		long value = System.nanoTime() - startTime;
		if (value / Math.pow(10, 9) < 1) {
			if (value / Math.pow(10, 6) < 1) {
				if (value / Math.pow(10, 3) < 1) {
					return value + " nanoseconds";
				} else
					return TimeUnit.MICROSECONDS.convert(value, TimeUnit.NANOSECONDS) + " microseconds";
			} else
				return TimeUnit.MILLISECONDS.convert(value, TimeUnit.NANOSECONDS) + " milliseconds";
		} else
			return TimeUnit.SECONDS.convert(value, TimeUnit.NANOSECONDS) + " seconds";
	}

	public static String sendGet(String url) throws Exception {
		StringBuilder response = new StringBuilder();
		HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

		// optional default is GET
		httpClient.setRequestMethod("GET");
		httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

		try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

			String line;

			while ((line = in.readLine()) != null) {
				response.append(line);
			}
		}
		return response.toString();
	}

}