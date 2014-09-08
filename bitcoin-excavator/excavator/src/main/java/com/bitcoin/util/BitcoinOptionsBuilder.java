/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 3, 2014.
 */
package com.bitcoin.util;

import com.bitcoin.util.serialization.ObjectDeserializer;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Class responsible for build {@link com.bitcoin.util.BitcoinOptions}.
 *
 * @author m4gik <michal.szczygiel@wp.pl>, Aleksander Śmierciak
 */
public class BitcoinOptionsBuilder {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitcoinOptions.class);

    private static final String URL_SEPARATOR = "+++++";

    ObjectDeserializer<BitcoinOptions> deserializer;

    public BitcoinOptionsBuilder(ObjectDeserializer<BitcoinOptions> deserializer) {
        this.deserializer = deserializer;
    }

    public BitcoinOptions fromFile(String path) throws IOException {
        return BitcoinValidator.validateNetworkParameters(deserializer.loadFromFile(path));
    }

    /**
     * This method returns the instance of {@link BitcoinOptions} class
     * from terminal arguments.
     * (example string  -l http://m4gik24119m4gik:qetuo1357@api.polmine.pl:8347)
     *
     * @param args The list of arguments given from terminal.
     * @return The instance of {@link BitcoinOptions} class.
     */
    public static BitcoinOptions terminalOptions(String... args) {
        Options options = new Options();
        PosixParser parser = new PosixParser();

        options.addOption("u", "user", true, "bitcoin host username");
        options.addOption("p", "pass", true, "bitcoin host password");
        options.addOption("o", "host", true, "bitcoin host IP");
        options.addOption("r", "port", true, "bitcoin host port");
        options.addOption("l", "url", true, "bitcoin host url");
        options.addOption("q", "protocol", true, "bitcoin protocol");
        options.addOption("x", "proxy", true,
                "optional proxy settings IP:PORT<:username:password>");
        options.addOption("g", "worklifetime", true,
                "maximum work lifetime in seconds");
        options.addOption("d", "debug", false, "enable debug output");
        options.addOption("dt", "debugtimer", false,
                "run for 1 minute and quit");
        options.addOption("D", "devices", true,
                "devices to enable, default all");
        options.addOption("f", "fps", true, "target GPU execution timing");
        options.addOption("na", "noarray", false, "turn GPU kernel array off");
        options.addOption("v", "vectors", true, "vector size in GPU kernel");
        options.addOption("w", "worksize", true, "override GPU worksize");
        options.addOption("ds", "ksource", false, "output GPU kernel source");
        options.addOption("h", "help", false, "this help");

        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(options, args);

            if (commandLine.hasOption("help")) {
                throw new ParseException("");
            }

        } catch (ParseException e) {
            log.error(e.getLocalizedMessage());
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(
                    "BitcoinExcavator -u myuser -p mypassword [args]\n", "",
                    options,
                    "\nRemember to set rpcuser and rpcpassword in your ~/.bitcoin/bitcoin.conf "
                            + "before starting bitcoind or bitcoin --daemon");
        }

        return BitcoinValidator.validateNetworkParameters(getOptionsFromCommanLine(commandLine));
    }

    /**
     * This method get infomration from command line and put it to {@link BitcoinOptions}
     *
     * @param commandLine The instance of {@link org.apache.commons.cli.CommandLine} with command line parameters.
     * @return The instance of {@link BitcoinOptions} with options.
     */
    public static BitcoinOptions getOptionsFromCommanLine(
            CommandLine commandLine) {
        BitcoinOptions bitcoinOptions = new BitcoinOptions();

        bitcoinOptions.addCredential(getCredentail(commandLine));

        if (commandLine.hasOption("proxy")) {
            final String[] proxySettings = commandLine.getOptionValue("proxy")
                    .split(":");

            if (proxySettings.length >= 2) {
                bitcoinOptions.setProxy(new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(proxySettings[0],
                                Integer.valueOf(proxySettings[2]))));
            }

            if (proxySettings.length >= 3) {
                Authenticator.setDefault(new Authenticator() {

                    /**
                     * Called when password authorization is needed.  Subclasses should
                     * override the default implementation, which returns null.
                     *
                     * @return The PasswordAuthentication collected from the
                     * user, or null if none is provided.
                     */
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxySettings[2],
                                proxySettings[3].toCharArray());
                    }
                });

            }
        }

        if (commandLine.hasOption("worklifetime")) {
            bitcoinOptions.setWorklifetime(Integer.parseInt(
                    commandLine.getOptionValue("worklifetime")) * 1000);
        }

        if (commandLine.hasOption("debug")) {
            bitcoinOptions.setDebug(true);
        }

        if (commandLine.hasOption("debugtimer")) {
            bitcoinOptions.setDebugtimer(true);
        }

        if (commandLine.hasOption("devices")) {
            String devices[] = commandLine.getOptionValues("devices");
            bitcoinOptions.setEnabledDevices(new HashSet<>());

            for (String device : devices) {
                bitcoinOptions.getEnabledDevices().add(device);

                if (Integer.parseInt(device) == 0) {
                    log.error("Do not use 0 with -D, devices start at 1");
                }
            }
        }

        if (commandLine.hasOption("fps")) {
            bitcoinOptions.setGPUTargetFPS(Double.parseDouble(commandLine
                    .getOptionValue("fps")));

            if (bitcoinOptions.getGPUTargetFPS() < 0.1) {
                log.error("--fps argument is too low, adjusting to 0.1");
                bitcoinOptions.setGPUTargetFPS(0.1);
            }
        }

        if (commandLine.hasOption("noarray")) {
            bitcoinOptions.setGPUNoArray(true);
        }

        if (commandLine.hasOption("worksize")) {
            bitcoinOptions.setGPUForceWorkSize(
                    Integer.parseInt(commandLine.getOptionValue("worksize")));
        }

        if (commandLine.hasOption("vectors")) {
            String tempVectors[] = commandLine.getOptionValue("vectors")
                    .split(",");
            bitcoinOptions.setGPUVectors(new Integer[tempVectors.length]);

            for (int i = 0; i < bitcoinOptions.getGPUVectors().length; i++) {
                bitcoinOptions.getGPUVectors()[i] = Integer
                        .parseInt(tempVectors[i]);

                if (bitcoinOptions.getGPUVectors()[i] > 16) {
                    log.error("Use comma-seperated vector layouts");
                } else if (bitcoinOptions.getGPUVectors()[i] != 1
                        && bitcoinOptions.getGPUVectors()[i] != 2
                        && bitcoinOptions.getGPUVectors()[i] != 3
                        && bitcoinOptions.getGPUVectors()[i] != 4
                        && bitcoinOptions.getGPUVectors()[i] != 8
                        && bitcoinOptions.getGPUVectors()[i] != 16) {
                    log.error(bitcoinOptions.getGPUVectors()[i]
                            + "is not a vector length of 1, 2, 3, 4, 8, or 16");
                }
            }
            Arrays.sort(bitcoinOptions.getGPUVectors(),
                    Collections.reverseOrder());
        } else {
            bitcoinOptions.setGPUVectors(new Integer[1]);
            bitcoinOptions.getGPUVectors()[0] = 1;
        }

        if (commandLine.hasOption("ds")) {
            bitcoinOptions.setGPUDebugSource(true);
        }

        return bitcoinOptions;
    }

    /**
     * Method gets credentials from command line.
     *
     * @param commandLine  The instance of {@link org.apache.commons.cli.CommandLine} with command line parameters.
     * @return the instance of {@link com.bitcoin.util.Credential}.
     */
    private static Credential getCredentail(CommandLine commandLine) {
        Credential credential = new Credential();

        if(commandLine.hasOption("url")) {
            String uniformResourceLocator = commandLine.getOptionValue("url");

            if(uniformResourceLocator != null) {
                String[] usernameFix = uniformResourceLocator.split("@", 3);
                
                if(usernameFix.length > 2) {
                    uniformResourceLocator = usernameFix[0] + URL_SEPARATOR + usernameFix[1] + "@" + usernameFix[2];
                }

                try {
                    URL url = new URL(uniformResourceLocator);

                    if(url.getProtocol() != null && url.getProtocol().length() > 1)
                        credential.setProtocol(url.getProtocol());

                    if(url.getHost() != null && url.getHost().length() > 1)
                        credential.setHost(url.getHost());

                    if(url.getPort() != -1)
                        credential.setPort(url.getPort());

                    if(url.getPath() != null && url.getPath().length() > 1)
                        credential.setPath(url.getPath());

                    if(url.getUserInfo() != null && url.getUserInfo().length() > 1) {
                        String[] userPassSplit = url.getUserInfo().split(":");

                       credential.setLogin(userPassSplit[0].replace(URL_SEPARATOR, "@"));

                        if(userPassSplit.length > 1 && userPassSplit[1].length() > 1)
                            credential.setPassword(userPassSplit[1]);
                    }

                } catch (MalformedURLException e) {
                   log.error(e.getMessage());
                }
            }
        }

        if (commandLine.hasOption("user")) {
            credential.setLogin(commandLine.getOptionValue("user"));
        }

        if (commandLine.hasOption("pass")) {
            credential.setPassword(commandLine.getOptionValue("pass"));
        }

        if (commandLine.hasOption("host")) {
            credential.setHost(commandLine.getOptionValue("host"));
        }

        if (commandLine.hasOption("port")) {
            credential.setPort(Integer.parseInt(commandLine.getOptionValue("port")));
        }

        if (commandLine.hasOption("protocol")) {
            credential.setProtocol(commandLine.getOptionValue("protocol"));
        }

        return credential;
    }
}
