/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */

package com.bitcoin.core.device;

import com.bitcoin.core.network.WorkState;

/**
 * This class is responsible for getting information about execution state on device.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public abstract class ExecutionState {

    private String executionName;

    private WorkState workState;

    /**
     * The constructor for {@link com.bitcoin.core.device.ExecutionState} class.
     *
     * @param executionName The name for execution process.
     */
    public ExecutionState(String executionName) {
        setExecutionName(executionName);
        setWorkState(null);
    }

    public String getExecutionName() {
        return executionName;
    }

    public void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    public WorkState getWorkState() {
        return workState;
    }

    public void setWorkState(WorkState workState) {
        this.workState = workState;
    }
}
