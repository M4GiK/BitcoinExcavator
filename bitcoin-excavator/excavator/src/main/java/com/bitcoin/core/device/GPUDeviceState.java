/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class represents GPU device.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GPUDeviceState extends DeviceState {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory
            .getLogger(GPUHardwareType.class);

    public final static int OUTPUTS = 16;

    private final PointerBuffer localWorkSize = BufferUtils.createPointerBuffer(1);
    private final CLKernel kernel;
    private final CLContext context;
    private final CLDevice device;
    private final ExecutionState executions[];
    private final PlatformVersion platformVersion;
    private long deviceWorkSize;
    private long workSizeBase;
    private Boolean hardwareCheck;
    private GPUHardwareType hardwareType;
    private Excavator excavator;
    private CLPlatform platform;
    private AtomicLong workSize = new AtomicLong(0);

    private AtomicLong runs = new AtomicLong(0);
    private long lastRuns = 0;
    private long startTime = 0;
    private long lastTime = 0;

    /**
     * Constructor for {@link com.bitcoin.core.device.GPUHardwareType}.
     *
     * @param hardwareType
     * @param deviceName
     * @param platform
     * @param version
     * @param device
     */
    public GPUDeviceState(GPUHardwareType hardwareType, String deviceName, CLPlatform platform,
                          PlatformVersion version, CLDevice device) throws ExcavatorFatalException {
        this.device = device;
        this.platformVersion = version;
        this.platform = platform;
        this.hardwareType = hardwareType;
        this.excavator = hardwareType.getExcavator();
        this.executions = new ExecutionState[GPUHardwareType.EXECUTION_TOTAL];
        setDeviceName(deviceName);
        setResetNetworkState(hardwareType.getExcavator().getCurrentTime());
        context = prepareContext(excavator.getBitcoinOptions().getGPUForceWorkSize());

        Boolean hasBitAlign = isBitAlign();
        Boolean hasBFI_INT = isBFI_INT(hasBitAlign);

        String compileOptions = prepareCompileOptions(hasBitAlign, hasBFI_INT);
        CLProgram program = CL10.clCreateProgramWithSource(context, hardwareType.getKernel(), null);
        validBuild(CL10.clBuildProgram(program, device, compileOptions, null), program);
        operationsForBFI_INT(hasBFI_INT, program, compileOptions);
        kernel = createKernel(program);
        setLocalWorkSize(excavator.getBitcoinOptions().getGPUForceWorkSize());
    }

    /**
     * Sets local work size for {@link com.bitcoin.core.device.GPUDeviceState}.
     *
     * @param forceWorkSize The value of force work size.
     */
    private void setLocalWorkSize(Integer forceWorkSize) {
        if (forceWorkSize == 0) {
            ByteBuffer rkwgs = BufferUtils.createByteBuffer(8);

            localWorkSize.put(0, rkwgs.getLong(0));

            if (!(CL10.clGetKernelWorkGroupInfo(kernel, device, CL10.CL_KERNEL_WORK_GROUP_SIZE, rkwgs, null)
                    == CL10.CL_SUCCESS) || localWorkSize.get(0) == 0)
                localWorkSize.put(0, deviceWorkSize);
        } else {
            localWorkSize.put(0, forceWorkSize);
        }

        excavator.info("Added " + getDeviceName() + " (" + device.getInfoInt(CL10.CL_DEVICE_MAX_COMPUTE_UNITS)
                + " CU, local work size of " + localWorkSize.get(0) + ")");

        workSizeBase = 64 * 512;

        workSize.set(workSizeBase * 16);

        for (int i = 0; i < GPUHardwareType.EXECUTION_TOTAL; i++) {
            String executorName = getDeviceName() + "/" + i;
            executions[i] = new GPUExecutionState(executorName);
            Thread thread = new Thread(executions[i], "Excavator executor (" + executorName + ")");
            thread.start();
            excavator.addThread(thread);
        }
    }

    /**
     * Creates kernel for {@link com.bitcoin.core.device.GPUDeviceState}.
     *
     * @param program The instance of {@link org.lwjgl.opencl.CLProgram}.
     * @return instance of {@link org.lwjgl.opencl.CLKernel}.
     * @throws ExcavatorFatalException
     */
    private CLKernel createKernel(CLProgram program) throws ExcavatorFatalException {
        if (program == null) {
            throw new IllegalArgumentException("Argument for create kernel can not be null");
        }

        CLKernel kernel = CL10.clCreateKernel(program, "search", null);

        if (kernel == null) {
            throw new ExcavatorFatalException(excavator, "Failed to create kernel on " + getDeviceName());
        }

        return kernel;
    }

    /**
     * This method is working with additional options of BFI_INT flag is marked.
     *
     * @param hasBFI_INT     The logic value about status of BFI_INT.
     * @param program        The instance of {@link org.lwjgl.opencl.CLProgram}.
     * @param compileOptions The instance of prepared options for {@link org.lwjgl.opencl.CLProgram}.
     * @throws ExcavatorFatalException
     */
    private void operationsForBFI_INT(Boolean hasBFI_INT, CLProgram program, String compileOptions) throws ExcavatorFatalException {
        if (hasBFI_INT) {
            excavator.info("BFI_INT patching enabled, disabling hardware check errors");
            hardwareCheck = false;

            Integer binarySize = (int) program.getInfoSizeArray(CL10.CL_PROGRAM_BINARY_SIZES)[0];
            ByteBuffer binary = BufferUtils.createByteBuffer(binarySize);
            program.getInfoBinaries(binary);

            for (int pos = 0; pos < binarySize - 4; pos++) {
                if ((long) (0xFFFFFFFF & binary.getInt(pos)) == 0x464C457FL
                        && (long) (0xFFFFFFFF & binary.getInt(pos + 4)) == 0x64010101L) {
                    boolean firstText = true;

                    int offset = binary.getInt(pos + 32);
                    short entrySize = binary.getShort(pos + 46);
                    short entryCount = binary.getShort(pos + 48);
                    short index = binary.getShort(pos + 50);

                    int header = pos + offset;

                    int nameTableOffset = binary.getInt(header + index * entrySize + 16);
                    int size = binary.getInt(header + index * entrySize + 20);

                    int entry = header;

                    for (int section = 0; section < entryCount; section++) {
                        int nameIndex = binary.getInt(entry);
                        offset = binary.getInt(entry + 16);
                        size = binary.getInt(entry + 20);

                        int name = pos + nameTableOffset + nameIndex;

                        if ((long) (0xFFFFFFFF & binary.getInt(name)) == 0x7865742E) {
                            if (firstText) {
                                firstText = false;
                            } else {
                                int sectionStart = pos + offset;
                                for (int i = 0; i < size / 8; i++) {
                                    long instruction1 = (long) (0xFFFFFFFF & binary.getInt(sectionStart + i * 8));
                                    long instruction2 = (long) (0xFFFFFFFF & binary.getInt(sectionStart + i * 8 + 4));

                                    if ((instruction1 & 0x02001000L) == 0x00000000L
                                            && (instruction2 & 0x9003F000L) == 0x0001A000L) {
                                        instruction2 ^= (0x0001A000L ^ 0x0000C000L);

                                        binary.putInt(sectionStart + i * 8 + 4, (int) instruction2);
                                    }
                                }
                            }
                        }

                        entry += entrySize;
                    }
                    break;
                }
            }

            IntBuffer binaryErr = BufferUtils.createIntBuffer(1);
            CL10.clReleaseProgram(program);
            program = CL10.clCreateProgramWithBinary(context, device, binary, binaryErr, null);
            validBuild(CL10.clBuildProgram(program, device, compileOptions, null), program);
        }
    }

    /**
     * This method valids build on current device.
     *
     * @param status  Information about build status on current device.
     * @param program The instance of {@link org.lwjgl.opencl.CLProgram}.
     * @throws ExcavatorFatalException
     */
    private void validBuild(int status, CLProgram program) throws ExcavatorFatalException {
        if (status != CL10.CL_SUCCESS) {
            ByteBuffer logBuffer = BufferUtils.createByteBuffer(1024);
            byte[] logs = new byte[1024];
            CL10.clGetProgramBuildInfo(program, device, CL10.CL_PROGRAM_BUILD_LOG, logBuffer, null);
            logBuffer.get(logs);
            log.error(new String(logs));
            throw new ExcavatorFatalException(excavator, "Failed to build program on " + getDeviceName());
        }
    }

    /**
     * Prepares options to compile by {@link org.lwjgl.opencl.CLProgram}.
     *
     * @param hasBitAlign The logic value about status of bit align.
     * @param hasBFI_INT  The logic value about status of BFI_INT.
     * @return prepared options for {@link org.lwjgl.opencl.CLProgram}.
     */
    private String prepareCompileOptions(Boolean hasBitAlign, Boolean hasBFI_INT) {
        String compileOptions = " -D WORKSIZE=" + deviceWorkSize;

        if (hasBitAlign) {
            compileOptions += " -D BITALIGN";
        }

        if (hasBFI_INT) {
            compileOptions += " -D BFIINT";
        }

        return compileOptions;
    }

    /**
     * Prepares {@link org.lwjgl.opencl.CLContext} for {@link com.bitcoin.core.device.GPUDeviceState}.
     *
     * @param forceWorkSize Force work size for {@link com.bitcoin.core.device.GPUDeviceState}.
     * @return prepared {@link org.lwjgl.opencl.CLContext}.
     */
    private CLContext prepareContext(Integer forceWorkSize) {
        CLContext preparedCLContext = null;

        PointerBuffer properties = BufferUtils.createPointerBuffer(3);
        properties.put(CL10.CL_CONTEXT_PLATFORM).put(platform.getPointer()).put(0).flip();

        if (forceWorkSize > 0) {
            deviceWorkSize = forceWorkSize;
        } else if (LWJGLUtil.getPlatform() != LWJGLUtil.PLATFORM_MACOSX) {
            deviceWorkSize = device.getInfoSize(CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE);
        } else {
            deviceWorkSize = 64;
        }

        preparedCLContext = CL10.clCreateContext(properties, device, new CLContextCallback() {
            @Override
            protected void handleMessage(String errinfo, ByteBuffer private_info) {
                excavator.error(errinfo);
            }
        }, null);

        return preparedCLContext;
    }

    /**
     *  Checks the status of current device.
     */
    @Override
    public void checkDevice() {
        long now = excavator.getCurrentTime();
        long elapsed = now - lastTime;
        long currentRuns = runs.get();
        double targetFPSBasis = hardwareType.getTargetFPSBasis();
        int totalVectors = hardwareType.getTotalVectors();
        long ws = workSize.get();

        if (now > startTime + Excavator.TIME_OFFSET * 2 && currentRuns > lastRuns + excavator.getBitcoinOptions().getGPUTargetFPS()) {
            setBasis((double) elapsed / (double) (currentRuns - lastRuns));

            if (getBasis() < targetFPSBasis / 4)
                ws += workSizeBase * 16;
            else if (getBasis() < targetFPSBasis / 2)
                ws += workSizeBase * 4;
            else if (getBasis() < targetFPSBasis)
                ws += workSizeBase;
            else if (getBasis() > targetFPSBasis * 4)
                ws -= workSizeBase * 16;
            else if (getBasis() > targetFPSBasis * 2)
                ws -= workSizeBase * 4;
            else if (getBasis() > targetFPSBasis)
                ws -= workSizeBase;

            if (ws < workSizeBase)
                ws = workSizeBase;
            else if (ws > Excavator.TWO32 / totalVectors - 1)
                ws = Excavator.TWO32 / totalVectors - 1;

            lastRuns = currentRuns;
            lastTime = now;

            workSize.set(ws);
        }
    }

    /**
     * Checks if bit is align on {@link com.bitcoin.core.device.GPUDeviceState}.
     *
     * @return true if bit is align false if is not.
     */
    public Boolean isBitAlign() {
        Boolean hasBitAlign;

        ByteBuffer extb = BufferUtils.createByteBuffer(1024);
        CL10.clGetDeviceInfo(device, CL10.CL_DEVICE_EXTENSIONS, extb, null);
        byte[] exta = new byte[1024];
        extb.get(exta);

        if (new String(exta).contains("cl_amd_media_ops")) {
            hasBitAlign = true;
        } else {
            hasBitAlign = false;
        }

        return hasBitAlign;
    }

    /**
     * Checks if bit is BFI_INT.
     *
     * @param hasBitAlign The logic value about status of bit align.
     * @return true if bit is BFI_INT false if is not.
     */
    private Boolean isBFI_INT(Boolean hasBitAlign) {
        Boolean hasBFI_INT = false;

        if (hasBitAlign) {
            if (getDeviceName().contains("Cedar")
                    || getDeviceName().contains("Redwood")
                    || getDeviceName().contains("Juniper")
                    || getDeviceName().contains("Cypress")
                    || getDeviceName().contains("Hemlock")
                    || getDeviceName().contains("Caicos")
                    || getDeviceName().contains("Turks")
                    || getDeviceName().contains("Barts")
                    || getDeviceName().contains("Cayman")
                    || getDeviceName().contains("Antilles")
                    || getDeviceName().contains("Palm")
                    || getDeviceName().contains("Sumo")
                    || getDeviceName().contains("Wrestler")
                    || getDeviceName().contains("WinterPark")
                    || getDeviceName().contains("BeaverCreek")) {
                hasBFI_INT = true;
            }
        }

        return hasBFI_INT;
    }
}
