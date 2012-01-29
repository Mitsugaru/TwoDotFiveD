package com.ATeam.twoDotFiveD.Debug;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

public class Logging {
	//Class variables
	public static Logger log;
	private static final String fileSeparator = System.getProperty("file.separator");
	private FileHandler fh;

	public Logging(String name)
	{
		log = Logger.getLogger(name);
		try {
			final File folder = new File(System.getProperty("user.dir") + fileSeparator + "log");
			folder.mkdir();
			final File logFile = new File(folder.getCanonicalPath() + fileSeparator + "log-" + System.currentTimeMillis());
			fh = new FileHandler(logFile.getCanonicalPath());
			fh.setFormatter(new XMLFormatter());
			log.addHandler(fh);
			log.info("Logging @ " + logFile.getCanonicalPath());
			//TODO potentially have a SQLite database for logging?
			//Mark what to save log into config file, as well as enable/disable log
			//Need way to auto prune older files
		} catch (SecurityException e) {
			log.warning("Could not create log file...");
			e.printStackTrace();
		} catch (IOException e) {
			log.warning("Could not create log file...");
			e.printStackTrace();
		}
	}
}
