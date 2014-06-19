/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */

package com.bitcoin.core.device;

import com.bitcoin.core.network.WorkState;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * This class is responsible for getting information about execution state on device.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public abstract class ExecutionState implements Runnable {

    private String executionName;

    private WorkState workState;

    private LinkedBlockingDeque<WorkState> incomingQueue = new LinkedBlockingDeque<WorkState>();

    /**
     * The constructor for {@link com.bitcoin.core.device.ExecutionState} class.
     *
     * @param executionName The name for execution process.
     */
    public ExecutionState(String executionName) {
        setExecutionName(executionName);
        setWorkState(null);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public abstract void run();

    /**
     * Adds instance of {@link com.bitcoin.core.network.WorkState} to queue.
     *
     * @param workState The instance of {@link com.bitcoin.core.network.WorkState} class.
     */
    public void addIncomingQueue(WorkState workState) {
        incomingQueue.add(workState);
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
