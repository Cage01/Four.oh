package zerotek.gradecalculator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Other.Font;


public class AddSemester extends AppCompatActivity {


    TextView info, addText;
    Database database;
    EditText startDate, endDate;
    String startDateDatabase, endDateDatabase;
    EditText editSemester;
    Calendar calendarStart = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener dateStart = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            calendarStart.set(Calendar.YEAR, year);
            calendarStart.set(Calendar.MONTH, monthOfYear);
            calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStart();

        }
    };
    Calendar calendarEnd = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener dateEnd = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            calendarEnd.set(Calendar.YEAR, year);
            calendarEnd.set(Calendar.MONTH, monthOfYear);
            calendarEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEnd();

        }
    };
    private Button nextButton;
    private boolean changingSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(AddSemester.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_semester);


        try {
            changingSemester = getIntent().getExtras().getBoolean("changingSemester");
        } catch (NullPointerException e) {
            changingSemester = false;
        }
        //try{ database.getCurrentSemesterDate();}catch (CursorIndexOutOfBoundsException e){changingSemester = true;}


        try {
            setTexts();

        } catch (CursorIndexOutOfBoundsException ee) {
            welcome();

        }


        if (database.numOfSemesters() > 0 && !changingSemester) {
            Intent intent = new Intent(getApplicationContext(), GradeActivity.class);
            startActivity(intent);
        }


        editSemester.setSingleLine(true);
        editSemester.setImeOptions(EditorInfo.IME_ACTION_DONE); //instead of having enter button that takes to the next line, it is now a done button to hide keyboard
        startDate = (EditText) findViewById(R.id.start_date_edit);


        endDate = (EditText) findViewById(R.id.end_date_edit);


        nextButton = (Button) findViewById(R.id.semester_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSemester();
            }
        });


        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearKeyboard();
                new DatePickerDialog(AddSemester.this, dateStart, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH),
                        calendarStart.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearKeyboard();
                new DatePickerDialog(AddSemester.this, dateEnd, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH),
                        calendarEnd.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent(AddSemester.this, SemesterList.class);
        startActivity(intent2);
    }

    private void welcome() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddSemester.this);

        final LinearLayout layout = new LinearLayout(AddSemester.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        builder.setTitle("Welcome to Four.oh!");
        builder.setMessage("Welcome!\n" +
                "\n" +
                "This is your first time using the app. So before we get started, we need to know if you're on the Quarterly system, or on the Semester system. This screen is where you will set the name and dates of your current quarter or semester.\n\n" +
                "Thanks again, and welcome!");

        String[] select = {"Quarter", "Semester"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddSemester.this, android.R.layout.simple_spinner_dropdown_item, select);

        final Spinner sp = new Spinner(AddSemester.this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adapter);
        layout.addView(sp);

        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                database.insertSystem(sp.getSelectedItem().toString());

                setTexts();


            }
        });
        builder.show();
    }

    private void setTexts() {
        addText = (TextView) findViewById(R.id.add_semester_text);
        info = (TextView) findViewById(R.id.add_semester_info);
        editSemester = (EditText) findViewById(R.id.semester_name);


        addText.setText("Add a " + database.getSystem());
        //  info.setText("Adding " + database.getSystem() + "'s will help your organize all your courses and grades to give you the best statistics possible");
        editSemester.setHint(database.getSystem() + " name");
    }

    public void addSemester() {//adds the semester to the databse

        if (!editSemester.getText().toString().isEmpty() && startDateDatabase != null && endDateDatabase != null) {
            if (!database.duplicateSemester(editSemester.getText().toString())) {
                if (calendarStart.after(calendarEnd)) {
                    Toast.makeText(AddSemester.this, "It looks like the start date is after the end date!", Toast.LENGTH_LONG).show();
                } else if (calendarStart.before(calendarEnd)) {//if the start date is before the end date all is well!
                    database.insertSemester(editSemester.getText().toString(), startDateDatabase, endDateDatabase);
                    Intent intent = new Intent(AddSemester.this, GradeActivity.class);
                    intent.putExtra("semesterID", database.getSemesterID(editSemester.getText().toString()));
                    startActivity(intent);
                    finish();
                } else if (calendarStart.equals(calendarEnd)) {
                    areYouSure();//checking if user is okay with equal dates
                }
            } else
                Toast.makeText(AddSemester.this, "A " + database.getSystem() + " with this name already exists! Choose something else.", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(AddSemester.this, "Something seems to be blank, be sure to fill in all fields!", Toast.LENGTH_LONG).show();
        }//if theres any empty fields, this will run

    }

    public void updateStart() {
        String formatDisplay = "MM/dd/yy";
        String formatDatabase = "yyyy-MM-dd";


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDisplay, Locale.US);
        SimpleDateFormat simpleDateDatabase = new SimpleDateFormat(formatDatabase, Locale.US);


        startDateDatabase = simpleDateDatabase.format(calendarStart.getTime());
        startDate.setText(simpleDateFormat.format(calendarStart.getTime()));
    }

    public void updateEnd() {
        String formatDisplay = "MM/dd/yy";
        String formatDatabase = "yyyy-MM-dd";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDisplay, Locale.US);
        SimpleDateFormat simpleDateDatabase = new SimpleDateFormat(formatDatabase, Locale.US);


        endDate.setText(simpleDateFormat.format(calendarEnd.getTime()));
        endDateDatabase = simpleDateDatabase.format(calendarEnd.getTime());

    }


    public void areYouSure() {
        final Context context = AddSemester.this;
        final AlertDialog.Builder areYouSure = new AlertDialog.Builder(context);
        TextView textView = new TextView(context);
        textView.setText("\nThe semester's start date and end date are the same, are you sure about this?");
        textView.setPadding(20, 5, 5, 20);
        areYouSure.setTitle("Just Checking!");
        areYouSure.setView(textView);

        areYouSure.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                database.insertSemester(editSemester.getText().toString(), startDateDatabase, endDateDatabase);
                Intent intent = new Intent(AddSemester.this, GradeActivity.class);
                intent.putExtra("semesterID", database.getSemesterID(editSemester.getText().toString()));
                startActivity(intent);

            }
        });

        areYouSure.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        areYouSure.create().show();
    }

    private void clearKeyboard() {
        editSemester.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editSemester.getWindowToken(), 0);
    }


}

