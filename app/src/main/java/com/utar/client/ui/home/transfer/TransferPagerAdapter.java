package com.utar.client.ui.home.transfer;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TransferPagerAdapter extends FragmentStateAdapter {

    public TransferPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1: return new ReceiveTransferFragment();
            case 0:
            default: return new TransferOutFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
