package com.example.xiaomi.schedule;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 6;
    private String tabTitles[];
    private Context context;

    SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        tabTitles = new String[]{
                context.getString(R.string.MondayShort),
                context.getString(R.string.TuesdayShort),
                context.getString(R.string.WednesdayShort),
                context.getString(R.string.ThursdayShort),
                context.getString(R.string.FridayShort),
                context.getString(R.string.SaturdayShort)
        };
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position];
    }
}
