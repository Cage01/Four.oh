package zerotek.gradecalculator;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import zerotek.gradecalculator.Other.Advertisements;
import zerotek.gradecalculator.Adapters.SemesterViewAdapter;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Other.DrawerOptions;
import zerotek.gradecalculator.Other.Tutorials;

//todo add advertisement on this screen

public class SemesterList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public ListView list;
    Database database;
    Cursor res;
    SemesterViewAdapter semesterViewAdapter;
    ArrayList<String> semesterList, startDateList, endDateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(SemesterList.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_semester_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(database.getSystem() + " List");
        setSupportActionBar(toolbar);

        Advertisements.bannerAd(SemesterList.this);

        //Side menu drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.class_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem system = menu.findItem(R.id.change_semester);
        system.setTitle("Change " + database.getSystem() + "s");

        if (database.getSemesterList() != 1) {
            Tutorials.semesterList(SemesterList.this);
            database.insertSemesterList(1);
        }


        semesterList = new ArrayList<String>();
        startDateList = new ArrayList<String>();
        endDateList = new ArrayList<String>();

        String dateFormat = "MMM dd yyyy";
        String changedStartDate, changedEndDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
        SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (list == null) {
            list = (ListView) findViewById(R.id.semester_listview);
        }

        semesterViewAdapter = new SemesterViewAdapter(this, semesterList, startDateList, endDateList);
        list.setAdapter(semesterViewAdapter);
        setListAdapter(semesterViewAdapter);

        semesterViewAdapter.setMode(Attributes.Mode.Single);

        res = database.getSemesters();
        if (res.getCount() != 0) {
            res.moveToFirst();

            do {
                try {
                    Date date = databaseFormat.parse(res.getString(2));
                    Date date2 = databaseFormat.parse(res.getString(3));
                    changedStartDate = simpleDateFormat.format(date);
                    changedEndDate = simpleDateFormat.format(date2);
                } catch (ParseException e) {
                    changedStartDate = res.getString(2);
                    changedEndDate = res.getString(3);
                }


                addItems(null, res.getString(1), changedStartDate, changedEndDate); //adds items to list by name

                //takes the data from the database to display it on the screen as a string
            } while (res.moveToNext());


        } else {
            TextView noSemester = (TextView) findViewById(R.id.no_semesters);
            noSemester.setText("No " + database.getSystem() + "s");
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SemesterList.this, GradeActivity.class);
                intent.putExtra("semesterID", database.getSemesterID(semesterList.get(position)));//sends the ID of the class

                startActivity(intent);
            }
        });


        final com.github.clans.fab.FloatingActionButton fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_semester);
        fab.show(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SemesterList.this, AddSemester.class);
                intent.putExtra("changingSemester", true);
                startActivity(intent);

            }
        });

        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            int last_item;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (last_item < firstVisibleItem) {
                    //System.out.println("List is scrolling upwards");
                    fab.hide(true);

                } else if (last_item > firstVisibleItem) {
                    //  System.out.println("List is scrolling downwards");
                    fab.show(true);

                } else if (last_item == firstVisibleItem) fab.show(true);
                last_item = firstVisibleItem;
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (database.numOfSemesters() == 0) {
            Intent intent1 = new Intent(this, AddSemester.class);
            startActivity(intent1);
        } else {
            Intent intent2 = new Intent(this, GradeActivity.class);
            startActivity(intent2);
        }
    }

    public void addItems(View v, String name, String startDate, String endDate) {
        semesterList.add(name);
        startDateList.add(startDate);
        endDateList.add(endDate);
    }

    protected ListView getListView() {
        if (list == null) {
            list = (ListView) findViewById(R.id.semester_listview);
        }
        return list;
    }

    protected ListAdapter getListAdapter() {
        ListAdapter adapter = getListView().getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        } else {
            return adapter;
        }
    }

    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.class_drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
        }


        int id = item.getItemId();

        if (id == R.id.home) {
            if(database.numOfSemesters() > 0) {
                Intent intent = new Intent(SemesterList.this, GradeActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(SemesterList.this, AddSemester.class);
                startActivity(intent);
            }
        } else if (id == R.id.grade_calculator) {
            Intent intent = new Intent(SemesterList.this, GradeCalculator.class);
            startActivity(intent);

        } else if (id == R.id.upcoming_assignments) {
            Intent intent = new Intent(SemesterList.this, UpcomingWork.class);
            intent.putExtra("appCreated", true);
//            intent.putExtra("semesterID", database.getCurrentSemesterDate());
            startActivity(intent);

        } else if (id == R.id.change_semester) {


        } else if (id == R.id.contact_dev) {
            DrawerOptions.contactDeveloper(SemesterList.this);

        } else if (id == R.id.report_bug) {
            DrawerOptions.bugReport(SemesterList.this);

        } else if (id == R.id.newsletter_signup) {
            DrawerOptions.newsletterSignup(SemesterList.this);
        } else if (id == R.id.facebook) {
            DrawerOptions.getOpenFacebookIntent(SemesterList.this);
        }


        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


}

