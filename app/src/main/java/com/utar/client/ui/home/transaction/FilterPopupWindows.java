package com.utar.client.ui.home.transaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.utar.client.R;


public class FilterPopupWindows implements View.OnClickListener {

    private Context context;
    private Activity activity;

    private View view;
    private PopupWindow popupWindow;

    public FilterPopupWindows(Context context, LinearLayout mainLayout) {
        this.context = context;
        this.activity = (Activity) context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popup_transaction_filter, null);

        view.findViewById(R.id.backBtn).setOnClickListener(this::onClick);
        view.findViewById(R.id.tv_period).setOnClickListener(this::onClick);
        view.findViewById(R.id.tv_reset).setOnClickListener(this::onClick);


        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        mainLayout.setBackgroundColor(Color.GRAY);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mainLayout.setBackgroundColor(Color.WHITE);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backBtn:{
                activity.onBackPressed();
                break;
            }
            case R.id.tv_period:{
                new FilterTransactionType(context, popupWindow);
                break;
            }
            case R.id.tv_reset:{

            }
        }
    }
}
