/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at June 9, 2014.
 */
package com.bitcoin.core;

import com.bitcoin.core.device.DeviceState;
import com.bitcoin.core.device.GPUHardwareType;
import com.bitcoin.core.network.NetworkState;
import com.bitcoin.core.network.NetworkStateBuilder;
import com.bitcoin.util.BitcoinOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Main core class for dig bitcoins. Responsible for start devices to found
 * collision for bitcoins hash.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
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

    private AtomicLong attempts = new AtomicLong(0);

    private AtomicLong blocks = new AtomicLong(0);

    private Set<String> enabledDevices = null;

    private AtomicLong hashCount = new AtomicLong(0);

    private AtomicLong hwErrors = new AtomicLong(0);

    private AtomicLong rejects = new AtomicLong(0);

    private AtomicBoolean running = new AtomicBoolean(true);

    private BitcoinOptions bitcoinOptions = null;

    private ArrayList<NetworkState> networkStates = null;

    private NetworkState networkStateHead = null;

    private NetworkState networkStateTail = null;

    private Long startTime;

    public BitcoinExcavator(BitcoinOptions bitcoinOptions) throws BitcoinExcavatorFatalException {
        if (bitcoinOptions == null) {
            throw new BitcoinExcavatorFatalException(this, "Bitcoin options can not be null");
        }

        this.bitcoinOptions = bitcoinOptions;
    }

    /**
     * List of threads.
     */
    List<Thread> threads = new ArrayList<>();

    /**
     * This method add delta and return hash count.
     *
     * @param delta The value to add.
     * @return actual hash count after add operation.
     */
    public Long addAndGetHashCount(Long delta) {
        return hashCount.addAndGet(delta);
    }

    /**
     * This method adds thread on running process.
     *
     * @param thread The thread to add.
     */
    public void addThread(Thread thread) {
        threads.add(thread);
    }

    /**
     * Display error message what happen and interrupt the action.
     *
     * @param reason Message with error.
     * @return Formatted error to display.
     */
    public String error(String reason) {
        log.error(reason);
        threads.get(0).interrupt();

        return dateTime() + " error: " + reason;
    }

    /**
     * This method runs process for dig coins.
     */
    public void execute() {
        log.info("Bitcoin Excavator process started");
        threads.add(Thread.currentThread());
        networkStates = NetworkStateBuilder.networkConfiguration(bitcoinOptions, this);


        StringBuilder list = new StringBuilder();

        for (int i = 0; i < networkStates.size(); i++) {
            log.info("user: " + networkStates.get(i).getUser() + " url: "
                    + networkStates.get(i)
                    .getQueryUrl());

            list.append(networkStates.get(i).getQueryUrl().toString());

            if(networkStateHead == null) {
                networkStateHead = networkStateTail = networkStates.get(i);
            } else {
                networkStateTail.setNetworkStateNext(networkStates.get(i));
                networkStateTail = networkStates.get(i);
            }

            if (i >= 1 && i < networkStates.size() - 1) {
                list.append(", ");
            }
        }

        networkStateTail.setNetworkStateNext(networkStateHead);

        log.info("Connecting to: " + list);

        try {
            startGPU();
        } catch (ExcavatorFatalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Methods starts operations on GPU.
     */
    private void startGPU() throws ExcavatorFatalException {
        Long previousHashCount = 0L;
        Long previousAdjustedStartTime = this.startTime = (getCurrentTime() - 1);
        Double previousAdjustedHashCount = 0.0;

        StringBuilder hashMeter = new StringBuilder();
        Formatter hashMeterFormatter = new Formatter(hashMeter);
        Integer deviceCount = 0;

        List<List<? extends DeviceState>> allDeviceStates = new ArrayList<List<? extends  DeviceState>>();
        List<? extends  DeviceState> GPUDeviceStates = new GPUHardwareType(this).getDeviceStates();
        deviceCount += GPUDeviceStates.size();
        allDeviceStates.add(GPUDeviceStates);

        while(running.get()) {
            for(List<? extends  DeviceState> deviceStates : allDeviceStates) {
                for(DeviceState deviceState : deviceStates) {
                    deviceState.checkDevice();
                }
            }

            Long now = getCurrentTime();
            Long currentHashCount = hashCount.get();
            Double adjustedHashCount = (double) (currentHashCount - previousHashCount)
                    / (double) (now - previousAdjustedStartTime);
            Double hashLongCount = (double) currentHashCount / (double) (now - startTime) / 1000.0;

            if(now - startTime > TIME_OFFSET * 2) {
                double averageHashCount = (adjustedHashCount + previousAdjustedHashCount) / 2.0 / 1000.0;

                hashMeter.setLength(0);

                if(!bitcoinOptions.getDebug()) {
                    hashMeterFormatter.format("\rmhash: %.1f/%.1f | accept: %d | reject: %d | hw error: %d",
                            averageHashCount, hashLongCount, blocks.get(), rejects.get(), hwErrors.get());
                } else {
                    hashMeterFormatter.format("\rmh: %.1f/%.1f | a/r/hwe: %d/%d/%d | gh: ", averageHashCount,
                            hashLongCount, blocks.get(), rejects.get(), hwErrors.get());
                    Double basisAverage = 0.0;

                    for(List<? extends DeviceState> deviceStates : allDeviceStates) {
                        for(DeviceState deviceState : deviceStates) {
                            hashMeterFormatter.format("%.1f ", deviceState.getLongDeviceHashCount()
                                    / 1000.0 / 1000.0 / 1000.0);
                            basisAverage += deviceState.getBasis();
                        }
                    }

                    basisAverage = 1000 / (basisAverage / deviceCount);

                    hashMeterFormatter.format("| fps: %.1f", basisAverage);
                }

                System.out.print(hashMeter);
                //log.info(String.valueOf(hashMeter));
            } else {
                System.out.print("\rWaiting...");
                //log.info("\rWaiting...");
            }

            if(getCurrentTime() - TIME_OFFSET * 2 > previousAdjustedStartTime) {
                previousHashCount = currentHashCount;
                previousAdjustedHashCount = adjustedHashCount;
                previousAdjustedStartTime = now - 1;
            }

            if(bitcoinOptions.getDebugtimer() && getCurrentTime() > startTime + 60 * 1000) {
                info("Debug timer is up, quitting...");
                System.exit(0);
            }

            try {
                if (now - startTime > TIME_OFFSET) {
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {

            }
        }
        hashMeterFormatter.close();
    }

    /**
     * This is an implementation of the ROT algorithm.
     * @param x
     * @param y
     * @return The rot value.
     */
    public static int rot(int x, int y) {
        return (x >>> y) | (x << (32 - y));
    }

    public static void sharound(int out[], int na, int nb, int nc, int nd, int ne, int nf, int ng, int nh, int x, int K) {
        int a = out[na];
        int b = out[nb];
        int c = out[nc];
        int d = out[nd];
        int e = out[ne];
        int f = out[nf];
        int g = out[ng];
        int h = out[nh];

        int t1 = h + (rot(e, 6) ^ rot(e, 11) ^ rot(e, 25)) + ((e & f) ^ ((~e) & g)) + K + x;
        int t2 = (rot(a, 2) ^ rot(a, 13) ^ rot(a, 22)) + ((a & b) ^ (a & c) ^ (b & c));

        out[nd] = d + t1;
        out[nh] = t1 + t2;
    }

    /**
     * Gets current time from system.
     *
     * @return The time in millisecnonds.
     */
    public Long getCurrentTime() {
        return System.nanoTime() / 1000000L;
    }

    /**
     * Gets information about running status.
     *
     * @return True if process is running, false if is not.
     */
    public Boolean getRunning() {
        return running.get();
    }

    /**
     * Method stops digging, and close all running process.
     */
    public void stop() {
        running.set(false);

        for (int i = 0; i < getThreads().size(); i++) {
            Thread thread = getThreads().get(i);

            if (thread != Thread.currentThread()) {
                thread.interrupt();
            }
        }
    }

    /**
     * Increments attempts.
     *
     * @return the amount of attempts.
     */
    public Long incrementAttempts() {
        return attempts.incrementAndGet();
    }

    /**
     * Increments blocks amount.
     *
     * @return the amount of blocks
     */
    public Long incrementBlocks() {
        return blocks.incrementAndGet();
    }

    /**
     * Increments HW errors.
     *
     * @return the amount of HW errors.
     */
    public Long incrementHWErrors() {
        return hwErrors.incrementAndGet();
    }

    /**
     * Increments rejects amount.
     *
     * @return the amount of rejects.
     */
    public Long incrementRejects() {
        return rejects.incrementAndGet();
    }

    /**
     * Gets information about current process.
     *
     * @param information
     */
    public void info(String information) {
        log.info(getCurrentTime() + " " + information);
        threads.get(0).interrupt();
    }

    /**
     * Gets debug message.
     *
     * @param message
     */
    public void debug(String message) {
        if(bitcoinOptions.getDebug()) {
            log.debug(dateTime() + " DEBUG: " + message);
            threads.get(0).interrupt();
        }
    }

    /**
     * Gets list of {@link com.bitcoin.core.network.NetworkState}.
     *
     * @return the list of {@link com.bitcoin.core.network.NetworkState}.
     */
    public List<NetworkState> getNetworkStates() {
        return networkStates;
    }

    /**
     * Gets head of network states.
     *
     * @return head of network states.
     */
    public NetworkState getNetworkStateHead() {
        return networkStateHead;
    }

    public AtomicLong getAttempts() {
        return attempts;
    }

    public AtomicLong getBlocks() {
        return blocks;
    }

    public Set<String> getEnabledDevices() {
        return enabledDevices;
    }

    public AtomicLong getHashCount() {
        return hashCount;
    }

    public AtomicLong getHwErrors() {
        return hwErrors;
    }

    public AtomicLong getRejects() {
        return rejects;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    /**
     * Gets options for {@link com.bitcoin.core.Excavator} class.
     *
     * @return The instance of {@link com.bitcoin.util.BitcoinOptions}.
     */
    public BitcoinOptions getBitcoinOptions() {
        return bitcoinOptions;
    }

    public void setRejects(AtomicLong rejects) {
        this.rejects = rejects;
    }

    public void setAttempts(AtomicLong attempts) {
        this.attempts = attempts;
    }

    public void setBlocks(AtomicLong blocks) {
        this.blocks = blocks;
    }

    public void setEnabledDevices(Set<String> enabledDevices) {
        this.enabledDevices = enabledDevices;
    }

    public void setHashCount(AtomicLong hashCount) {
        this.hashCount = hashCount;
    }

    public void setHwErrors(AtomicLong hwErrors) {
        this.hwErrors = hwErrors;
    }

    public void setBitcoinOptions(BitcoinOptions bitcoinOptions) {
        this.bitcoinOptions = bitcoinOptions;
    }

    @Override
    public void run() {
        execute();
    }
}
