package de.skuld.util;

import de.skuld.radix.manager.RNGManager;
import de.skuld.radix.manager.SeedManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
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
      }
    }
  }

  public static Configuration getConfig() {
    if (config == null) {
      loadDefaultConfig();
    }
    return config;
  }

  public static long calculateDiskSpacePerTree() {
    long seeds = new SeedManager().getSeeds(new Date()).length;
    long prngs = RNGManager.getPRNGs().size();

    // 4 kiByte
    int fileOverhead = 4096;

    // 256 byte
    int iNodeOverhead = 256;

    int maxHeight = getConfig().getInt("radix.height.max");
    int partitionSize = getConfig().getInt("radix.partition.size");
    int bytesPerPrng = config.getInt("radix.prng.amount");
    int partitionOnDisk = getConfig().getInt("radix.partition.serialized");

    long result = 0;

    // leaf file overhead
    result += Math.pow(256, maxHeight) * (fileOverhead + iNodeOverhead);

    // leaf file contents
    long amountOfPartitions = prngs * seeds * bytesPerPrng / partitionSize;
    result += amountOfPartitions * partitionOnDisk;

    // directories
    for (int i = 0; i <= maxHeight; i++) {
      result += Math.pow(256, i) * (fileOverhead + iNodeOverhead);
    }

    return result;
  }
}
