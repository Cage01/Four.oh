package zerotek.gradecalculator;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import zerotek.gradecalculator.Adapters.UpcomingWorkAdapter;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Items.UpcomingItem;
import zerotek.gradecalculator.Other.DrawerOptions;
import zerotek.gradecalculator.Other.ExtraDialogs;
import zerotek.gradecalculator.Other.Tutorials;

public class UpcomingWork extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener, OnMonthChangedListener {

    private final DateFormat FORMATTER2 = SimpleDateFormat.getDateInstance();
    public Calendar calendar;
    public ArrayList<UpcomingItem> upcomingWorkList = new ArrayList<UpcomingItem>();
    public ListView list;
    public Cursor cCatList, cClassList, upcomingRes;
    public String sDueDate;
    public ArrayList<String> weightList = new ArrayList<String>();
    private Database database;
    private Integer semesterID;
    private UpcomingWorkAdapter upcomingWorkAdapter;
    private com.github.clans.fab.FloatingActionButton fab;
    private MaterialCalendarView materialCalendarView;
    private String formatDatabase = "yyyy-MM-dd";//the only date format that the database will accept
    private final SimpleDateFormat FORMATTER = new SimpleDateFormat(formatDatabase, Locale.US);
    private String alertFormat = "yyyy-MM-dd HH:mm a";//holds hours and minutes for the alert, and sets
    private final SimpleDateFormat alertFormatter = new SimpleDateFormat(alertFormat, Locale.US);
    private SimpleDateFormat simpleDateDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_upcoming_work);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = new Database(this);
        calendar = Calendar.getInstance();
        String dateFormat = "MMM dd yyyy";
        SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd");
        String changedDueDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);


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


        try {
            semesterID = database.getCurrentSemesterDate();
        } catch (CursorIndexOutOfBoundsException e) {
            noCurrentSemester();
        }

        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        materialCalendarView.setSelectedDate(CalendarDay.today());
        materialCalendarView.setOnDateChangedListener(this);
        materialCalendarView.setOnMonthChangedListener(this);


        upcomingWorkAdapter = new UpcomingWorkAdapter(this, upcomingWorkList);


        if (database.getUpcomingWork() != 1) {
            Tutorials.upcomingWork(UpcomingWork.this);
            database.insertUpcomintWork(1);
        }


        try {
            getIntent().getExtras().getBoolean("appCreated");


        } catch (NullPointerException e) {
            try {
                //try {
                Cursor cursor = database.getTodaysWork();
                cursor.moveToFirst();
                do {
                    ExtraDialogs.assignmentDue(UpcomingWork.this, cursor.getInt(0), cursor.getInt(5), cursor.getInt(6), semesterID);
                } while (cursor.moveToNext());
                //}catch (NullPointerException iee){}
            } catch (CursorIndexOutOfBoundsException ie) {
            }

        }//todo reminder crashes when you set a reminder for an assignment on the last day of


        if (list == null) {
            list = (ListView) findViewById(R.id.upcoming_work_list);
        }
        upcomingRes = database.getAllUpcomingWork();
        upcomingRes.moveToFirst();

        //todo upcoming work list is no longer displaying
        try {

            do {
                try {
                    Date date = databaseFormat.parse(upcomingRes.getString(4));
                    changedDueDate = simpleDateFormat.format(date);
                    getList(database.getClassText(upcomingRes.getInt(6)), changedDueDate + " - " + upcomingRes.getString(1));
                } catch (ParseException e) {
                }

            } while (upcomingRes.moveToNext());
        } catch (CursorIndexOutOfBoundsException e) {
        }

        list.setAdapter(upcomingWorkAdapter);
        setListAdapter(upcomingWorkAdapter);


        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_upcoming_work);
        fab.show(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addAssignment();
                } catch (CursorIndexOutOfBoundsException e) {
                    ExtraDialogs.createClass(UpcomingWork.this, semesterID);
                }
            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                upcomingRes.moveToPosition(position);
                editOrDelete(upcomingRes.getInt(0), upcomingRes.getInt(5), upcomingRes.getInt(6));
            }
        });



    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @Nullable CalendarDay date, boolean selected) {
        //textView.setText(getSelectedDatesString());
        fab.show(true);
    }


    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        fab.hide(true);
    }


    private String getSelectedDateString() {
        CalendarDay date = materialCalendarView.getSelectedDate();
        if (date == null) {
            return "No Selection";
        }
        return FORMATTER.format(date.getDate());
    }


    private String getSelectedDateStringDisplay() {
        CalendarDay date = materialCalendarView.getSelectedDate();
        if (date == null) {

            return "No Selection";

        }
        return FORMATTER2.format(date.getDate());
    }

    private String getSelectedDateStringAlert() {
        CalendarDay date = materialCalendarView.getSelectedDate();
        if (date == null) {

            return "No Selection";

        }
        return alertFormatter.format(date.getDate());
    }


    public void addAssignment() {
        final Context context = UpcomingWork.this;

        final Database database = new Database(context);
        cClassList = database.getChosenClasses(semesterID);
        final ArrayList<String> classList = new ArrayList<String>();
        final ArrayList<String> catList = new ArrayList<String>();
        final Spinner spCat = new Spinner(context);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        builder.setTitle("New Assignment");

        final EditText title = new EditText(context);
        title.setHint("Name the assignment");
        layout.addView(title);

        final EditText maxPoints = new EditText(context);
        maxPoints.setHint("Points Possible");
        layout.addView(maxPoints);
        maxPoints.setInputType(InputType.TYPE_CLASS_NUMBER);


        /**---------------------------------------CLASS DROPDOWN-------------------------------------------*/
        cClassList.moveToFirst();
        do {
            classList.add(cClassList.getString(1));
        } while (cClassList.moveToNext());


        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, classList);

        final Spinner spClass = new Spinner(context);
        spClass.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        spClass.setAdapter(adapter);
        layout.addView(spClass);


        /**---------------------------------------CATEGORY DROPDOWN------------------------------------------*/
        //the category lists only reacts and appears after the selection of a class, to get the right info on the dialog
        spClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layout.removeView(spCat);
                cCatList = database.getCategories(database.getClassID(spClass.getSelectedItem().toString(), semesterID));
                cCatList.moveToFirst();
                if (!catList.isEmpty()) catList.clear();
                do {
                    catList.add(cCatList.getString(1));
                } while (cCatList.moveToNext());

                final ArrayAdapter<String> adapterCat = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, catList);
                spCat.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


                spCat.setAdapter(adapterCat);

                layout.addView(spCat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int classID = database.getClassID(spClass.getSelectedItem().toString(), semesterID);
                int categoryID = database.getCategoryID(spCat.getSelectedItem().toString(), classID, semesterID);

                String sToday;
                Calendar cal = Calendar.getInstance();
                sToday = FORMATTER.format(cal.getTime());
                Date today;
                Date selectedDate;

                try {
                    //   today = FORMATTER.parse(String.valueOf(today);
                    today = FORMATTER.parse(sToday);
                    selectedDate = FORMATTER.parse(getSelectedDateString());


                    if (!database.duplicateWork(title.getText().toString(), categoryID, classID, semesterID)) {
                        if (!selectedDate.equals(today) && !selectedDate.before(today)) {
                            database.insertWork(title.getText().toString(),//title of work
                                    -1,//received points
                                    Integer.parseInt(maxPoints.getText().toString()),//points possible
                                    categoryID,//category ID
                                    classID,//Class ID
                                    semesterID,//Semester ID
                                    getSelectedDateString());//Due date

                            ExtraDialogs.setAlert(UpcomingWork.this, getSelectedDateStringAlert(), getSelectedDateStringDisplay());//Sets reminder for that date
                            Toast.makeText(UpcomingWork.this, getSelectedDateStringAlert(), Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(context, "Invalid date. An upcoming assignment cannot be created for today or a previous day.", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(context, "There is an assignment already created with this exact name, edit the name to something else", Toast.LENGTH_LONG).show();
                    }
                } catch (ParseException e) {
                    Toast.makeText(context, "Parse Exception", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
        int id = item.getItemId();

        if (id == R.id.home) {

            Intent intent = new Intent(getApplicationContext(), GradeActivity.class);
            if (semesterID != null) intent.putExtra("semesterID", semesterID);
            startActivity(intent);
        } else if (id == R.id.grade_calculator) {
            Intent intent = new Intent(UpcomingWork.this, GradeCalculator.class);
            startActivity(intent);

        } else if (id == R.id.upcoming_assignments) {

        } else if (id == R.id.change_semester) {
            Intent intent = new Intent(getApplicationContext(), SemesterList.class);
            startActivity(intent);

        } else if (id == R.id.contact_dev) {
            DrawerOptions.contactDeveloper(UpcomingWork.this);

        } else if (id == R.id.report_bug) {
            DrawerOptions.bugReport(UpcomingWork.this);

        } else if (id == R.id.newsletter_signup) {
            DrawerOptions.newsletterSignup(UpcomingWork.this);

        } else if (id == R.id.facebook) {
            DrawerOptions.getOpenFacebookIntent(UpcomingWork.this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.class_drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


//TODO if editing the assignment, the assignment due dialog should not be cancelable


    //displays the list onto the screen
    protected ListView getListView() {
        if (list == null) {
            list = (ListView) findViewById(R.id.upcoming_work_list);
        }
        return list;
    }


    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }


    public void getList(String thisClass, String thisAssignment) {
        upcomingWorkList.add(new UpcomingItem(thisClass, thisAssignment));
        upcomingWorkAdapter.notifyDataSetChanged();
    }


    private void editOrDelete(final int workID, final int categoryID, final int classID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpcomingWork.this);
        builder.setMessage("Would you like to Edit this assignment or Remove it?");
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edit(workID, categoryID, classID);
            }
        });

        builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                remove(workID, categoryID, classID);
            }
        });
        builder.show();
    }

    private void edit(final int workID, int categoryID, final int classID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpcomingWork.this);
        final LinearLayout layout = new LinearLayout(UpcomingWork.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        builder.setTitle("Edit Assignment");


        Cursor rRes = database.getSelectedWork(workID, categoryID, classID, semesterID);
        rRes.moveToFirst();

        final EditText titleBox = new EditText(UpcomingWork.this);
        titleBox.setText(rRes.getString(1));
        layout.addView(titleBox);


        final EditText maxPoints = new EditText(UpcomingWork.this);
        maxPoints.setText(String.valueOf(rRes.getInt(3)));

        maxPoints.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(maxPoints);

        //date picker dialog
        final EditText dueDate = new EditText(UpcomingWork.this);


        //formats the date from the database version to an easily displayed version
        String dateFormat = "MM/dd/yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
        final SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date = databaseFormat.parse(rRes.getString(4));

            dueDate.setText(String.valueOf(simpleDateFormat.format(date)));
            dueDate.setFocusable(true);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        /**---------------------Supposed to be the date picker dialog on edit text click----------------*/
        dueDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (dueDate.isFocused()) {
                    dueDate.clearFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(dueDate.getWindowToken(), 0);


                    final Calendar calendarDueDate = Calendar.getInstance();

                    DatePickerDialog.OnDateSetListener dateDue = new DatePickerDialog.OnDateSetListener() {


                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            calendarDueDate.set(Calendar.YEAR, year);
                            calendarDueDate.set(Calendar.MONTH, monthOfYear);
                            calendarDueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            String formatDisplay = "MM/dd/yy";
                            String formatDatabase = "yyyy-MM-dd";

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDisplay, Locale.US);
                            simpleDateDatabase = new SimpleDateFormat(formatDatabase, Locale.US);


                            sDueDate = simpleDateDatabase.format(calendarDueDate.getTime());
                            dueDate.setText(simpleDateFormat.format(calendarDueDate.getTime()));

                        }
                    };
                    /**--------------------------------------------END----------------------------------------------------- */
                    new DatePickerDialog(UpcomingWork.this, dateDue, calendarDueDate.get(Calendar.YEAR), calendarDueDate.get(Calendar.MONTH),
                            calendarDueDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });


        layout.addView(dueDate);

        Cursor catRes = database.getCategories(classID);
        catRes.moveToFirst();
        weightList.clear();

        if (weightList.size() == 0) {
            do {
                weightList.add(catRes.getString(1));
            } while (catRes.moveToNext());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpcomingWork.this, android.R.layout.simple_spinner_dropdown_item, weightList);
        final Spinner sp = new Spinner(UpcomingWork.this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adapter);

        sp.setSelection(getCategoryPos(database.getCategoryString(categoryID, classID)));
        layout.addView(sp);

        builder.setView(layout);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int categoryID = database.getCategoryID(sp.getSelectedItem().toString(), classID, semesterID);
                Calendar calendar = Calendar.getInstance();
                String sToday = FORMATTER.format(calendar.getTime());

                try {
                    Date due = FORMATTER.parse(sDueDate);
                    Date today = FORMATTER.parse(sToday);

                    database.updateWork(workID, categoryID,
                            titleBox.getText().toString(), -1,
                            Integer.parseInt(maxPoints.getText().toString()),
                            sDueDate);

                    boolean assignmentDueDone = false;
                    if (today.equals(due) || today.after(due)) {
                        ExtraDialogs.assignmentDue(UpcomingWork.this, workID, categoryID, classID, semesterID);
                    } else assignmentDueDone = true;


                    if (assignmentDueDone) {
                        Intent intent = new Intent(UpcomingWork.this, UpcomingWork.class);
                        intent.putExtra("appCreated", true);

                        startActivity(intent);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(UpcomingWork.this, "Didn't update work", Toast.LENGTH_LONG).show();
                }


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


    }

    private void remove(final int workID, final int categoryID, final int classID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpcomingWork.this);
        builder.setMessage("Are you sure you want to remove this assignment?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                database.removeWork(workID, categoryID, classID, semesterID);
                Intent intent = new Intent(UpcomingWork.this, UpcomingWork.class);
                intent.putExtra("appCreated", true);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private int getCategoryPos(String category) {
        return weightList.indexOf(category);
    }

    private void noCurrentSemester() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpcomingWork.this);
        builder.setTitle("No Current " + database.getSystem());
        builder.setMessage("You currently are not in a " + database.getSystem() + ", and therefore cannot add any upcoming assignments. Would you like to create one now?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(UpcomingWork.this, AddSemester.class);
                intent.putExtra("changingSemester", true);
                startActivity(intent);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

}
