package zerotek.gradecalculator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import zerotek.gradecalculator.Other.Tutorials;


public class CreateClass extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static Cursor res;
    public String className, newClassName;
    public ArrayList<Weight> categoryList = new ArrayList<Weight>();
    Database database;
    ArrayList<String> titleArray;
    ArrayList<Double> percentPointArray;
    WeightAdapter weightAdapter;
    int semesterID;
    double totalPointPercent;
    private ListView list;
    private TextView total, noCat;
    private Button classDisplayText;
    private boolean weighted;
    private String type;
    private boolean editSemester;
    private com.github.clans.fab.FloatingActionButton fab;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new Database(this);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Advertisements.bannerAd(CreateClass.this);

        if (database.getCreateClass() != 1) {
            Tutorials.createClass(CreateClass.this);
            database.insertCreateClass(1);
        }

        noCat = (TextView) findViewById(R.id.no_categories);

        titleArray = new ArrayList<String>();
        percentPointArray = new ArrayList<Double>();


        weightAdapter = new WeightAdapter(this, categoryList);
        className = getIntent().getStringExtra("className");
        weighted = getIntent().getExtras().getBoolean("weighted");
        semesterID = getIntent().getExtras().getInt("semesterID");
        editSemester = getIntent().getExtras().getBoolean("editSemester");

        //setting the total points at the top of the screen to 0 pts or % depending on the class type
        if (weighted) type = "%";
        else type = " pts.";
        total = (TextView) findViewById(R.id.total);
        total.setText("Total: 0" + type);


        //this is for the button to be able to change the text of the class if need be
        classDisplayText = (Button) findViewById(R.id.weight_class_name);
        Font.setFont(CreateClass.this, classDisplayText);
        classDisplayText.setText(className);
        classDisplayText.clearFocus();


//setup for the list

        if (list == null) {
            list = (ListView) findViewById(R.id.weight_list);
        }

        list.setAdapter(weightAdapter);
        setListAdapter(weightAdapter);


        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabw);
        fab.show(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newCategory(null, null, null);

            }
        });


        editCategory(list);


        //Change class name if the user wants to
        classDisplayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateClass.this);

                final EditText titleBox = new EditText(CreateClass.this);
                titleBox.setText(className);
                builder.setTitle("Edit Class Name");
                builder.setView(titleBox);

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        className = titleBox.getText().toString();
                        classDisplayText.setText(className);
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


        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            int last_item;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (last_item < firstVisibleItem) {
                    //list scrolling upwards
                    fab.hide(true);

                } else if (last_item > firstVisibleItem) {
                    //list scrolling downwards
                    fab.show(true);

                } else if (last_item == firstVisibleItem) fab.show(true);
                last_item = firstVisibleItem;
            }
        });

        if (titleArray.size() == 0) noCat.setText("No Categories");


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
                if (titleArray.size() <= 0) empty();
                else if (weighted && (totalPointPercent < 100 || totalPointPercent > 100)) {
                    offPercentage();
                } else addData();//allows it to insert the data into the database on press
                break;
        }
        return true;
    }


    public void addData() {


        try {

            int typeI;
            if (!weighted) typeI = 0;
            else typeI = 1;
            database.insertClass(className, semesterID, typeI);
            for (int i = 0; i < titleArray.size(); i++) {
                database.insertCategory(titleArray.get(i), percentPointArray.get(i), database.getClassID(className, semesterID), semesterID);
            }
        } catch (Exception e) {
            String error = e.toString();
            System.out.println(error);
        }
        if (!editSemester) {
            Intent intent = new Intent(CreateClass.this, GradeActivity.class);
            intent.putExtra("semesterID", semesterID);
            startActivity(intent);
        }

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
        final Context context = CreateClass.this;
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


        } else {//else its points based and will display points rather than percentage
            if (pText != null) pointPercentBox.setText(String.format("%.0f", pText));
            else pointPercentBox.setHint("Points");


        }
        layout.addView(pointPercentBox);


        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClass.this);
        if (titleText == null & pText == null) builder.setTitle("Create a Category");
        else builder.setTitle("Edit Category");

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
                    Toast.makeText(CreateClass.this, "Please enter a valid Category", Toast.LENGTH_LONG).show();


                else {//if neither field is blank it will run the rest of the method

                    //checking for duplicate categories within the class


                    if (!duplicate(titleBox.getText().toString(), position)) {

                        //Creating a new category
                        if (titleText == null & pText == null & position == null) {//checking if its a new category or an edited one
                            titleArray.add(titleBox.getText().toString());//for new category

                            if (weighted) {
                                //if weighted setting it for percentage
                                double rPercent = Double.parseDouble(pointPercentBox.getText().toString()) / 100;
                                percentPointArray.add(rPercent);
                                getList(titleBox.getText().toString(), pointPercentBox.getText().toString() + "%", null);

                            } else {
                                //if points setting it for points only
                                double rPoints = Double.parseDouble(pointPercentBox.getText().toString());
                                percentPointArray.add(rPoints);
                                getList(titleBox.getText().toString(), pointPercentBox.getText().toString() + "pts.", null);

                            }


                        } else {//if it is an edited class it will run this code
                            //Editing an already created category
                            titleArray.set(position, titleBox.getText().toString());
                            double rPercent;
                            if(weighted) rPercent = Double.parseDouble(pointPercentBox.getText().toString()) / 100;
                            else rPercent = Double.parseDouble(pointPercentBox.getText().toString());
                            percentPointArray.set(position, rPercent);
                            getList(titleBox.getText().toString(), pointPercentBox.getText().toString() + "%", position);

                        }


                        //creating the total text to display the amount that the user has input so far
                        totalPointPercent = 0;
                        for (int i = 0; i < percentPointArray.size(); i++) {
                            totalPointPercent += percentPointArray.get(i);
                        }
                        if (weighted) totalPointPercent *= 100;

                        total.setText("Total: " + String.format("%.0f", totalPointPercent) + type);

                    } else
                        Toast.makeText(context, "This class already contains a category with that name! please change the name", Toast.LENGTH_LONG).show();
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
        noCat.setText("");
        if (position != null) categoryList.set(position, new Weight(thisTitle, thisPercent));
        else categoryList.add(new Weight(thisTitle, thisPercent));
        weightAdapter.notifyDataSetChanged();
    }


    //small dialog menu to ask if the user is sure
    public void removeWeight(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(CreateClass.this);
        builder.setTitle("Are you sure you would like to remove this weight?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                titleArray.remove(position);
                percentPointArray.remove(position);
                categoryList.remove(position);

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
        final AlertDialog.Builder builder = new AlertDialog.Builder(CreateClass.this);
        builder.setTitle("You have not entered any categories! \n" +
                "Would you like to go back?");

        builder.setPositiveButton("Leave this Page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(CreateClass.this, GradeActivity.class);
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


    private void offPercentage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateClass.this);
        builder.setMessage("The categories you have created don't add up to 100%, are you sure you want to continue or edit this class?");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addData();
            }
        });
        builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
            @Override
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
