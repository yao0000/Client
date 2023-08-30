package com.utar.client.ui.home.transaction;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.utar.client.R;
import com.utar.client.data.Transaction;
import com.utar.client.ui.home.TransactionActivity;

import java.util.ArrayList;
import java.util.List;

public class FilterTransactionType implements View.OnClickListener {

    private Context context;

    private PopupWindow popupWindow, filterWindows;
    private CheckBox cb_reload, cb_withdraw, cb_paymentReceive;
    private View view;
    private List<CheckBox> checkBoxList;
    private FilterTypeListener mListener;

    public interface FilterTypeListener{
        List getCurrentDisplayList();
        void displayFilteredList(List list);
    }

    public FilterTransactionType(Context context, PopupWindow filterWindows) {
        this.context = context;
        this.filterWindows = filterWindows;
        this.checkBoxList = new ArrayList<>();
        this.mListener = (FilterTypeListener) context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popup_transaction_type, null);

        cb_reload = view.findViewById(R.id.cb_reload);
        cb_withdraw = view.findViewById(R.id.cb_withdraw);
        cb_paymentReceive = view.findViewById(R.id.cb_payment_receive);

        checkBoxList.add(cb_reload);
        checkBoxList.add(cb_withdraw);
        checkBoxList.add(cb_paymentReceive);

        view.findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this::onClick);
        view.findViewById(R.id.btn_reset).setOnClickListener(this::onClick);
        view.findViewById(R.id.btn_done).setOnClickListener(this::onClick);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn: {
                popupWindow.dismiss();
                break;
            }
            case R.id.btn_cancel: {
                popupWindow.dismiss();
                filterWindows.dismiss();
                break;
            }
            case R.id.btn_reset: {
                for (int i = 0; i < checkBoxList.size(); i++) {
                    checkBoxList.get(i).setChecked(false);
                }
                break;
            }
            case R.id.btn_done: {
                int count = 0;
                for (int i = 0; i < checkBoxList.size(); i++) {
                    if (checkBoxList.get(i).isChecked()) {
                        count++;
                    }
                }
                if (count == 0) {
                    break;
                }

                List<Transaction> transactionList = mListener.getCurrentDisplayList();
                List<Transaction> filteredList = new ArrayList<>();

                for(Transaction transaction : transactionList){
                    if(!cb_reload.isChecked() && transaction.getType().equals(Transaction.RELOAD)){
                        continue;
                    }
                    else if (!cb_withdraw.isChecked() && transaction.getType().equals(Transaction.WITHDRAW)) {
                        continue;
                    }
                    else if(!cb_paymentReceive.isChecked() && transaction.getType().equals(Transaction.PAYMENT_RECEIVE)){
                        continue;
                    }
                    filteredList.add(0, transaction);
                }

                if(filteredList.isEmpty()){
                    Toast.makeText(context, context.getString(R.string.noRecord), Toast.LENGTH_SHORT).show();
                    break;
                }
                mListener.displayFilteredList(filteredList);
                popupWindow.dismiss();
                filterWindows.dismiss();
                break;
            }
        }
    }
}
