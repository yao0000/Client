package com.utar.client.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utar.client.MyApplication;
import com.utar.client.R;
import com.utar.client.data.Account;
import com.utar.client.data.Transaction;
import com.utar.client.ui.auth.AuthActivity;

public class ReloadWithdrawActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG     = "ReloadWithdrawActivity";
    private static final int REQUEST_CODE_AUTHENTICATION_ACTIVITY = 4;
    public static final int RELOAD      = 1;
    public static final int WITHDRAW    = 2;

    private int mode;
    private TextView tv_title, tv_activity_title;
    private EditText et_amount;
    private ProgressBar progressBar;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload_withdraw);

        initialization();

        if(getIntent().hasExtra("mode")){
            mode = getIntent().getIntExtra("mode", RELOAD);
        }
        else{
            Log.e(TAG, "extra passing null");
            finish();
        }

        if(mode == RELOAD){
            tv_title.setText(getString(R.string.reload));
            tv_activity_title.setText(getString(R.string.reload));
        }
        else if (mode == WITHDRAW) {
            tv_title.setText(getString(R.string.withdraw));
            tv_activity_title.setText(getString(R.string.withdraw));
        }

    }

    private void initialization(){
        account = MyApplication.getInstance().getAccount();

        tv_activity_title = findViewById(R.id.tv_title);
        tv_title = findViewById(R.id.reload_withdraw_title);
        et_amount = findViewById(R.id.et_reload_withdraw_amount);
        progressBar = findViewById(R.id.panel_progressBar);

        findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        findViewById(R.id.btn_rm10).setOnClickListener(this::onClick);
        findViewById(R.id.btn_rm20).setOnClickListener(this::onClick);
        findViewById(R.id.btn_rm50).setOnClickListener(this::onClick);
        findViewById(R.id.btn_rm100).setOnClickListener(this::onClick);
        findViewById(R.id.btn_rm200).setOnClickListener(this::onClick);
        findViewById(R.id.btn_floating_action).setOnClickListener(this::onClick);
    }

    private void reloadWithdraw(){
        double amount = Double.parseDouble(et_amount.getText().toString().trim());
        double balance = Double.parseDouble(account.getBalance());

        if(mode == WITHDRAW) {
            if (amount > balance) {
                et_amount.setError(getString(R.string.insufficient_balance));
                return;
            }
            balance -= amount;
        }
        else if(mode == RELOAD){
            balance += amount;
        }
        updateBalance(balance, amount);
    }

    private void updateBalance(double finalBalance, double amount){
        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance()
                .getReference("user")
                .child(FirebaseAuth.getInstance().getUid())
                .child("balance")
                .setValue(String.format("%.2f",finalBalance))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String type = (mode == RELOAD ? Transaction.RELOAD : Transaction.WITHDRAW);
                        Transaction transaction = new Transaction(type, amount, type);

                        FirebaseDatabase.getInstance()
                                .getReference("transactions")
                                .child(FirebaseAuth.getInstance().getUid())
                                .push()
                                .setValue(transaction)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),
                                                (mode == RELOAD ?
                                                        getString(R.string.top_up_success)
                                                        :
                                                        getString(R.string.withdraw_success)),
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),
                                                e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backBtn:{
                onBackPressed();
                break;
            }
            case R.id.btn_rm10:{
                et_amount.setText("10");
                break;
            }
            case R.id.btn_rm20:{
                et_amount.setText("20");
                break;
            }
            case R.id.btn_rm50:{
                et_amount.setText("50");
                break;
            }
            case R.id.btn_rm100:{
                et_amount.setText("100");
                break;
            }
            case R.id.btn_rm200:{
                et_amount.setText("200");
                break;
            }
            case R.id.btn_floating_action:{
                if(et_amount.getText().toString().trim().isEmpty()){
                    et_amount.setError(getString(R.string.require_field));
                    break;
                }
                if(mode == WITHDRAW){
                    Intent intent = new Intent(ReloadWithdrawActivity.this, AuthActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_AUTHENTICATION_ACTIVITY);
                    break;
                }
                reloadWithdraw();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == REQUEST_CODE_AUTHENTICATION_ACTIVITY){
            if(resultCode == RESULT_OK){
                reloadWithdraw();
            }
        }
    }
}