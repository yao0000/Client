package com.utar.client.card;

import static com.utar.client.card.HCEService.BuildSelectApdu;
import static com.utar.client.card.HCEService.ByteArrayToHexString;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.Login;
import com.utar.client.NfcLoginActivity;
import com.utar.client.R;
import com.utar.client.data.Account;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class LoginNfcReader implements NfcAdapter.ReaderCallback{

    private final String TAG = "LoginNfcReader";
    private final String MERCHANT_LOGIN_AID = "F000222444";

    private final byte[] CORRECT_RESPOND_APP = {(byte)0x90, (byte)0x00};
    private final byte[] UNKNOWN_CMD_SW = {(byte)0x00, (byte)0x00};
    private final byte[] REQUEST_INFO = {(byte)0x91, (byte)0x92};

    private WeakReference<LoginNfcReader.LoginCallback> mAccountCallback;
    public LoginNfcReader(LoginNfcReader.LoginCallback accountCallback) {
        mAccountCallback = new WeakReference<LoginNfcReader.LoginCallback>(accountCallback);
    }


    public interface LoginCallback{
        public void setAnimation(int rawRes, boolean repeat);
        public void countDownReset();
        public void setText(int stringRes);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Log.i(TAG, "New tag is discovered");
        if(tag == null){
            Log.i(TAG, "tag is null object reference");
            return;
        }
        IsoDep isoDep = IsoDep.get(tag);

        if(isoDep == null){
            return;
        }

        try {
            isoDep.connect();

            if(!isoDep.isConnected()){
                throw new IOException("Connection fail");
            }

            byte[] apduCmd = BuildSelectApdu(MERCHANT_LOGIN_AID);
            Log.i(TAG, "Requesting AID: " + ByteArrayToHexString(apduCmd));

            byte[] result = isoDep.transceive(apduCmd);
            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, result.length - 2);

            if(Arrays.equals(CORRECT_RESPOND_APP, statusWord)){
                Log.i(TAG, "Arrays is equal");
                mAccountCallback.get().setAnimation(R.raw.nfc_processing, true);
                mAccountCallback.get().setText(R.string.processing);
                result = isoDep.transceive(REQUEST_INFO);
                NfcLoginActivity.getInstance().disableReaderMode();

                String uid = new String(result, "UTF-8");
                Log.i(TAG, "UID: " + uid);

                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                        .getReference("user")
                        .child(uid);

                dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            Account account = task.getResult().getValue(Account.class);
                            Log.i(TAG, "Email: " + account.getEmail());
                            Log.i(TAG, "Password: " + account.getPassword());
                            FirebaseAuth.getInstance()
                                    .signInWithEmailAndPassword(account.getEmail(), account.getPassword())
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Log.i(TAG, "Task is success");
                                                NfcLoginActivity.getInstance().finish();
                                                String deviceId = Settings.Secure.getString(NfcLoginActivity.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
                                                dbRef.child("deviceId").setValue(deviceId);

                                            }
                                            else{
                                                Log.i(TAG, "Task is unsuccess");
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else{
                Log.i(TAG, "Else case");
                mAccountCallback.get().setAnimation(R.raw.card_fail, false);
                mAccountCallback.get().countDownReset();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

}
