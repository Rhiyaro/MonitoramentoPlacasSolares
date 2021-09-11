package com.example.monitoramentoplacassolares.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private String[] titles;
    private Fragment[] fragments;

    public MyFragmentPagerAdapter(FragmentManager fm, String[] titles, Fragment[] fragments) {
        super(fm);
        this.titles = titles;
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
            return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
