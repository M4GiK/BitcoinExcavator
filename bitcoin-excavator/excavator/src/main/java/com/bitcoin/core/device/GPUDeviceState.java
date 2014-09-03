/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 1, 2014.
 */
package com.bitcoin.core.device;

import com.bitcoin.core.BitcoinExcavator;
import com.bitcoin.core.Excavator;
import com.bitcoin.core.ExcavatorFatalException;
import com.bitcoin.core.network.WorkState;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class represents GPU device.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class GPUDeviceState extends DeviceState {

    /**
     * This class represents GPU execution state.
     *
     * @author m4gik <michal.szczygiel@wp.pl>
     */
    public class GPUExecutionState extends ExecutionState {

        private final IntBuffer errBuffer = BufferUtils.createIntBuffer(1);
        private final PointerBuffer workBaseBuffer = BufferUtils.createPointerBuffer(1);
        private final PointerBuffer workSizeBuffer = BufferUtils.createPointerBuffer(1);
        private final CLMem output[] = new CLMem[2];
        private final CLMem blank;

        private final ByteBuffer digestInput = ByteBuffer.allocate(80);
        private final int[] midstate2 = new int[16];
        private final MessageDigest digestInside;
        private final MessageDigest digestOutside;
        private final CLCommandQueue queue;

        private ByteBuffer outputBuffer;
        private Integer outputIndex = 0;
        private boolean requestedNewWork;
        private byte[] digestOutput;

        /**
         * Constructor for {@link com.bitcoin.core.device.GPUDeviceState.GPUExecutionState}.
         *
         * @param executorName The name of executor.
         * @throws ExcavatorFatalException
         */
        public GPUExecutionState(String executorName) throws ExcavatorFatalException {
            super(executorName);

            try {
                digestInside = MessageDigest.getInstance("SHA-256");
                digestOutside = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new ExcavatorFatalException(excavator, "Your Java implementation does not have a MessageDigest for SHA-256");
            }

            queue = createQueue();
            blank = blankInitialization();
            outputBuffer = CL10.clEnqueueMapBuffer(queue, output[outputIndex], 1, CL10.CL_MAP_READ, 0, OUTPUTS * 4, null, null, null);
            excavator.getNetworkStates().get(0).addGetQueue(this);
            requestedNewWork = true;
        }

        /**
         * Initializes the blank.
         *
         * @return {@link org.lwjgl.opencl.CLMem} initialized.
         * @throws ExcavatorFatalException
         */
        private CLMem blankInitialization() throws ExcavatorFatalException {
            CLMem blank = null;
            IntBuffer blankInit = BufferUtils.createIntBuffer(OUTPUTS * 4);

            for (int i = 0; i < OUTPUTS; i++) {
                blankInit.put(0);
            }

            blankInit.rewind();

            if (platformVersion == PlatformVersion.V1_1) {
                blank = CL10.clCreateBuffer(context, CL10.CL_MEM_COPY_HOST_PTR
                        | CL10.CL_MEM_READ_ONLY, blankInit, errBuffer);
            } else {
                blank = CL10.clCreateBuffer(context, CL10.CL_MEM_COPY_HOST_PTR
                        | CL10.CL_MEM_READ_ONLY | CL12.CL_MEM_HOST_NO_ACCESS, blankInit, errBuffer);
            }

            if (blank == null || errBuffer.get(0) != CL10.CL_SUCCESS) {
                throw new ExcavatorFatalException(excavator, "Failed to allocate blank buffer");
            }

            blankInit.rewind();

            for (int i = 0; i < 2; i++) {
                if (platformVersion == PlatformVersion.V1_1) {
                    output[i] = CL10.clCreateBuffer(context, CL10.CL_MEM_COPY_HOST_PTR
                            | CL10.CL_MEM_WRITE_ONLY, blankInit, errBuffer);
                } else {
                    output[i] = CL10.clCreateBuffer(context, CL10.CL_MEM_COPY_HOST_PTR
                            | CL10.CL_MEM_WRITE_ONLY | CL12.CL_MEM_HOST_READ_ONLY, blankInit, errBuffer);
                }

                blankInit.rewind();

                if (output[i] == null || errBuffer.get(0) != CL10.CL_SUCCESS) {
                    throw new ExcavatorFatalException(excavator, "Failed to allocate output buffer");
                }
            }

            return blank;
        }

        /**
         * Creates queue for {@link org.lwjgl.opencl.CLCommandQueue}.
         *
         * @return created queue.
         * @throws ExcavatorFatalException
         */
        private CLCommandQueue createQueue() throws ExcavatorFatalException {
            CLCommandQueue queue = CL10.clCreateCommandQueue(context, device, 0, errBuffer);

            if (queue == null || errBuffer.get(0) != CL10.CL_SUCCESS) {
                throw new ExcavatorFatalException(excavator, "Failed to allocate queue");
            }

            return queue;
        }

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
        @Override
        public void run() {
            boolean submittedBlock;
            boolean resetBuffer;
            boolean hwError;
            boolean skipProcessing;
            boolean skipUnmap = false;

            while (excavator.getRunning()) {
                submittedBlock = false;
                resetBuffer = false;
                hwError = false;
                skipProcessing = false;

                WorkState workIncoming = null;

                if (requestedNewWork) {
                    try {
                        workIncoming = getIncomingQueue().take();
                    } catch (InterruptedException f) {
                        continue;
                    }
                } else {
                    workIncoming = getIncomingQueue().poll();
                }

                if (workIncoming != null) {
                    setWorkState(workIncoming);
                    requestedNewWork = false;
                    resetBuffer = true;
                    skipProcessing = true;
                }

                if (!skipProcessing | !skipUnmap) {
                    for (int z = 0; z < OUTPUTS; z++) {
                        int nonce = outputBuffer.getInt(z * 4);

                        if (nonce != 0) {
                            for (int j = 0; j < 19; j++)
                                digestInput.putInt(j * 4, getWorkState().getData(j));

                            digestInput.putInt(19 * 4, nonce);

                            digestOutput = digestOutside.digest(digestInside.digest(digestInput.array()));

                            long G = ((long) (0xFF & digestOutput[27]) << 24) | ((long) (0xFF & digestOutput[26]) << 16)
                                    | ((long) (0xFF & digestOutput[25]) << 8) | ((long) (0xFF & digestOutput[24]));

                            long H = ((long) (0xFF & digestOutput[31]) << 24) | ((long) (0xFF & digestOutput[30]) << 16)
                                    | ((long) (0xFF & digestOutput[29]) << 8) | ((long) (0xFF & digestOutput[28]));

                            if (H == 0) {
                                excavator.debug("Attempt " + excavator.incrementAttempts() + " from " + getExecutionName());

                                if (getWorkState().getTarget(7) != 0 || G <= getWorkState().getTarget(6)) {
                                    getWorkState().submitNonce(nonce);
                                    submittedBlock = true;
                                }
                            } else {
                                hwError = true;
                            }
                            resetBuffer = true;
                        }
                    }

                    if (hwError && submittedBlock == false) {
                        if (hardwareCheck && !excavator.getBitcoinOptions().getDebug()) {
                            excavator.error("Invalid solution " + excavator.incrementHWErrors()
                                    + " from " + getDeviceName() + ", possible driver or hardware issue");
                        } else {
                            excavator.debug("Invalid solution " + excavator.incrementHWErrors()
                                    + " from " + getExecutionName() + ", possible driver or hardware issue");
                        }
                    }
                }

                if (resetBuffer) {
                    CL10.clEnqueueCopyBuffer(queue, blank, output[outputIndex], 0, 0, OUTPUTS * 4, null, null);
                }

                if (!skipUnmap) {
                    CL10.clEnqueueUnmapMemObject(queue, output[outputIndex], outputBuffer, null, null);
                    outputIndex = (outputIndex == 0) ? 1 : 0;
                }

                long workBase = getWorkState().getBase();
                long increment = workSize.get();

                if (excavator.getCurrentTime() - 3600000 > getResetNetworkState()) {
                    setResetNetworkState(excavator.getCurrentTime());

                    excavator.getNetworkStates().get(0).addGetQueue(this);
                    requestedNewWork = skipUnmap = true;
                } else {
                    requestedNewWork = skipUnmap = getWorkState().update(increment);
                }

                if (!requestedNewWork) {
                    excavator.addAndGetHashCount(increment);
                    getDeviceHashCount().addAndGet(increment);
                    runs.incrementAndGet();

                    workSizeBuffer.put(0, increment);
                    workBaseBuffer.put(0, workBase);

                    System.arraycopy(getWorkState().getMidstate(), 0, midstate2, 0, 8);

                    BitcoinExcavator.sharound(midstate2, 0, 1, 2, 3, 4, 5, 6, 7, getWorkState().getData(16), 0x428A2F98);
                    BitcoinExcavator.sharound(midstate2, 7, 0, 1, 2, 3, 4, 5, 6, getWorkState().getData(17), 0x71374491);
                    BitcoinExcavator.sharound(midstate2, 6, 7, 0, 1, 2, 3, 4, 5, getWorkState().getData(18), 0xB5C0FBCF);

                    int W16 = getWorkState().getData(16) + (BitcoinExcavator.rot(getWorkState().getData(17), 7)
                            ^ BitcoinExcavator.rot(getWorkState().getData(17), 18) ^ (getWorkState().getData(17) >>> 3));
                    int W17 = getWorkState().getData(17) + (BitcoinExcavator.rot(getWorkState().getData(18), 7)
                            ^ BitcoinExcavator.rot(getWorkState().getData(18), 18) ^ (getWorkState().getData(18) >>> 3))
                            + 0x01100000;
                    int W18 = getWorkState().getData(18) + (BitcoinExcavator.rot(W16, 17) ^ BitcoinExcavator.rot(W16, 19)
                            ^ (W16 >>> 10));
                    int W19 = 0x11002000 + (BitcoinExcavator.rot(W17, 17) ^ BitcoinExcavator.rot(W17, 19) ^ (W17 >>> 10));
                    int W31 = 0x00000280 + (BitcoinExcavator.rot(W16, 7) ^ BitcoinExcavator.rot(W16, 18) ^ (W16 >>> 3));
                    int W32 = W16 + (BitcoinExcavator.rot(W17, 7) ^ BitcoinExcavator.rot(W17, 18) ^ (W17 >>> 3));

                    int PreVal4 = getWorkState().getMidstate(4) + (BitcoinExcavator.rot(midstate2[1], 6)
                            ^ BitcoinExcavator.rot(midstate2[1], 11) ^ BitcoinExcavator.rot(midstate2[1], 25))
                            + (midstate2[3] ^ (midstate2[1] & (midstate2[2] ^ midstate2[3]))) + 0xe9b5dba5;
                    int T1 = (BitcoinExcavator.rot(midstate2[5], 2) ^ BitcoinExcavator.rot(midstate2[5], 13)
                            ^ BitcoinExcavator.rot(midstate2[5], 22)) + ((midstate2[5] & midstate2[6])
                            | (midstate2[7] & (midstate2[5] | midstate2[6])));

                    int PreVal4_state0 = PreVal4 + getWorkState().getMidstate(0);
                    int PreVal4_state0_k7 = (int) (PreVal4_state0 + 0xAB1C5ED5L);
                    int PreVal4_T1 = PreVal4 + T1;
                    int B1_plus_K6 = (int) (midstate2[1] + 0x923f82a4L);
                    int C1_plus_K5 = (int) (midstate2[2] + 0x59f111f1L);
                    int W16_plus_K16 = (int) (W16 + 0xe49b69c1L);
                    int W17_plus_K17 = (int) (W17 + 0xefbe4786L);

                    kernel.setArg(0, PreVal4_state0).setArg(1, PreVal4_state0_k7).setArg(2, PreVal4_T1)
                            .setArg(3, W18).setArg(4, W19).setArg(5, W16).setArg(6, W17).setArg(7, W16_plus_K16)
                            .setArg(8, W17_plus_K17).setArg(9, W31).setArg(10, W32)
                            .setArg(11, (int) (midstate2[3] + 0xB956c25bL)).setArg(12, midstate2[1])
                            .setArg(13, midstate2[2]).setArg(14, midstate2[7]).setArg(15, midstate2[5])
                            .setArg(16, midstate2[6]).setArg(17, C1_plus_K5).setArg(18, B1_plus_K6)
                            .setArg(19, getWorkState().getMidstate(0)).setArg(20, getWorkState().getMidstate(1))
                            .setArg(21, getWorkState().getMidstate(2)).setArg(22, getWorkState().getMidstate(3))
                            .setArg(23, getWorkState().getMidstate(4)).setArg(24, getWorkState().getMidstate(5))
                            .setArg(25, getWorkState().getMidstate(6)).setArg(26, getWorkState().getMidstate(7))
                            .setArg(27, output[outputIndex]);

                    int err = CL10.clEnqueueNDRangeKernel(queue, kernel, 1, workBaseBuffer, workSizeBuffer,
                            localWorkSize, null, null);

                    if (err != CL10.CL_SUCCESS && err != CL10.CL_INVALID_KERNEL_ARGS
                            && err != CL10.CL_INVALID_GLOBAL_OFFSET) {
                        try {
                            throw new ExcavatorFatalException(excavator, "Failed to queue kernel, error " + err);
                        } catch (ExcavatorFatalException e) {
                            log.error(e.getMessage());
                        }
                    } else {
                        if (err == CL10.CL_INVALID_KERNEL_ARGS) {
                            excavator.debug("Spurious CL_INVALID_KERNEL_ARGS error, ignoring");
                            skipUnmap = true;
                        } else if (err == CL10.CL_INVALID_GLOBAL_OFFSET) {
                            excavator.error("Spurious CL_INVALID_GLOBAL_OFFSET error, offset: "
                                    + workBase + ", work size: " + increment);
                            skipUnmap = true;
                        } else {
                            outputBuffer = CL10.clEnqueueMapBuffer(queue, output[outputIndex],
                                    1, CL10.CL_MAP_READ, 0, OUTPUTS * 4, null, null, null);
                        }
                    }
                }
            }
        }
    }

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
     * @throws ExcavatorFatalException
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
     * @throws ExcavatorFatalException
     */
    private void setLocalWorkSize(Integer forceWorkSize) throws ExcavatorFatalException {
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
     * Checks the status of current device.
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
