package com.ATeam.twoDotFiveD.debug;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import com.ATeam.twoDotFiveD.TwoDotFiveD;

/**
 * Central class to log information to an XML file. Holds static info about the
 * OS, Java, and JVM that the program is running in. Also has method to give
 * details on memory.
 * <p>
 * Shout out to sk89q for some of the info here.
 * <p>
 * Standard system properties:
 * http://www.mindspring.com/~mgrand/java-system-properties.htm
 * 
 * @author Mitsugaru
 * 
 */
public class Logging
{
	// Class variables
	public static final Logger	log				= Logger.getLogger(TwoDotFiveD.class
														.getName());
	private static final String	fileSeparator	= System.getProperty("file.separator");
	private FileHandler			fh;
	// OS info
	private static final String	osName			= System.getProperty("os.name");
	private static final String	osVersion		= System.getProperty("os.version");
	private static final String	osArch			= System.getProperty("os.arch");
	// Java info
	private static final String	javaVendor		= System.getProperty("java.vendor");
	private static final String	javaVersion		= System.getProperty("java.version");
	private static final String	javaVendorURL	= System.getProperty("java.vendor.url");
	// JVM info
	private static final String	jvmVendor		= System.getProperty("java.vm.vendor");
	private static final String	jvmName			= System.getProperty("java.vm.name");
	private static final String	jvmVersion		= System.getProperty("java.vm.version");
	
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
	
	/**
	 * Prints system info to the log
	 */
	public static void systemInfo()
	{
		// Grab runtime
		final Runtime rt = Runtime.getRuntime();
		// Print/Log OS info
		final String os = String.format("System: %s %s (%s)", osName,
				osVersion, osArch);
		Logging.log.info(os);
		// Print/Log Java info
		final String java = String.format("Java: %s %s (%s)", javaVendor,
				javaVersion, javaVendorURL);
		Logging.log.info(java);
		// Print/Log JVM info
		final String jvm = String.format("JVM: %s %s (%s)", jvmVendor, jvmName,
				jvmVersion);
		Logging.log.info(jvm);
		// Print/Log other system info
		final String processorCount = String.format("Available processors: %s",
				rt.availableProcessors());
		Logging.log.info(processorCount);
		
	}
	
	/**
	 * Prints memory info to the log
	 */
	public static void memoryInfo()
	{
		// Grab runtime
		final Runtime rt = Runtime.getRuntime();
		// Print/Log memory info
		final String availableMemory = String.format(
				"Available total memory: %.2f MB",
				Math.floor(rt.maxMemory() / 1024.0 / 1024.0));
		Logging.log.info(availableMemory);
		final String jvmAllocatedMemory = String.format(
				"JVM allocated memory: %.2f MB",
				Math.floor(rt.totalMemory() / 1024.0 / 1024.0));
		Logging.log.info(jvmAllocatedMemory);
		final String freeAllocatedMemory = String.format(
				"Free allocated memory: %.2f MB",
				Math.floor(rt.freeMemory() / 1024.0 / 1024.0));
		Logging.log.info(freeAllocatedMemory);
	}
}
