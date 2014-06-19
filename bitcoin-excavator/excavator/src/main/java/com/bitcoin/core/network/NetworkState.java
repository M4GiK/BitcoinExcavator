/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */

package com.bitcoin.core.network;

import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is responsible for provide gathering information about network state.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public abstract class NetworkState {

    private Byte hostChain;

    private Byte hostProtocol;

    private Long workLifetime;

    private Boolean rollNTime;

    private URL queryUrl;

    private String user;

    private String password;

    private AtomicLong refreshTimestamp;

    public static final byte PROTOCOL_JSONRPC = 0;

    public static final byte PROTOCOL_STRATUM = 1;

    public static final byte CHAIN_BITCOIN = 0;

    public static final byte CHAIN_LITECOIN = 1;

    public static final String STRATUM = "stratum";

    /**
     * Constructor for {@link com.bitcoin.core.network.NetworkState} class.
     */
    public NetworkState() {
        setRefreshTimestamp(new AtomicLong(0));
    }

    /**
     * Constructor for {@link com.bitcoin.core.network.NetworkState} class.
     *
     * @param hostChain The chain of hosts.
     * @param queryUrl The url query.
     * @param user The username for mine
     * @param password The password
     */
    public NetworkState(URL queryUrl, Byte hostChain, String user,
            String password) {
        setQueryUrl(queryUrl);
        setHostChain(hostChain);
        setUser(user);
        setPassword(password);
        setRefreshTimestamp(new AtomicLong(0));

        if (getQueryUrl().getProtocol().equals(STRATUM)) {
            setHostProtocol(PROTOCOL_STRATUM);
        }
    }

    public Byte getHostChain() {
        return hostChain;
    }

    public void setHostChain(Byte hostChain) {
        this.hostChain = hostChain;
    }

    public Byte getHostProtocol() {
        return hostProtocol;
    }

    public void setHostProtocol(Byte hostProtocol) {
        this.hostProtocol = hostProtocol;
    }

    public Long getWorkLifetime() {
        return workLifetime;
    }

    public void setWorkLifetime(Long workLifetime) {
        this.workLifetime = workLifetime;
    }

    public Boolean getRollNTime() {
        return rollNTime;
    }

    public void setRollNTime(Boolean rollNTime) {
        this.rollNTime = rollNTime;
    }

    public URL getQueryUrl() {
        return queryUrl;
    }

    public void setQueryUrl(URL queryUrl) {
        this.queryUrl = queryUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AtomicLong getRefreshTimestamp() {
        return refreshTimestamp;
    }

    public void setRefreshTimestamp(AtomicLong refreshTimestamp) {
        this.refreshTimestamp = refreshTimestamp;
    }
}
