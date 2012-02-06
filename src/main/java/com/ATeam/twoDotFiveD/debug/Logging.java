package com.ATeam.twoDotFiveD.debug;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import com.ATeam.twoDotFiveD.TwoDotFiveD;

public class Logging
{
	// Class variables
	public static final Logger	log				= Logger.getLogger(TwoDotFiveD.class
														.getName());
	private static final String	fileSeparator	= System.getProperty("file.separator");
	private FileHandler			fh;
	
	public Logging(String path)
	{
		try
		{
			final File rootFolder = new File(path);
			final boolean rootMade = rootFolder.mkdir();
			if (rootMade)
			{
				log.info("Made root directory @ "
						+ rootFolder.getCanonicalPath());
			}
			final File folder = new File(path + fileSeparator + "log");
			final boolean logFolderMade = folder.mkdir();
			if (logFolderMade)
			{
				log.info("Made log directory @ " + folder.getCanonicalPath());
			}
			// TODO different files for different types, such as .txt, .xml,
			// .html, etc.
			final File logFile = new File(folder.getCanonicalPath()
					+ fileSeparator + "log-" + System.currentTimeMillis()
					+ ".xml");
			fh = new FileHandler(logFile.getCanonicalPath());
			fh.setFormatter(new XMLFormatter());
			log.addHandler(fh);
			log.info("Logging @ " + logFile.getCanonicalPath());
			// TODO potentially have a SQLite database for logging?
			// Mark what to save log into config file, as well as enable/disable
			// log
			// Need way to auto prune older files
		}
		catch (SecurityException e)
		{
			log.log(Level.SEVERE, "Could not create log file...", e);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "Could not create log file...", e);
			e.printStackTrace();
		}
	}
}
