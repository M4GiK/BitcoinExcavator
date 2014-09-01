/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;

import java.util.List;

/**
 * This class represents abstract item of hardware.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public abstract class HardwareType {

    private Excavator excavator;

    private List<? extends DeviceState> deviceStates = null;

    /**
     * Constructor for {@link com.bitcoin.core.device.HardwareType} class.
     *
     * @param excavator The instance of {@link com.bitcoin.core.Excavator}.
     * @throws ExcavatorFatalException
     */
    public HardwareType(Excavator excavator) throws ExcavatorFatalException {
        setExcavator(excavator);
    }

    /**
     * Gets list of {@link com.bitcoin.core.device.DeviceState}.
     *
     * @return list of {@link com.bitcoin.core.device.DeviceState}.
     */
    public abstract List<? extends DeviceState> getDeviceStates();

    /**
     * Gets instance of {@link com.bitcoin.core.Excavator}.
     *
     * @return instance of {@link com.bitcoin.core.Excavator}.
     */
    public Excavator getExcavator() {
        return excavator;
    }

    /**
     * Sets instance of {@link com.bitcoin.core.Excavator}.
     *
     * @param excavator The instance of {@link com.bitcoin.core.Excavator}.
     */
    public void setExcavator(Excavator excavator) {
        this.excavator = excavator;
    }
}
