package com.bitcoin.core.network;

import com.bitcoin.core.Excavator;
import com.bitcoin.util.BitcoinOptions;
import com.bitcoin.util.Credential;
import com.bitcoin.util.URLBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class NetworkStatesBuilder {
    /**
     * This method makes configuration for network connections.
     *
     * @param bitcoinOptions The instance of {@link com.bitcoin.util.BitcoinOptions}
     *                       with options.
     * @param excavator      The instance of  {@link com.bitcoin.core.Excavator} to bind the Threads.
     * @return List of networks states.
     */
    public ArrayList<NetworkState> createNetworkStates(BitcoinOptions bitcoinOptions, Excavator excavator) {
        ArrayList<NetworkState> networkStates = new ArrayList<>();

        byte defaultHostChain = 0;
        URLBuilder urlBuilder = new URLBuilder();

        for (Credential credential : bitcoinOptions.getCredentials()) {
            System.out.println(credential.getPort() + "TUTAJ");
            try {
                URL url = urlBuilder.createURL(credential);
                NetworkState networkState = new JSONRPCNetworkState(excavator, url, credential.getLogin(), credential.getPassword(), defaultHostChain);
                networkStates.add(networkState);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return networkStates;
    }
}
