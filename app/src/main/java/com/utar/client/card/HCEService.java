package com.utar.client.card;


import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.utar.client.R;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class HCEService extends HostApduService {
    private static final String TAG = "HCEService";
    //AID for card services
    private static final String WALLET_CARD_AID = "F222444888";
    // -> Select the corresponding Application ID
    private static final String SELECT_APDU_HEADER = "00A40400";
    // -> To get data
    private static final String GET_DATA_APDU_HEADER = "00CA0000";
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    private static final byte[] UNKNOWN_CMD_SW = {(byte) 0x00, (byte)0x00};
    private static final byte[] TRANSACTION_SUCCESS = {(byte)0x91, (byte)0x92};
    private static final byte[] INSUFFICIENT_BALANCE = {(byte)0x91, (byte)0x93};

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));

        if(Arrays.equals(BuildSelectApdu(WALLET_CARD_AID), commandApdu)){
            if(AccountAssistant.mAccountCallback != null) {
                AccountAssistant.mAccountCallback.get().setStatusText(R.string.processing);
                AccountAssistant.mAccountCallback.get().setAnimation(R.raw.nfc_processing, true);
            }
            String userID = FirebaseAuth.getInstance().getUid();
            return ConcatArrays(userID.getBytes(), SELECT_OK_SW);
        }

        int commandLength = commandApdu.length;
        byte[] statusWord = {commandApdu[commandLength-2], commandApdu[commandLength-1]};
        byte[] payload = Arrays.copyOf(commandApdu, commandLength - 2);

        if(Arrays.equals(statusWord, TRANSACTION_SUCCESS)){
            if(AccountAssistant.mAccountCallback != null) {
                try {
                    AccountAssistant.mAccountCallback.get().setStatusText(R.string.payment_success, "\nRM" + new String(payload, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                AccountAssistant.mAccountCallback.get().setAnimation(R.raw.nfc_finish, false);
                AccountAssistant.mAccountCallback.get().countDownFinish();
            }
            return SELECT_OK_SW;
        }
        else if(Arrays.equals(statusWord, INSUFFICIENT_BALANCE)){
            if(AccountAssistant.mAccountCallback != null) {
                AccountAssistant.mAccountCallback.get().setStatusText(R.string.insufficient_balance);
                AccountAssistant.mAccountCallback.get().setAnimation(R.raw.card_fail, false);
                AccountAssistant.mAccountCallback.get().countDownFinish();
            }
            return SELECT_OK_SW;
        }

        return UNKNOWN_CMD_SW;
    }

    @Override
    public void onDeactivated(int reason) {

    }

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
        //return HexStringToByteArray(SELECT_APDU_HEADER);

    }

    /**
     * Build APDU for GET_DATA command. See ISO 7816-4.
     *
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildGetDataApdu() {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(GET_DATA_APDU_HEADER + "0FFF");
    }

    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws java.lang.IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Utility method to concatenate two byte arrays.
     * @param first First array
     * @param rest Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
