/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */
package com.bitcoin.core;

import com.bitcoin.util.BitcoinOptions;

/**
 * Interface for excavating coins.
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public interface Excavator {

    /**
     * Adds and gets hash count.
     * 
     * @param delta
     *            The value to add.
     * @return actual hash count after add operation.
     */
    Long addAndGetHashCount(Long delta);

    /**
     * This method adds thread on running process.
     * 
     * @param thread
     *            The thread to add.
     */
    void addThread(Thread thread);

    /**
     * Display error message what happen and interrupt the action.
     * 
     * @param reason
     *            Message with error.
     * @return Formatted error to display.
     */
    String error(String reason);

    /**
     * This method runs process for dig coins.
     * 
     * @param bitcoinOptions The options to run execute process.
     */
    void execute(BitcoinOptions bitcoinOptions);

    /**
     * Method stops digging, and close all running process.
     */
    void halt();

    /**
     * Increments attempts.
     * 
     * @return the amount of attempts.
     */
    Long incrementAttempts();

    /**
     * Increments blocks amount.
     * 
     * @return the amount of blocks
     */
    Long incrementBlocks();

    /**
     * Increments HW errors.
     * 
     * @return the amount of HW errors.
     */
    Long incrementHWErrors();

    /**
     * Increments rejects amount.
     * 
     * @return the amount of rejects.
     */
    Long incrementRejects();

    /**
     * Gets information about current process.
     * 
     * @param information
     */
    void info(String information);
}
