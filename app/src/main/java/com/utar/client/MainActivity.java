package com.utar.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.data.Account;
import com.utar.client.ui.auth.AuthActivity;
import com.utar.client.card.HCEService;
import com.utar.client.ui.home.HomeFragment;
import com.utar.client.ui.payment.PaymentActivity;
import com.utar.client.ui.settings.SettingsFragment;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PAY_ACTIVITY = 2;
    private static final int REQUEST_CODE_AUTHENTICATION_ACTIVITY = 4;

    public static BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopService(new Intent(this, HCEService.class));
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        //first page
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();

        checkDeviceID();
    }

    //Bottom Navigation Set up
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()){
                case R.id.home: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();
                    break;
                }

                case R.id.payment: {
                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);

                    if (nfcAdapter == null) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.unavailable_nfc))
                                .setNegativeButton("OK", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        break;
                    }

                    if (!nfcAdapter.isEnabled()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.alert))
                                .setMessage(getString(R.string.nfc_enable_alert))
                                .setPositiveButton(getString(R.string.proceed_to_enable), (dialog, which) -> {
                                    startActivity(new Intent("android.settings.NFC_SETTINGS"));
                                })
                                .setNegativeButton(getString(R.string.cancel), null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        break;
                    }

                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_AUTHENTICATION_ACTIVITY);
                    break;
                }
                case R.id.settings: {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new SettingsFragment()).commit();
                    break;
                }
            }
            return true;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_PAY_ACTIVITY){
            if(resultCode == RESULT_OK){
                Log.i("result", String.valueOf(RESULT_OK));
                startActivity(new Intent(this, PaymentActivity.class));
            }
        }
        else if(requestCode == REQUEST_CODE_AUTHENTICATION_ACTIVITY){
            if(resultCode == RESULT_OK){
                Intent intent = new Intent(this, PaymentActivity.class);
                startActivity(intent);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getUid() == null){
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    private void checkDeviceID(){
        if(FirebaseAuth.getInstance().getUid() == null) return;

        FirebaseDatabase.getInstance()
                .getReference("user")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Account account = snapshot.getValue(Account.class);
                        if(account == null) return;

                        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        if(!account.getDeviceId().equals(deviceId)){
                            Log.i(TAG, deviceId);
                            FirebaseAuth.getInstance().signOut();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}