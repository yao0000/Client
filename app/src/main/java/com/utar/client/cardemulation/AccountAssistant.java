package com.utar.client.cardemulation;

import java.lang.ref.WeakReference;

public class AccountAssistant {

    public static WeakReference<AccountCallback> mAccountCallback;
    public AccountAssistant(AccountCallback accountCallback){
        mAccountCallback = new WeakReference<AccountCallback>(accountCallback);
    }

    public interface AccountCallback{
        public void setStatusText(int resId);
        public void setStatusText(int resId, String appendMsg);
        public void setAnimation(int rawRes, boolean repeat);
        public void countDownFinish();
    }
}
