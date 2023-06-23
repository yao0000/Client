package com.utar.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.cardemulation.HCEService;
import com.utar.client.data.Account;
import com.utar.client.ui.settings.LanguageActivity;

import java.util.Locale;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginFragment";
    private final int NFC_PERMISSION_REQUEST_CODE = 1;
    private NfcAdapter nfcAdapter;

    EditText editTextEmail, editTextPassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "HceService is disabled in the entry point of application");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        "com.utar.client.cardemulation.HCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Log.i(TAG, "HceService is disabled");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        "com.utar.client.cardemulation.HCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //nfcPermissionCheck();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        String lang = sharedPreferences.getString("language", "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.login_email);
        editTextPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.btn_login);

        progressBar = findViewById(R.id.login_progressBar);

        findViewById(R.id.iv_lang).setOnClickListener(v -> startActivity(new Intent(this, LanguageActivity.class)));

        findViewById(R.id.login_tv_register).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), Register.class)));

        findViewById(R.id.login_tv_forgotPassword).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText().toString());

                if(email.trim().isEmpty()){
                    editTextEmail.setError(getString(R.string.require_field));
                    return;
                }

                if(!Register.isEmailFormatValid(email.trim())){
                    editTextEmail.setError(getString(R.string.invalid_email));
                    return;
                }

                if(password.trim().isEmpty()){
                    editTextPassword.setError(getString(R.string.require_field));
                    return;
                }

                if(password.length() < 6){
                    editTextPassword.setError(getString(R.string.minimum_password_length));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().getUid())
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    Account account = snapshot.getValue(Account.class);

                                                    if(!account.getRole().equals(Account.FIX_CLIENT)){
                                                        Toast.makeText(getApplicationContext(), getString(R.string.invalid_account), Toast.LENGTH_SHORT).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                    }
                                                    else{
                                                        Toast.makeText(Login.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                                                        MyApplication.getInstance().firebaseUserUpdate();
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(Login.this, getString(R.string.invalid_account),
                                            Toast.LENGTH_SHORT).show();

                                }
                            }

                        });
            }
        });
    }

}