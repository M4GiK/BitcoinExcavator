/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 29, 2014.
 */
package wallet.controls;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import wallet.utils.TextFieldValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for validating names for wallets.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class BitcoinNameWalletValidator {

    private Node[] nodes;

    public BitcoinNameWalletValidator(TextField field, Node... nodes) {
        this.nodes = nodes;
        // Handle the red highlighting, but don't highlight in red just when the field is empty because that makes
        // the example/prompt address hard to read.
        new TextFieldValidator(field, text -> text.isEmpty() || isWalletName(text));
        // However we do want the buttons to be disabled when empty so we apply a different test there.
        field.textProperty().addListener((observableValue, prev, current) -> {
            toggleButtons(current);
        });
        toggleButtons(field.getText());
    }

    private void toggleButtons(String current) {
        boolean valid = isWalletName(current);
        for (Node n : nodes) n.setDisable(!valid);
    }

    private boolean isWalletName(String text) {
        if (text == null || text == "" || identifyingSpecialCharacters(text)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method is used to identify the special characters in the given value,
     * if there are any special characters it return true else return false.
     *
     * @param value
     * @return
     */
    public static boolean identifyingSpecialCharacters(String value) {
        boolean returnVal = false;
        char[] charArray = value.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if ((((int) charArray[i]) >= 32 && ((int) charArray[i]) <= 47) ||
                    (((int) charArray[i]) >= 58 && ((int) charArray[i]) <= 64) ||
                    (((int) charArray[i]) >= 91 && ((int) charArray[i]) <= 96) ||
                    (((int) charArray[i]) >= 123 && ((int) charArray[i]) <= 127)) {
                returnVal = true;
                break;
            }
        }

        return returnVal;
    }
}
