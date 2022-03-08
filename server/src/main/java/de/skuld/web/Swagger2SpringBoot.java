package de.skuld.web;

import de.skuld.util.ConfigurationHelper;
import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableOpenApi
@ComponentScan(basePackages = { "de.skuld.web", "de.skuld.web.api" , "de.skuld.web.configuration"})
public class Swagger2SpringBoot implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void run(String... arg0) {
        if (arg0.length > 0 && arg0[0].equals("exitcode")) {
            throw new ExitException();
        }
    }

    public static void main(String[] args) throws ConfigurationException {
        Options options = setupCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println(e.getMessage());
            formatter.printHelp("help", options);

            System.exit(1);
        }

        if (cmd.hasOption("c")) {
            ConfigurationHelper.loadConfig(new File(cmd.getOptionValue("c")));
        }

        new SpringApplication(Swagger2SpringBoot.class).run(args);

        long sizeInBytes = ConfigurationHelper.calculateDiskSpacePerTree();
        LOGGER.info("Each tree will take roughly " + humanReadableByteCountBin(sizeInBytes) + " of disk space.");
        LOGGER.info("Cores available: " + Runtime.getRuntime().availableProcessors());
        LOGGER.info("Heap available: " + humanReadableByteCountSI(Runtime.getRuntime().maxMemory()));
    }

    /**
     * https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
     * @param bytes bytes to format
     * @return string human readable
     */
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    /**
     * https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
     * @param bytes bytes to format
     * @return string human readable
     */
    private static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.3f %ciB", value / 1024.0, ci.current());
    }

    static class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
            return 10;
        }

    }

    private static Options setupCommandLineOptions() {
        Options options = new Options();

        Option configOption = new Option("c", "config", true, "The configuration");
        configOption.setRequired(false);
        options.addOption(configOption);

        return options;
    }
}
