/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */
package com.bitcoin.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main core class for dig bitcoins. Responsible for start devices to found
 * collision for bitcoins hash.
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class Excavator {
    
    
    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(Excavator.class);

    List<Thread> threads = new ArrayList<Thread>();

    /**
     * Display error message what happen and interrupt the action.
     * 
     * @param reason
     */
    public void error(String reason) {
        log.error(reason);
        threads.get(0).interrupt();
    }


    /**
     * 
     */
    public void halt() {
        // TODO Auto-generated method stub

    }
}
