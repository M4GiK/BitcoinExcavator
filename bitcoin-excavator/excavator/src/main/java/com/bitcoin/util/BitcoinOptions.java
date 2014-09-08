/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */

package com.bitcoin.util;

import java.net.Proxy;
import java.util.*;

/**
 * Container for Bitcoin options.
 *
 * @author m4gik <michal.szczygiel@wp.pl>, Aleksander Śmierciak
 */
public class BitcoinOptions {
    private List<Credential> credentials = new ArrayList<>();

    private Proxy proxy;

    private Integer worklifetime = 5000;

    private Boolean debug = false;

    private Boolean debugtimer = false;

    private Set<String> enabledDevices;

    private Double GPUTargetFPS = 30.0;

    private Double GPUTargetFPSBasis;

    private Integer GPUForceWorkSize = 0;

    private Integer GPUVectors[];

    private Boolean GPUNoArray = false;

    private Boolean GPUDebugSource = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BitcoinOptions that = (BitcoinOptions) o;

        if (GPUDebugSource != null ? !GPUDebugSource.equals(that.GPUDebugSource) : that.GPUDebugSource != null)
            return false;
        if (GPUForceWorkSize != null ? !GPUForceWorkSize.equals(that.GPUForceWorkSize) : that.GPUForceWorkSize != null)
            return false;
        if (GPUNoArray != null ? !GPUNoArray.equals(that.GPUNoArray) : that.GPUNoArray != null) return false;
        if (GPUTargetFPS != null ? !GPUTargetFPS.equals(that.GPUTargetFPS) : that.GPUTargetFPS != null) return false;
        if (GPUTargetFPSBasis != null ? !GPUTargetFPSBasis.equals(that.GPUTargetFPSBasis) : that.GPUTargetFPSBasis != null)
            return false;
        if (!Arrays.equals(GPUVectors, that.GPUVectors)) return false;
        if (credentials != null ? !credentials.equals(that.credentials) : that.credentials != null) return false;
        if (debug != null ? !debug.equals(that.debug) : that.debug != null) return false;
        if (debugtimer != null ? !debugtimer.equals(that.debugtimer) : that.debugtimer != null) return false;
        if (enabledDevices != null ? !enabledDevices.equals(that.enabledDevices) : that.enabledDevices != null)
            return false;
        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) return false;
        if (worklifetime != null ? !worklifetime.equals(that.worklifetime) : that.worklifetime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = credentials != null ? credentials.hashCode() : 0;
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        result = 31 * result + (worklifetime != null ? worklifetime.hashCode() : 0);
        result = 31 * result + (debug != null ? debug.hashCode() : 0);
        result = 31 * result + (debugtimer != null ? debugtimer.hashCode() : 0);
        result = 31 * result + (enabledDevices != null ? enabledDevices.hashCode() : 0);
        result = 31 * result + (GPUTargetFPS != null ? GPUTargetFPS.hashCode() : 0);
        result = 31 * result + (GPUTargetFPSBasis != null ? GPUTargetFPSBasis.hashCode() : 0);
        result = 31 * result + (GPUForceWorkSize != null ? GPUForceWorkSize.hashCode() : 0);
        result = 31 * result + (GPUVectors != null ? Arrays.hashCode(GPUVectors) : 0);
        result = 31 * result + (GPUNoArray != null ? GPUNoArray.hashCode() : 0);
        result = 31 * result + (GPUDebugSource != null ? GPUDebugSource.hashCode() : 0);
        return result;
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

    public Credential getCredential(int index) {
        return credentials.get(index);
    }

    public Collection<Credential> getCredentials() {
        return credentials;
    }

    public void addCredential(Credential credential) {
        credentials.add(credential);
    }

    public void removeCredential(Credential credential) {
        credentials.remove(credential);
    }
}
