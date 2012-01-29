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
			fh = new FileHandler(new File(System.getProperty("user.dir") + "\\log\\log" + System.currentTimeMillis() +".txt").getCanonicalPath());
			fh.setFormatter(new XMLFormatter());
			log.addHandler(fh);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Logger getLogger()
	{
		return log;
	}
}
