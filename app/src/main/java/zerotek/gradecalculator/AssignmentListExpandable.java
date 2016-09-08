package zerotek.gradecalculator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import zerotek.gradecalculator.Adapters.WorkAdapterExpandable;
import zerotek.gradecalculator.Calculations.GradeCalculations;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Items.Work;
import zerotek.gradecalculator.Items.WorkHeader;
import zerotek.gradecalculator.Other.Advertisements;
import zerotek.gradecalculator.Other.Tutorials;


public class AssignmentListExpandable extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Cursor res;
    public String className;
    public ArrayList<WorkHeader> catList = new ArrayList<WorkHeader>();
    public ArrayList<Work> workList = new ArrayList<Work>();
    public Cursor weightRes, workRes, catRes;
    public String sDueDate;
    Database database;
    ArrayList<String> titleArray;
    ArrayList<Double> percentPointArray;
    WorkAdapterExpandable workAdapter;
    ArrayList<String> weightList;
    SimpleDateFormat simpleDateDatabase;
    private ExpandableListView list;
    private ArrayList<String> catListString = new ArrayList<String>();
    private GradeCalculations calculations;
    private TextView classGrade;
    private boolean weighted;
    private int semesterID;
    private HashMap<ArrayList<WorkHeader>, ArrayList<Work>> childList = new HashMap<>();
    private com.github.clans.fab.FloatingActionButton fab;


    //todo if able to be done quickly, it should read which cat is open and when the fab button is clicked, set the category in the spinner to that automatically
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(this);

        className = getIntent().getExtras().getString("class");
        semesterID = getIntent().getExtras().getInt("semesterID");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_list_expandable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(className);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Advertisements.bannerAd(AssignmentListExpandable.this);

        if (database.getAssignmentList() != 1) {
            Tutorials.assignmentList(AssignmentListExpandable.this);
            database.insertAssignmentList(1);
        }
        calculations = new GradeCalculations();

        titleArray = new ArrayList<String>();
        percentPointArray = new ArrayList<Double>();


        workAdapter = new WorkAdapterExpandable(this, catList, childList, workList);
        res = database.getClass(database.getClassID(className, semesterID));
        res.moveToFirst();


        classGrade = (TextView) findViewById(R.id.work_class_grade);


        catRes = database.getCategories(database.getClassID(className, semesterID));
        catRes.moveToFirst();

        workRes = database.getWorkFromCategory(database.getClassID(className, semesterID), catRes.getInt(0));


        //determines if its points based or percentage based from the database
        //the database can only take 1 or 0 values, no true or false values work, so they need to be converted here

        weighted = res.getInt(2) == 1;


        //sets the class grade
        double grade = calculations.classGrade(AssignmentListExpandable.this, res.getInt(0), weighted);
        int iGrade = (int) Math.round(grade);
        if (grade != -1)
            classGrade.setText(String.format("%.0f", grade) + "% " + calculations.letterGrade(iGrade));
        else classGrade.setText("No Grade");


        GradeCalculations calculations = new GradeCalculations();

        /**--------------ADDING CATEGORY HEADER--------------------*/
        try {
            getHeader(catRes.getString(1), calculations.CategoryPointGrade(AssignmentListExpandable.this, database.getClassID(className, semesterID), catRes.getInt(0)));
        } catch (CursorIndexOutOfBoundsException e) {
            getHeader(catRes.getString(1), -1);
        }


        while (catRes.moveToNext()) {
            try {
                getHeader(catRes.getString(1), calculations.CategoryPointGrade(AssignmentListExpandable.this, database.getClassID(className, semesterID), catRes.getInt(0)));
            } catch (CursorIndexOutOfBoundsException e) {
                getHeader(catRes.getString(1), -1);
            }
        }


        if (list == null) {
            list = (ExpandableListView) findViewById(R.id.work_list);
        }

        list.setAdapter(workAdapter);


        list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                workList.clear();
                workAdapter.notifyDataSetChanged();


                workRes = database.getWorkFromCategory(database.getClassID(className, semesterID), database.getCategoryID(catListString.get(groupPosition), database.getClassID(className, semesterID), semesterID));
                for (int i = 0; i < list.getCount(); i++) {
                    if (i != groupPosition) {
                        list.collapseGroup(i);
                    }
                }


                try {
                    workRes.moveToFirst();
                    do {
                        getList(workRes.getString(1), workRes.getDouble(2), workRes.getDouble(3), null);
                    } while (workRes.moveToNext());

                } catch (CursorIndexOutOfBoundsException e) {
                    getList("No Assignments", 0.0, 0.0, null);
                }

            }
        });


//todo allow long click multiple delete, this changes the colors, need a delete button still, and need to remove focus and return the color to normal as well
//todo also shouldnt allow categories to be highlighted if possible
       /* list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (ExpandableListView.getPackedPositionType(i) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                   int childPosition = ExpandableListView.getPackedPositionChild(i);

                    // You now have everything that you would as if this was an OnChildClickListener()
                    // Add your logic here.
                    // Return true as we are handling the event.

                    adapterView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.colorPrimary));


                }
                return false;

            }
        });*/

        list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (!workList.get(childPosition).getTitle().equals("No Assignments")) {
                    workRes.moveToPosition(childPosition);
                    addWork(false, workRes.getString(1), groupPosition);
                }

                return true;
            }
        });


        //wipes the work list to
        list.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                workList.clear();
                workAdapter.notifyDataSetChanged();

            }
        });


        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_assignment);
        fab.show(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWork(true, null, null);
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
                    fab.hide(true);

                } else if (last_item > firstVisibleItem) {
                    System.out.println("List is scrolling downwards");
                    fab.show(true);

                }//else if (last_item == firstVisibleItem)fab.show(true);
                last_item = firstVisibleItem;
            }
        });


    }


    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), GradeActivity.class);
        myIntent.putExtra("semesterID", semesterID);
        startActivityForResult(myIntent, 0);
        finish();
        return true;

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AssignmentListExpandable.this, GradeActivity.class);
        intent.putExtra("semesterID", semesterID);
        startActivity(intent);
        finish();
    }


    public void addWork(boolean _new, String workName, Integer groupPosition) {

        weightList = new ArrayList<String>();
        weightRes = database.getCategories(res.getInt(0));
        weightRes.moveToFirst();
        weightList.add(weightRes.getString(1));
        while (weightRes.moveToNext()) {
            weightList.add(weightRes.getString(1));

        }
        if (_new) dialog();
        else editOrDelete(workName, groupPosition);

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void dialog() {
        final Context context = AssignmentListExpandable.this;

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);


//    -------- FIRST DIALOG LAYOUT ---------
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText titleBox = new EditText(context);
        titleBox.setHint("Title");
        layout.addView(titleBox);

        final EditText pointsEarned = new EditText(context);
        pointsEarned.setInputType(InputType.TYPE_CLASS_NUMBER);

        pointsEarned.setHint("Points Earned");

        layout.addView(pointsEarned);

        final EditText maxPoints = new EditText(context);
        maxPoints.setInputType(InputType.TYPE_CLASS_NUMBER);

        maxPoints.setHint("Total Possible Points");

        layout.addView(maxPoints);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, weightList);
        final Spinner sp = new Spinner(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adapter);
        layout.addView(sp);


//----------------- FINISH DIALOG SETUP ---------------


        dialog.setTitle("Add Work for " + res.getString(1));
        // Set up the input
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        dialog.setView(layout);


        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (titleBox.getText().toString().equals("") || pointsEarned.getText().toString().equals("") || maxPoints.getText().toString().equals(""))//checking if input is empty
                    Toast.makeText(context, "Please enter a valid Assignment", Toast.LENGTH_LONG).show();

                else {//if the input is not empty
                    int classID = database.getClassID(className, semesterID);
                    int categoryID = database.getCategoryID(sp.getSelectedItem().toString(), classID, semesterID);

                    if (!database.duplicateWork(titleBox.getText().toString(), categoryID, classID, semesterID)) {

                        //inserting weighted class into database
                        database.insertWork(titleBox.getText().toString(), Integer.parseInt(pointsEarned.getText().toString()), Integer.parseInt(maxPoints.getText().toString()),
                                database.getCategoryID(sp.getSelectedItem().toString(), res.getInt(0), semesterID),
                                res.getInt(0),
                                semesterID, null);


                        Intent intent = new Intent(context, AssignmentListExpandable.class);
                        intent.putExtra("class", className);
                        intent.putExtra("semesterID", semesterID);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(context, "There is an assignment already created with this exact name, edit the name to something else", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }


    public void editOrDelete(final String workName, final Integer groupPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentListExpandable.this);
        builder.setMessage("Would you like to Edit or Remove this assignment?");

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editAssignment(workName, groupPosition);
            }
        });

        builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                areYouSure(workName, groupPosition);
            }
        });


        builder.show();

    }

    private void areYouSure(final String workName, final Integer groupPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentListExpandable.this);
        builder.setMessage("Are you sure you want to delete this assignment?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int classID = database.getClassID(className, semesterID);
                int categoryID = database.getCategoryID(weightList.get(groupPosition), classID, semesterID);
                int workID = database.getWorkID(workName, classID, categoryID, semesterID);

                database.removeWork(workID, categoryID, classID, semesterID);

                Intent intent = new Intent(AssignmentListExpandable.this, AssignmentListExpandable.class);
                intent.putExtra("semesterID", semesterID);
                intent.putExtra("class", className);
                startActivity(intent);
                finish();
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            // Handle the camera action
        } else if (id == R.id.grade_calculator) {

        } else if (id == R.id.upcoming_assignments) {

        } else if (id == R.id.change_semester) {

        } else if (id == R.id.contact_dev) {

        } else if (id == R.id.report_bug) {

        }


        return true;
    }


    //displays the list onto the screen
    protected ListView getListView() {
        if (list == null) {
            list = (ExpandableListView) findViewById(R.id.work_list);
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

    public void getList(String thisTitle, Double thisPoints, Double maxPoints, Integer position) {
        if (position != null) workList.set(position, new Work(thisTitle, thisPoints, maxPoints));
        else workList.add(new Work(thisTitle, thisPoints, maxPoints));
        workAdapter.notifyDataSetChanged();
    }

    public void getHeader(String title, double grade) {
        catList.add(new WorkHeader(title, grade));
        catListString.add(title);
        workAdapter.notifyDataSetChanged();
//        catList.notify();
    }


    public void editAssignment(String workName, int groupPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentListExpandable.this);
        final LinearLayout layout = new LinearLayout(AssignmentListExpandable.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        builder.setTitle("Edit Assignment");

        final int classID = database.getClassID(className, semesterID);
        int categoryID = database.getCategoryID(weightList.get(groupPosition), classID, semesterID);
        final int workID = database.getWorkID(workName, classID, categoryID, semesterID);


        Cursor rRes = database.getSelectedWork(workID, categoryID, classID, semesterID);
        rRes.moveToFirst();

        final EditText titleBox = new EditText(AssignmentListExpandable.this);
        titleBox.setSingleLine(true);
        titleBox.setImeOptions(EditorInfo.IME_ACTION_DONE); //instead of having enter button that takes to the next line, it is now a done button to hide keyboard

        titleBox.setText(workName);
        layout.addView(titleBox);

        final EditText pointsEarned = new EditText(AssignmentListExpandable.this);
        pointsEarned.setText(String.valueOf(rRes.getInt(2)));
        pointsEarned.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(pointsEarned);

        final EditText maxPoints = new EditText(AssignmentListExpandable.this);
        maxPoints.setText(String.valueOf(rRes.getInt(3)));

        maxPoints.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(maxPoints);

        //date picker dialog
        final EditText dueDate = new EditText(AssignmentListExpandable.this);


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
                    new DatePickerDialog(AssignmentListExpandable.this, dateDue, calendarDueDate.get(Calendar.YEAR), calendarDueDate.get(Calendar.MONTH),
                            calendarDueDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });


        layout.addView(dueDate);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AssignmentListExpandable.this, android.R.layout.simple_spinner_dropdown_item, weightList);
        final Spinner sp = new Spinner(AssignmentListExpandable.this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adapter);
        sp.setSelection(groupPosition);
        layout.addView(sp);

        builder.setView(layout);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int categoryID = database.getCategoryID(sp.getSelectedItem().toString(), classID, semesterID);

                database.updateWork(workID, categoryID,
                        titleBox.getText().toString(),
                        Integer.parseInt(pointsEarned.getText().toString()),
                        Integer.parseInt(maxPoints.getText().toString()),
                        sDueDate);


                Intent intent = new Intent(AssignmentListExpandable.this, AssignmentListExpandable.class);
                intent.putExtra("class", className);
                intent.putExtra("semesterID", semesterID);
                startActivity(intent);


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


}
