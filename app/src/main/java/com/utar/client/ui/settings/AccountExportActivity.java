package com.utar.client.ui.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.MainActivity;
import com.utar.client.R;
import com.utar.client.card.AccountExportHCEService;
import com.utar.client.data.Account;

public class AccountExportActivity extends AppCompatActivity implements AccountExportHCEService.ViewCallback, View.OnClickListener{

    private static final String TAG = "AccountExportActivity";
    private static AccountExportActivity accountExportActivity;
    private LottieAnimationView animationView;
    private TextView tv_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_export);
        animationView = findViewById(R.id.animation);
        tv_status = findViewById(R.id.tv_status);
        findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        AccountExportHCEService accountExportHCEService = new AccountExportHCEService(this);

        accountExportActivity = this;
    }

    public static AccountExportActivity getInstance(){
        return accountExportActivity;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Account Transfer Hce Service is started");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        AccountExportHCEService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "Account Transfer Hce Service is stopped");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        AccountExportHCEService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void setAnimation(int rawRes, boolean repeat) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animationView.setAnimation(rawRes);
                animationView.loop(repeat);
            }
        });
    }

    @Override
    public void setText(int rawRes) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_status.setText(getString(R.string.processing));
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.backBtn:{
                finish();
                break;
            }
        }
    }
}