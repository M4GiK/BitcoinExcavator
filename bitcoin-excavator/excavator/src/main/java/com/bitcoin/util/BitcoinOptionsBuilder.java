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
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class BitCoinOptionsBuilder {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitCoinOptions.class);

    ObjectDeserializer<BitCoinOptions> deserializer;

    public BitCoinOptionsBuilder(ObjectDeserializer<BitCoinOptions> deserializer) {
        this.deserializer = deserializer;
    }

    public BitCoinOptions fromFile(String path) throws IOException {
        return BitCoinValidator.validateNetworkParameters(deserializer.loadFromFile(path));
    }

    /**
     * This method returns the instance of {@link BitCoinOptions} class
     * from terminal arguments.
     * (example string  -l http://m4gik24119m4gik:qetuo1357@api.polmine.pl:8347)
     *
     * @param args The list of arguments given from terminal.
     * @return The instance of {@link BitCoinOptions} class.
     */
    public static BitCoinOptions terminalOptions(String... args) {
        Options options = new Options();
        PosixParser parser = new PosixParser();

        options.addOption("u", "user", true, "bitcoin host username");
        options.addOption("p", "pass", true, "bitcoin host password");
        options.addOption("o", "host", true, "bitcoin host IP");
        options.addOption("r", "port", true, "bitcoin host port");
        options.addOption("l", "url", true, "bitcoin host url");
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

        return BitCoinValidator.validateNetworkParameters(getOptionsFromCommanLine(commandLine));
    }

    /**
     * This method get infomration from command line and put it to {@link BitCoinOptions}
     *
     * @param commandLine The instance of {@link org.apache.commons.cli.CommandLine} with command line parameters.
     * @return The instance of {@link BitCoinOptions} with options.
     */
    public static BitCoinOptions getOptionsFromCommanLine(
            CommandLine commandLine) {
        BitCoinOptions bitCoinOptions = new BitCoinOptions();

//        if (commandLine.hasOption("user")) {
//            bitcoinOptions.setUser(commandLine.getOptionValues("user"));
//        }
//
//        if (commandLine.hasOption("pass")) {
//            bitcoinOptions.setPassword(commandLine.getOptionValues("pass"));
//        }
//
//        if (commandLine.hasOption("host")) {
//            bitcoinOptions.setHost(commandLine.getOptionValues("host"));
//        }
//
//        if (commandLine.hasOption("port")) {
//            bitcoinOptions.setPort(commandLine.getOptionValues("port"));
//        }

        if (commandLine.hasOption("proxy")) {
            final String[] proxySettings = commandLine.getOptionValue("proxy")
                    .split(
                            ":");

            if (proxySettings.length >= 2) {
                bitCoinOptions.setProxy(new Proxy(Proxy.Type.HTTP,
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
            bitCoinOptions.setWorklifetime(Integer.parseInt(
                    commandLine.getOptionValue("worklifetime")) * 1000);
        }

        if (commandLine.hasOption("debug")) {
            bitCoinOptions.setDebug(true);
        }

        if (commandLine.hasOption("debugtimer")) {
            bitCoinOptions.setDebugtimer(true);
        }

        if (commandLine.hasOption("devices")) {
            String devices[] = commandLine.getOptionValues("devices");
            bitCoinOptions.setEnabledDevices(new HashSet<>());

            for (String device : devices) {
                bitCoinOptions.getEnabledDevices().add(device);

                if (Integer.parseInt(device) == 0) {
                    log.error("Do not use 0 with -D, devices start at 1");
                }
            }
        }

        if (commandLine.hasOption("fps")) {
            bitCoinOptions.setGPUTargetFPS(Double.parseDouble(commandLine
                    .getOptionValue("fps")));

            if (bitCoinOptions.getGPUTargetFPS() < 0.1) {
                log.error("--fps argument is too low, adjusting to 0.1");
                bitCoinOptions.setGPUTargetFPS(0.1);
            }
        }

        if (commandLine.hasOption("noarray")) {
            bitCoinOptions.setGPUNoArray(true);
        }

        if (commandLine.hasOption("worksize")) {
            bitCoinOptions.setGPUForceWorkSize(
                    Integer.parseInt(commandLine.getOptionValue("worksize")));
        }

        if (commandLine.hasOption("vectors")) {
            String tempVectors[] = commandLine.getOptionValue("vectors")
                    .split(",");
            bitCoinOptions.setGPUVectors(new Integer[tempVectors.length]);

            for (int i = 0; i < bitCoinOptions.getGPUVectors().length; i++) {
                bitCoinOptions.getGPUVectors()[i] = Integer
                        .parseInt(tempVectors[i]);

                if (bitCoinOptions.getGPUVectors()[i] > 16) {
                    log.error("Use comma-seperated vector layouts");
                } else if (bitCoinOptions.getGPUVectors()[i] != 1
                        && bitCoinOptions.getGPUVectors()[i] != 2
                        && bitCoinOptions.getGPUVectors()[i] != 3
                        && bitCoinOptions.getGPUVectors()[i] != 4
                        && bitCoinOptions.getGPUVectors()[i] != 8
                        && bitCoinOptions.getGPUVectors()[i] != 16) {
                    log.error(bitCoinOptions.getGPUVectors()[i]
                            + "is not a vector length of 1, 2, 3, 4, 8, or 16");
                }
            }
            Arrays.sort(bitCoinOptions.getGPUVectors(),
                    Collections.reverseOrder());
        } else {
            bitCoinOptions.setGPUVectors(new Integer[1]);
            bitCoinOptions.getGPUVectors()[0] = 1;
        }

        if (commandLine.hasOption("ds")) {
            bitCoinOptions.setGPUDebugSource(true);
        }

        return bitCoinOptions;
    }
}
