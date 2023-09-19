package com.utar.client.card;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utar.client.MyApplication;
import com.utar.client.R;
import com.utar.client.data.Account;
import com.utar.client.data.Transaction;

//to become a reader to read NFC card emulator
public class TransferNfcReader implements NfcAdapter.ReaderCallback{

    private Account payeeAccount, myAccount;

    private static final String TAG = "TransferNfcReader";
    private IsoDep isoDep;
    private static final String TRANSFER_AID = "F444222888";

    // -> Select the corresponding Application ID
    private static final String SELECT_APDU_HEADER = "00A40400";
    // -> To get data
    private static final String GET_DATA_APDU_HEADER = "00CA0000";
    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
    private static final byte[] TRANSACTION_SUCCESS = {(byte)0x91, (byte)0x92};
    private static final byte[] TRANSACTION_FAIL = {(byte)0x91, (byte)0x93};
    private static final byte[] UNKNOWN_CMD_SW = {(byte) 0x00, (byte)0x00};

    private static final byte[] REQUEST_AMOUNT = {(byte)0x80, (byte)0x81};

    private WeakReference<AccountCallback> mAccountCallback;

    public interface AccountCallback {
        public void setStatusText(int id);
        public void setAnimation(int rawRes, boolean repeat);
        public void countDownFinish();
        public void countDownReset();
    }

    public TransferNfcReader(AccountCallback accountCallback) {
        mAccountCallback = new WeakReference<AccountCallback>(accountCallback);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Log.i(TAG, "New Tag discovered");
        isoDep = IsoDep.get(tag);

        if(tag == null){
            Log.e(TAG, "Tag is null object reference");
            return;
        }

        try{
            isoDep.connect();

            isoDep.setTimeout(3600);

            byte[] apduCommand = BuildSelectApdu(TRANSFER_AID);
            Log.i(TAG, "Connect to corresponding AID by using command: " + ByteArrayToHexString(apduCommand));

            byte[] result = isoDep.transceive(apduCommand);
            mAccountCallback.get().setStatusText(R.string.processing);
            mAccountCallback.get().setAnimation(R.raw.loading, true);

            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, result.length - 2);

            if(Arrays.equals(SELECT_OK_SW, statusWord)){
                //get User ID first
                String payeeID = new String(payload, "UTF-8");

                result = isoDep.transceive(REQUEST_AMOUNT);
                resultLength = result.length;
                statusWord = new byte[]{result[resultLength - 2], result[resultLength - 1]};
                payload = Arrays.copyOf(result, result.length - 2);

                if(!Arrays.equals(REQUEST_AMOUNT, statusWord)){
                    Log.e(TAG, "Request amount error");
                    return;
                }

                //get amount
                double amount = Double.parseDouble(new String(payload, "UTF-8"));

                DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("user");
                DatabaseReference transactionDatabaseReference = FirebaseDatabase.getInstance().getReference("transactions");

                userDatabaseReference.child(payeeID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(!task.isSuccessful()){
                            Log.e(TAG, "Error occur");
                            return;
                        }

                        //account initialization
                        myAccount = MyApplication.getInstance().getAccount();
                        payeeAccount = task.getResult().getValue(Account.class);

                        //payee
                        double payeeBal = Double.parseDouble(payeeAccount.getBalance());
                        payeeBal -= amount;

                        //update payee balance
                        userDatabaseReference.child(payeeID).child("balance")
                                .setValue(String.format("%.2f", payeeBal));

                        //update transaction database
                        Transaction payeeTransaction = new Transaction(myAccount.getName(),
                                amount, Transaction.TRANSFER_OUT);
                        transactionDatabaseReference.child(payeeID).push()
                                .setValue(payeeTransaction);

                        //receiver or me
                        double myBalance = Double.parseDouble(myAccount.getBalance());
                        myBalance += amount;
                        String uid = FirebaseAuth.getInstance().getUid();

                        //update user balance
                        userDatabaseReference.child(uid).child("balance")
                                .setValue(String.format("%.2f", myBalance));

                        //update transaction database
                        Transaction myTransaction = new Transaction(payeeAccount.getName(),
                                amount, Transaction.TRANSFER_IN);
                        transactionDatabaseReference.child(uid)
                                .push().setValue(myTransaction);



                        try {
                            byte[] res = isoDep.transceive(TRANSACTION_SUCCESS);
                            mAccountCallback.get().setAnimation(R.raw.done, false);
                            mAccountCallback.get().setStatusText(R.string.success_transfer);
                            mAccountCallback.get().countDownFinish();

                            if(Arrays.equals(res, TRANSACTION_FAIL)){
                                throw new Exception("Fail transaction");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        finally{
                            try {
                                isoDep.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
            else{
                mAccountCallback.get().setStatusText(R.string.err);
                mAccountCallback.get().setAnimation(R.raw.error, false);
                mAccountCallback.get().countDownReset();
            }

        }catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            mAccountCallback.get().setStatusText(R.string.err);
            mAccountCallback.get().setAnimation(R.raw.error, false);
            mAccountCallback.get().countDownReset();
        }
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
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
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
     * Utility class to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Utility class to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     */
    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
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
