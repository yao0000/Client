package com.utar.client.ui.home.transfer;

import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.utar.client.R;
import com.utar.client.cardemulation.TransferNfcReader;

public class ReceiveTransferFragment extends Fragment implements View.OnClickListener, TransferNfcReader.AccountCallback {

    private static final String TAG = "ReceiveTransferFragment";
    private TransferNfcReader transferNfcReader;
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private LottieAnimationView animationView;
    private TextView tv_nfc_status;
    private CountDownTimer resetTimer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive_transfer, container, false);
        transferNfcReader = new TransferNfcReader(this);

        animationView = view.findViewById(R.id.animation);
        tv_nfc_status = view.findViewById(R.id.nfc_status);

        resetTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                animationView.setAnimation(R.raw.nfc_tap);
                animationView.loop(true);
                animationView.playAnimation();

                tv_nfc_status.setText(getString(R.string.wait_nfc_device));
            }
        };

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "Disabling reader mode");

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(getContext());
        if(nfc != null){
            nfc.disableReaderMode(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Enabling reader mode");

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(getContext());
        if(nfc != null){
            nfc.enableReaderMode(getActivity(), transferNfcReader, READER_FLAGS, null);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backBtn:{
                getActivity().onBackPressed();
                break;
            }
        }
    }

    @Override
    public void setStatusText(int id) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_nfc_status.setText(getString(id));
            }
        });
    }

    @Override
    public void setAnimation(int rawRes, boolean repeat) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animationView.setAnimation(rawRes);
                animationView.loop(repeat);
                animationView.playAnimation();
            }
        });
    }

    @Override
    public void countDownFinish() {
        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                getActivity().finish();
            }
        };

        countDownTimer.start();
    }

    @Override
    public void countDownReset() {
        resetTimer.cancel();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                resetTimer.start();
            }
        });
    }
}