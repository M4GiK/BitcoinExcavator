/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 19, 2014.
 */

package com.bitcoin.core.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Formatter;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.bitcoin.core.device.ExecutionState;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitcoin.core.Excavator;

/**
 * This class is responsible for JSON RPC network state.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class JSONRPCNetworkState extends NetworkState {

    /**
     * This class is responsible for get work asynchronously.
     *
     * @author m4gik <michal.szczygiel@wp.pl>
     */
    public class GetWorkAsync implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            while (getExcavator().getRunning()) {
                ExecutionState executionState = null;

                try {
                    executionState = getQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }

                if (executionState != null) {
                    WorkState workState = incomingQueue.poll();

                    if (workState == null) {
                        try {
                            workState = doGetWorkMessage(false);
                        } catch (IOException e) {
                            log.error("Cannot connect to "
                                    + getQueryUrl().getHost() + ": "
                                    + e.getLocalizedMessage());
                            getNetworkStateNext().addGetQueue(executionState);

                            if (!noDelay) {
                                try {
                                    Thread.sleep(250L);
                                } catch (InterruptedException e1) {
                                    continue;
                                }
                            }
                        }
                    }
                    workState.setExecutionState(executionState);
                    executionState.addIncomingQueue(workState);
                }
            }
        }
    }

    /**
     * This class is responsible for long poll asynchronously.
     *
     * @author m4gik <michal.szczygiel@wp.pl>
     */
    public class LongPollAsync implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            while (getExcavator().getRunning()) {
                try {
                    WorkState workState = doGetWorkMessage(true);
                    incomingQueue.add(workState);
                    refreshTimestamp.set(workState.getTimestamp());
                    log.debug(
                            (getQueryUrl().getHost() + ": Long poll returned"));
                } catch (IOException e) {
                    log.error("Cannot connect to " + getQueryUrl().getHost()
                            + ": " + e.getLocalizedMessage());
                }

                if (!noDelay) {
                    try {
                        Thread.sleep(250L);
                    } catch (InterruptedException e) {
                        log.error("Something goes wrong");
                    }
                }
            }
        }
    }

    /**
     * This class is responsible for send work asynchronously.
     *
     * @author m4gik <michal.szczygiel@wp.pl>
     */
    public class SendWorkAsync implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            while (getExcavator().getRunning()) {
                WorkState workState = null;

                try {
                    workState = sendQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }

                if (workState != null) {
                    Boolean accepted = false;

                    try {
                        accepted = doSendWorkMessage(workState);
                    } catch (IOException e) {
                        log.error("Cannot connect to "
                                + getQueryUrl().getHost() + ": "
                                + e.getLocalizedMessage());
                        sendQueue.addFirst(workState);
                    }

                    if (!noDelay) {
                        try {
                            Thread.sleep(250L);
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }

                    if (accepted) {
                        log.info(getQueryUrl().getHost()
                                + " accepted block "
                                + getExcavator().incrementBlocks()
                                + " from "
                                + workState.getExecutionState()
                                .getExecutionName());
                    } else {
                        log.info(getQueryUrl().getHost()
                                + " rejected block "
                                + getExcavator().incrementRejects()
                                + " from "
                                + workState.getExecutionState()
                                .getExecutionName());
                        log.debug("Rejected block "
                                + (float) (
                                (getExcavator().getCurrentTime() - workState
                                        .getTimestamp()) / 1000L)
                                + " seconds old, roll ntime set to "
                                + workState.getNetworkState().getRollNTime()
                                + ", rolled " + workState.getRolledNTime()
                                + " times");
                    }

                    if (rejectReason != null) {
                        log.info("Reject reason: " + rejectReason);
                        rejectReason = null;
                    }
                }
            }
        }
    }

    /**
     * Static variable represents fifteen seconds.
     */
    public static final Integer FIFTEEN_SECONDS = 15 * 1000;

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory
            .getLogger(JSONRPCNetworkState.class);

    /**
     * Static variable represents ten minutes.
     */
    public static final Integer TEN_MINUTES = 10 * 60 * 1000;

    private final GetWorkAsync getWorkAsync = this.new GetWorkAsync();

    private final SendWorkAsync sendWorkAsync = this.new SendWorkAsync();

    private final ObjectMapper mapper = new ObjectMapper();

    private LinkedBlockingDeque<WorkState> incomingQueue = new LinkedBlockingDeque<>();

    private LongPollAsync longPollAsync;

    private URL longPollUrl;

    private Boolean noDelay = false;

    private String rejectReason;

    private Boolean rollNTime = false;

    private String userPassword;

    /**
     * Constructor for {@link com.bitcoin.core.network.JSONRPCNetworkState}
     * class.
     *
     * @param excavator
     * @param queryUrl
     * @param user
     * @param password
     * @param hostChain
     */
    public JSONRPCNetworkState(Excavator excavator, URL queryUrl, String user,
                               String password, Byte hostChain) {
        super(excavator, queryUrl, hostChain, user, password);
        setUserPassword("Basic "
                + Base64.encodeBase64String((user + ":" + password).getBytes())
                .trim().replace("\r\n", ""));

        Thread thread = new Thread(getWorkAsync,
                "Excavator JSONRPC GetWorkAsync for " + queryUrl.getHost());
        thread.start();
        getExcavator().addThread(thread);

        thread = new Thread(sendWorkAsync,
                "Excavator JSONRPC SendWorkAsync for " + queryUrl.getHost());
        thread.start();
        getExcavator().addThread(thread);
    }

    /**
     * @param longPoll
     * @return
     * @throws IOException
     */
    private WorkState doGetWorkMessage(Boolean longPoll) throws IOException {
        ObjectNode workMessage = mapper.createObjectNode();

        workMessage.put("method", "getwork");
        workMessage.putArray("params");
        workMessage.put("id", 1);

        JsonNode responseMessage = doJSONRPCCall(longPoll, workMessage);

        String datas;
        String midstates;
        String targets;

        try {
            datas = responseMessage.get("data").asText();
            midstates = responseMessage.get("midstate").asText();
            targets = responseMessage.get("target").asText();
        } catch (Exception e) {
            throw new IOException("Bitcoin returned unparsable JSON");
        }

        WorkState workState = new WorkState(this);
        String parse;

        for (int i = 0; i < 32; i++) {
            parse = datas.substring(i * 8, (i * 8) + 8);
            workState.setData(i, Integer.reverseBytes((int) Long.parseLong(parse, 16)));
        }

        for (int i = 0; i < 8; i++) {
            parse = midstates.substring(i * 8, (i * 8) + 8);
            workState.setMidstate(i, Integer.reverseBytes((int) Long.parseLong(parse, 16)));
        }

        for (int i = 0; i < 8; i++) {
            parse = targets.substring(i * 8, (i * 8) + 8);
            workState.setTarget(i, (Long.reverseBytes(Long.parseLong(parse, 16) << 16)) >>> 16);
        }

        return workState;
    }

    public JsonNode doJSONRPCCall(Boolean longPoll, ObjectNode message)
            throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url;

            if (longPoll) {
                url = longPollUrl;
            } else {
                url = getQueryUrl();
            }

            Proxy proxy = getExcavator().getBitcoinOptions().getProxy();

            if (proxy == null) {
                connection = (HttpURLConnection) url.openConnection();
            } else {
                connection = (HttpURLConnection) url.openConnection(proxy);
            }

            if (longPoll) {
                connection.setConnectTimeout(TEN_MINUTES);
                connection.setReadTimeout(TEN_MINUTES);
            } else {
                connection.setConnectTimeout(FIFTEEN_SECONDS);
                connection.setReadTimeout(FIFTEEN_SECONDS);
            }

            connection.setRequestProperty("Authorization", userPassword);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("User-Agent", "DiabloMiner");
            connection.setRequestProperty("X-Mining-Extensions",
                    "longpoll rollntime switchto");
            connection.setDoOutput(true);

            OutputStream requestStream = connection.getOutputStream();
            Writer request = new OutputStreamWriter(requestStream);
            request.write(message.toString());
            request.close();
            requestStream.close();

            ObjectNode responseMessage = null;

            InputStream responseStream = null;

            try {
                String xLongPolling = connection
                        .getHeaderField("X-Long-Polling");

                if (xLongPolling != null && !"".equals(xLongPolling)
                        && longPollAsync == null) {
                    if (xLongPolling.startsWith("http")) {
                        longPollUrl = new URL(xLongPolling);
                    } else if (xLongPolling.startsWith("/")) {
                        longPollUrl = new URL(getQueryUrl().getProtocol(),
                                getQueryUrl().getHost(), getQueryUrl()
                                .getPort(), xLongPolling);
                    } else {
                        longPollUrl = new URL(getQueryUrl().getProtocol(),
                                getQueryUrl().getHost(), getQueryUrl()
                                .getPort(),
                                (url.getFile() + "/" + xLongPolling).replace(
                                        "//", "/"));
                    }

                    longPollAsync = new LongPollAsync();
                    Thread thread = new Thread(longPollAsync,
                            "Excavator JSONRPC LongPollAsync for "
                                    + url.getHost());
                    thread.start();
                    getExcavator().addThread(thread);

                    setWorkLifetime(60000L);

                    log.debug(getQueryUrl().getHost()
                            + ": Enabling long poll support");
                }

                String xRollNTime = connection.getHeaderField("X-Roll-NTime");

                if (xRollNTime != null && !"".equals(xRollNTime)) {
                    if (!"n".equalsIgnoreCase(xRollNTime)
                            && rollNTime == false) {
                        rollNTime = true;

                        if (xRollNTime.startsWith("expire=")) {
                            try {
                                setWorkLifetime(Integer.parseInt(xRollNTime
                                        .substring(7)) * 1000L);
                            } catch (NumberFormatException ex) {
                            }
                        } else {
                            setWorkLifetime(60000L);
                        }

                        log.debug(getQueryUrl().getHost()
                                + ": Enabling roll ntime support, expire after "
                                + (getWorkLifetime() / 1000) + " seconds");
                    } else if ("n".equalsIgnoreCase(xRollNTime)
                            && rollNTime == true) {
                        rollNTime = false;

                        if (longPoll) {
                            setWorkLifetime(60000L);
                        } else {
                            setWorkLifetime(Long.valueOf(
                                    getExcavator().getBitcoinOptions()
                                            .getWorklifetime()));
                        }

                        log.debug(getQueryUrl().getHost()
                                + ": Disabling roll ntime support");
                    }
                }

                String xSwitchTo = connection.getHeaderField("X-Switch-To");

                if (xSwitchTo != null && !"".equals(xSwitchTo)) {
                    String oldHost = getQueryUrl().getHost();
                    JsonNode newHost = mapper.readTree(xSwitchTo);

                    setQueryUrl(new URL(getQueryUrl().getProtocol(), newHost
                            .get("host").asText(), newHost.get("port")
                            .getIntValue(), getQueryUrl().getPath()));

                    if (longPollUrl != null) {
                        longPollUrl = new URL(longPollUrl.getProtocol(),
                                newHost.get("host").asText(), newHost.get(
                                "port").getIntValue(),
                                longPollUrl.getPath());
                    }

                    log.info(oldHost + ": Switched to "
                            + getQueryUrl().getHost());
                }

                String xRejectReason = connection
                        .getHeaderField("X-Reject-Reason");

                if (xRejectReason != null && !"".equals(xRejectReason)) {
                    rejectReason = xRejectReason;
                }

                String xIsP2Pool = connection.getHeaderField("X-Is-P2Pool");

                if (xIsP2Pool != null && !"".equals(xIsP2Pool)) {
                    if (!noDelay) {
                        log.info("P2Pool no delay mode enabled");
                    }

                    noDelay = true;
                }

                if (connection.getContentEncoding() != null) {
                    if (connection.getContentEncoding()
                            .equalsIgnoreCase("gzip")) {
                        responseStream = new GZIPInputStream(
                                connection.getInputStream());
                    } else if (connection.getContentEncoding()
                            .equalsIgnoreCase("deflate")) {
                        responseStream = new InflaterInputStream(
                                connection.getInputStream());
                    }
                } else {
                    responseStream = connection.getInputStream();
                }

                if (responseStream == null) {
                    throw new IOException("Drop to error handler");
                }

                Object output = mapper.readTree(responseStream);

                if (NullNode.class.equals(output.getClass())) {
                    throw new IOException("Bitcoin returned unparsable JSON");
                } else {
                    try {
                        responseMessage = (ObjectNode) output;
                    } catch (ClassCastException e) {
                        throw new IOException(
                                "Bitcoin returned unparsable JSON");
                    }
                }

                responseStream.close();
            } catch (JsonProcessingException e) {
                throw new IOException("Bitcoin returned unparsable JSON");
            } catch (IOException e) {
                InputStream errorStream = null;
                IOException e2 = null;

                if (connection.getErrorStream() == null) {
                    throw new IOException(
                            "Bitcoin disconnected during response: "
                                    + connection.getResponseCode() + " "
                                    + connection.getResponseMessage());
                }

                if (connection.getContentEncoding() != null) {
                    if (connection.getContentEncoding()
                            .equalsIgnoreCase("gzip")) {
                        errorStream = new GZIPInputStream(
                                connection.getErrorStream());
                    } else if (connection.getContentEncoding()
                            .equalsIgnoreCase("deflate")) {
                        errorStream = new InflaterInputStream(
                                connection.getErrorStream());
                    }
                } else {
                    errorStream = connection.getErrorStream();
                }

                if (errorStream == null) {
                    throw new IOException(
                            "Bitcoin disconnected during response: "
                                    + connection.getResponseCode() + " "
                                    + connection.getResponseMessage());
                }

                byte[] errorbuf = new byte[8192];

                if (errorStream.read(errorbuf) < 1) {
                    throw new IOException(
                            "Bitcoin returned an error, but with no message");
                }

                String error = new String(errorbuf).trim();

                if (error.startsWith("{")) {
                    try {
                        Object output = mapper.readTree(error);

                        if (NullNode.class.equals(output.getClass())) {
                            throw new IOException(
                                    "Bitcoin returned an error message: "
                                            + error);
                        } else {
                            try {
                                responseMessage = (ObjectNode) output;
                            } catch (ClassCastException f) {
                                throw new IOException(
                                        "Bitcoin returned unparsable JSON");
                            }
                        }

                        if (responseMessage.get("error") != null) {
                            if (responseMessage.get("error").get("message")
                                    != null
                                    && responseMessage.get("error")
                                    .get("message").asText() != null) {
                                error = responseMessage.get("error")
                                        .get("message").asText().trim();
                                e2 = new IOException(
                                        "Bitcoin returned error message: "
                                                + error);
                            } else if (responseMessage.get("error").asText()
                                    != null) {
                                error = responseMessage.get("error").asText()
                                        .trim();

                                if (!"null".equals(error) && !""
                                        .equals(error)) {
                                    e2 = new IOException(
                                            "Bitcoin returned an error message: "
                                                    + error);
                                }
                            }
                        }
                    } catch (JsonProcessingException f) {
                        e2 = new IOException(
                                "Bitcoin returned unparsable JSON");
                    }
                } else {
                    e2 = new IOException("Bitcoin returned an error message: "
                            + error);
                }

                errorStream.close();

                if (responseStream != null) {
                    responseStream.close();
                }

                if (e2 == null) {
                    e2 = new IOException(
                            "Bitcoin returned an error, but with no message");
                }

                throw e2;
            }

            if (responseMessage.get("error") != null) {
                if (responseMessage.get("error").get("message") != null
                        && responseMessage.get("error").get("message").asText()
                        != null) {
                    String error = responseMessage.get("error").get("message")
                            .asText().trim();
                    throw new IOException("Bitcoin returned error message: "
                            + error);
                } else if (responseMessage.get("error").asText() != null) {
                    String error = responseMessage.get("error").asText().trim();

                    if (!"null".equals(error) && !"".equals(error)) {
                        throw new IOException(
                                "Bitcoin returned error message: " + error);
                    }
                }
            }

            JsonNode result;

            try {
                result = responseMessage.get("result");
            } catch (Exception e) {
                throw new IOException("Bitcoin returned unparsable JSON");
            }

            if (result == null) {
                throw new IOException(
                        "Bitcoin did not return a result or an error");
            }

            return result;
        } catch (IOException e) {
            if (connection != null) {
                connection.disconnect();
            }
            throw e;
        }
    }

    Boolean doSendWorkMessage(WorkState workState) throws IOException {
        StringBuilder dataOutput = new StringBuilder(8 * 32 + 1);
        Formatter dataFormatter = new Formatter(dataOutput);
        Integer[] data = workState.getData();

        dataFormatter
                .format("%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x"
                                + "%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x%08x",
                        Integer.reverseBytes(data[0]),
                        Integer.reverseBytes(data[1]),
                        Integer.reverseBytes(data[2]),
                        Integer.reverseBytes(data[3]),
                        Integer.reverseBytes(data[4]),
                        Integer.reverseBytes(data[5]),
                        Integer.reverseBytes(data[6]),
                        Integer.reverseBytes(data[7]),
                        Integer.reverseBytes(data[8]),
                        Integer.reverseBytes(data[9]),
                        Integer.reverseBytes(data[10]),
                        Integer.reverseBytes(data[11]),
                        Integer.reverseBytes(data[12]),
                        Integer.reverseBytes(data[13]),
                        Integer.reverseBytes(data[14]),
                        Integer.reverseBytes(data[15]),
                        Integer.reverseBytes(data[16]),
                        Integer.reverseBytes(data[17]),
                        Integer.reverseBytes(data[18]),
                        Integer.reverseBytes(data[19]),
                        Integer.reverseBytes(data[20]),
                        Integer.reverseBytes(data[21]),
                        Integer.reverseBytes(data[22]),
                        Integer.reverseBytes(data[23]),
                        Integer.reverseBytes(data[24]),
                        Integer.reverseBytes(data[25]),
                        Integer.reverseBytes(data[26]),
                        Integer.reverseBytes(data[27]),
                        Integer.reverseBytes(data[28]),
                        Integer.reverseBytes(data[29]),
                        Integer.reverseBytes(data[30]),
                        Integer.reverseBytes(data[31]));

        ObjectNode sendWorkMessage = mapper.createObjectNode();
        sendWorkMessage.put("method", "getwork");
        ArrayNode params = sendWorkMessage.putArray("params");
        params.add(dataOutput.toString());
        sendWorkMessage.put("id", 1);
        JsonNode responseMessage = doJSONRPCCall(false, sendWorkMessage);
        dataFormatter.close();

        Boolean accepted = false;

        try {
            accepted = responseMessage.getBooleanValue();
        } catch (Exception e) {
            throw new IOException("Bitcoin returned unparsable JSON");
        }

        return accepted;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
