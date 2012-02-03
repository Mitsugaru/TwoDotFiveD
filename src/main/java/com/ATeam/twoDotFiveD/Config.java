package com.ATeam.twoDotFiveD;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ATeam.twoDotFiveD.debug.Logging;

/**
 * Handles the default config.yml for the program.
 *
 * @author Mitsugaru
 *
 */
public class Config {
	private YamlConfiguration config = null;
	private static final String fileSeparator = System
			.getProperty("file.separator");
	private static boolean vSync = false;
	private static boolean fullScreen = false;

	/**
	 * Constructor that loads the config.yml from a given path.
	 * 
	 * @param Path to the program folder
	 */
	public Config(String path) {

		try {
			final File rootFolder = new File(path);
			final boolean rootMade = rootFolder.mkdir();
			if (rootMade) {
				Logging.log.info("Made root directory @ "
						+ rootFolder.getCanonicalPath());
			}
			final File configFile = new File(rootFolder.getCanonicalFile()
					+ fileSeparator + "config.yml");
			if(!configFile.exists())
			{
				if(configFile.createNewFile())
				{
					Logging.log.info("Made config.yml @ "
							+ configFile.getCanonicalPath());
				}
			}
			config = YamlConfiguration.loadConfiguration(configFile);

			final Map<String, Object> defaults = new HashMap<String, Object>();
			// TODO have a central place to put program info, such as version
			defaults.put("version", "0.01");
			defaults.put("window.vSync", false);
			defaults.put("window.fullScreen", false);
			// Insert defaults into config file if they're not present
			for (final Entry<String, Object> e : defaults.entrySet()) {
				if (!config.contains(e.getKey())) {
					config.set(e.getKey(), e.getValue());
				}
			}
			// Save
			config.save(configFile);
			//Load variables from config
			Config.vSync = config.getBoolean("window.vSync", false);
			Config.fullScreen = config.getBoolean("window.fullScreen", false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Reloads config
	 */
	public void reload() {
		//TODO implement
	}

}
