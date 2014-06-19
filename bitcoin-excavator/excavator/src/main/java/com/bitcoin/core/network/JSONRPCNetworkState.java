/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */

package com.bitcoin.core.network;

import java.net.URL;

/**
 * This class is responsible for JSON RPC network state.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class JSONRPCNetworkState extends NetworkState {

    private GetWorkAsync getWorkAsync;

    private SendWorkAsync sendWorkAsync;

    private LongPollAsync longPollAsync;

    private URL longPollUrl;

    private String userPassword;

    private Boolean rollNTime;

    private Boolean noDelay;

    private String rejectReason;
}
