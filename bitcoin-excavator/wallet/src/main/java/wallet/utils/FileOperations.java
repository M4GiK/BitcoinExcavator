/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 29, 2014.
 */
package wallet.utils;

import org.apache.commons.io.FileDeleteStrategy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import wallet.view.MainView;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class represents operations for file.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class FileOperations {

    private final static Logger LOGGER = Logger.getLogger(FileOperations.class
            .getName());

    public static final String PROPERTIES = "/wallets/properties.json";

    public static String APP_PATH = System.getProperty("user.dir");

    private final static FileFilter walletsOnly = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith("wallet") && !pathname.isDirectory();

        }
    };

    /**
     * Updates properties files with given wallet name.
     *
     * @param walletName wallet name to add to properties.
     */
    public static void updateProperty(String walletName) {

        if (walletName == null) {
            throw new IllegalArgumentException();
        }

        if (isPropertiesExisting(PROPERTIES)) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) parser.parse(new FileReader(APP_PATH + PROPERTIES));
                JSONArray wallets = (JSONArray) jsonObject.get("wallets");
                wallets.add(walletName);

                FileWriter file = new FileWriter(APP_PATH + PROPERTIES);
                file.write(buildProperties(wallets).toJSONString());
                file.flush();
                file.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Checks if property file exist.
     *
     * @param filePath path to property file.
     * @return true if property file exist, false if does not.
     */
    public static Boolean isPropertiesExisting(String filePath) {

        if (filePath == null || filePath == "") {
            throw new IllegalArgumentException("File path mustn't be null or empty");
        }

        File file = new File(APP_PATH + filePath);

        return file.exists() && !file.isDirectory();
    }

    /**
     * Reads properties file for wallet application from given directory.
     *
     * @param filePath The location of file relative to the application.
     * @return the filled {@link java.util.List} of {@link wallet.utils.BitcoinWallet}.
     */
    public static List<BitcoinWallet> readProperties(String filePath) {

        if (filePath == null || filePath == "") {
            throw new IllegalArgumentException("File path mustn't be null or empty");
        }

        List<BitcoinWallet> bitcoinWallets = new ArrayList<BitcoinWallet>();
        JSONParser parser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(APP_PATH + filePath));
            JSONArray wallets = (JSONArray) jsonObject.get("wallets");
            Iterator<String> iterator = wallets.iterator();

            while (iterator.hasNext()) {
                bitcoinWallets.add(new BitcoinWallet(MainView.params, new File(APP_PATH + "/wallets/."),
                        removeFileExtension(iterator.next())));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitcoinWallets;
    }

    /**
     * Removes file extension from given string.
     *
     * @param stringWithExtension The string for removes extension
     * @return string without extension
     */
    public static String removeFileExtension(String stringWithExtension) {
        final int lastPeriodPos = stringWithExtension.lastIndexOf('.');

        return lastPeriodPos <= 0 ? stringWithExtension : stringWithExtension.substring(0, lastPeriodPos);
    }

    /**
     * Saves given {@link java.util.List} to file.
     *
     * @param walletNames Data to save.
     * @param filePath    The location of file relative to the application.
     */
    public static void saveProperties(List<String> walletNames, String filePath) {

        if (filePath == null || filePath == "") {
            throw new IllegalArgumentException("File path mustn't be null or empty");
        }

        JSONArray wallets = new JSONArray();

        for (String wallet : walletNames) {
            wallets.add(wallet);
        }

        try {
            FileWriter file = new FileWriter(APP_PATH + filePath);
            file.write(buildProperties(wallets).toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves list of wallets from given path.
     *
     * @param folderPath path to wallets.
     * @return list of wallets.
     */
    public static List<String> collectWallets(String folderPath) {

        if (folderPath == null || folderPath == "") {
            throw new IllegalArgumentException("Folder path mustn't be null or empty");
        }

        LOGGER.info("Searching for files:");
        List<String> walletList = new ArrayList<String>();
        File src = new File(APP_PATH + folderPath);
        LOGGER.info(src.getName() + ":");

        if (src.isDirectory()) {
            File[] files = src.listFiles(walletsOnly);

            for (File file : files) {
                LOGGER.info(src.getName() + "/" + file.getName());
                walletList.add(file.getName());
            }
        } else {
            LOGGER.warning("ERROR: Invalid Directory");
        }

        return walletList;
    }

    /**
     * This method build JSON object with given properties.
     *
     * @param wallets The name ofwallets.
     * @return The prepared JSONObject.
     */
    public static JSONObject buildProperties(JSONArray wallets) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wallets", wallets);
        jsonObject.put("date", getCurrentDateTime());

        return jsonObject;
    }

    /**
     * This method gets current data time.
     *
     * @return The current data time in format yyyy/MM/dd HH:mm:ss.
     */
    public static String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        return dateFormat.format(cal.getTime()).toString();
    }

    /**
     * Deletes wallet from disk and from properties.
     *
     * @param walletName The wallet to delete.
     */
    public static void deleteWallet(String walletName) {
        if (walletName == null) {
            throw new IllegalArgumentException();
        }

        if (isPropertiesExisting(PROPERTIES)) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = null;
            try {


                File fileWallet = new File(APP_PATH + "/wallets/" + walletName + ".wallet");
                File fileSpvchain = new File(APP_PATH + "/wallets/" + walletName + ".spvchain");

                if(FileDeleteStrategy.FORCE.deleteQuietly(fileWallet)
                        && FileDeleteStrategy.FORCE.deleteQuietly(fileSpvchain)) {
                    jsonObject = (JSONObject) parser.parse(new FileReader(APP_PATH + PROPERTIES));
                    JSONArray wallets = (JSONArray) jsonObject.get("wallets");
                    wallets.remove(walletName);

                    FileWriter file = new FileWriter(APP_PATH + PROPERTIES);
                    file.write(buildProperties(wallets).toJSONString());
                    file.flush();
                    file.close();

                    LOGGER.info("Wallet " + walletName + " is deleted.");
                } else {
                    LOGGER.warning("Delete operation for delete wallet " + walletName + " is failed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            throw new NullPointerException();
        }
    }
}
