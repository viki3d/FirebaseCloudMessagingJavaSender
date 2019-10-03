package com.viki3d.firebase.sender;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogService {
	
	public static boolean logToFile = false;

	public static void logToFile(Class<Object> loggerClass, String logFilename) {
		try {
			//  Let logger log to file
			Logger logger = Logger.getLogger(loggerClass.getSimpleName());
			FileHandler fileHandler = new FileHandler(logFilename);
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);
			
			//  Set default log level
			logger.setLevel(Level.INFO);
			
		}
		catch (IOException ioex) {
			System.out.println(ioex.getMessage());
		}
	}
	
	public static void error(Class<Object> loggerClass, String message) {
		Logger.getLogger(loggerClass.getSimpleName()).log(Level.SEVERE, message);
	}

	public static void warning(Class<Object> loggerClass, String message) {
		Logger.getLogger(loggerClass.getSimpleName()).log(Level.WARNING, message);
	}
	
	
	public static void debug(Class<Object> loggerClass, String message) {
		Logger.getLogger(loggerClass.getSimpleName()).log(Level.INFO, message);
	}

	
}
