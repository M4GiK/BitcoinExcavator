/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */
package com.bitcoin.core;

/**
 * Class for gathering exception information, based on {@link Exception} class.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class BitCoinExcavatorFatalException extends Exception {

    /**
     * Auto generated serial version UID.
     */
    private static final long serialVersionUID = 2838224304757516060L;

    /**
     * Constructor for {@link BitCoinExcavatorFatalException} class.
     *
     * @param bitCoinExcavator Instance of {@link BitCoinExcavator} class.
     * @param reason           Message with error.
     */
    public BitCoinExcavatorFatalException(BitCoinExcavator bitCoinExcavator, String reason) {
        super(reason);
        bitCoinExcavator.error(reason);
        bitCoinExcavator.stop();
    }
}
