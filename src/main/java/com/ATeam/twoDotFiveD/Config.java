package com.ATeam.twoDotFiveD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ATeam.twoDotFiveD.debug.Logging;

/**
 * Handles the default config.yml for the program.
 * 
 * @author Mitsugaru
 * 
 */
//TODO make this a singleton?
public class Config
{
	//Class variables
	private File				configFile			= new File(
															System.getProperty("user.dir"));
	private YamlConfiguration	config				= null;
	private static final String	fileSeparator		= System.getProperty("file.separator");
	private boolean				logEnabled			= false;
	private int					logLimit			= 20;
	//Display settings
	private boolean				vSync				= false;
	private boolean				fullScreen			= false;
	private int					displayWidth		= -1;
	private int					displayHeight		= -1;
	private int					displayColorBits	= -1;
	private int					displayFrequency	= -1;
	private int					depthBufferBits		= 24;
	
	/**
	 * Constructor that loads the config.yml from a given path.
	 * 
	 * @param Path
	 *            to the program folder
	 */
	public Config(String path)
	{
		init(path);
	}

	/**
	 * Method to initialize variables and generate the config.yml with all
	 * essential default values, if they are missing.
	 * 
	 * @param Path
	 *            to the program folder
	 */
	private void init(String path)
	{
		try
		{
			final File rootFolder = new File(path);
			final boolean rootMade = rootFolder.mkdir();
			if (rootMade)
			{
				Logging.log.info("Made root directory @ "
						+ rootFolder.getCanonicalPath());
			}
			configFile = new File(rootFolder.getCanonicalFile() + fileSeparator
					+ "config.yml");
			if (!configFile.exists())
			{
				if (configFile.createNewFile())
				{
					Logging.log.info("Made config.yml @ "
							+ configFile.getCanonicalPath());
				}
			}
			config = YamlConfiguration.loadConfiguration(configFile);
			
			final Map<String, Object> defaults = new HashMap<String, Object>();
			// Central place to put program info, such as version
			defaults.put("version", "0.01");
			defaults.put("window.vSync", false);
			defaults.put("window.fullScreen", false);
			defaults.put("window.displayWidth", -1);
			defaults.put("window.displayHeight", -1);
			defaults.put("window.displayColorBits", -1);
			defaults.put("window.displayFrequency", -1);
			defaults.put("window.depthBufferBits", 24);
			defaults.put("debug.log.enabled", false);
			defaults.put("debug.log.limit", 20);
			// Insert defaults into config file if they're not present
			for (final Entry<String, Object> e : defaults.entrySet())
			{
				if (!config.contains(e.getKey()))
				{
					config.set(e.getKey(), e.getValue());
				}
			}
			// Save
			config.save(configFile);
			// Load variables from config
			this.vSync = config.getBoolean("window.vSync", false);
			this.fullScreen = config.getBoolean("window.fullScreen", false);
			this.displayWidth = config.getInt("window.displayWidth", -1);
			this.displayHeight = config.getInt("window.displayHeight", -1);
			this.displayColorBits = config.getInt("window.displayColorBits", -1);
			this.displayFrequency = config.getInt("window.displayFrequency", -1);
			this.depthBufferBits = config.getInt("window.depthBufferBits", 24);
			// Check bounds
			checkBounds();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void checkBounds()
	{
		// Check for valid log limit
		if (logLimit < 1)
		{
			Logging.log
					.warning("[Config] debug.log.limit has an invalid value. Disabling logging.");
			logLimit = 20;
			logEnabled = false;
		}
	}
	
	/**
	 * Save config.yml
	 */
	public void save()
	{
		try
		{
			config.save(configFile);
		}
		catch (IOException e)
		{
			Logging.log.log(Level.SEVERE, "Failed to save config file.", e);
		}
	}
	
	/**
	 * Set property at given path with given object in the yaml.
	 * @param path of node
	 * @param object to put for node
	 */
	public void setProperty(String path, Object o) {
		config.set(path, o);
	}
	
	/**
	 * Reloads config
	 */
	public void reload()
	{
		try
		{
			config.load(configFile);
			// Load variables from config
			this.vSync = config.getBoolean("window.vSync", false);
			this.fullScreen = config.getBoolean("window.fullScreen", false);
			// Check bounds
			checkBounds();
		}
		catch (FileNotFoundException e)
		{
			// File was not found...
			try
			{
				Logging.log.log(Level.WARNING,
						"[Config] File '" + configFile.getCanonicalPath()
								+ "' not found! Attempting to generate again.",
						e);
				init(configFile.getPath());
			}
			catch (IOException e1)
			{
				Logging.log.log(Level.SEVERE,
						"[Config] Could not generate config.yml!", e);
			}
		}
		catch (IOException e)
		{
			Logging.log.log(Level.SEVERE, "[Config] IOException!", e);
		}
		catch (InvalidConfigurationException e)
		{
			Logging.log.log(Level.SEVERE,
					"[Config] Config has invalid configuration!", e);
		}
	}
	
	/**
	 * Get config value of window.vSync
	 * 
	 * @return If vsync is enabled
	 */
	public boolean getVSync()
	{
		return vSync;
	}
	
	/**
	 * Get config value of window.fullscreen
	 * 
	 * @return If Fullscreen is enabled
	 */
	public boolean getFullScreen()
	{
		return fullScreen;
	}
	
	public boolean getLogEnabled()
	{
		return logEnabled;
	}

	public int getDisplayWidth()
	{
		return displayWidth;
	}
	
	public int getDisplayHeight()
	{
		return displayHeight;
	}

	public int getDisplayColorBits()
	{
		return displayColorBits;
	}

	public int getDisplayFrequency()
	{
		return displayFrequency;
	}

	public int getDepthBufferBits()
	{
		return depthBufferBits;
	}
}
