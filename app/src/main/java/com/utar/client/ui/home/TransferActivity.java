package com.utar.client.ui.home;


import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.tabs.TabLayout;
import com.utar.client.R;
import com.utar.client.ui.home.transfer.TransferPagerAdapter;

public class TransferActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private TransferPagerAdapter transferPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        findViewById(R.id.backBtn).setOnClickListener(v -> onBackPressed());
        tabLayout = findViewById(R.id.transfer_tabL_layout);
        viewPager2 = findViewById(R.id.view_pager);
        transferPagerAdapter = new TransferPagerAdapter(this);
        viewPager2.setAdapter(transferPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setVisibility(View.VISIBLE);
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager2.setVisibility(View.VISIBLE);
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                    case 1:
                        tabLayout.getTabAt(position).select();
                }
                super.onPageSelected(position);
            }
        });
    }


}