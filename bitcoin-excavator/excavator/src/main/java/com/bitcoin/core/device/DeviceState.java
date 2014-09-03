/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Device abstract class.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public abstract class DeviceState {

    private AtomicLong deviceHashCount = new AtomicLong(0);
    private String deviceName;
    private Double basis;
    private Long resetNetworkState;
    private ExecutionState executionState;

    public abstract void checkDevice();

    public Long getLongDeviceHashCount() {
        return deviceHashCount.get();
    }

    public AtomicLong getDeviceHashCount() {
        return deviceHashCount;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Double getBasis() {
        return basis;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public Long getResetNetworkState() {
        return resetNetworkState;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }

    public void setResetNetworkState(Long resetNetworkState) {
        this.resetNetworkState = resetNetworkState;
    }

    public void setBasis(Double basis) {
        this.basis = basis;
    }
}
