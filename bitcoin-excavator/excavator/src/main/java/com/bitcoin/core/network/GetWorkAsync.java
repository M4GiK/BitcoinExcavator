/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */
package com.bitcoin.core.network;

import com.bitcoin.core.Excavator;

/**
 * This class is responsible for get work asynchronously.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GetWorkAsync implements Runnable{

    private Excavator excavator;

    /**
     * The constructor for {@link com.bitcoin.core.network.GetWorkAsync} class.
     *
     * @param excavator The instance of {@link com.bitcoin.core.Excavator}
     */
    public GetWorkAsync(Excavator excavator) {
        this.excavator = excavator;
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
    @Override public void run() {

    }
}
