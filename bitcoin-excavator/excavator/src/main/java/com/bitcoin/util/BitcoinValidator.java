/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 3, 2014.
 */
package com.bitcoin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Class responsible for validateNetworkParameters connection options.
 *
 * @author m4gik <michal.szczygiel@wp.pl>, Aleksander Śmierciak
 */
public class BitCoinValidator {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitCoinOptions.class);

    /**
     * Valids the options for networks parameters for {@link BitCoinOptions} class. If validation fails
     * throws exception {@link IllegalArgumentException}
     *
     * @param bitCoinOptions The instance with options.
     * @return The instance of {@link BitCoinOptions} class with validated options.
     */
    public static BitCoinOptions validateNetworkParameters(BitCoinOptions bitCoinOptions) {
        if (bitCoinOptions == null) {
            throw new IllegalArgumentException("Bitcoin options cannot be null");
        }
        Collection<Credential> credentials = bitCoinOptions.getCredentials();
        if (credentials == null) {
            throw new IllegalArgumentException("Credential collection cannot be null");
        }
        if (credentials.size() == 0) {
            log.error(
                    "You forgot to give any bitcoin connection info," +
                            " please add either -l, or -u -p -o and -r");
            throw new IllegalArgumentException("Connection parameters were not specified");
        }

        return bitCoinOptions;
    }
}
