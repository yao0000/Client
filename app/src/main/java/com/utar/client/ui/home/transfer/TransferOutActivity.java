package com.utar.client.ui.home.transfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.utar.client.R;
import com.utar.client.card.AccountAssistant;

public class TransferOutActivity extends AppCompatActivity implements View.OnClickListener, AccountAssistant.AccountCallback {

    private static final String TAG = "TransferOutActivity";
    private LottieAnimationView lottieAnimationView;
    private TextView tv_nfc;
    private AccountAssistant accountAssistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_out);

        findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        findViewById(R.id.btn_floating_action_cancel).setOnClickListener(this::onClick);
        lottieAnimationView = findViewById(R.id.animation);
        tv_nfc = findViewById(R.id.nfc_status);

        accountAssistant = new AccountAssistant(this);
        Log.i(TAG, "Activity Triggered");
        getAmount();

    }

    @Override
    protected void onResume() {
        super.onResume();
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(TransferOutActivity.this,
                        "com.utar.client.card.TransferHCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(TransferOutActivity.this,
                        "com.utar.client.card.TransferHCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_floating_action_cancel:
            case R.id.backBtn:{
                setResult(RESULT_CANCELED);
                onBackPressed();
                break;
            }

        }
    }

    @Override
    public void setStatusText(int resId) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_nfc.setText(getString(resId));
            }
        });
    }

    @Override
    public void setStatusText(int resId, String appendMsg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_nfc.setText(getString(resId) + appendMsg);
            }
        });
    }

    @Override
    public void setAnimation(int rawRes, boolean repeat) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lottieAnimationView.setAnimation(rawRes);
                lottieAnimationView.loop(repeat);
                lottieAnimationView.playAnimation();
            }
        });
    }

    @Override
    public void countDownFinish() {
        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //startActivity(new Intent(PaymentActivity.this, MainActivity.class));
                finish();
            }
        };

        countDownTimer.start();
    }

    @Override
    public double getAmount() {
        SharedPreferences pref = getSharedPreferences("AmountPreference", MODE_PRIVATE);
        String strAmount = pref.getString("amount", "NA");

        if(strAmount.equals("NA")){
            Log.e(TAG, "strAmount from shared preferences is null value");
            return -1;
        }
        else{
            Log.i(TAG, "strAmount: " + strAmount);
            return Double.parseDouble(strAmount);
        }
    }

}