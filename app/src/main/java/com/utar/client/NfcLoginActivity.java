package com.utar.client;

import androidx.appcompat.app.AppCompatActivity;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.utar.client.card.LoginNfcReader;

public class NfcLoginActivity extends AppCompatActivity implements View.OnClickListener, LoginNfcReader.LoginCallback{

    private static final String TAG= "NfcLoginActivity";
    private LottieAnimationView animationView;
    private TextView tv_status;
    private CountDownTimer countDownTimer;
    private LoginNfcReader loginNfcReader;
    public static NfcLoginActivity nfcLoginActivity;
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_login);
        init();

    }

    private void init(){
        nfcLoginActivity = this;
        findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        animationView = findViewById(R.id.animation);
        tv_status = findViewById(R.id.tv_status);
        loginNfcReader = new LoginNfcReader(this);

        countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                enableReaderMode();
                animationView.setAnimation(R.raw.nfc_scan_j);
                animationView.loop(true);
                animationView.playAnimation();
            }
        };
    }

    public static NfcLoginActivity getInstance(){
        return nfcLoginActivity;
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.backBtn:{
                onBackPressed();
                break;
            }
        }
    }

    @Override
    public void setAnimation(int rawRes, boolean repeat) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animationView.setAnimation(rawRes);
                animationView.loop(repeat);
                animationView.playAnimation();
            }
        });
    }


    @Override
    public void countDownReset() {
        Log.i(TAG, "Countdown start");
        disableReaderMode();
        countDownTimer.start();
    }

    @Override
    public void setText(int stringRes) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void enableReaderMode(){
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if(nfc != null){
            nfc.enableReaderMode(this, loginNfcReader, READER_FLAGS, null);
        }
    }

    public void disableReaderMode(){
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if(nfc != null){
            nfc.disableReaderMode(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableReaderMode();
    }
}