package com.viki3d.firebase.sender;

import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogService {
	
	public static boolean logToFile = false;

	public static void logToFile(Class loggerClass, String logFilename) {
		try {
			//  Let logger log to file
			Logger logger = Logger.getLogger(loggerClass.getSimpleName());
			FileHandler fileHandler = new FileHandler(logFilename);
			fileHandler.setFormatter(new SimpleFormatter() {
				private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
				@Override
				public synchronized String format(LogRecord lr) {
					return String.format(format,
							new Date(lr.getMillis()),
				            lr.getLevel().getLocalizedName(),
				            lr.getMessage()
					);
				}
			});
			logger.addHandler(fileHandler);
			
			//  Set default log level
			logger.setLevel(Level.INFO);
			
		}
		catch (IOException ioex) {
			System.out.println(ioex.getMessage());
		}
	}
	
	public static void error(Class loggerClass, String message) {
		Logger.getLogger(loggerClass.getSimpleName()).log(Level.SEVERE, message);
	}

	public static void warning(Class loggerClass, String message) {
		Logger.getLogger(loggerClass.getSimpleName()).log(Level.WARNING, message);
	}
	
	
	public static void debug(Class loggerClass, String message) {
		Logger.getLogger(loggerClass.getSimpleName()).log(Level.INFO, message);
	}

	
}
