package com.example.ProSudoku;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class About extends FragmentActivity{

    TabsPagerAdapter mTabsPagerAdapter;

    ViewPager mViewPager;
    ImageView imageViewAbout;
    LinearLayout infoLayout, aboutLayout;
    String[] tabs = { "School", "Tweets"};

    public void onCreate(Bundle savedInstanceState) {
	    Prefs.setSettings(this);
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);

        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        // Set up action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);

	    PagerTitleStrip pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
	    Prefs.setPagerTitleStripColor(this, pagerTitleStrip);

        infoLayout = (LinearLayout) findViewById(R.id.info_layout);
        aboutLayout = (LinearLayout) findViewById(R.id.about_layout);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();

        imageViewAbout = (ImageView)findViewById(R.id.imageViewAbout);
        imageViewAbout.getLayoutParams().width = (int)(metrics.widthPixels * 0.4);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class TabsPagerAdapter extends FragmentStatePagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case 0:
                    return new AboutInfoFragment();
                case 1:
                    return new AboutTwitterFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }

}
