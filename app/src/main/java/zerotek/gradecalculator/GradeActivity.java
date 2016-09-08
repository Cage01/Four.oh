package zerotek.gradecalculator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;

import zerotek.gradecalculator.Adapters.ClassViewAdapter;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Other.Advertisements;
import zerotek.gradecalculator.Other.DrawerOptions;
import zerotek.gradecalculator.Other.ExtraDialogs;
import zerotek.gradecalculator.Other.Tutorials;

//todo add assignment due checker here like in upcoming work
//todo add advertisement on this screen
public class GradeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Cursor res;
    public ListView list;
    public ArrayList<String> classList;
    Database database;
    ClassViewAdapter classViewAdapter;
    Integer semesterID;
    LinearLayout classText;
    TextView noClasses;
    int exit = 0;
    private com.github.clans.fab.FloatingActionMenu fabMenu;
    private com.github.clans.fab.FloatingActionButton fab, fab2;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        database = new Database(this);

        if (database.getHomeScreen() != 1) {
            Tutorials.homeScreen(GradeActivity.this);
            database.insertHome(1);
        }


        noClasses = (TextView) findViewById(R.id.no_classes);
        classText = (LinearLayout) findViewById(R.id.list_layout);
        classList = new ArrayList<String>();

        if (list == null) {
            list = (ListView) findViewById(R.id.class_list);
        }

        Advertisements.bannerAd(GradeActivity.this);


//floating action buttons -----------

        fabMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.menu_float);
        fabMenu.showMenu(false);
        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabb);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weightClass();

            }
        });

        fab2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pointClass();

            }
        });


        try {

            try {
                //tries to get the variable passed through activities, and if it cant grab it, it checks for a current semester, then for future or past semesters to run
                res = database.getChosenClasses(getIntent().getExtras().getInt("semesterID"));
                semesterID = getIntent().getExtras().getInt("semesterID");

            } catch (NullPointerException e) {


                try {
                    //gets the current semester bassed on date, if today date doesnt fit any semesters in the database it jumps down to the catch scenario's
                    res = database.getCurrentClasses();
                    semesterID = database.getCurrentSemesterDate();
                } catch (CursorIndexOutOfBoundsException ee) {

                    //checks if there's a semester created for the future
                    try {
                        Cursor nextSemester = database.getNextSemester();
                        nextSemester.moveToFirst();

                        res = database.getChosenClasses(nextSemester.getInt(0));

                        //if there is an existing previous semester and an existing future semester but no current one
                        if (database.checkForPreviousSemester() && database.checkForNextSemester()) {
                            Cursor cursor = database.getPreviousOrNextSemester();
                            cursor.moveToLast();
                            //runs the dialog and sets the classes show approriately
                            moveToNextSemester(nextSemester.getInt(0), cursor.getInt(0));
                        } else {
                            //if there is a next semester just set the ID to it and run the dialog
                            //there should never be an instance where there is no previous or future or current semester, otherwise AddSemester will run
                            if (database.checkForNextSemester())
                                semesterID = nextSemester.getInt(0);
                            noPreviousSemester();
                        }

                    } catch (CursorIndexOutOfBoundsException eee) {
                        //if not it will ask you to create a new semester, or to stay and edit the one you're on still

                        Cursor semesterRes = database.getPreviousOrNextSemester();
                        semesterRes.moveToLast();

                        semesterID = semesterRes.getInt(0);
                        res = database.getChosenClasses(semesterID);

                        allSemestersEnded();
                    }
                }
            }


//once the correct semester is chosen, it will build the list of classes here, or display the correct message if there are none
            try {
                if (res.getCount() != 0) {
                    res.moveToFirst();

                    do {
                        addItems(null, res.getString(1)); //adds items to list by name
                        //takes the data from the database to display it on the screen as a string
                    } while (res.moveToNext());

                } else noClasses.setText("No Classes");
            } catch (NullPointerException e) {
            }
        } catch (CursorIndexOutOfBoundsException e) {
            noClasses.setText("No Classes");
        }
        try {
            classViewAdapter = new ClassViewAdapter(this, classList, null, null, semesterID);
        }catch (NullPointerException e){
            Cursor cursor = database.getSemesters();
            cursor.moveToFirst();

            semesterID = cursor.getInt(0);
            classViewAdapter = new ClassViewAdapter(this, classList, null, null, semesterID);

        }

        list.setAdapter(classViewAdapter);
        setListAdapter(classViewAdapter);

        classViewAdapter.setMode(Attributes.Mode.Single);

        try {
            Cursor cursor = database.getTodaysWork();
            cursor.moveToFirst();
            do {
                ExtraDialogs.assignmentDue(GradeActivity.this, cursor.getInt(0), cursor.getInt(5), cursor.getInt(6), semesterID);
            } while (cursor.moveToNext());
        } catch (CursorIndexOutOfBoundsException e) {
        }


        /** ----------------------ACTION BAR INITIALIZATION------------------------*/
        try {
            toolbar.setTitle(database.getSemesterName(semesterID) + "'s Classes");
        }catch (CursorIndexOutOfBoundsException e){
            toolbar.setTitle("Classes");
        }
        setSupportActionBar(toolbar);
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
        /** -------------------------------------------------------------------- */


        //takes you to the screen where you can view class information
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  ((SwipeLayout)(list.getChildAt(position - list.getFirstVisiblePosition()))).open();
                Intent intent = new Intent(GradeActivity.this, AssignmentListExpandable.class);
                res.moveToPosition(position);
                intent.putExtra("class", classList.get(position));
                intent.putExtra("semesterID", semesterID);
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
                    System.out.println("List is scrolling upwards");
                    fabMenu.hideMenu(true);


                } else if (last_item > firstVisibleItem) {
                    System.out.println("List is scrolling downwards");
                    fabMenu.showMenu(true);


                } else if (last_item == firstVisibleItem) fabMenu.showMenu(true);
                last_item = firstVisibleItem;
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.class_drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            fabMenu.close(true);
        } else {
            if (exit < 1) {
                Toast.makeText(GradeActivity.this, "Press back again to exit Four.oh", Toast.LENGTH_SHORT).show();
                exit++;
            } else if (exit == 1) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                System.exit(0);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        fabMenu.close(true);

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.class_drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            fabMenu.close(true);
        }


        int id = item.getItemId();

        if (id == R.id.home) {

        } else if (id == R.id.grade_calculator) {
            Intent intent = new Intent(GradeActivity.this, GradeCalculator.class);
            intent.putExtra("semesterID", semesterID);
            startActivity(intent);

        } else if (id == R.id.upcoming_assignments) {
            try {
                Intent intent = new Intent(GradeActivity.this, UpcomingWork.class);
                intent.putExtra("appCreated", true);
                intent.putExtra("semesterID", semesterID);
                startActivity(intent);
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(GradeActivity.this, "You cannot set any upcoming assignments in a " + database.getSystem() + "That you're not currently in.", Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.change_semester) {
            Intent intent = new Intent(getApplicationContext(), SemesterList.class);
            startActivity(intent);

        } else if (id == R.id.contact_dev) {
            DrawerOptions.contactDeveloper(GradeActivity.this);

        } else if (id == R.id.report_bug) {
            DrawerOptions.bugReport(GradeActivity.this);

        } else if (id == R.id.newsletter_signup) {
            DrawerOptions.newsletterSignup(GradeActivity.this);
        } else if (id == R.id.facebook) {
            DrawerOptions.getOpenFacebookIntent(GradeActivity.this);
        }


        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    //handles the adding of classes, allows them to be added dynamically
    private void addItems(View v, String input) {
        classList.add(input);
    }

    //displays the list onto the screen
    protected ListView getListView() {
        if (list == null) {
            list = (ListView) findViewById(R.id.class_list);
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

    //pulls up the dialog to ask for class name then takes you to another screen to add weights
    private void weightClass() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GradeActivity.this);
        builder.setTitle("Create a new class");

        // Set up the input
        final EditText input = new EditText(GradeActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter Name");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Tries for blank input, and spits our a message accordingly
                String className = input.getText().toString();

                if (!database.duplicateClass(className, semesterID)) {
                    if (className.equals(""))
                        Toast.makeText(GradeActivity.this, "Please enter a valid name", Toast.LENGTH_LONG).show();

                    else {
                        Intent intent = new Intent(GradeActivity.this, CreateClass.class);
                        intent.putExtra("weighted", true);
                        intent.putExtra("className", className);
                        intent.putExtra("semesterID", semesterID);
                        intent.putExtra("editSemester", false);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(GradeActivity.this, "This class already exists, choose a new name", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    //pulls up a dialog to enter the class name and the number of total points, and points you have currently
    private void pointClass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GradeActivity.this);
        builder.setTitle("Create a new Class");

        // Set up the input
        final LinearLayout layout = new LinearLayout(GradeActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(GradeActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setHint("Enter Name");
        layout.addView(input);


        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // database.insertClass(input.getText().toString(), Integer.parseInt(points.getText().toString()), Integer.parseInt(max.getText().toString()));

                //Tries for blank input, and spits our a message accordingly
                String className = input.getText().toString();

                //TODO when editing class names, the old value still exists somehow, and not allowing you to create a class with that original name
                if (!database.duplicateClass(className, semesterID)) {
                    if (className.equals(""))
                        Toast.makeText(GradeActivity.this, "Please enter a valid name", Toast.LENGTH_LONG).show();

                    else {
                        Intent intent = new Intent(GradeActivity.this, CreateClass.class);

                        intent.putExtra("weighted", false);
                        intent.putExtra("className", className);
                        intent.putExtra("semesterID", semesterID);
                        intent.putExtra("editSemester", false);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(GradeActivity.this, "This class already exists, choose a new name", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    //if the try/catch went through just fine, it will display this alert dialog, and will refresh the screen with the next semesters classes on the screen
    private void allSemestersEnded() {

        AlertDialog.Builder builder = new AlertDialog.Builder(GradeActivity.this).setMessage("Your " + database.getSystem() + " seems to have ended! You can either continue editing your previous one, " +
                "or you can create a new " + database.getSystem() + ".");

        builder.setPositiveButton("New", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GradeActivity.this, SemesterList.class);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }


    private void noPreviousSemester() {

        AlertDialog.Builder builder = new AlertDialog.Builder(GradeActivity.this).setMessage("You seem to have no previous " + database.getSystem() + "s, you can either create a new "
                + database.getSystem() + " or edit the current one.");

        builder.setPositiveButton("New", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GradeActivity.this, SemesterList.class);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.show();

    }


    //if the try/catch went through just fine, it will display this alert dialog, and will refresh the screen with the next semesters classes on the screen
    private void moveToNextSemester(final int getFutureID, final int getPastID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GradeActivity.this).setMessage("Your semester seems to have ended! You can either continue editing your previous one, " +
                "or you can move to your next " + database.getSystem() + ".");

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GradeActivity.this, GradeActivity.class);
                intent.putExtra("semesterID", getFutureID);
                startActivity(intent);

            }
        });

        builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GradeActivity.this, GradeActivity.class);
                intent.putExtra("semesterID", getPastID);
                startActivity(intent);
            }
        });
        builder.show();
    }


}

