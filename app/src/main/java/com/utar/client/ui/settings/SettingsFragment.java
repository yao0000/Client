package com.utar.client.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utar.client.Login;
import com.utar.client.R;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {
    TextView tv_logout;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        String userID = FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user").child(userID).child("transactions");

        tv_logout = v.findViewById(R.id.setting_tv_logout);
        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), Login.class);
                startActivity(intent);
                Toast.makeText(getContext(), "Log out successfully", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });

        v.findViewById(R.id.tv_language).setOnClickListener(event ->
                startActivity(new Intent(getActivity(), LanguageActivity.class)));
        return v;
    }
}