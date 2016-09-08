package zerotek.gradecalculator.Calculations;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import java.util.ArrayList;

import zerotek.gradecalculator.Database.Database;

/**
 * Created by Mason on 4/26/2016.
 */
public class GradeCalculations {
    double categoryTotals;
    private ArrayList<Double> catGrades = new ArrayList<Double>();
    private ArrayList<Double> categoryUsed = new ArrayList<Double>();
    private Database database;
    private Cursor classRes, categoryRes, workCatRes;

    public String letterGrade(int calculation) {
        if (calculation <= 59) return "F";
        else if (calculation <= 69 && calculation >= 60) return "D";
        else if (calculation <= 79 && calculation >= 70) return "C";
        else if (calculation <= 89 && calculation >= 80) return "B";
        else if (calculation <= 100 && calculation >= 90 || calculation > 100) return "A";
        else return null;
    }


    public double classGrade(Context context, int classID, boolean weighted) {

        double finalGrade = 0;
        int amountUsed = 0;
        clear();


        database = new Database(context);
        classRes = database.getClass(classID);
        categoryRes = database.getCategories(classID);

        classRes.moveToFirst();

        categoryRes.moveToFirst();

        if (weighted) {

            //category with 20% and a category with 30% the total of the class is .5 and the two are .2 and .3 you figure out what you got out of the .2 and .3
            //and add them together to get your grade


            //adds all the totals of USED categories
            classRatio(classID, categoryRes.getInt(0));

            while (categoryRes.moveToNext()) {
                classRatio(classID, categoryRes.getInt(0));
            }

            for (int i = 0; i < categoryUsed.size(); i++) {
                categoryTotals += categoryUsed.get(i);
            }

            //adds work grades after getting cat totals
            categoryRes.moveToFirst();
            if (getCatUsed(classID, categoryRes.getInt(0)) == true) {
                amountUsed++;
                catGrades.add(categoryPercentGrade(context, classID, categoryRes.getInt(0)));
            }
            while (categoryRes.moveToNext()) {

                if (getCatUsed(classID, categoryRes.getInt(0)) == true) {
                    amountUsed++;
                    catGrades.add(categoryPercentGrade(context, classID, categoryRes.getInt(0)));
                }
            }


            for (int i = 0; i < catGrades.size(); i++) {
                finalGrade += catGrades.get(i);
            }
            if (amountUsed != 0) return (finalGrade) * 100;
            else return -1;


        } else {
            amountUsed = 0;
            try {
                catGrades.add(CategoryPointGrade(context, classID, categoryRes.getInt(0)));
                amountUsed++;
            } catch (CursorIndexOutOfBoundsException e) {

                System.out.print("Category not being used yet");
            }
            while (categoryRes.moveToNext()) {
                try {
                    catGrades.add(CategoryPointGrade(context, classID, categoryRes.getInt(0)));
                    amountUsed++;
                } catch (CursorIndexOutOfBoundsException e) {
                    System.out.print("Category not being used yet");
                }

            }

            for (int i = 0; i < catGrades.size(); i++) {
                finalGrade += catGrades.get(i);
            }
            if (amountUsed != 0) return (finalGrade) * 100;
            else return -1;

        }


    }


    private double categoryPercentGrade(Context context, int classID, int catID) {
        database = new Database(context);
        double maxPoints = 0, earnedPoints = 0;
        workCatRes = database.getWorkFromCategory(classID, catID);
        Cursor category = database.getCurrentCategory(catID);
        category.moveToFirst();

        workCatRes.moveToFirst();
        earnedPoints += workCatRes.getInt(2);
        maxPoints += workCatRes.getInt(3);
        while (workCatRes.moveToNext()) {
            earnedPoints += workCatRes.getInt(2);
            maxPoints += workCatRes.getInt(3);
        }


        return ((earnedPoints / maxPoints) * (category.getDouble(2) / categoryTotals));


    }


    public double CategoryPointGrade(Context context, int classID, int catID) {
        database = new Database(context);
        double maxPoints = 0, earnedPoints = 0;
        workCatRes = database.getWorkFromCategory(classID, catID);
        Cursor category = database.getCurrentCategory(catID);
        category.moveToFirst();

        workCatRes.moveToFirst();
        earnedPoints += workCatRes.getInt(2);
        maxPoints += workCatRes.getInt(3);
        while (workCatRes.moveToNext()) {
            earnedPoints += workCatRes.getInt(2);
            maxPoints += workCatRes.getInt(3);
        }


        return (earnedPoints / maxPoints);


    }


    public void classRatio(int classID, int catID) {
        Cursor res = database.getCurrentCategory(catID);
        res.moveToFirst();

        if (getCatUsed(classID, catID)) categoryUsed.add(res.getDouble(2));

    }

    public boolean getCatUsed(int classID, int catID) {

        try {
            Cursor wCatRes = database.getWorkFromCategory(classID, catID);
            wCatRes.moveToFirst();
            wCatRes.getInt(2);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }
    }


    public void clear() {
        catGrades.clear();
        categoryUsed.clear();

    }

}
