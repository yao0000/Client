package com.utar.client;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    ProgressBar progressBar;
    EditText et_email;
    TextView tv_login;
    Button btn_submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        progressBar = findViewById(R.id.forgot_progressBar);
        et_email = findViewById(R.id.forgot_email);
        btn_submit = findViewById(R.id.forgot_btnSubmit);
        tv_login = findViewById(R.id.forgot_loginNow);

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(et_email.getText());

                if(email.trim().isEmpty()){
                    et_email.setError("This field cannot be blank");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {


                            @Override
                            public void onComplete(Task<Void> task) {
                                if(task.isSuccessful()){
                                    progressBar.setVisibility(View.GONE);
                                    toast(getString(R.string.reset_password_email_sent));
                                    finish();
                                }
                                else{
                                    progressBar.setVisibility(View.GONE);
                                    toast(getString(R.string.err) + ": " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}