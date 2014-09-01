/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class represents GPU hardware.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GPUHardwareType extends HardwareType {

    private String kernel;

    public static String KERNEL_PATH = System.getProperty("user.dir")
            + "/excavator/src/main/java/com/bitcoin/kernel/BitCoinExcavator.cl";

    /**
     * The constructor for {@link com.bitcoin.core.device.GPUHardwareType}.
     *
     * @param excavator The instance of {@link com.bitcoin.core.Excavator}.
     * @throws ExcavatorFatalException
     */
    public GPUHardwareType(Excavator excavator) throws ExcavatorFatalException {
        super(excavator);
        kernel = loadKernel(KERNEL_PATH);
    }

    /**
     * Loads source kerenel to string.
     *
     * @param kernelPath Path to file with kernel.
     * @return {@link java.lang.String} with loaded kernel.
     */
    private String loadKernel(String kernelPath) throws ExcavatorFatalException {
        byte[] data= null;

        try (InputStream stream = new FileInputStream(kernelPath)){
            data = new byte[64 * 1024];

            while ((stream.read(data)) != -1) {
            }

            stream.close();
        } catch (IOException e) {
            throw new ExcavatorFatalException(getExcavator(), "Unable to read Kernel.cl");
        }

        return new String(data).trim();
    }

    /**
     * Gets list of {@link com.bitcoin.core.device.DeviceState}.
     *
     * @return list of {@link com.bitcoin.core.device.DeviceState}.
     */
    @Override
    public List<? extends DeviceState> getDeviceStates() {
        return null;
    }
}
