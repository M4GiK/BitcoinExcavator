/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */

package com.bitcoin.util;

import com.bitcoin.core.BitcoinExcavatorFatalException;
import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;
import com.bitcoin.core.network.JSONRPCNetworkState;
import com.bitcoin.core.network.NetworkState;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.*;

/**
 * Container for Bitcoin options.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class BitcoinOptions {

    private static final String URL_SEPARATOR = "+++++";

    private String[] url;

    private String[] user;

    private String[] password;

    private String[] host;

    private String[] port;

    private Proxy proxy;

    private Integer worklifetime = 5000;

    private Integer networkStatesAmount;

    private Boolean debug = false;

    private Boolean debugtimer = false;

    private Set<String> enabledDevices;

    private Double GPUTargetFPS = 30.0;

    private Double GPUTargetFPSBasis;

    private Integer GPUForceWorkSize = 0;

    private Integer GPUVectors[];

    private Boolean GPUNoArray = false;

    private Boolean GPUDebugSource = false;

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

        return validOptions(getOptionsFromCommanLine(commandLine));
    }

    /**
     * This method get infomration from command line and put it to {@link com.bitcoin.util.BitcoinOptions}
     *
     * @param commandLine The instance of {@link org.apache.commons.cli.CommandLine} with command line parameters.
     * @return The instance of {@link com.bitcoin.util.BitcoinOptions} with options.
     */
    public static BitcoinOptions getOptionsFromCommanLine(
            CommandLine commandLine) {
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

        if (commandLine.hasOption("proxy")) {
            final String[] proxySettings = commandLine.getOptionValue("proxy")
                    .split(
                            ":");

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
            bitcoinOptions.setEnabledDevices(new HashSet<String>());

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

        if(commandLine.hasOption("ds")) {
            bitcoinOptions.setGPUDebugSource(true);
        }

        return bitcoinOptions;
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
     * This method makes configuration for network connections.
     *
     * @param bitcoinOptions The instance of {@link com.bitcoin.util.BitcoinOptions}
     *                       with options.
     * @param excavator      The instance of  {@link com.bitcoin.core.Excavator} to bind the Threads.
     * @return List of networks states.
     */
    public static ArrayList<NetworkState> networkConfiguration(
            BitcoinOptions bitcoinOptions, Excavator excavator) {
        ArrayList<NetworkState> networkStatesList = new ArrayList<NetworkState>(
                bitcoinOptions.getNetworkStatesAmount());

        for (int i = 0; i < bitcoinOptions.getNetworkStatesAmount(); i++) {

            String protocol = "http";
            String host = "localhost";
            Integer port = 8332;
            String path = "/";
            String user = "excavatorer";
            String password = "excavatorer";
            Byte hostChain = 0;

            if (bitcoinOptions.getUrl().length > i) {

                try {
                    // TODO Need refactor
                    String[] usernameFix = bitcoinOptions.getUrl()[i]
                            .split("@", 3);
                    if (usernameFix.length > 2) {
                        bitcoinOptions.getUrl()[i] =
                                usernameFix[0] + URL_SEPARATOR + usernameFix[1]
                                        + "@"
                                        + usernameFix[2];
                    }

                    URL url = new URL(bitcoinOptions.getUrl()[i]);

                    if (url.getProtocol() != null
                            && url.getProtocol().length() > 1) {
                        protocol = url.getProtocol();
                    }

                    if (url.getHost() != null && url.getHost().length() > 1) {
                        host = url.getHost();
                    }

                    if (url.getPort() != -1) {
                        port = url.getPort();
                    }

                    if (url.getPath() != null && url.getPath().length() > 1) {
                        path = url.getPath();
                    }

                    if (url.getUserInfo() != null
                            && url.getUserInfo().length() > 1) {
                        String[] userPassSplit = url.getUserInfo().split(":");

                        user = userPassSplit[0].replace(URL_SEPARATOR, "@");

                        if (userPassSplit.length > 1
                                && userPassSplit[1].length() > 1)
                            password = userPassSplit[1];
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                if (bitcoinOptions.getUser() != null
                        && bitcoinOptions.getUser().length > i) {
                    user = bitcoinOptions.getUser()[i];
                }

                if (bitcoinOptions.getPassword() != null
                        && bitcoinOptions.getPassword().length > i) {
                    password = bitcoinOptions.getPassword()[i];
                }

                if (bitcoinOptions.getHost() != null
                        && bitcoinOptions.getHost().length > i) {
                    host = bitcoinOptions.getHost()[i];
                }

                if (bitcoinOptions.getPort() != null
                        && bitcoinOptions.getPort().length > i) {
                    port = Integer.parseInt(bitcoinOptions.getPort()[i]);
                }

                NetworkState networkState = null;

                try {
                    networkState = new JSONRPCNetworkState(excavator,
                            new URL(protocol, host, port, path), user, password,
                            hostChain);
                } catch (MalformedURLException e) {
                    try {
                        throw new ExcavatorFatalException(excavator,
                                "Malformed connection paramaters");
                    } catch (ExcavatorFatalException ex) {
                        log.error(ex.getLocalizedMessage());
                    }
                }

                networkStatesList.add(i, networkState);
            }
        }

        return networkStatesList;
    }

    /**
     * This method makes configuration for network connections.
     *
     * @param excavator The instance of  {@link com.bitcoin.core.Excavator} to bind the Threads.
     * @return List of networks states.
     */
    public ArrayList<NetworkState> networkConfiguration(Excavator excavator) {
        ArrayList<NetworkState> networkStatesList = new ArrayList<NetworkState>(
                getNetworkStatesAmount());

        for (int i = 0; i < getNetworkStatesAmount(); i++) {

            String protocol = "http";
            String host = "localhost";
            Integer port = 8332;
            String path = "/";
            String user = "excavatorer";
            String password = "excavatorer";
            Byte hostChain = 0;

            if (getUrl().length > i) {

                try {
                    // TODO Need refactor
                    String[] usernameFix = getUrl()[i]
                            .split("@", 3);
                    if (usernameFix.length > 2) {
                        getUrl()[i] =
                                usernameFix[0] + URL_SEPARATOR + usernameFix[1]
                                        + "@"
                                        + usernameFix[2];
                    }

                    URL url = new URL(getUrl()[i]);

                    if (url.getProtocol() != null
                            && url.getProtocol().length() > 1) {
                        protocol = url.getProtocol();
                    }

                    if (url.getHost() != null && url.getHost().length() > 1) {
                        host = url.getHost();
                    }

                    if (url.getPort() != -1) {
                        port = url.getPort();
                    }

                    if (url.getPath() != null && url.getPath().length() > 1) {
                        path = url.getPath();
                    }

                    if (url.getUserInfo() != null
                            && url.getUserInfo().length() > 1) {
                        String[] userPassSplit = url.getUserInfo().split(":");

                        user = userPassSplit[0].replace(URL_SEPARATOR, "@");

                        if (userPassSplit.length > 1
                                && userPassSplit[1].length() > 1)
                            password = userPassSplit[1];
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                if (getUser() != null
                        && getUser().length > i) {
                    user = getUser()[i];
                }

                if (getPassword() != null
                        && getPassword().length > i) {
                    password = getPassword()[i];
                }

                if (getHost() != null
                        && getHost().length > i) {
                    host = getHost()[i];
                }

                if (getPort() != null
                        && getPort().length > i) {
                    port = Integer.parseInt(getPort()[i]);
                }

                NetworkState networkState = null;

                try {
                    networkState = new JSONRPCNetworkState(excavator,
                            new URL(protocol, host, port, path), user, password,
                            hostChain);
                } catch (MalformedURLException e) {
                    try {
                        throw new ExcavatorFatalException(excavator,
                                "Malformed connection paramaters");
                    } catch (ExcavatorFatalException ex) {
                        log.error(ex.getLocalizedMessage());
                    }
                }

                networkStatesList.add(i, networkState);
            }
        }

        return networkStatesList;
    }

    /**
     * Gets proxy information.
     *
     * @return The {@link java.net.Proxy} instance.
     */
    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Gets lifetime for current work.
     *
     * @return The lifetime for work.
     */
    public Integer getWorklifetime() {
        return worklifetime;
    }

    public void setWorklifetime(Integer worklifetime) {
        this.worklifetime = worklifetime;
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

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public Boolean getDebugtimer() {
        return debugtimer;
    }

    public void setDebugtimer(Boolean debugtimer) {
        this.debugtimer = debugtimer;
    }

    public Set<String> getEnabledDevices() {
        return enabledDevices;
    }

    public void setEnabledDevices(Set<String> enabledDevices) {
        this.enabledDevices = enabledDevices;
    }

    public Double getGPUTargetFPS() {
        return GPUTargetFPS;
    }

    public void setGPUTargetFPS(Double GPUTargetFPS) {
        this.GPUTargetFPS = GPUTargetFPS;
    }

    public Double getGPUTargetFPSBasis() {
        return GPUTargetFPSBasis;
    }

    public void setGPUTargetFPSBasis(Double GPUTargetFPSBasis) {
        this.GPUTargetFPSBasis = GPUTargetFPSBasis;
    }

    public Integer getGPUForceWorkSize() {
        return GPUForceWorkSize;
    }

    public void setGPUForceWorkSize(Integer GPUForceWorkSize) {
        this.GPUForceWorkSize = GPUForceWorkSize;
    }

    public Integer[] getGPUVectors() {
        return GPUVectors;
    }

    public void setGPUVectors(Integer[] GPUVectors) {
        this.GPUVectors = GPUVectors;
    }

    public Boolean getGPUNoArray() {
        return GPUNoArray;
    }

    public void setGPUNoArray(Boolean GPUNoArray) {
        this.GPUNoArray = GPUNoArray;
    }

    public Boolean getGPUDebugSource() {
        return GPUDebugSource;
    }

    public void setGPUDebugSource(Boolean GPUDebugSource) {
        this.GPUDebugSource = GPUDebugSource;
    }
}
