package com.utar.client.ui.home.transaction;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.utar.client.R;
import com.utar.client.MyApplication;
import com.utar.client.data.Transaction;
import com.utar.client.ui.home.TransactionActivity;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

    private TextView tv_object, tv_amount, tv_date, tv_type;
    private final int displayHeight = MyApplication.getInstance().getDisplayHeight();
    private final int displayWidth = MyApplication.getInstance().getDisplayWidth();

    private TransactionActivity app;

    public TransactionViewHolder(View itemView) {
        super(itemView);

        tv_object = itemView.findViewById(R.id.tv_object);
        tv_amount = itemView.findViewById(R.id.tv_amount);
        tv_date = itemView.findViewById(R.id.tv_date);
        tv_type = itemView.findViewById(R.id.tv_type);
    }

    public void bind(Transaction transaction) {
        app = TransactionActivity.getInstance();
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

        String type = transaction.getType();
        String typeDisplay = "";

        if (type.equals(Transaction.PAYMENT)) {
            typeDisplay = app.getIdString(R.string.payment);
        }
        else if (type.equals(Transaction.TRANSFER_OUT)) {
            typeDisplay = app.getIdString(R.string.transfer_out);
        }
        else if (type.equals(Transaction.RELOAD)) {
            typeDisplay = app.getIdString(R.string.reload);
        }
        else if (type.equals(Transaction.WITHDRAW)) {
            typeDisplay = app.getIdString(R.string.withdraw);
        }
        else if (type.equals(Transaction.TRANSFER_IN)) {
            typeDisplay = app.getIdString(R.string.transfer_in);
        }
        else if (type.equals(Transaction.PAYMENT_RECEIVE)) {
            typeDisplay = app.getIdString(R.string.payment_receive);
        }

        tv_type.setText(typeDisplay);
        tv_type.setTextSize(displayWidth * displayHeight / 100000 - 8);
        tv_type.setTextColor(Color.BLACK);
    }
}
