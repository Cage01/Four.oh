package zerotek.gradecalculator.Adapters;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.R;
import zerotek.gradecalculator.SemesterList;

/**
 * Created by Mason on 4/13/2016.
 */
public class SemesterViewAdapter extends BaseSwipeAdapter {
    ArrayList<String> semesters, startDate, endDate;
    boolean doubleCheck;
    SimpleDateFormat simpleDateDatabase;
    String sStartDate, sEndDate;


    private Context mContext;

    public SemesterViewAdapter(Context mContext, ArrayList<String> theSemesters, ArrayList<String> theStart, ArrayList<String> theEnd) {
        this.mContext = mContext;
        this.semesters = theSemesters;
        this.startDate = theStart;
        this.endDate = theEnd;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_semester;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        final Database database = new Database(mContext);
        final View v = LayoutInflater.from(mContext).inflate(R.layout.semester_text, null);
        final SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, R.id.bottom_wrapper_semester);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.bottom_wrapper_semester2);

        try {
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {
                    doubleCheck = false;
                    TextView textView = (TextView) v.findViewById(R.id.trash_semester);
                    textView.setText("Delete");

                }
            });


            swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
                @Override
                public void onDoubleClick(SwipeLayout layout, boolean surface) {
                }
            });
            v.findViewById(R.id.trash_semester).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor res = database.getClasses();
                    res.moveToPosition(position);

                    TextView textView = (TextView) v.findViewById(R.id.trash_semester);

                    if (!doubleCheck) {
                        textView.setText("Are you sure?");
                        doubleCheck = true;
                        return;
                    }
                    if (doubleCheck) {
                        String pos = String.valueOf(database.getSemesterID(semesters.get(position)));

                        database.removeSemester(database.getSemesterID(semesters.get(position)));
                        Intent intent = new Intent(mContext, SemesterList.class);
                        mContext.startActivity(intent);
                        return;
                    }
                }
            });
            v.findViewById(R.id.semester_edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    editSemester(position);


                }
            });


        } catch (NullPointerException e) {
        }
        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {

        TextView t = (TextView) convertView.findViewById(R.id.semester_list_name);
        t.setText(semesters.get(position));

        TextView start = (TextView) convertView.findViewById(R.id.semester_list_start);
        start.setText(startDate.get(position));

        TextView end = (TextView) convertView.findViewById(R.id.semester_list_end);
        end.setText(endDate.get(position));
    }

    @Override
    public int getCount() {
        return semesters.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void editSemester(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);


        final Database database = new Database(mContext);
        final Cursor semCursor = database.getSemester(database.getSemesterID(semesters.get(position)));
        semCursor.moveToFirst();

        sStartDate = semCursor.getString(2);
        sEndDate = semCursor.getString(3);

        builder.setTitle("Edit " + semesters.get(position));

        final EditText title = new EditText(mContext);
        title.setText(semesters.get(position));
        layout.addView(title);


        final EditText eStartDate = new EditText(mContext);
        eStartDate.setText(startDate.get(position));


        final EditText eEndDate = new EditText(mContext);
        eEndDate.setText(endDate.get(position));




        eStartDate.setText(startDate.get(position));
        eEndDate.setText(endDate.get(position));
        eStartDate.setFocusable(true);
        eEndDate.setFocusable(true);


        //sets the date picker dialog for start date
        eStartDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (eStartDate.isFocused()) {
                    eStartDate.clearFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(eStartDate.getWindowToken(), 0);


                    final Calendar calendarStartDate = Calendar.getInstance();

                    DatePickerDialog.OnDateSetListener dialogStartDate = new DatePickerDialog.OnDateSetListener() {


                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            calendarStartDate.set(Calendar.YEAR, year);
                            calendarStartDate.set(Calendar.MONTH, monthOfYear);
                            calendarStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            String formatDatabase = "yyyy-MM-dd";
                            String dateFormat = "MMM dd yyyy";


                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
                            simpleDateDatabase = new SimpleDateFormat(formatDatabase, Locale.US);


                            sStartDate = simpleDateDatabase.format(calendarStartDate.getTime());
                            eStartDate.setText(simpleDateFormat.format(calendarStartDate.getTime()));

                        }
                    };
                    /**--------------------------------------------END----------------------------------------------------- */
                    new DatePickerDialog(mContext, dialogStartDate, calendarStartDate.get(Calendar.YEAR), calendarStartDate.get(Calendar.MONTH),
                            calendarStartDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        //sets the date picker dialog for end date
        eEndDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (eEndDate.isFocused()) {
                    eEndDate.clearFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(eEndDate.getWindowToken(), 0);


                    final Calendar calendarEndDate = Calendar.getInstance();

                    DatePickerDialog.OnDateSetListener dialogEndDate = new DatePickerDialog.OnDateSetListener() {


                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            calendarEndDate.set(Calendar.YEAR, year);
                            calendarEndDate.set(Calendar.MONTH, monthOfYear);
                            calendarEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            String formatDatabase = "yyyy-MM-dd";
                            String dateFormat = "MMM dd yyyy";


                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
                            simpleDateDatabase = new SimpleDateFormat(formatDatabase, Locale.US);


                            sEndDate = simpleDateDatabase.format(calendarEndDate.getTime());
                            eEndDate.setText(simpleDateFormat.format(calendarEndDate.getTime()));

                        }
                    };
                    /**--------------------------------------------END----------------------------------------------------- */
                    new DatePickerDialog(mContext, dialogEndDate, calendarEndDate.get(Calendar.YEAR), calendarEndDate.get(Calendar.MONTH),
                            calendarEndDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });


        layout.addView(eStartDate);
        layout.addView(eEndDate);


        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Date dStart, dEnd;
                    try {
                        dStart = simpleDateDatabase.parse(sStartDate);
                        dEnd = simpleDateDatabase.parse(sEndDate);

                        if (!dStart.equals(dEnd)) {
                            if (!dStart.after(dEnd)) {
                                Database database = new Database(mContext);
                                int semesterID = database.getSemesterID(semesters.get(position));
                                database.updateSemester(semesterID, title.getText().toString(), sStartDate, sEndDate);
                                Intent intent = new Intent(mContext, SemesterList.class);
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "It looks like the start date is after the end date!", Toast.LENGTH_LONG).show();
                        }
                    } catch (NullPointerException e) {

                                Database database = new Database(mContext);
                                int semesterID = database.getSemesterID(semesters.get(position));
                                database.updateSemester(semesterID, title.getText().toString(), null, null);
                                Intent intent = new Intent(mContext, SemesterList.class);
                                mContext.startActivity(intent);



                    }


                } catch (ParseException e) {
                    e.printStackTrace();
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


}
