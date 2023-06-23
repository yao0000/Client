package com.utar.client.ui.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.utar.client.MyApplication;
import com.utar.client.R;

import java.util.ArrayList;
import java.util.List;

public class RegisterPinActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int CREATE_PIN = 0;
    public static final int CHANGE_PIN = 1;

    private List<View> viewList;
    private TextView tv_hint;
    private ArrayList<String> passcodeList;
    private String oldPasscode, firstPasscode, secondPasscode;
    private int mode;
    private Animation animation;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pin_activity);
        initialize();

        if(MyApplication.getInstance().getAccount().getPin().isEmpty()){
            mode = CREATE_PIN;
            tv_hint.setText(getString(R.string.new_pin));
        }
        else{
            mode = CHANGE_PIN;
            tv_hint.setText(getString(R.string.old_pin));
        }
    }

    private void initialize(){
        oldPasscode = "";
        firstPasscode = "";
        secondPasscode = "";
        passcodeList = new ArrayList<>();
        tv_hint = findViewById(R.id.tv_pin_hint);
        linearLayout = findViewById(R.id.ll_passcode);

        findViewById(R.id.tv_reset).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(v -> onBackPressed());

        viewList = new ArrayList<>();
        viewList.add(findViewById(R.id.view_pass_1));
        viewList.add(findViewById(R.id.view_pass_2));
        viewList.add(findViewById(R.id.view_pass_3));
        viewList.add(findViewById(R.id.view_pass_4));
        viewList.add(findViewById(R.id.view_pass_5));
        viewList.add(findViewById(R.id.view_pass_6));

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
        findViewById(R.id.tv_clear).setOnClickListener(this::onClick);

        animation = AnimationUtils.loadAnimation(this, R.anim.vibrant_effect);
        animation.setDuration(50);
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
            case R.id.tv_reset:{
                tv_hint.setText("Enter Payment PIN");
                firstPasscode = "";
                secondPasscode = "";
                passcodeReset();
                break;
            }
            case R.id.tv_clear:{
                passcodeReset();
                break;
            }
        }
    }



    private void passcodeProcess(String passcode){
        if(passcode == null){
            if(passcodeList.size() != 0) {
                passcodeList.remove(passcodeList.size() - 1);
            }
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

        if(passcodeList.size() == 6){

            //if case for change payment pin mode
            if(mode == CHANGE_PIN){
                if(oldPasscode.isEmpty()){
                    for(int i = 0; i < passcodeList.size(); i++){
                        oldPasscode += passcodeList.get(i);
                    }

                    if(!oldPasscode.equals(MyApplication.getInstance().getAccount().getPin())){
                        linearLayout.startAnimation(animation);
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if(vibrator.hasVibrator()){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                            }
                        }
                        oldPasscode = "";
                        passcodeReset();
                        return;
                    }

                    tv_hint.setText(getString(R.string.new_pin));
                    passcodeReset();
                    return;
                }
            }


            if(firstPasscode.isEmpty()){
                firstPasscode = "";

                for(int k = 0; k < passcodeList.size(); k++){
                    firstPasscode += passcodeList.get(k);
                }

                passcodeReset();
                tv_hint.setText("Enter confirm payment PIN");
                return;
            }


            secondPasscode = "";
            for(int k = 0; k < passcodeList.size(); k++){
                secondPasscode += passcodeList.get(k);
            }

            if(firstPasscode.equals(secondPasscode)){
                Log.i("Passcode", "match");
                FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().getUid())
                                .child("pin").setValue(secondPasscode)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                setResult(RESULT_OK);
                                Toast.makeText(RegisterPinActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
            else{
                new AlertDialog.Builder(RegisterPinActivity.this)
                        .setTitle(getString(R.string.alert))
                        .setMessage("Mismatch Payment PIN")
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                passcodeReset();
            }

        }
    }


    private void passcodeReset(){
        passcodeList.clear();
        for(int i = 0; i < viewList.size();i++){
            viewList.get(i).setBackgroundResource(R.drawable.bg_view_grey_oval);
        }
    }
}