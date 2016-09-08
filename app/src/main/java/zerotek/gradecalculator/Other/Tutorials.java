package zerotek.gradecalculator.Other;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import zerotek.gradecalculator.Database.Database;

public class Tutorials {

    public static void homeScreen(final Context context) {

        String tut1 = "Welcome to the home screen! This is where you will be brought to each time you enter the app. This screen displays all the classes that you're currently in. You can add a class by clicking on the button near the bottom of the screen.";
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tutorial")
                .setMessage(tut1)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hm2(context);

                    }
                }).show();


    }

    public static void hm2(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String tut2 = "You can also remove or edit classes by swiping the class itself to the left or right!";

        builder.setTitle("Tutorial")
                .setMessage(tut2)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hm3(context);

                    }
                }).show();
    }

    public static void hm3(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String tut3 = "Located on the top left of the screen is the menu icon, when clicking on that you can navigate to other parts of the app, visit our website or facebook page and more!";

        builder.setTitle("Tutorial")
                .setMessage(tut3)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Database database = new Database(context);

                        database.insertHome(1);
                        dialogInterface.dismiss();

                    }
                }).show();
    }

    public static void createClass(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String tut1 = "On this screen you add all of the categories for your class. HINT: That will be located on your course syllabus! You can add categories by clicking on the plus button at the bottom.";


        builder.setTitle("Tutorial")
                .setMessage(tut1)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cc1(context);
                    }
                }).show();

    }

    public static void cc1(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String tut2 = "By clicking on the class name that you've entered at the top, you can edit the name. Also clicking on the categories you create you have the option to edit or remove it!";


        builder.setTitle("Tutorial")
                .setMessage(tut2)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Database database = new Database(context);

                        database.insertCreateClass(1);
                        dialogInterface.dismiss();
                    }
                }).show();

    }

    public static void gradeCalculator(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String tut1 = "On the Grade Calculator you can calculate what your future grade will be based off of points that you enter!";
        builder.setTitle("Tutorial")
                .setMessage(tut1)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gc1(context);
                    }
                }).show();

    }

    public static void gc1(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String tut2 = "Simply select a class from the dropdown, and select the category you want to calculate it with, and enter the points. After that it will display what your grade will be.";
        builder.setTitle("Tutorial")
                .setMessage(tut2)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Database database = new Database(context);

                        database.insertGradeCalc(1);
                        dialogInterface.dismiss();
                    }
                }).show();

    }

    public static void upcomingWork(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String tut1 = "The upcoming work screen will help you set reminders for yourself about when assignments are due.";
        builder.setTitle("Tutorial")
                .setMessage(tut1)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        uw1(context);
                    }
                }).show();

    }

    public static void uw1(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String tut2 = "Just select a date on the calendar and click the button on the bottom of the screen. On the date and the time you select a reminder will be sent to your phone then to let you know that an assignment is due.";
        builder.setTitle("Tutorial")
                .setMessage(tut2)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Database database = new Database(context);

                        database.insertUpcomintWork(1);
                        dialogInterface.dismiss();
                    }
                }).show();

    }

    public static void semesterList(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Database database = new Database(context);

        builder.setTitle("Tutorial")
                .setMessage("This screen will display all of your past present and future " + database.getSystem() + "'s that you've already created. By swiping one to the left or right, you can edit or delete the " + database.getSystem() + " entirely!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.insertSemesterList(1);
                        dialogInterface.dismiss();
                    }
                }).show();

    }

    public static void assignmentList(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String tut1 = "On the assignment list screen, you see all the listed categories for this class, and its corresponding grade, as well as your class grade a the top!";
        builder.setTitle("Tutorial")
                .setMessage(tut1)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        al1(context);
                    }
                }).show();

    }

    public static void al1(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String tut2 = "By clicking on a category you have the ability to view all the assignments listed under it. By clicking on the button at the bottom you can add assignments to this class, and edit your grade!";
        builder.setTitle("Tutorial")
                .setMessage(tut2)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Database database = new Database(context);

                        database.insertAssignmentList(1);
                        dialogInterface.dismiss();
                    }
                }).show();

    }
}
