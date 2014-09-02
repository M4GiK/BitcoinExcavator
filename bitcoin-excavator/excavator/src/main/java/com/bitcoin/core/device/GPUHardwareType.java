/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class represents GPU hardware.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GPUHardwareType extends HardwareType {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory
            .getLogger(GPUHardwareType.class);

    private final static Integer EXECUTION_TOTAL = 2;

    private final static String UPPER[] = {"X", "Y", "Z", "W", "T", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "k"};

    private final static String LOWER[] = {"x", "y", "z", "w", "t", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"};

    private String kernel;

    private Integer totalVectors = 0;

    private Double targetFPSBasis;

    private List<GPUDeviceState> deviceStates;

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
        debugSource(excavator.getBitcoinOptions().getGPUDebugSource());
        targetFPSBasis = 1000.0 / (getExcavator().getBitcoinOptions().getGPUTargetFPS());
        deviceStates = preparePlatform();
    }

    /**
     * Prepares platform to lunch GPU devices.
     *
     * @return the list of prepared GPU devices.
     */
    private List<GPUDeviceState> preparePlatform() throws ExcavatorFatalException {
        List<GPUDeviceState> preparedDeviceStates = new ArrayList<GPUDeviceState>();
        List<CLPlatform> platforms = null;

        try {
            CL.create();
            platforms = CLPlatform.getPlatforms();
        } catch (Exception e) {
            throw new ExcavatorFatalException(getExcavator(),
                    "Failed to initialize OpenCL, make sure your environment is setup correctly");
        }

        if (platforms == null || platforms.isEmpty()) {
            throw new ExcavatorFatalException(getExcavator(), "No OpenCL platforms found");
        }

        Set<String> enabledDevices = getExcavator().getBitcoinOptions().getEnabledDevices();
        Integer count = 1;
        Integer platformCount = 0;

        for (CLPlatform platform : platforms) {
            PlatformVersion version;
            log.info("Using " + platform.getInfoString(CL10.CL_PLATFORM_NAME).trim()
                    + " " + platform.getInfoString(CL10.CL_PLATFORM_VERSION));
            String versions = platform.getInfoString(CL10.CL_PLATFORM_VERSION);

            if (versions.contains("OpenCL  1.0")) {
                version = PlatformVersion.V1_0;
            } else if (versions.contains("OpenCL 1.1")) {
                version = PlatformVersion.V1_1;
            } else {
                version = PlatformVersion.V1_2;
            }

            if (version == PlatformVersion.V1_0) {
                getExcavator().error("OpenCL platform " + platform.getInfoString(CL10.CL_PLATFORM_NAME).trim()
                        + " is not OpenCL 1.1 or later");
                continue;
            }

            List<CLDevice> devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU | CL10.CL_DEVICE_TYPE_ACCELERATOR);

            if(devices == null || devices.isEmpty()) {
                getExcavator().error("OpenCL platform " + platform.getInfoString(CL10.CL_PLATFORM_NAME).trim()
                        + " contains no devices");
                continue;
            }

            if (devices != null) {
                for(CLDevice device : devices) {
                    if(enabledDevices == null || enabledDevices.contains(platformCount + "."
                            + count) || enabledDevices.contains(Integer.toString(count))) {
                        String deviceName = device.getInfoString(CL10.CL_DEVICE_NAME).trim() + " (#" + count + ")";
                        preparedDeviceStates.add(new GPUDeviceState(this, deviceName, platform, version, device));
                    }

                    count++;
                }
            }

            platformCount++;
        }

        if(preparedDeviceStates.size() == 0) {
            throw new ExcavatorFatalException(getExcavator(), "No OpenCL devices found");
        }

        return preparedDeviceStates;
    }

    /**
     * This method debug the source code of kernel. Shows the output of prepared kernel.
     *
     * @param debugMode
     */
    private void debugSource(Boolean debugMode) {
        if (debugMode) {
            log.info(kernel);
        }
    }

    /**
     * Loads source kernel to string.
     *
     * @param kernelPath Path to file with kernel.
     * @return {@link java.lang.String} with loaded kernel.
     */
    private String loadKernel(String kernelPath) throws ExcavatorFatalException {
        byte[] data = null;

        try (InputStream stream = new FileInputStream(kernelPath)) {
            data = new byte[64 * 1024];
            stream.read(data);
            stream.close();
        } catch (IOException e) {
            throw new ExcavatorFatalException(getExcavator(), "Unable to read Kernel.cl");
        }

        return prepareKernel(new String(data).trim());
    }

    /**
     * Prepares loaded kernel to lunch on platform.
     *
     * @param kernelToPrepare Data to prepare.
     * @return prepared kernel.
     */
    private String prepareKernel(String kernelToPrepare) throws ExcavatorFatalException {
        String preparedKernel = "";
        String kernelLines[] = kernelToPrepare.split("\n");
        Long vectorBase = 0L;
        Integer totalVectorsPOT;
        Integer[] vectors = getExcavator().getBitcoinOptions().getGPUVectors();

        for (Integer vector : vectors) {
            totalVectors += vector;
        }

        if (totalVectors > 16) {
            throw new ExcavatorFatalException(getExcavator(), "Excavator does not support more than 16 total vectors yet");
        }

        if (totalVectors != (1 << (32 - Integer.numberOfLeadingZeros(totalVectors) - 1))) {
            totalVectorsPOT = 1 << (32 - Integer.numberOfLeadingZeros(totalVectors));
        } else {
            totalVectorsPOT = totalVectors;
        }

        for (int i = 0; i < kernelLines.length; i++) {
            String kernelLine = kernelLines[i];

            if (getExcavator().getBitcoinOptions().getGPUNoArray() && !kernelLine.contains("z ZA")) {
                kernelLine = kernelLine.replaceAll("ZA\\[([0-9]+)\\]", "ZA$1");
            }

            if (kernelLine.contains("zz")) {
                if (totalVectors > 1) {
                    kernelLine = kernelLine.replaceAll("zz", String.valueOf(totalVectorsPOT));
                } else {
                    kernelLine = kernelLine.replaceAll("zz", "");
                }
            }

            if (kernelLine.contains("= (io) ? Znonce")) {
                int count = 0;
                String change = "(uintzz)(";

                for (int j = 0; j < vectors.length; j++) {
                    change += UPPER + "nonce";
                    count += vectors[j];

                    if (j != vectors.length - 1) {
                        change += ", ";
                    }
                }

                for (int j = count; j < totalVectorsPOT; j++) {
                    change += ", 0";
                }

                change += ")";

                kernelLine = kernelLine.replace("Znonce", change);

                if (totalVectors > 1) {
                    kernelLine = kernelLine.replaceAll("zz", String.valueOf(totalVectorsPOT));
                } else {
                    kernelLine = kernelLine.replaceAll("zz", "");
                }

                preparedKernel += kernelLine + "\n";
            } else if ((kernelLine.contains("Z") || kernelLine.contains("z")) && !kernelLine.contains("__")) {
                for (int j = 0; j < vectors.length; j++) {
                    String replace = kernelLine;

                    if (getExcavator().getBitcoinOptions().getGPUNoArray() && replace.contains("z ZA")) {
                        replace = "";

                        for (int k = 0; k < 930; k += 5) {
                            replace += "		 ";

                            for (int m = 0; m < 5; m++) {
                                replace += "z ZA" + (k + m) + "; ";
                            }

                            replace += "\n";
                        }
                    }

                    if (vectors[j] > 1 && replace.contains("typedef")) {
                        replace = replace.replace("uint", "uint" + vectors[j]);
                    } else if (replace.contains("z Znonce")) {
                        String vectorGlobal;

                        if (vectors[j] > 1) {
                            vectorGlobal = " + (uint" + vectors[j] + ")(";
                        } else {
                            vectorGlobal = " + (uint)(";
                        }

                        for (int k = 0; k < vectors[j]; k++) {
                            vectorGlobal += Long.toString(vectorBase + k);

                            if (k != vectors[j] - 1) {
                                vectorGlobal += ", ";
                            }
                        }

                        vectorGlobal += ");";

                        replace = replace.replace(";", vectorGlobal);

                        vectorBase += vectors[j];
                    }

                    if (vectors[j] == 1 && replace.contains("bool Zio")) {
                        replace = replace.replace("any(", "(");
                    }

                    preparedKernel += replace.replaceAll("Z", UPPER[j]).replaceAll("z", LOWER[j]) + "\n";
                }
            } else if (totalVectors == 1 && kernelLine.contains("any(nonce")) {
                preparedKernel += kernelLine.replace("any", "") + "\n";
            } else if (kernelLine.contains("__global")) {
                if (totalVectors > 1) {
                    preparedKernel += kernelLine.replaceAll("uint", "uint" + totalVectorsPOT) + "\n";
                } else {
                    preparedKernel += kernelLine + "\n";
                }
            } else {
                preparedKernel += kernelLine + "\n";
            }
        }

        return preparedKernel;
    }


    public String getKernel() {
        return kernel;
    }

    public double getTargetFPSBasis() {
        return targetFPSBasis;
    }

    public int getTotalVectors() {
        return totalVectors;
    }

    /**
     * Gets list of {@link com.bitcoin.core.device.DeviceState}.
     *
     * @return list of {@link com.bitcoin.core.device.DeviceState}.
     */
    @Override
    public List<? extends DeviceState> getDeviceStates() {
        return deviceStates;
    }
}
