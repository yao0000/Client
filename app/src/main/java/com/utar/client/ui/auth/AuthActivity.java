package com.utar.client.ui.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.utar.client.MyApplication;
import com.utar.client.R;
import com.utar.client.data.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "AuthActivity";
    private static final int REQUEST_CODE_REGISTER_PIN_ACTIVITY = 10;

    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    private List<View> viewList;
    ArrayList<String> passcodeList = new ArrayList<>();

    private LinearLayout linearLayout;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(MyApplication.getInstance().getAccount().getPin().isEmpty()) {
            Intent intent = new Intent(this, RegisterPinActivity.class);
            intent.putExtra("mode", RegisterPinActivity.CREATE_PIN);
            startActivityForResult(intent, REQUEST_CODE_REGISTER_PIN_ACTIVITY);
            return;
        }
        setContentView(R.layout.activity_pin_auth);
        init();
        fingerprintAuthenticate();
    }

    private void init(){
        linearLayout = findViewById(R.id.ll_passcode);

        findViewById(R.id.tv_pass_0).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_1).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_2).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_3).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_4).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_5).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_6).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_7).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_8).setOnClickListener(this::onClick);
        findViewById(R.id.tv_pass_9).setOnClickListener(this::onClick);
        findViewById(R.id.tv_backspace).setOnClickListener(this::onClick);
        findViewById(R.id.view_fingerprint).setOnClickListener(v -> fingerprintAuthenticate());
        findViewById(R.id.backBtn).setOnClickListener(v -> onBackPressed());

        viewList = new ArrayList<>();
        viewList.add(findViewById(R.id.view_pass_1));
        viewList.add(findViewById(R.id.view_pass_2));
        viewList.add(findViewById(R.id.view_pass_3));
        viewList.add(findViewById(R.id.view_pass_4));
        viewList.add(findViewById(R.id.view_pass_5));
        viewList.add(findViewById(R.id.view_pass_6));

        animation = AnimationUtils.loadAnimation(this, R.anim.vibrant_effect);
        animation.setDuration(50);
    }



    public void fingerprintAuthenticate(){
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()){
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE: {
                toast("Device doesn't have fingerprint");
                break;
            }

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:{
                toast("Not working");
                break;
            }

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:{
                toast("No fingerprint assigned");
                break;
            }
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                toast("Authentication success");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                toast("Authentication fail");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Authentication")
                .setDescription("Use fingerprint to Login").setNegativeButtonText("Use password").build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tv_pass_0:{
                passcodeProcess("0");
                break;
            }
            case R.id.tv_pass_1:{
                passcodeProcess("1");
                break;
            }
            case R.id.tv_pass_2:{
                passcodeProcess("2");
                break;
            }
            case R.id.tv_pass_3:{
                passcodeProcess("3");
                break;
            }
            case R.id.tv_pass_4:{
                passcodeProcess("4");
                break;
            }
            case R.id.tv_pass_5:{
                passcodeProcess("5");
                break;
            }
            case R.id.tv_pass_6:{
                passcodeProcess("6");
                break;
            }
            case R.id.tv_pass_7:{
                passcodeProcess("7");
                break;
            }
            case R.id.tv_pass_8:{
                passcodeProcess("8");
                break;
            }
            case R.id.tv_pass_9:{
                passcodeProcess("9");
                break;
            }
            case R.id.tv_backspace:{
                passcodeProcess(null);
                break;
            }
        }
    }

    private void passcodeProcess(String passcode){
        if(passcode == null){
            passcodeList.remove(passcodeList.size()-1);
        }
        else{
            passcodeList.add(passcode);
        }

        for(int i = 0; i < passcodeList.size(); i++){
            viewList.get(i).setBackgroundResource(R.drawable.bg_view_blue_oval);
        }

        for(int i = 5; i >= passcodeList.size(); i-- ){
            viewList.get(i).setBackgroundResource(R.drawable.bg_view_grey_oval);
        }

        if(passcodeList.size() != 6){
            return;
        }

        Account account = MyApplication.getInstance().getAccount();
        String pass = "";

        for(int k = 0; k < passcodeList.size(); k++){
            pass += passcodeList.get(k);
        }

        if(account.getPin().equals(pass)){
            setResult(RESULT_OK);
            finish();
        }
        else{
            linearLayout.startAnimation(animation);
            passcodeList.clear();
            for(int i = 0; i < viewList.size();i++){
                viewList.get(i).setBackgroundResource(R.drawable.bg_view_grey_oval);
            }
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator.hasVibrator()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_REGISTER_PIN_ACTIVITY){
            if(resultCode == RESULT_OK){
                recreate();
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        this.getString(R.string.err),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}