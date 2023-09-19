package com.utar.client.ui.home.transfer;


import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.MainActivity;
import com.utar.client.MyApplication;
import com.utar.client.R;
import com.utar.client.ui.auth.AuthActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TransferOutFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "TransferOutFragment";
    private EditText et_amount;
    private static final int REQUEST_CODE_TRANSFER_OUT_ACTIVITY = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transfer_out, container, false);

        et_amount = v.findViewById(R.id.et_amount);

        v.findViewById(R.id.btn_rm10).setOnClickListener(this::onClick);
        v.findViewById(R.id.btn_rm20).setOnClickListener(this::onClick);
        v.findViewById(R.id.btn_rm50).setOnClickListener(this::onClick);
        v.findViewById(R.id.btn_rm100).setOnClickListener(this::onClick);
        v.findViewById(R.id.btn_rm200).setOnClickListener(this::onClick);
        v.findViewById(R.id.btn_floating_action).setOnClickListener(this::onClick);

        et_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = et_amount.getText().toString();
                if(!after.isEmpty()) {
                    if(MyApplication.getInstance().getAccount().getBalance() == null){
                        FirebaseDatabase.getInstance().getReference("user")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child("balance")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        double balance = Double.parseDouble(snapshot.getValue(String.class));
                                        if (Double.parseDouble(after) > balance) {
                                            et_amount.setError(getString(R.string.insufficient_balance));
                                        }
                                        else{
                                            et_amount.setError(null);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                    else {
                        if (Double.parseDouble(after) > Double.parseDouble(MyApplication.getInstance().getAccount().getBalance())) {
                            et_amount.setError(getString(R.string.insufficient_balance));
                        }
                        else{
                            et_amount.setError(null);
                        }
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
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

                String strAmount = et_amount.getText().toString().trim();
                if(strAmount.isEmpty()){
                    et_amount.setError(getString(R.string.require_field));
                    break;
                }

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
                if (nfcAdapter == null) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.alert))
                            .setMessage(getString(R.string.unavailable_nfc))
                            .setNegativeButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                }

                if (!nfcAdapter.isEnabled()) {
                    new AlertDialog.Builder(getContext())
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

                Log.i(TAG, "Amount: " + String.valueOf(Double.parseDouble(strAmount)));
                Log.i(TAG, "my balance: " + Double.parseDouble(MyApplication.getInstance().getAccount().getBalance()));
                Log.i(TAG, "compare: " + (Double.parseDouble(strAmount) < Double.parseDouble(MyApplication.getInstance().getAccount().getBalance())?"true":"false"));
                if(Double.parseDouble(strAmount) > Double.parseDouble(MyApplication.getInstance().getAccount().getBalance())){
                    et_amount.setError(getString(R.string.insufficient_balance));
                    break;
                }

                SharedPreferences pref = getActivity().getSharedPreferences("AmountPreference", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("amount", strAmount);
                editor.commit();

                Log.i(TAG, strAmount);

                Intent intent = new Intent(getContext(), AuthActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TRANSFER_OUT_ACTIVITY);
                break;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_TRANSFER_OUT_ACTIVITY){
            if(resultCode == RESULT_OK){
                Log.i("result", String.valueOf(RESULT_OK));
                startActivity(new Intent(getContext(), TransferOutActivity.class));
                getActivity().finish();
            }
        }
    }
}