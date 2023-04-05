package com.utar.client.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utar.client.R;
import com.utar.client.TransactionActivity;
import com.utar.client.cardemulation.HCEService;
import com.utar.client.data.*;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    TextView tv_amount, tv_name;
    ImageView iv_reload, iv_withdraw, iv_history;
    String userID;
    DatabaseReference databaseReference;
    DatabaseReference transactionDatabaseReference;
    Account account;

    View reloadWithdrawPanel;
    PopupWindow popupWindow;
    TextView popupTitle;
    EditText et_amount;
    Button btn_panel;
    ProgressBar progressBar;
    private static final String RELOAD = "Reload";
    private static final String WITHDRAW = "Withdraw";
    Intent serviceIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //serviceIntent = new Intent(getContext(), HCEService.class);
        //getActivity().stopService(serviceIntent);

        iv_reload = v.findViewById(R.id.home_iv_reload);
        iv_withdraw = v.findViewById(R.id.home_iv_withdraw);
        iv_history = v.findViewById(R.id.home_iv_history);
        tv_amount = v.findViewById(R.id.home_tv_balance);
        tv_name = v.findViewById(R.id.home_tv_name);

        String userID = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference("user").child(userID).keepSynced(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("user").child(userID);
        transactionDatabaseReference = FirebaseDatabase.getInstance().getReference("transactions").child(userID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                account = snapshot.getValue(Account.class);
                tv_amount.setText("RM " + account.getBalance());
                tv_name.setText("Welcome, " + account.getName());
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        iv_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), TransactionActivity.class));
            }
        });

        iv_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowInit(RELOAD);
                popupWindowShow();
            }
        });

        iv_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowInit(WITHDRAW);
                popupWindowShow();
            }
        });
        popupWindow = new PopupWindow(getContext());
        popupWindow.setFocusable(true);

        reloadWithdrawPanel = getLayoutInflater().inflate(R.layout.layout_reload_withdraw, container, false);
        popupTitle = reloadWithdrawPanel.findViewById(R.id.reload_withdraw_title);
        et_amount = reloadWithdrawPanel.findViewById(R.id.reload_withdraw_amount);
        btn_panel = reloadWithdrawPanel.findViewById(R.id.btn_reloadWithdraw);
        progressBar = reloadWithdrawPanel.findViewById(R.id.panel_progressBar);
        popupWindow.setContentView(reloadWithdrawPanel);

        return v;
    }


    private void popupWindowInit(String mode){
        popupTitle.setText(mode);
        btn_panel.setText(mode);
        et_amount.setText("");

        btn_panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_amount.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                double balance = Double.parseDouble(account.getBalance());
                double amount = Double.parseDouble(String.valueOf(et_amount.getText()));

                if(mode.equals(RELOAD)){
                    Log.i("Balance: ", String.valueOf(balance));
                    Log.i("adding amount", String.valueOf(amount));

                    String sum = String.format("%.2f",(balance + amount));
                    Log.i("sum", sum);

                    databaseReference.child("balance").setValue(sum)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressBar.setVisibility(View.GONE);
                                    Transaction transactionReload = new Transaction(RELOAD, amount, Transaction.RELOAD);
                                    transactionDatabaseReference.push().setValue(transactionReload);
                                    Toast.makeText(getContext(), "Reload Success", Toast.LENGTH_SHORT).show();
                                    popupWindow.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    popupWindow.dismiss();
                                }
                            });

                }
                else{
                    if(amount > balance){
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Insufficient Balance", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String finalValue = String.format("%.2f",(balance - amount));
                    databaseReference.child("balance").setValue(finalValue)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressBar.setVisibility(View.GONE);
                                    Transaction transactionWithdraw = new Transaction(WITHDRAW, amount, Transaction.WITHDRAW);
                                    transactionDatabaseReference.push().setValue(transactionWithdraw);
                                    Toast.makeText(getContext(), "Withdraw Success", Toast.LENGTH_SHORT).show();
                                    popupWindow.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    popupWindow.dismiss();
                                }
                            });
                }
            }
        });
    }

    private void popupWindowShow(){
        popupWindow.showAtLocation(reloadWithdrawPanel, Gravity.CENTER, 0, 0);
        popupWindow.update(0,0, (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.9), (int)(getContext().getResources().getDisplayMetrics().heightPixels * 0.5));
    }
}