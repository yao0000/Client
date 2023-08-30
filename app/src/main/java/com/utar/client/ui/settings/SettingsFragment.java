package com.utar.client.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.utar.client.Login;
import com.utar.client.R;

import com.google.firebase.auth.FirebaseAuth;
import com.utar.client.ui.auth.RegisterPinActivity;

import java.util.Set;

public class SettingsFragment extends Fragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        v.findViewById(R.id.setting_tv_logout).setOnClickListener(this::onClick);
        v.findViewById(R.id.tv_language).setOnClickListener(this::onClick);
        v.findViewById(R.id.tv_change_pin).setOnClickListener(this::onClick);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_language:{
                startActivity(new Intent(getActivity(), LanguageActivity.class));
                break;
            }
            case R.id.tv_change_pin:{
                startActivity(new Intent(getActivity(), RegisterPinActivity.class));
                break;
            }
            case R.id.setting_tv_logout:{

                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.log_out))
                        .setMessage(getString(R.string.log_out) + "?")
                        .setPositiveButton("OK", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getContext(), Login.class);
                            startActivity(intent);
                            Toast.makeText(getContext(), getString(R.string.log_out_success), Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            }

        }
    }
}