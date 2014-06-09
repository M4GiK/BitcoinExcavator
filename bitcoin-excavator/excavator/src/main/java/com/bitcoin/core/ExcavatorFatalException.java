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
 * 
 */
public class ExcavatorFatalException extends Exception {

    /**
     * Auto generated serial version UID.
     */
    private static final long serialVersionUID = 2838224304757516060L;

    /**
     * Constructor for {@link ExcavatorFatalException} class.
     * 
     * @param excavator
     *            Instance of {@link Excavator} class.
     * @param reason
     *            Message with error.
     */
    public ExcavatorFatalException(Excavator excavator, String reason) {
        super(reason);
        excavator.error(reason);
        excavator.halt();
    }
}
