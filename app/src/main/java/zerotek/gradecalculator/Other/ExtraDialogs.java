package zerotek.gradecalculator.Other;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import zerotek.gradecalculator.CreateClass;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.UpcomingWorkNotification;

/**
 * Created by Mason on 7/1/2016.
 */

//todo should maybe have the set alert in here
public class ExtraDialogs {

    private String alertFormat = "yyyy-MM-dd HH:mm a";//holds hours and minutes for the alert, and sets
    private final SimpleDateFormat alertFormatter = new SimpleDateFormat(alertFormat, Locale.US);

    public static void createClass(final Context context, final int semesterID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LinearLayout layout = new LinearLayout(context);

        layout.setOrientation(LinearLayout.VERTICAL);
        builder.setTitle("No Classes!");

        builder.setMessage("You have not created any classes to add any upcoming work to. Lets do that now!");

        final EditText name = new EditText(context);
        name.setHint("Class Name");
        layout.addView(name);


        String[] classType = new String[]{"Weight Based", "Points Based"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, classType);
        final Spinner sp = new Spinner(context);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adapter);
        layout.addView(sp);

        builder.setView(layout);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean weighted;
                Intent intent = new Intent(context, CreateClass.class);
                intent.putExtra("className", name.getText().toString());

                weighted = sp.getSelectedItem().toString().equals("Weight Based");
                intent.putExtra("weighted", weighted);

                intent.putExtra("semesterID", semesterID);
                intent.putExtra("editSemester", false);

                context.startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();


    }

    //sets the exact time that the notification will appear
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setAlert(final Context context, final String date, final String displayDate) {
        final Activity activity = (Activity) context;

        String alertFormat = "yyyy-MM-dd HH:mm a";//holds hours and minutes for the alert, and sets
        final SimpleDateFormat alertFormatter = new SimpleDateFormat(alertFormat, Locale.US);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Remind me on " + displayDate);
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText hour = new EditText(context);
        hour.setHint("12");
        hour.setWidth(30);
        hour.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(2),
        });
        hour.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(hour);


        TextView colon = new TextView(context);
        colon.setText(":");
        layout.addView(colon);

        final EditText minute = new EditText(context);
        minute.setHint("00");
        minute.setWidth(30);
        minute.setInputType(InputType.TYPE_CLASS_NUMBER);


        minute.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(2),
        });
        layout.addView(minute);


        String[] amORpm = {"AM", "PM"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, amORpm);

        final Spinner spAMpm = new Spinner(context);
        spAMpm.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
        spAMpm.setAdapter(adapter);
        layout.addView(spAMpm);

        builder.setView(layout);


        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //checks to see if the entered time is valid
                if (!(Integer.parseInt(minute.getText().toString()) >= 60) && !(Integer.parseInt(hour.getText().toString()) > 12)) {

                    Calendar cal = Calendar.getInstance();
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent notificationIntent = new Intent(context, UpcomingWorkNotification.class);
                    notificationIntent.setAction("android.media.action.DISPLAY_NOTIFICATION");
                    notificationIntent.addCategory("android.intent.category.DEFAULT");
                    PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    try {
                        cal.setTime(alertFormatter.parse(date));
                        System.out.print("Date added successfully");
                    } catch (ParseException e) {
                        System.out.println("Failed to add date");
                    }
                    cal.add(Calendar.HOUR, Integer.parseInt(hour.getText().toString()));
                    cal.add(Calendar.MINUTE, Integer.parseInt(minute.getText().toString()));
                    cal.add(Calendar.SECOND, 0);
                    if (spAMpm.getSelectedItem().equals("AM")) cal.add(Calendar.AM_PM, Calendar.AM);
                    else if (spAMpm.getSelectedItem().equals("PM"))
                        cal.add(Calendar.AM_PM, Calendar.PM);


                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);

                    Intent intent = new Intent(context, activity.getClass());
                    intent.putExtra("appCreated", true);
                    context.startActivity(intent);
                    // alertIsSet(workNametoID, categoryID, classID);

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Intent intent = new Intent(context, activity.getClass());
                intent.putExtra("appCreated", true);
                context.startActivity(intent);
            }
        });
        builder.show();

    }


    public static void assignmentDue(final Context context, final int workID, final int categoryID, final int classID, final int semesterID) {
        final Database database = new Database(context);


        final Cursor cursor = database.getSelectedWork(workID, categoryID, classID, semesterID);
        cursor.moveToFirst();
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Assignment Due Today");
        builder.setMessage(cursor.getString(1) + " is due today. Would you like to enter your score in, or set a reminder to do that later?");

        builder.setPositiveButton("Enter Score", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addPoints(context, workID, categoryID);
                // assignmentDueDone = true;
            }

        });

        builder.setNegativeButton("Set Reminder", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialogInterface, int i) {

                final Calendar calendard = Calendar.getInstance();

                DatePickerDialog.OnDateSetListener dialogStartDate = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        calendard.set(Calendar.YEAR, year);
                        calendard.set(Calendar.MONTH, monthOfYear);
                        calendard.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String formatDatabase = "yyyy-MM-dd";
                        String dateFormat = "MMM dd yyyy";

                        SimpleDateFormat simpleDateDatabase = new SimpleDateFormat(formatDatabase, Locale.US);
                        SimpleDateFormat simpleDateFormatDisplay = new SimpleDateFormat(dateFormat, Locale.US);

                        String displayDate = simpleDateFormatDisplay.format(calendard.getTime());
                        String dDueDate = simpleDateDatabase.format(calendard.getTime());

                        database.updateWork(workID, categoryID, null, null, null, dDueDate);
                        setAlert(context, dDueDate, displayDate);
                        //todo the display date is wrong, its only showing current date, need to go and change that, or remove it entirely
                    }
                };
                /**--------------------------------------------END----------------------------------------------------- */
                new DatePickerDialog(context, dialogStartDate, calendard.get(Calendar.YEAR), calendard.get(Calendar.MONTH),
                        calendard.get(Calendar.DAY_OF_MONTH)).show();


            }
        });
        builder.show();

    }


    //adding the amount of points you got for the assignment
    private static void addPoints(Context context, final int workID, final int categoryID) {
        final Database database = new Database(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Points");

        final EditText points = new EditText(context);
        points.setInputType(InputType.TYPE_CLASS_NUMBER);
        points.setHint("Points Earned");

        builder.setView(points);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                database.updateWork(workID, categoryID, null, Integer.parseInt(points.getText().toString()), null, null);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


}
