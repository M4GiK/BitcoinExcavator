/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;

/**
 * This class represents GPU device.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GPUDeviceState extends DeviceState {

    public GPUDeviceState(GPUHardwareType gpuHardwareType, String deviceName, CLPlatform platform, PlatformVersion version, CLDevice device) {
        super();
    }

    @Override
    public void checkDevice() {

    }
}
