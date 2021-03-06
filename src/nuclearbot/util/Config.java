package nuclearbot.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/*
 * Copyright (C) 2017 NuclearCoder
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Static class for configuration.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class Config {

    private static final Properties prop;
    private static final File configFile;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new ConfigShutdownHook()));

        configFile = new File("config.properties");

        if (configFile.isDirectory()) {
            Logger.error("Couldn't write to config.properties in the program's directory.");
            System.exit(1);
        }
        if (!configFile.exists() && configFile.mkdirs() && configFile.delete()) { // create an empty config file if it doesn't exist
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                Logger.error("An error occurred while creating config file.");
                Logger.printStackTrace(e);
                System.exit(1);
            }
        }

        prop = new Properties();

        try (final FileReader in = new FileReader(configFile)) {
            prop.load(in);
        } catch (IOException e) {
            Logger.error("An error occurred while loading config.");
            Logger.printStackTrace(e);
        }
    }

    /**
     * Writes the configuration into the file.
     *
     * @throws IOException if the file exists but is a directory
     *                     rather than a regular file, does not exist but cannot be
     *                     created, or cannot be opened for any other reason
     */
    public static void saveConfig() throws IOException {
        try (final FileWriter writer = new FileWriter(configFile)) {
            prop.store(writer, "please do not attempt to edit anything manually unless explicitly directed otherwise");
        }
    }

    /**
     * Reloads the configuration from the file.
     *
     * @throws IOException if the file does not exist, is
     *                     a directory rather than a regular file, or for some other
     *                     reason cannot be opened for reading.
     */
    public static void reloadConfig() throws IOException {
        try (final FileReader reader = new FileReader(configFile)) {
            prop.load(reader);
        }
    }

    /**
     * Returns the property with the specified key in this
     * configuration. If the key is not found in the list,
     * the method returns the default value and the property is set.
     *
     * @param key          the property key
     * @param defaultValue the default value
     * @return the value in this property list with the specified key
     */
    public static String get(final String key, final String defaultValue) {
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        } else {
            prop.setProperty(key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the property with the specified key in this
     * configuration. If the key is not found in the list,
     * the method returns an empty string and the property is set.
     *
     * @param key the property key
     * @return the value in this property list with the specified key
     */
    public static String get(final String key) {
        return get(key, "");
    }

    /**
     * Sets the property with the specified key in this
     * configuration with the specified value. This method
     * returns the previous value, or null if there was none.
     *
     * @param key   the property key
     * @param value the new value
     * @return the previous value, or null
     */
    public static String set(final String key, final String value) {
        return (String) prop.setProperty(key, value);
    }

    private static class ConfigShutdownHook implements Runnable {

        @Override
        public void run() {
            Logger.info("(Exit) Saving config...");
            try {
                saveConfig();
            } catch (IOException e) {
                Logger.error("(Exit) Couldn't save config.");
                Logger.printStackTrace(e);
            }
        }

    }

}
