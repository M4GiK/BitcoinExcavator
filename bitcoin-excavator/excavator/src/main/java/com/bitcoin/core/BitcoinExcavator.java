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
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitcoin.util.BitcoinOptions;

/**
 * Main core class for dig bitcoins. Responsible for start devices to found
 * collision for bitcoins hash.
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class BitcoinExcavator implements Excavator {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory
            .getLogger(BitcoinExcavator.class);

    /**
     * Returns actual time.
     * 
     * @return the actual time.
     */
    public static String dateTime() {
        return "["
                + DateFormat.getDateTimeInstance(DateFormat.SHORT,
                        DateFormat.MEDIUM).format(new Date()) + "]";
    }

    AtomicLong attempts = new AtomicLong(0);

    AtomicLong blocks = new AtomicLong(0);

    Set<String> enabledDevices = null;

    AtomicLong hashCount = new AtomicLong(0);

    AtomicLong hwErrors = new AtomicLong(0);

    AtomicLong rejects = new AtomicLong(0);

    /**
     * List of threads.
     */
    List<Thread> threads = new ArrayList<Thread>();

    public Long addAndGetHashCount(Long delta) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addThread(Thread thread) {
        // TODO Auto-generated method stub

    }

    /**
     * Display error message what happen and interrupt the action.
     * 
     * @param reason
     *            Message with error.
     * @return Formatted error to display.
     */
    public String error(String reason) {
        log.error(reason);
        threads.get(0).interrupt();

        return dateTime() + " error: " + reason;
    }

    public void execute(BitcoinOptions bitcoinOptions) {
        // TODO Auto-generated method stub

    }

    public void halt() {
        // TODO Auto-generated method stub

    }

    public Long incrementAttempts() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long incrementBlocks() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long incrementHWErrors() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long incrementRejects() {
        // TODO Auto-generated method stub
        return null;
    }

    public void info(String information) {
        // TODO Auto-generated method stub

    }
}
