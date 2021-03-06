package com.group5.atoms;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    //members
    FirebaseUser currentUser;
    static CalendarFragment calendarFragment;
    int currentTimeFrame;
    Date dateChosen;
    private static Long calendarId;
    FragmentManager fragmentManager;
    public static Boolean switchPref;
    SharedPreferences sharedPref;
    public static String dateSwitchPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set the default values for the preference manager
        android.support.v7.preference.PreferenceManager
                .setDefaultValues(this, R.xml.preferences, false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set the firebase user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //change the nav header contents
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderDN = headerView.findViewById(R.id.nav_header_display_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        ImageView navHeaderImage = headerView.findViewById(R.id.nav_header_image);

        if (navHeaderDN != null) {
            navHeaderDN.setText(currentUser.getDisplayName());
        }

        if (navHeaderEmail != null) {
            navHeaderEmail.setText(currentUser.getEmail());
        }

        if (navHeaderImage != null) {
            // show The Image in a ImageView
            Picasso.with(this).setLoggingEnabled(true);
            System.out.println(currentUser.getPhotoUrl());
            Picasso.with(this).load(currentUser.getPhotoUrl()).transform(new PicassoCircleTransformation()).into(navHeaderImage);
        }

        //get the shared preferences
        sharedPref = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);

        //get the switch preference
        switchPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_EXAMPLE_SWITCH, false);

        //get the date format preference
        dateSwitchPref = sharedPref.getString(SettingsActivity.DATE_PREFERENCE_KEY, "MM/dd/yyyy");

        //swap the fragment layout with the calendar fragment
        calendarFragment = new CalendarFragment();

        //set the calendar fragment to be the fragment that opens with the app
        setFragment(calendarFragment);

        //get a reference to the fab button
        FloatingActionButton fab = findViewById(R.id.fab);

        //set the on click listener for the floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start the addEventActivity
                Intent intent = new Intent(getApplicationContext(), AddEventActivity.class);
                startActivity(intent);
            }
        });

        //set the view offset and timeframe
        this.currentTimeFrame = 0;

    }

    //method to set the fragment displayed in main activity
    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getClass().getName());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //if the settings option is clicked in the menu start the settings fragment
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        //else if the logout option is selected, log the user out of the application
        else if(id == R.id.action_logout) {
            Intent intentToLogin = new Intent(this, LoginActivity.class);
            //sign out
            FirebaseAuth.getInstance().signOut();

            this.startActivity(intentToLogin);
        }
        //if the date picker option is selected, show the datepicker to choose the current date
        else if (id == R.id.action_datepicker) {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog;

            if (dateChosen != null) {

                calendar.setTime(dateChosen);

                datePickerDialog = new DatePickerDialog(
                        MainActivity.this, MainActivity.this,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            }
            else {
                datePickerDialog = new DatePickerDialog(
                        MainActivity.this, MainActivity.this,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            }

            datePickerDialog.show();
        }

        //return the super method result
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        setFragment(calendarFragment);

        if (calendarFragment.isVisible()) {
            if (id == R.id.nav_daily_view) {
                this.currentTimeFrame = 0;
                calendarFragment.readEvents(0, null);
            } else if (id == R.id.nav_weekly_view) {
                this.currentTimeFrame = 1;
                calendarFragment.readEvents(1, null);
            } else if (id == R.id.nav_monthly_view) {
                this.currentTimeFrame = 2;
                calendarFragment.readEvents(2, null);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendarFragment.readEvents(this.currentTimeFrame, new GregorianCalendar(year, month, dayOfMonth).getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        this.dateChosen = calendar.getTime();
    }

    public static Long getCalendarId() {
        return calendarId;
    }

    public static void setCalendarId(Long calendarId) {
        MainActivity.calendarId = calendarId;
    }
}
