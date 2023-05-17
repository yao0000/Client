package com.utar.client.fragment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.utar.client.MainActivity;
import com.utar.client.R;
import com.utar.client.cardemulation.AccountAssistant;
import com.utar.client.cardemulation.HCEService;

public class PaymentActivity extends AppCompatActivity implements AccountAssistant.AccountCallback {
    private static final String TAG = "PaymentActivity";
    LottieAnimationView lottieAnimationView;
    TextView tv_nfcStatus;
    AccountAssistant accountAssistant;
    Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        accountAssistant = new AccountAssistant(this);
        startService(new Intent(this, HCEService.class));
        lottieAnimationView = findViewById(R.id.nfc_animation);
        tv_nfcStatus = findViewById(R.id.nfc_status);
        btn_cancel = findViewById(R.id.nfc_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(PaymentActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Hce Service is started and ready for payment");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        "com.utar.client.cardemulation.HCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "Payment success and the Hce Service is stopped");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        "com.utar.client.cardemulation.HCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }



    @Override
    public void setStatusText(String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_nfcStatus.setText(msg);
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
        CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
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
}