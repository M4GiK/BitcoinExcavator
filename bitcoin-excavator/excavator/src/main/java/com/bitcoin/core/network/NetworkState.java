/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */

package com.bitcoin.core.network;

import java.net.URL;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.device.ExecutionState;

/**
 * This class is responsible for provide gathering information about network
 * state.
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public abstract class NetworkState {

    public static final byte CHAIN_BITCOIN = 0;

    public static final byte CHAIN_LITECOIN = 1;

    public static final byte PROTOCOL_JSONRPC = 0;

    public static final byte PROTOCOL_STRATUM = 1;

    public static final String STRATUM = "stratum";

    private Excavator excavator;

    protected LinkedBlockingDeque<ExecutionState> getQueue;

    private Byte hostChain;

    private Byte hostProtocol;

    private NetworkState networkStateNext;

    private String password;

    private URL queryUrl;

    protected AtomicLong refreshTimestamp;

    private Boolean rollNTime;

    protected LinkedBlockingDeque<WorkState> sendQueue;

    private String user;

    private Long workLifetime;

    /**
     * Constructor for {@link com.bitcoin.core.network.NetworkState} class.
     */
    public NetworkState() {
        setRefreshTimestamp(new AtomicLong(0));
        this.sendQueue = new LinkedBlockingDeque<WorkState>();
        this.getQueue = new LinkedBlockingDeque<ExecutionState>();
    }

    /**
     * Constructor for {@link com.bitcoin.core.network.NetworkState} class.
     * 
     * @param excavator
     *            The instance of excavator.
     * @param hostChain
     *            The chain of hosts.
     * @param queryUrl
     *            The url query.
     * @param user
     *            The username for mine
     * @param password
     *            The password
     */
    public NetworkState(Excavator excavator, URL queryUrl, Byte hostChain,
            String user, String password) {
        setExcavator(excavator);
        setQueryUrl(queryUrl);
        setHostChain(hostChain);
        setUser(user);
        setPassword(password);
        setRefreshTimestamp(new AtomicLong(0));

        if (getQueryUrl().getProtocol().equals(STRATUM)) {
            setHostProtocol(PROTOCOL_STRATUM);
        }

        this.sendQueue = new LinkedBlockingDeque<WorkState>();
        this.getQueue = new LinkedBlockingDeque<ExecutionState>();
    }

    /**
     * This method adds {@link com.bitcoin.core.device.ExecutionState} instance
     * to {@link java.util.concurrent.LinkedBlockingDeque}.
     * 
     * @param executionState
     *            The executionState to add.
     */
    public void addGetQueue(ExecutionState executionState) {
        this.getQueue.add(executionState);
    }

    /**
     * This method adds {} ins@link WorkState} instance to
     * {@link java.util.concurrent.LinkedBlockingDeque}.
     * 
     * @param workState
     *            Th workState to add.
     */
    public void addSendQueue(WorkState workState) {
        this.sendQueue.add(workState);
    }

    /**
     * @return the excavator
     */
    public Excavator getExcavator() {
        return excavator;
    }

    /**
     * @return the hostChain
     */
    public Byte getHostChain() {
        return hostChain;
    }

    /**
     * @return the hostProtocol
     */
    public Byte getHostProtocol() {
        return hostProtocol;
    }

    /**
     * @return the networkStateNext
     */
    public NetworkState getNetworkStateNext() {
        return networkStateNext;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the queryUrl
     */
    public URL getQueryUrl() {
        return queryUrl;
    }

    /**
     * @return the refreshTimestamp
     */
    public Long getRefreshTimestamp() {
        return refreshTimestamp.get();
    }

    /**
     * @return the rollNTime
     */
    public Boolean getRollNTime() {
        return rollNTime;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the workLifetime
     */
    public Long getWorkLifetime() {
        return workLifetime;
    }

    /**
     * @param excavator
     *            the excavator to set
     */
    public void setExcavator(Excavator excavator) {
        this.excavator = excavator;
    }

    /**
     * @param hostChain
     *            the hostChain to set
     */
    public void setHostChain(Byte hostChain) {
        this.hostChain = hostChain;
    }

    /**
     * @param hostProtocol
     *            the hostProtocol to set
     */
    public void setHostProtocol(Byte hostProtocol) {
        this.hostProtocol = hostProtocol;
    }

    /**
     * @param networkStateNext
     *            the networkStateNext to set
     */
    public void setNetworkStateNext(NetworkState networkStateNext) {
        this.networkStateNext = networkStateNext;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param queryUrl
     *            the queryUrl to set
     */
    public void setQueryUrl(URL queryUrl) {
        this.queryUrl = queryUrl;
    }

    /**
     * @param refreshTimestamp
     *            the refreshTimestamp to set
     */
    public void setRefreshTimestamp(AtomicLong refreshTimestamp) {
        this.refreshTimestamp = refreshTimestamp;
    }

    /**
     * @param rollNTime
     *            the rollNTime to set
     */
    public void setRollNTime(Boolean rollNTime) {
        this.rollNTime = rollNTime;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @param workLifetime
     *            the workLifetime to set
     */
    public void setWorkLifetime(Long workLifetime) {
        this.workLifetime = workLifetime;
    }

}
