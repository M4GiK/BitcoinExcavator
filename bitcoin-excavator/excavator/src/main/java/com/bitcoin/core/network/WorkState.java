/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */
package com.bitcoin.core.network;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.device.ExecutionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents state for working connections.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class WorkState {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory
            .getLogger(WorkState.class);

    private final Integer[] data = new Integer[32];

    private final int[] midstate = new int[8];

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
        setTimestamp(getExcavator().getCurrentTime());
        setBase(0L);
        setRolledNTime(0);
    }

    /**
     * This method updates status.
     *
     * @param delta The additional value added to base.
     * @return True status is updated, false if is not.
     */
    public Boolean update(Long delta) {
        Boolean isWorking = false;

        if ((getExcavator().getCurrentTime() - getTimestamp()) + 1000L
                >= getNetworkState().getWorkLifetime()) {
            log.debug(getExecutionState().getExecutionName()
                    + ": Refresh work: work expired");
            isWorking = true;
        } else if (getNetworkState().getRefreshTimestamp() > getTimestamp()) {
            log.debug(getExecutionState().getExecutionName()
                    + ": Refresh work: longpoll");
            isWorking = true;
        } else if (getBase() + delta > Excavator.TWO32) {
            if (getNetworkState().getRollNTime()) {
                log.debug(getExecutionState().getExecutionName()
                        + ": Rolled NTime");
                setBase(0L);
                getData()[17] = Integer
                        .reverseBytes(Integer.reverseBytes(getData()[17]) + 1);
                setRolledNTime(getRolledNTime() + 1);
                isWorking = false;
            } else {
                log.debug(getExecutionState().getExecutionName()
                        + ": Refresh work: range expired");
                isWorking = true;
            }
        } else {
            setBase(getBase() + delta);
            isWorking = false;
        }

        if (isWorking) {
            getNetworkState().addGetQueue(getExecutionState());
        }

        return isWorking;
    }

    /**
     * Submits nonce value.
     *
     * @param nonce The nonce value.
     */
    public void submitNonce(int nonce) {
        data[19] = nonce;
        getNetworkState().addSendQueue(this);
    }


    public Integer[] getData() {
        return data;
    }

    public int getData(int n) {
        return data[n];
    }

    public void setData(Integer index, Integer value) {
        data[index] = value;
    }

    public int[] getMidstate() {
        return midstate;
    }

    public int getMidstate(int n) {
        return midstate[n];
    }

    public void setMidstate(Integer index, Integer value) {
        midstate[index] = value;
    }

    public Long[] getTarget() {
        return target;
    }

    public long getTarget(int n) {
        return target[n];
    }

    public void setTarget(Integer index, Long value) {
        target[index] = value;
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
