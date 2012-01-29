package com.ATeam.twoDotFiveD.Debug;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import com.ATeam.twoDotFiveD.TwoDotFiveD;

public class Logging {
	//Class variables
	public static final Logger log = Logger.getLogger(TwoDotFiveD.class.getName());
	private FileHandler fh;

	public Logging()
	{
		try {
			//TODO OS specific setup
			final File folder = new File(System.getProperty("user.dir") + "/log");
			folder.mkdir();
			final File logFile = new File(folder.getCanonicalPath() + "/log-" + System.currentTimeMillis());
			fh = new FileHandler(logFile.getCanonicalPath());
			fh.setFormatter(new XMLFormatter());
			log.addHandler(fh);
			log.info("Logging @ " + logFile.getCanonicalPath());
			//TODO potentially have a SQLite as logging?
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

	public static Logger getLogger()
	{
		return log;
	}
}
