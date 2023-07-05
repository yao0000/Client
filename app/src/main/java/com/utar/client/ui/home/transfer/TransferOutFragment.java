package com.utar.client.ui.home.transfer;


import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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
        /*et_amount.setOnClickListener(v -> {
            new NumberKeyboard(ReloadWithdrawActivity.this, et_amount).show();
        });*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            et_amount.setShowSoftInputOnFocus(false);
        }
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
                    if (Double.parseDouble(after) > Double.parseDouble(MyApplication.getInstance().getAccount().getBalance())) {
                        et_amount.setError(getString(R.string.insufficient_balance));
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