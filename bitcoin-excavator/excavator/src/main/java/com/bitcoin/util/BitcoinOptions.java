/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */

package com.bitcoin.util;

import com.bitcoin.core.network.JSONRPCNetworkState;
import com.bitcoin.core.network.NetworkState;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Container for Bitcoin options.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class BitcoinOptions {

    private String[] url;

    private String[] user;

    private String[] password;

    private String[] host;

    private String[] port;

    private Integer networkStatesAmount;

    private NetworkState networkStateHead;

    private NetworkState networkStateTail;

    /**
     * This method returns the instance of {@link BitcoinOptions} class from properties file.
     *
     * @param filepath The filepath to file with properties.
     * @return The instance of {@link BitcoinOptions} class.
     */
    public static BitcoinOptions propertiesFile(String filepath) {
        //TODO Add implementation for read options from properties file.
        return null;
    }

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory
            .getLogger(BitcoinOptions.class);

    /**
     * This method returns the instance of {@link BitcoinOptions} class
     * from terminal arguments.
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
        options.addOption("ds", "ksource", false,
                "output GPU kernel source and quit");
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

        BitcoinOptions bitcoinOptions = new BitcoinOptions();

        if (commandLine.hasOption("url")) {
            bitcoinOptions.setUrl(commandLine.getOptionValues("url"));
        }

        if (commandLine.hasOption("user")) {
            bitcoinOptions.setUser(commandLine.getOptionValues("user"));
        }

        if (commandLine.hasOption("pass")) {
            bitcoinOptions.setPassword(commandLine.getOptionValues("pass"));
        }

        if (commandLine.hasOption("host")) {
            bitcoinOptions.setHost(commandLine.getOptionValues("host"));
        }

        if (commandLine.hasOption("port")) {
            bitcoinOptions.setPort(commandLine.getOptionValues("port"));
        }

        return validOptions(bitcoinOptions);
    }

    /**
     * Valids the options for {@link BitcoinOptions} class. If validation fails
     * throws exception {@link IllegalArgumentException}
     *
     * @param bitcoinOptions The instance with options.
     * @return The instance of {@link BitcoinOptions} class with validated options.
     */
    public static BitcoinOptions validOptions(BitcoinOptions bitcoinOptions) {
        try {
            Integer networkOptions = 0;

            if (bitcoinOptions.getUrl() != null) {
                networkOptions = bitcoinOptions.getUrl().length;
            } else if (bitcoinOptions.getUser() != null
                    && bitcoinOptions.getPassword() != null
                    && bitcoinOptions.getHost() != null
                    && bitcoinOptions.getPort() != null) {
                networkOptions = Math
                        .max(bitcoinOptions.getUser().length, networkOptions);
                networkOptions = Math.max(bitcoinOptions.getPassword().length,
                        networkOptions);
                networkOptions = Math
                        .max(bitcoinOptions.getHost().length, networkOptions);
                networkOptions = Math
                        .max(bitcoinOptions.getPort().length, networkOptions);
            } else {
                throw new IllegalArgumentException();
            }

            bitcoinOptions.setNetworkStatesAmount(networkOptions);

        } catch (IllegalArgumentException ex) {
            log.error(
                    "You forgot to give any bitcoin connection info," +
                            " please add either -l, or -u -p -o and -r");
        }

        return bitcoinOptions;
    }

    /**
     * @param bitcoinOptions
     * @return
     */
    public static ArrayList<NetworkState> networkConfiguration(
            BitcoinOptions bitcoinOptions) {
        ArrayList<NetworkState> networkStatesList = new ArrayList<NetworkState>(
                bitcoinOptions.getNetworkStatesAmount());

        for (int i = 0; i < bitcoinOptions.getNetworkStatesAmount(); i++) {
            NetworkState networkState = new JSONRPCNetworkState();
            if (bitcoinOptions.getUrl().length > i) {
                try {
                    URL url = new URL(bitcoinOptions.getUrl()[i]);

                    if (url.getProtocol() != null
                            && url.getProtocol().length() > 1) {
                        //networkState.setProtocol(url.getProtocol());
                    }

                    if(url.getHost() != null && url.getHost().length() > 1) {

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * @return
     */
    public ArrayList<NetworkState> networkConfiguration() {
        return null;
    }

    public String[] getUrl() {
        return url;
    }

    public void setUrl(String[] url) {
        this.url = url;
    }

    public String[] getUser() {
        return user;
    }

    public void setUser(String[] user) {
        this.user = user;
    }

    public String[] getPassword() {
        return password;
    }

    public void setPassword(String[] password) {
        this.password = password;
    }

    public String[] getHost() {
        return host;
    }

    public void setHost(String[] host) {
        this.host = host;
    }

    public String[] getPort() {
        return port;
    }

    public void setPort(String[] port) {
        this.port = port;
    }

    public Integer getNetworkStatesAmount() {
        return networkStatesAmount;
    }

    public void setNetworkStatesAmount(Integer networkStatesAmount) {
        this.networkStatesAmount = networkStatesAmount;
    }
}
