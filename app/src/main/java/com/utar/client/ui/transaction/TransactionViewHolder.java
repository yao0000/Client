package com.utar.client.ui.transaction;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.utar.client.R;
import com.utar.client.data.MyApplication;
import com.utar.client.data.Transaction;
import com.utar.client.ui.TransactionActivity;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

    private TextView tv_object, tv_amount, tv_date, tv_type;
    private final int displayHeight = MyApplication.getInstance().getDisplayHeight();
    private final int displayWidth = MyApplication.getInstance().getDisplayWidth();

    private final MyApplication app = MyApplication.getInstance();

    public TransactionViewHolder(View itemView) {
        super(itemView);

        tv_object = itemView.findViewById(R.id.tv_object);
        tv_amount = itemView.findViewById(R.id.tv_amount);
        tv_date = itemView.findViewById(R.id.tv_date);
        tv_type = itemView.findViewById(R.id.tv_type);
    }

    public void bind(Transaction transaction) {
        tv_object.setText(transaction.getObjectName());
        tv_object.setTextSize(displayWidth * displayHeight / 100000 - 5);

        tv_amount.setText(String.valueOf(transaction.getAmount()));
        tv_amount.setTextSize(displayWidth * displayHeight / 100000 - 5);
        if (transaction.getType().equals(Transaction.PAYMENT) || transaction.getType().equals(Transaction.TRANSFER_OUT) || transaction.getType().equals(Transaction.WITHDRAW)) {
            tv_amount.setText(String.format("- RM%.2f", transaction.getAmount()));
            tv_amount.setTextColor(app.getResources().getColor(R.color.dark_red));
        } else {
            tv_amount.setText(String.format("RM%.2f", transaction.getAmount()));
            tv_amount.setTextColor(app.getResources().getColor(R.color.dark_green));
        }

        tv_date.setText(transaction.getTime());
        tv_date.setTextSize(displayWidth * displayHeight / 100000 - 8);
        tv_date.setTextColor(Color.BLACK);

        tv_type.setText(transaction.getType());
        tv_type.setTextSize(displayWidth * displayHeight / 100000 - 8);
        tv_type.setTextColor(Color.BLACK);
    }
}
