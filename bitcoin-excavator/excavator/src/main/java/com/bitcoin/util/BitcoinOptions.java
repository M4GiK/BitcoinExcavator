/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */

package com.bitcoin.util;

import java.net.Proxy;
import java.util.Set;

/**
 * Container for Bitcoin options.
 *
 * @author m4gik <michal.szczygiel@wp.pl>, Aleksander Śmierciak
 */
public class BitcoinOptions {
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
