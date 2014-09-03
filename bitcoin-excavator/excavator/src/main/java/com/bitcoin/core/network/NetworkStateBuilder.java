/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 3, 2014.
 */
package com.bitcoin.core.network;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;
import com.bitcoin.util.BitcoinOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Class responsible for build {@link com.bitcoin.core.network.NetworkState}.
 *
 * @author m4gik <michal.szczygiel@wp.pl>, Aleksander Śmierciak
 */
public class NetworkStateBuilder {
    private static final String URL_SEPARATOR = "+++++";

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitcoinOptions.class);

    /**
     * This method makes configuration for network connections.
     *
     * @param bitcoinOptions The instance of {@link com.bitcoin.util.BitcoinOptions}
     *                       with options.
     * @param excavator      The instance of  {@link com.bitcoin.core.Excavator} to bind the Threads.
     * @return List of networks states.
     */
    public static ArrayList<NetworkState> networkConfiguration(BitcoinOptions bitcoinOptions, Excavator excavator) {
        setNetworkOptions(bitcoinOptions);
        ArrayList<NetworkState> networkStatesList = new ArrayList<NetworkState>(bitcoinOptions.getNetworkStatesAmount());

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
     * Sets network options for {@link com.bitcoin.core.network.NetworkStateBuilder}.
     *
     * @param bitcoinOptions The instance of {@link com.bitcoin.util.BitcoinOptions}
     *                       with options.
     */
    private static void setNetworkOptions(BitcoinOptions bitcoinOptions) {
        Integer networkOptions = 0;

        if (bitcoinOptions.getUrl() != null) {
            networkOptions = bitcoinOptions.getUrl().length;
        } else {
            networkOptions = Math
                    .max(bitcoinOptions.getUser().length, networkOptions);
            networkOptions = Math.max(bitcoinOptions.getPassword().length,
                    networkOptions);
            networkOptions = Math
                    .max(bitcoinOptions.getHost().length, networkOptions);
            networkOptions = Math
                    .max(bitcoinOptions.getPort().length, networkOptions);
        }

        bitcoinOptions.setNetworkStatesAmount(networkOptions);
    }
}
