/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */
package com.bitcoin.core;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
public class BitcoinExcavator {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitcoinExcavator.class);

    /**
     * List of threads.
     */
    List<Thread> threads = new ArrayList<Thread>();

    /**
     * Returns actual time.
     *
     * @return the actual time.
     */
    public static String dateTime() {
        return "[" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date()) + "]";
    }

    /**
     * Display error message what happen and interrupt the action.
     *
     * @param reason
     */
    public String error(String reason) {
        log.error(reason);
        threads.get(0).interrupt();

        return dateTime() + " error: " + reason;
    }


    /**
     *
     */
    public void halt() {
        // TODO Auto-generated method stub

    }
}
