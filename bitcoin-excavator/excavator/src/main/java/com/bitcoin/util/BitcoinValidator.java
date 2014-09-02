package com.bitcoin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoinValidator {
    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(BitcoinOptions.class);

    /**
     * Valids the options for {@link BitcoinOptions} class. If validation fails
     * throws exception {@link IllegalArgumentException}
     *
     * @param bitcoinOptions The instance with options.
     * @return The instance of {@link BitcoinOptions} class with validated options.
     */
    public BitcoinOptions validate(BitcoinOptions bitcoinOptions) {
        if (bitcoinOptions.getUser() == null ||
            bitcoinOptions.getPassword() == null ||
            bitcoinOptions.getHost() == null ||
            bitcoinOptions.getPort() == null) {
            log.error(
                    "You forgot to give any bitcoin connection info," +
                            " please add either -l, or -u -p -o and -r");
            throw new IllegalArgumentException("Connection parameters were not specified");
        }

        Integer networkOptions = 0;

        if (bitcoinOptions.getUrl() != null) {
            networkOptions = bitcoinOptions.getUrl().length;
        } else {
            networkOptions = Math
                    .max(bitcoinOptions.getUser().length, networkOptions);
            networkOptions = Math.max(bitcoinOptions.getPassword().length,
                    networkOptions);
            networkOptions = Math
                    .max(bitcoinOptions.getHost().length, networkOptions);
            networkOptions = Math
                    .max(bitcoinOptions.getPort().length, networkOptions);
        }

        bitcoinOptions.setNetworkStatesAmount(networkOptions);

        return bitcoinOptions;
    }
}
