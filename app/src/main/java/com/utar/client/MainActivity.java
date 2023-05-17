package com.utar.client;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.utar.client.cardemulation.AccountAssistant;
import com.utar.client.cardemulation.HCEService;
import com.utar.client.fragment.*;



public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    FirebaseAuth auth;
    Button btnLogout;
    FirebaseUser user;

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

        Log.i(TAG, "HceService is started");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,
                        "com.utar.client.cardemulation.HCEService"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    //Bottom Navigation Set up
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()){
                case R.id.home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new HomeFragment()).commit();
                    break;

                case R.id.payment:
                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
                    if(nfcAdapter.isEnabled()) {
                        startActivity(new Intent(MainActivity.this, PaymentActivity.class));
                    }
                    else{
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("NFC is disabled")
                                .setMessage("Please enable NFC module")
                                .setPositiveButton("Proceed to enable", (dialog, which) -> {
                                    startActivity(new Intent("android.settings.NFC_SETTINGS"));
                                })
                                .setNegativeButton("Cancel", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        ((BottomNavigationView) findViewById(R.id.bottomNavigationView)).getMenu().findItem(R.id.home).setChecked(true);
                    }
                    break;

                case R.id.settings:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new SettingsFragment()).commit();
                    break;
            }
            return true;
        }
    };


}