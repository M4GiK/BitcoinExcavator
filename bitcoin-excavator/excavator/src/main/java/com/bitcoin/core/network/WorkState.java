/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */
package com.bitcoin.core.network;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.device.ExecutionState;

/**
 * This class represents state for working connections.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class WorkState {

    private final Integer[] data = new Integer[32];

    private final Integer[] midstate = new Integer[8];

    private final Long[] target = new Long[8];

    private Long timestamp;

    private Long base;

    private Boolean rollNTimeEnable;

    private Integer rolledNTime;

    private Excavator excavator;

    private NetworkState networkState;

    private ExecutionState executionState;

    /**
     * The constructor for {@link com.bitcoin.core.network.WorkState} class.
     *
     * @param networkState The instance of {@link com.bitcoin.core.network.NetworkState}.
     */
    public WorkState(NetworkState networkState) {
        setNetworkState(networkState);
        setExcavator(networkState.getExcavator());
        setTimestamp(networkState.getExcavator().getCurrentTime());
        setBase(0L);
        setRolledNTime(0);
    }


    public Integer[] getData() {
        return data;
    }

    public Integer[] getMidstate() {
        return midstate;
    }

    public Long[] getTarget() {
        return target;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getBase() {
        return base;
    }

    public void setBase(Long base) {
        this.base = base;
    }

    public Boolean getRollNTimeEnable() {
        return rollNTimeEnable;
    }

    public void setRollNTimeEnable(Boolean rollNTimeEnable) {
        this.rollNTimeEnable = rollNTimeEnable;
    }

    public Integer getRolledNTime() {
        return rolledNTime;
    }

    public void setRolledNTime(Integer rolledNTime) {
        this.rolledNTime = rolledNTime;
    }

    public Excavator getExcavator() {
        return excavator;
    }

    public void setExcavator(Excavator excavator) {
        this.excavator = excavator;
    }

    public NetworkState getNetworkState() {
        return networkState;
    }

    public void setNetworkState(NetworkState networkState) {
        this.networkState = networkState;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }
}
