/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 3, 2014.
 */
package com.bitcoin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for validateNetworkParameters connection options.
 *
 * @author m4gik <michal.szczygiel@wp.pl>, Aleksander Śmierciak
 */
public class BitcoinValidator {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitcoinOptions.class);

    /**
     * Valids the options for networks parameters for {@link BitcoinOptions} class. If validation fails
     * throws exception {@link IllegalArgumentException}
     *
     * @param bitcoinOptions The instance with options.
     * @return The instance of {@link BitcoinOptions} class with validated options.
     */
    public static BitcoinOptions validateNetworkParameters(BitcoinOptions bitcoinOptions) {
    if (!((bitcoinOptions.getUser() == null &&
            bitcoinOptions.getPassword() == null &&
            bitcoinOptions.getHost() == null &&
            bitcoinOptions.getPort() == null) || bitcoinOptions.getUrl() == null)) {
            log.error(
                    "You forgot to give any bitcoin connection info," +
                            " please add either -l, or -u -p -o and -r");
            throw new IllegalArgumentException("Connection parameters were not specified");
        }

        return bitcoinOptions;
    }
}
