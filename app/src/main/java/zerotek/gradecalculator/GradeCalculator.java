package zerotek.gradecalculator;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import zerotek.gradecalculator.Calculations.GradeCalculations;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Other.DrawerOptions;
import zerotek.gradecalculator.Other.ExtraDialogs;
import zerotek.gradecalculator.Other.Tutorials;
//todo add advertisement on this screen

public class GradeCalculator extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public ArrayList<String> classList, categoryList;
    Database database;
    EditText pointsEarned, maxPoints;
    TextView newGrade;
    Button calculate;
    Spinner classSelect, catSelect;
    Cursor classRes, catRes;
    private int semesterID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_calculator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = new Database(GradeCalculator.this);
        try {
            semesterID = getIntent().getExtras().getInt("semesterID");
        } catch (NullPointerException e) {
            semesterID = database.getCurrentSemesterDate();
        }
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

        if (database.getGradeCalc() != 1) {
            Tutorials.gradeCalculator(GradeCalculator.this);
            database.insertGradeCalc(1);
        }


        classList = new ArrayList<String>();
        categoryList = new ArrayList<String>();

        newGrade = (TextView) findViewById(R.id.new_grade_calculator);

        pointsEarned = (EditText) findViewById(R.id.points_earned_calc);
        //im not sure I want the next button, but it works here if I do
        //pointsEarned.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        maxPoints = (EditText) findViewById(R.id.max_points_calc);

        calculate = (Button) findViewById(R.id.grade_calculate_button);

        /**------------CLASS SPINNER------------*/
        classSelect = (Spinner) findViewById(R.id.class_select_spinner);

        try {
            classRes = database.getCurrentClasses();
        } catch (CursorIndexOutOfBoundsException e) {
            classRes = database.getChosenClasses(semesterID);
        }

        try {
            classRes.moveToFirst();
            do {
                classList.add(classRes.getString(1));
            } while (classRes.moveToNext());


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, classList);
            classSelect.setAdapter(adapter);


            /**------------CATEGORY SPINNER------------*/

            catSelect = (Spinner) findViewById(R.id.category_select_spinner);
            catRes = database.getCategories(database.getClassID(classSelect.getSelectedItem().toString(), semesterID));
            catRes.moveToNext();

            do {
                categoryList.add(catRes.getString(1));
            } while (catRes.moveToNext());
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item, categoryList);
            catSelect.setAdapter(adapter2);


            classSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    catRes = database.getCategories(database.getClassID(classSelect.getSelectedItem().toString(), semesterID));
                    catRes.moveToFirst();

                    categoryList.clear();
                    do {
                        categoryList.add(catRes.getString(1));
                    } while (catRes.moveToNext());

                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(GradeCalculator.this, R.layout.spinner_item, categoryList);
                    catSelect.setAdapter(adapter2);

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            calculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pointsEarned.getText().toString().length() != 0 && maxPoints.getText().toString().length() != 0) {

                        boolean weighted;

                        classRes.moveToPosition(classSelect.getSelectedItemPosition());
                        weighted = classRes.getInt(2) == 1;

                        calculateGrade(classSelect.getSelectedItem().toString(), catSelect.getSelectedItem().toString(), Integer.parseInt(pointsEarned.getText().toString()), Integer.parseInt(maxPoints.getText().toString()), weighted);
                    } else {
                        newGrade.setText("Fill in all values to calculate your grade");
                    }
                }

            });

        } catch (CursorIndexOutOfBoundsException e) {
            ExtraDialogs.createClass(GradeCalculator.this, semesterID);
        }


    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.class_drawer);

        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intent = new Intent(GradeCalculator.this, GradeActivity.class);
            intent.putExtra("semesterID", semesterID);
            startActivity(intent);

        } else if (id == R.id.grade_calculator) {

        } else if (id == R.id.upcoming_assignments) {
            try {
                Intent intent = new Intent(GradeCalculator.this, UpcomingWork.class);
                intent.putExtra("appCreated", true);
                startActivity(intent);
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(GradeCalculator.this, "You cannot set any upcoming assignments in a " + database.getSystem() + "That you're not currently in.", Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.change_semester) {
            Intent intent = new Intent(getApplicationContext(), SemesterList.class);
            //intent.putExtra("changingSemester", true);
            startActivity(intent);

        } else if (id == R.id.contact_dev) {
            DrawerOptions.contactDeveloper(GradeCalculator.this);

        } else if (id == R.id.report_bug) {
            DrawerOptions.bugReport(GradeCalculator.this);

        } else if (id == R.id.newsletter_signup) {
            DrawerOptions.newsletterSignup(GradeCalculator.this);
        } else if (id == R.id.facebook) {
            DrawerOptions.getOpenFacebookIntent(GradeCalculator.this);
        }


        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void calculateGrade(String classSelect, String category, int pointsEarned, int maxPoints, boolean weighted) {
        GradeCalculations calculations = new GradeCalculations();
        SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Date date = new Date();


        int classID = database.getClassID(classSelect, semesterID);
        int catID = database.getCategoryID(category, classID, semesterID);

        database.insertWork("blank_calculator$%", pointsEarned, maxPoints, catID, classID, semesterID, FORMATTER.format(date.getDate()));
        int workID = database.getWorkID("blank_calculator$%", classID, catID, semesterID);

        double grade = calculations.classGrade(GradeCalculator.this, classID, weighted);
        int iGrade = (int) Math.round(grade);
        String sGrade = String.format("%.0f", grade) + "% " + calculations.letterGrade(iGrade);
        newGrade.setText("New Grade: " + sGrade);

        database.removeWork(workID, catID, classID, semesterID);


    }


}
