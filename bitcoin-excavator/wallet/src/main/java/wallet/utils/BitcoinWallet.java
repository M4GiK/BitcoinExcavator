/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 30, 2014.
 */
package wallet.utils;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.kits.WalletAppKit;

import java.io.File;

/**
 * This class represents wallet for Bitcoin based on {@link com.google.bitcoin.kits.WalletAppKit}.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class BitcoinWallet extends WalletAppKit {

    private NetworkParameters networkParameters;
    private File directory;
    private String filePrefix;

    /**
     * Constructor for {@link wallet.utils.BitcoinWallet}.
     * @param params
     * @param directory
     * @param filePrefix
     */
    public BitcoinWallet(NetworkParameters params, File directory, String filePrefix) {
        super(params, directory, filePrefix);
        setNetworkParameters(params);
        setDirectory(directory);
        setFilePrefix(filePrefix);
    }

    public NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    public void setNetworkParameters(NetworkParameters networkParameters) {
        this.networkParameters = networkParameters;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }
}
