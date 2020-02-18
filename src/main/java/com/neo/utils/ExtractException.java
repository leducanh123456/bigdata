package com.neo.utils;

/**
 * @author MaiHuyCanh
 * @Date 01-11-2019
 *
 *       ExtractException.java
 */
public class ExtractException {
	public static String exceptionToString(Exception e) {
		java.io.StringWriter errors = new java.io.StringWriter();
		e.printStackTrace(new java.io.PrintWriter(errors));
		return errors.toString();
	}

	public static String runtimeToString(RuntimeException e) {
		java.io.StringWriter errors = new java.io.StringWriter();
		e.printStackTrace(new java.io.PrintWriter(errors));
		return errors.toString();
	}
}