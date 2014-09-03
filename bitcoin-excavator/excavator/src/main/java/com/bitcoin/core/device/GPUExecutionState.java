/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 3, 2014.
 */
package com.bitcoin.core.device;

/**
 * This class represents GPU execution state.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GPUExecutionState extends ExecutionState {

    /**
     * Constructor for {@link com.bitcoin.core.device.GPUExecutionState}.
     *
     * @param executorName The name of executor.
     */
    public GPUExecutionState(String executorName) {
        super(executorName);
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
    @Override
    public void run() {

    }
}
