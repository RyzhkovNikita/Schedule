package com.example.xiaomi.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.TabLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    Toolbar mActionBarToolbar;
    final String HTML_ADDRESS = "HTML_ADDRESS";
    String html;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabbedlayout);
        layoutInitialization();
        html = "";
        SharedPreferences sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
        if (sharedPreferences.contains(HTML_ADDRESS))
            html = sharedPreferences.getString(HTML_ADDRESS, "");
        if (html.equals("")) {
            Intent intent = new Intent(this, GroupsActivity.class);
            startActivityForResult(intent, 1);
        } else {
            new Thread(new DBLoader()).start();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new DBRefresher()).start();
        SharedPreferences sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
        if (sharedPreferences.contains("group")) {
            mActionBarToolbar.setTitle(sharedPreferences.getString("group", ""));
        }
        setWeekAndWeekName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            html = data.getStringExtra(HTML_ADDRESS);
            SharedPreferences sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putString(HTML_ADDRESS, html);
            ed.apply();
            Thread refresh = new Thread(new DBRefresher());
            Thread refreshVP = new Thread(() -> {
                MainActivity.this.runOnUiThread(() -> {
                    int position = viewPager.getCurrentItem();
                    viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));
                    viewPager.setCurrentItem(position);
                });
            });
            refresh.start();
            try {
                refresh.join();
            } catch (InterruptedException ignored) {
            }
            refreshVP.start();
        }
    }

    class DBLoader implements Runnable {
        @Override

        public void run() {
            DBHelper dbHelper = new DBHelper(MainActivity.this);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.close();
        }
    }

    class DBRefresher implements Runnable {
        @Override
        public void run() {
            DBHelper dbHelper = new DBHelper(MainActivity.this);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            dbHelper.refreshTable(database);
            database.close();
            switch (dbHelper.LastOperationResult()) {
                case OK:
                    MainActivity.this.runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, R.string.successful_operation, Toast.LENGTH_SHORT).show());
                    break;
                case NO_INTERNET:
                    MainActivity.this.runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, R.string.failed_operation, Toast.LENGTH_SHORT).show());

                    break;
                case NO_SCHEDULE:
                    MainActivity.this.runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, R.string.no_schedule, Toast.LENGTH_SHORT).show());
                    break;
                case EXCEPTION:
                    MainActivity.this.runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show());
                    break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Thread refreshThread = new Thread(()->{
                    hideViewPagerAndButton();
                    new DBRefresher().run();
                    showViewPagerAndButton();
                });
                refreshThread.start();
                break;
            case R.id.action_grouplist:
                Intent intent = new Intent(this, GroupsActivity.class);
                startActivityForResult(intent, 1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void layoutInitialization() {

        mActionBarToolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);


        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.sliding_tabs);

        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));
        tabLayout.setupWithViewPager(viewPager);

        //считаем сегодняшний день и присваиваем адаптеру
        int day = getCurrentDayIndex();
        viewPager.setCurrentItem(day - 2);

        viewPager.setVisibility(View.VISIBLE);

    }

    public void setWeekAndWeekName() {
        String daysOfWeek[] = new String[]{
                getString(R.string.Sunday),
                getString(R.string.Monday),
                getString(R.string.Tuesday),
                getString(R.string.Wednesday),
                getString(R.string.Thursday),
                getString(R.string.Friday),
                getString(R.string.Saturday)
        };

        TextView weekCounter = findViewById(R.id.weekTV);
        TextView weekName = findViewById(R.id.weekNameTV);

        String weekCountStr = "";
        SharedPreferences sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
        if (sharedPreferences.contains("weekCount")) {
            weekCountStr = sharedPreferences.getString("weekCount", "") + ", ";
            weekCounter.setText(weekCountStr);
        }

        String weekNameStr = "";
        if (sharedPreferences.contains("weekName")){
            weekNameStr = sharedPreferences.getString("weekName", "");
            weekName.setText(weekNameStr);
        }

        if (weekNameStr.equals(getString(R.string.Znam))) {
            weekName.setTextColor(getResources().getColor(R.color.paraBlue));
        } else {
            weekName.setTextColor(getResources().getColor(R.color.paraGreen));
        }

        String currentDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date());

        //считаем текущую дату

        int day = getCurrentDayIndex();
        currentDate = daysOfWeek[day - 1] + " " + currentDate;

        TextView dateTV = findViewById(R.id.dateTV);
        dateTV.setText(currentDate);
    }

    public int getCurrentDayIndex() {
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public void hideViewPagerAndButton() {
        MainActivity.this.runOnUiThread(() -> {
            viewPager.setVisibility(View.INVISIBLE);
            mActionBarToolbar.getMenu().getItem(1).setVisible(false);
        });
    }

    public void showViewPagerAndButton() {
        MainActivity.this.runOnUiThread(() -> {
            int position = viewPager.getCurrentItem();
            setWeekAndWeekName();
            viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));
            viewPager.setCurrentItem(position);
            viewPager.setVisibility(View.VISIBLE);
            mActionBarToolbar.getMenu().getItem(1).setVisible(true);
        });
    }
}

