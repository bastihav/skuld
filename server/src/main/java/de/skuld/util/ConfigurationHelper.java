package de.skuld.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigurationHelper {

  private static Configuration config;

  public static void loadDefaultConfig() {
    try {
      InputStream stream = ConfigurationHelper.class.getClassLoader().getResourceAsStream("config.properties");
      File f = File.createTempFile("skuld", "config");
      PrintWriter printWriter = new PrintWriter(f);
      InputStreamReader inputStreamReader = new InputStreamReader(stream);
      BufferedReader reader = new BufferedReader(inputStreamReader);

      printWriter.flush();
      loadConfig(f);

      while (reader.ready()) {
        printWriter.println(reader.readLine());
      }
    } catch (IOException | ConfigurationException e) {
      e.printStackTrace();
    }
  }

  public static void loadConfig(File file) throws ConfigurationException {
    Parameters params = new Parameters();
    FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params.properties()
                .setFileName("config.properties")
                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

    if (file != null) {
      if (file.exists()) {
        config = builder.getConfiguration();
        builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class)
            .configure(params.properties()
                .setFile(file)
                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

        Configuration localConfig = builder.getConfiguration();
        for (Iterator<String> it = localConfig.getKeys(); it.hasNext(); ) {
          String key = it.next();
          config.setProperty(key, localConfig.getProperty(key));
        }
        return;
      }
    }

    /*try {
      config = builder.getConfiguration();

      // Load external configuration file
      Path home = Paths.get(System.getProperty("user.home"), config.getString("home.folder"),
          "Configuration.properties");

      if (home.toFile().exists()) {
        builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class)
            .configure(params.properties()
                .setFile(home.toFile())
                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));

        Configuration localConfig = builder.getConfiguration();
        for (Iterator<String> it = localConfig.getKeys(); it.hasNext(); ) {
          String key = it.next();
          config.setProperty(key, localConfig.getProperty(key));
        }
      }
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }*/
  }

  public static Configuration getConfig() {
    if (config == null) {
      loadDefaultConfig();
    }
    return config;
  }
}
