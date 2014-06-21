/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */
package com.bitcoin.core;

import com.bitcoin.util.BitcoinOptions;

import java.net.Proxy;

/**
 * Interface for excavating coins.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public interface Excavator {

    /**
     * The value represents two 32 bits.
     */
    public final static Long TWO32 = 4294967295L;

    /**
     * Time offset for Excavator.
     */
    public final static Long TIME_OFFSET = 7500L;

    /**
     * Adds and gets hash count.
     *
     * @param delta The value to add.
     * @return actual hash count after add operation.
     */
    Long addAndGetHashCount(Long delta);

    /**
     * This method adds thread on running process.
     *
     * @param thread The thread to add.
     */
    void addThread(Thread thread);

    /**
     * Display error message what happen and interrupt the action.
     *
     * @param reason Message with error.
     * @return Formatted error to display.
     */
    String error(String reason);

    /**
     * This method runs process for dig coins.
     */
    void execute();

    /**
     * Gets current time from system.
     *
     * @return The time in millisecnonds.
     */
    Long getCurrentTime();

    /**
     * Gets options for {@link com.bitcoin.core.Excavator} class.
     *
     * @return The instance of {@link com.bitcoin.util.BitcoinOptions}.
     */
    BitcoinOptions getBitcoinOptions();

    /**
     * Gets information about running status.
     *
     * @return True if process is running, false if is not.
     */
    Boolean getRunning();

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
