package zerotek.gradecalculator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import zerotek.gradecalculator.Adapters.WeightAdapter;
import zerotek.gradecalculator.Database.Database;
import zerotek.gradecalculator.Items.Weight;
import zerotek.gradecalculator.Other.Advertisements;
import zerotek.gradecalculator.Other.Font;

public class EditClass extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static Cursor classRes, res;
    public ListView list;
    public Button classDisplayText;
    public String className, newClassName;
    public ArrayList<Weight> categoryList = new ArrayList<Weight>();
    public WeightAdapter weightAdapter;
    public boolean weighted;
    public double totalAmount;
    Database database;
    ArrayList<String> titleArray, fromTitle, editTitle, newTitle;
    ArrayList<Double> percentPointArray, fromPercentPoint, editPercentPoint, newPercentPoint;
    int count;
    TextView noCat;
    int semesterID;
    private int counter = 0;
    private TextView total;
    private int classID;
    private String type;
    private com.github.clans.fab.FloatingActionButton fab;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        Advertisements.bannerAd(EditClass.this);
        noCat = (TextView) findViewById(R.id.no_categories);

        className = getIntent().getStringExtra("className");
        newClassName = className;
        semesterID = getIntent().getExtras().getInt("semesterID");
        toolbar.setTitle("Edit " + className);
        setSupportActionBar(toolbar);


        fromTitle = new ArrayList<String>();
        editTitle = new ArrayList<String>();
        newTitle = new ArrayList<String>();
        titleArray = new ArrayList<String>();

        fromPercentPoint = new ArrayList<Double>();
        editPercentPoint = new ArrayList<Double>();
        newPercentPoint = new ArrayList<Double>();
        percentPointArray = new ArrayList<Double>();

        weightAdapter = new WeightAdapter(this, categoryList);


        classRes = database.getClass(database.getClassID(className, semesterID));
        classRes.moveToFirst();
        weighted = classRes.getInt(2) == 1;


        //this only applies if an existing class is being edited
        //its being used to build the list on the screen
        if (weighted) type = "%";
        else type = " pts.";
        total = (TextView) findViewById(R.id.total_edit);
        try {
            classID = getIntent().getExtras().getInt("classID");
            res = database.getCategories(classID);



            res.moveToFirst();
            do {
                if (weighted) {
                    titleArray.add(res.getString(1));
                    percentPointArray.add(res.getDouble(2));

                    getList(res.getString(1), String.format("%.0f", (res.getDouble(2) * 100)) + "%", null);
                } else {
                    titleArray.add(res.getString(1));
                    percentPointArray.add(res.getDouble(2));

                    getList(res.getString(1), res.getString(2) + " pts.", null);
                }
            }while (res.moveToNext());




            TextView totalView = (TextView) findViewById(R.id.total_edit);

            if (titleArray.size() == 0) noCat.setText("No Categories");

            res.moveToFirst();
            totalAmount = res.getDouble(2);
            while (res.moveToNext()) {
                totalAmount += res.getDouble(2);
            }
            if (!weighted) totalView.setText(String.valueOf(String.format("%.0f",totalAmount)) + "pts.");
            else totalView.setText(String.valueOf(String.format("%.0f", totalAmount * 100) + "%"));


            count = titleArray.size();

        } catch (CursorIndexOutOfBoundsException e) {
        }


        //this is for the button to be able to change the text of the class if need be
        classDisplayText = (Button) findViewById(R.id.weight_class_name_edit);
        Font.setFont(EditClass.this, classDisplayText);

        classDisplayText.setText(className);
        classDisplayText.clearFocus();


        classDisplayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditClass.this);

                final EditText titleBox = new EditText(EditClass.this);
                titleBox.setText(newClassName);
                builder.setTitle("Edit Class Name");
                builder.setView(titleBox);

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newClassName = titleBox.getText().toString();
                        classDisplayText.setText(newClassName);
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
        });


//setup for the list

        if (list == null) {
            list = (ListView) findViewById(R.id.weight_list_edit);
        }

        list.setAdapter(weightAdapter);
        setListAdapter(weightAdapter);


        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_edit);
        fab.show(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newCategory(null, null, null);

            }
        });


        editCategory(list);


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

                } else if (last_item == firstVisibleItem) fab.show(true);
                last_item = firstVisibleItem;
            }
        });


    }


    //these two menus are used to add a "Done" button to the action bar
    //this will add all the created weights to the database
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);//inflates the menu that controls the done button
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_done:
                if ((totalAmount * 100) > 100 && weighted) areYouSure();
                else {
                    if (titleArray.size() <= 0) empty();
                    else {
                        if (newTitle.size() > 0) {
                            addData();//allows it to insert the data into the database on press
                            counter++;
                        }
                        if (editTitle.size() > 0) {
                            editData();
                            counter++;
                        }

                        if (!className.equals(newClassName)) {
                            editClassName();
                        }

                        if (counter > 0) {
                            Intent intent = new Intent(EditClass.this, GradeActivity.class);
                            intent.putExtra("semesterID", semesterID);
                            startActivity(intent);
                        }
                    }
                }

        }

        return true;
    }


    public void addData() {

        for (int i = 0; i < newTitle.size(); i++) {
            database.insertCategory(newTitle.get(i), newPercentPoint.get(i), database.getClassID(className, semesterID), semesterID);
        }
    }

    public void editData() {

        for (int i = 0; i < editTitle.size(); i++) {
            database.updateCategoryName(database.getCategoryID(fromTitle.get(i), database.getClassID(className, semesterID), semesterID), editTitle.get(i), editPercentPoint.get(i));
        }
    }

    private void editClassName() {

        if (!database.duplicateClass(newClassName, semesterID)) {
            int classID = database.getClassID(className, semesterID);
            database.updateClassName(classID, newClassName, weighted);
            className = newClassName;
            counter++;


        } else
            Toast.makeText(EditClass.this, "A class with this name already exists. Please rename it to continue", Toast.LENGTH_LONG).show();


    }


    public void editCategory(ListView listView) {
        //click to edit the weight
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                newCategory(titleArray.get(i), percentPointArray.get(i), i);

            }
        });


        //long click to delete it
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                removeWeight(i);
                return true;
            }
        });
    }


    public void newCategory(final String titleText, final Double pText, final Integer position) {
        Context context = EditClass.this;
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText titleBox = new EditText(context);
        if (pText != null) titleBox.setText(titleText);
        else titleBox.setHint("Title");
        layout.addView(titleBox);

        final EditText pointPercentBox = new EditText(context);
        pointPercentBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        if (weighted) {//if the selection in the grade class was weighted, the follow through with this
            if (pText != null) {
                double percent = pText * 100;
                pointPercentBox.setText(String.format("%.0f", percent));
            } else pointPercentBox.setHint("Percentage");

            layout.addView(pointPercentBox);

        } else {//else its points based and will display points rather than percentage
            if (pText != null) pointPercentBox.setText(String.format("%.0f", pText));
            else pointPercentBox.setHint("Points");


            layout.addView(pointPercentBox);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(EditClass.this);
        if (titleText == null & pText == null) builder.setTitle("Create a Category");
        else {
            builder.setTitle("Edit Category");


        }

        // Set up the input

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(layout);


        // Set up the buttons
        String positiveButton;
        if (titleText == null & pText == null & position == null) positiveButton = "Add";
        else positiveButton = "Update";

        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                //trying for blank field in the input
                if (titleBox.getText().toString().equals("") || pointPercentBox.getText().toString().equals(""))
                    Toast.makeText(EditClass.this, "Please enter a valid Category", Toast.LENGTH_LONG).show();

                else {//if neither field is blank it will run the rest of the method


                    if (!duplicate(titleBox.getText().toString(), position)) {


                        //Creating a new category
                        if (titleText == null & pText == null & position == null) {//checking if its a new category or an edited one
                            titleArray.add(titleBox.getText().toString());//for new category
                            newTitle.add(titleBox.getText().toString());
                            if (weighted) {
                                //if weighted setting it for percentage
                                double rPercent = Double.parseDouble(pointPercentBox.getText().toString()) / 100;
                                percentPointArray.add(rPercent);
                                newPercentPoint.add(rPercent);
                                getList(titleBox.getText().toString(), pointPercentBox.getText().toString() + "%", null);

                            } else {
                                //if points setting it for points only
                                double rPoints = Double.parseDouble(pointPercentBox.getText().toString());
                                percentPointArray.add(rPoints);
                                newPercentPoint.add(rPoints);//keeps track of new entries to add to database
                                getList(titleBox.getText().toString(), pointPercentBox.getText().toString() + "pts.", null);

                            }


                        } else {//if it is an edited class it will run this code
                            //Editing an already created category
                            fromTitle.add(titleArray.get(position));
                            titleArray.set(position, titleBox.getText().toString());

                            editTitle.add(titleBox.getText().toString());
                            double rPercentPoint;
                            String operator;
                            try {
                                if (weighted) {
                                    rPercentPoint = Double.parseDouble(pointPercentBox.getText().toString()) / 100;
                                    operator = "%";
                                }else {
                                    rPercentPoint = Double.parseDouble(pointPercentBox.getText().toString());
                                    operator = " pts.";
                                }

                                fromPercentPoint.add(percentPointArray.get(position));
                                percentPointArray.set(position, rPercentPoint);
                                editPercentPoint.add(rPercentPoint);//keeps track of edited entries for database
                                getList(titleBox.getText().toString(), pointPercentBox.getText().toString() + operator, position);
                            } catch (NumberFormatException e) {
                                Toast.makeText(EditClass.this, "Please enter a valid Category", Toast.LENGTH_LONG).show();
                            }
                        }


                        //creating the total text to display the amount that the user has input so far
                        totalAmount = 0;
                        for (int i = 0; i < percentPointArray.size(); i++) {
                            totalAmount += percentPointArray.get(i);
                        }
                        if (weighted)
                            total.setText("Total: " + String.format("%.0f", (totalAmount * 100)) + type);
                        else total.setText("Total: " + String.format("%.0f",totalAmount) + type);

                    } else
                        Toast.makeText(EditClass.this, "This class already contains a category with that name! please change the name", Toast.LENGTH_LONG).show();
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
            list = (ListView) findViewById(R.id.class_list);
        }
        return list;
    }


    protected void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }


    public void getList(String thisTitle, String thisPercent, Integer position) {
        if (noCat.getText().toString().equals("No Categories")) noCat.setText("");
        if (position != null) categoryList.set(position, new Weight(thisTitle, thisPercent));
        else categoryList.add(new Weight(thisTitle, thisPercent));
        weightAdapter.notifyDataSetChanged();
    }


    //small dialog menu to ask if the user is sure
    public void removeWeight(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditClass.this);
        builder.setTitle("Are you sure you would like to remove this Category?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    database.removeCategory(database.getCategoryID(titleArray.get(position), classID, semesterID));
                } catch (CursorIndexOutOfBoundsException e) {
                    System.out.print("not in database, delete from array");
                }
                for (int ii = 0; ii < editTitle.size(); ii++) {
                    if (editTitle.get(ii).equals(titleArray.get(position))) {//is checking for the matching string in the main array to delete the item in the arraylist
                        editTitle.remove(ii);
                        editPercentPoint.remove(ii);
                    }
                }
                for (int ii = 0; ii < newTitle.size(); ii++) {
                    if (newTitle.get(ii).equals(titleArray.get(position))) {
                        newTitle.remove(ii);
                        newPercentPoint.remove(ii);
                    }
                }
                titleArray.remove(position);
                percentPointArray.remove(position);
                categoryList.remove(position);

                counter++;

                weightAdapter.notifyDataSetChanged();
                if (titleArray.size() == 0) noCat.setText("No Categories");
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


    public void empty() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditClass.this);
        builder.setTitle("You have not entered any categories! \n" +
                "Would you like to go back?");

        builder.setPositiveButton("Leave this Page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(EditClass.this, GradeActivity.class);
                intent.putExtra("semesterID", semesterID);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Stay Here", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    public void areYouSure() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditClass.this)
                .setMessage("The categories you created add up to over 100%, are you sure you want to continue?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(EditClass.this, GradeActivity.class);
                        intent.putExtra("semesterID", semesterID);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();

    }


    private boolean duplicate(String input, Integer position) {
        boolean dup = false;
        if (titleArray.size() != 0) {
            outerloop:
            for (int i = 0; i < titleArray.size(); i++) {


                if (position != null && i == position) continue;

                if (titleArray.get(i).equals(input)) {
                    dup = true;
                    break outerloop;
                }

            }
        }
        return dup;
    }

}




