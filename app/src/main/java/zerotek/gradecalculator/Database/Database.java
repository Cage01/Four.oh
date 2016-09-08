package zerotek.gradecalculator.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Mason on 10/28/2015.
 */
public class Database extends SQLiteOpenHelper {

    //Database and Tables
    public static final String DATABASE_NAME = "class.db";
    public static final String TABLE_NAME = "class_table";
    public static final String CATEGORY_TABLE = "category_table";
    public static final String WORK_TABLE = "work_table";
    public static final String SEMESTER_TABLE = "semester_table";
    public static final String EXTRAS_TABLE = "extras_table";

    //ID
    public static final String ID = "ID";
    public static final String category_id = "CATEGORY_ID";
    public static final String class_id = "CLASS_ID";
    public static final String SEMESTER_ID = "SEMESTER_ID";


    //Semester Table Columns
    public static final String SEMESTER_NAME = "SEMESTER_NAME";
    public static final String SEMESTER_START = "SEMESTER_START";
    public static final String SEMESTER_END = "SEMESTER_END";


    //Class Table Columns
    public static final String class_name = "NAME";
    public static final String weighted = "WEIGHTED";


    //Weight Table Columns
    public static final String category_name = "CATEGORY_NAME";
    public static final String category_percent_points = "PERCENT_POINTS";


    //Work Table Columns
    public static final String work_name = "WORK_NAME";
    public static final String points_earned = "POINTS_EARNED";
    public static final String max_points = "MAX_POINTS";
    public static final String due_date = "DUE_DATE";


    //Extra Columns
    public static final String QUARTER_OR_SEMESTER = "QUARTER_OR_SEMESTER";
    public static final String HOME_SCREEN = "HOME_SCREEN";
    public static final String CREATE_CLASS_SCREEN = "CREATE_CLASS_SCREEN";
    public static final String GRADE_CALCULATOR_SCREEN = "GRADE_CALCULATOR_SCREEN";
    public static final String SEMESTER_LIST_SCREEN = "SEMESTER_LIST_SCREEN";
    public static final String UPCOMING_WORK_SCREEN = "UPCOMING_WORK_SCREEN";
    public static final String ASSIGNMENT_LIST_SCREEN = "ASSIGNMENT_LIST_SCREEN";

    private Cursor
            semesters,
            classes,
            classBySemester,
            weights,
            work,
            findClassID,
            findSemesterID,
            findCatID,
            findWorkID,
            currentSemesterDate,
            getSpecCategory,
            schoolSystem,
            currentSemesterClasses;


    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");


        db.execSQL("create table " + EXTRAS_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + QUARTER_OR_SEMESTER + " TEXT, " + HOME_SCREEN + " INTEGER, " + CREATE_CLASS_SCREEN + " INTEGER, " + GRADE_CALCULATOR_SCREEN + " INTEGER, " + SEMESTER_LIST_SCREEN + " INTEGER, " + UPCOMING_WORK_SCREEN + " INTEGER, " + ASSIGNMENT_LIST_SCREEN + " INTEGER)");


        db.execSQL("create table " + SEMESTER_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SEMESTER_NAME + " TEXT, " + SEMESTER_START +
                " TEXT, " + SEMESTER_END + " TEXT)");//semester table


        db.execSQL("create table " + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + class_name + " TEXT, " + weighted + " INTEGER, " + SEMESTER_ID + " INTEGER," +
                "FOREIGN KEY (" + SEMESTER_ID + ") REFERENCES " + SEMESTER_TABLE + " (" + ID + "))");//class table


        //category table that contains the categories and the foriegn key to the associated class
        db.execSQL("create table " + CATEGORY_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + category_name + " TEXT, " + category_percent_points + " REAL, " + class_id + " INTEGER, " + SEMESTER_ID + " INTEGER," +
                "FOREIGN KEY(" + SEMESTER_ID + ")REFERENCES " + SEMESTER_TABLE + " (" + ID + "), FOREIGN KEY (" + class_id + ") REFERENCES " + TABLE_NAME + " (" + ID + "))");


        //work table contains work for associated category and class
        db.execSQL("create table " + WORK_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + work_name + " TEXT, " + points_earned + " REAL, " + max_points + " REAL, " + due_date + " TEXT, " +
                "" + category_id + " INTEGER, " + class_id + "," + SEMESTER_ID + " INTEGER, FOREIGN KEY(" + SEMESTER_ID + ") REFERENCES " + SEMESTER_TABLE + " (" + ID + "), FOREIGN KEY (" + category_id + ") REFERENCES " + CATEGORY_TABLE + " (" + ID + "), " +
                "FOREIGN KEY (" + class_id + ") REFERENCES " + TABLE_NAME + " (" + ID + "))");


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

//is creating a new table for each weight type for the one particular class

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WORK_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SEMESTER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXTRAS_TABLE);


        onCreate(db);


    }

    /**
     * ------------------------INSERT------------------------------
     */

    public void insertSemester(String name, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SEMESTER_NAME, name);
        contentValues.put(SEMESTER_START, startDate);
        contentValues.put(SEMESTER_END, endDate);

        try {
            db.insert(SEMESTER_TABLE, null, contentValues);
            System.out.println("Semester was Successfully Inserted");
        } catch (Exception e) {
            System.out.println("Semester was not inserted Database Class");
        }
    }

    public void insertSystem(String system) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(QUARTER_OR_SEMESTER, system);

        try {
            db.insert(EXTRAS_TABLE, null, contentValues);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

    public void insertClass(String name, Integer semesterID, Integer typeI) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        contentValues.put(class_name, name);
        contentValues.put(SEMESTER_ID, semesterID);
        contentValues.put(weighted, typeI);


        try {
            db.insert(TABLE_NAME, null, contentValues);
            System.out.println("Class was Successfully Inserted");
        } catch (Exception e) {
            System.out.println("Class was not inserted Database Class");
        }


    }

    //adds the categories to database
    public void insertCategory(String name, double percent, int classID, Integer semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (name != null) contentValues.put(category_name, name);
        contentValues.put(category_percent_points, percent);
        contentValues.put(class_id, classID);
        contentValues.put(SEMESTER_ID, semesterID);

        try {
            db.insert(CATEGORY_TABLE, null, contentValues);
            System.out.println("Weight was Successfully Inserted");
        } catch (Exception e) {
            System.out.println("Weight was not inserted Database Class");
        }

    }

    //adds assignments to database
    public void insertWork(String name, int pointsEarned, int maxPoints, Integer weightID, int classID, Integer semesterID, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (name != null) contentValues.put(work_name, name);
        if (maxPoints != 0) contentValues.put(max_points, maxPoints);
        contentValues.put(points_earned, pointsEarned);
        if (weightID != null) contentValues.put(category_id, weightID);
        contentValues.put(class_id, classID);
        contentValues.put(SEMESTER_ID, semesterID);

        if (dueDate != null) {
            contentValues.put(due_date, dueDate);
        } else {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currDate = dateFormat.format(date.getTime());

            contentValues.put(due_date, currDate);
        }


        try {
            db.insert(WORK_TABLE, null, contentValues);
            System.out.println("Work was Successfully Inserted");
        } catch (Exception e) {
            System.out.println("Work was not inserted Database Class");
        }

    }


    //reads the number of rows in the class table
    public int numOfClasses() {
        SQLiteDatabase db = this.getWritableDatabase();
        int numRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + TABLE_NAME, null);
        return numRows;
    }

    public int numOfSemesters() {
        SQLiteDatabase db = this.getWritableDatabase();
        int numRows = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + SEMESTER_TABLE, null);
        return numRows;
    }

    /**
     * --------------------REMOVE------------------------
     */
//removes the class from the database
    public void removeSemester(int rowNum) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + WORK_TABLE + " where " + SEMESTER_ID + " = " + rowNum);
        db.execSQL("delete from " + CATEGORY_TABLE + " where " + SEMESTER_ID + " = " + rowNum);
        db.execSQL("delete from " + TABLE_NAME + " where " + SEMESTER_ID + " = " + rowNum);
        db.execSQL("delete from " + SEMESTER_TABLE + " where " + ID + " = " + rowNum);

    }

    public void removeClass(int rowNum) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + WORK_TABLE + " where " + class_id + " = " + rowNum);
        db.execSQL("delete from " + CATEGORY_TABLE + " where " + class_id + " = " + rowNum);
        db.execSQL("delete from " + TABLE_NAME + " where " + ID + " = " + rowNum);


    }

    //removes the categories from database
    public void removeCategory(int categoryID) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + WORK_TABLE + " where " + category_id + " = " + categoryID);
        db.execSQL("delete from " + CATEGORY_TABLE + " where " + ID + " = " + categoryID);

    }

    //removes work from the database
    public void removeWork(int workID, int categoryID, int classID, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + WORK_TABLE + " where " + ID + " = " + workID + " and " + category_id + " = " + categoryID + " and " + class_id + " = " + classID + " AND " + SEMESTER_ID + " = " + semesterID);
    }


    /**
     * ------------------------GET----------------------
     */
//gets all semesters
    public Cursor getSemesters() {
        SQLiteDatabase db = this.getWritableDatabase();
        semesters = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " ORDER BY " + SEMESTER_START + " ASC", null);

        return semesters;
    }

    public Cursor getSemester(int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        semesters = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " where " + ID + " = " + semesterID + " ORDER BY " + SEMESTER_START + " ASC", null);

        return semesters;
    }

    public String getSystem() {
        SQLiteDatabase db = this.getWritableDatabase();

        schoolSystem = db.rawQuery("SELECT * FROM " + EXTRAS_TABLE, null);
        schoolSystem.moveToFirst();
        return schoolSystem.getString(1);
    }


    //reads classes
    public Cursor getClasses() {
        SQLiteDatabase db = this.getWritableDatabase();
        classes = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        return classes;
    }

    public Cursor getClass(int ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        classes = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ID = " + ID, null);
        return classes;
    }

    public String getClassText(int classID) {
        SQLiteDatabase db = this.getWritableDatabase();
        classes = db.rawQuery("SELECT " + class_name + " FROM " + TABLE_NAME + " WHERE ID = " + classID, null);
        classes.moveToFirst();
        return classes.getString(0);
    }

    public Cursor getChosenClasses(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        classBySemester = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + SEMESTER_ID + " = " + id, null);
        return classBySemester;
    }

    public Cursor getCurrentCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        getSpecCategory = db.rawQuery("SELECT * FROM " + CATEGORY_TABLE + " WHERE " + ID + " = " + id, null);

        return getSpecCategory;
    }


    public Cursor getCurrentClasses() {
        SQLiteDatabase db = this.getWritableDatabase();
        currentSemesterClasses = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + SEMESTER_ID + " = " + getCurrentSemesterDate(), null);

        return currentSemesterClasses;
    }


    //reads categories
    public Cursor getCategories(int classID) {
        SQLiteDatabase db = this.getWritableDatabase();
        weights = db.rawQuery("SELECT * FROM " + CATEGORY_TABLE + " WHERE " + class_id + " = " + classID, null);

        return weights;

    }

    //reads work
    public Cursor getWork(int classID) {
        SQLiteDatabase db = this.getWritableDatabase();
        work = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + class_id + " = " + classID, null);

        return work;
    }


    public Cursor getWorkFromCategory(int classID, int catID) {
        SQLiteDatabase db = this.getWritableDatabase();
        work = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + class_id + " = " + classID + " AND " + category_id + " = " + catID + " AND " + due_date + " <= date() AND " + points_earned + " > -1 ORDER BY " + due_date + " ASC", null);

        return work;
    }

    public String getCategoryString(int categoryID, int classID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + category_name + " from " + CATEGORY_TABLE + " where " + ID + " = " + categoryID + " and " + class_id + " = " + classID, null);
        cursor.moveToFirst();

        return cursor.getString(0);

    }

    public Cursor getSelectedWork(int workID, int categoryID, int classID, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        work = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + ID + " = " + workID + " AND " + category_id + " = " + categoryID + " AND " + class_id + " = " + classID + " AND " + SEMESTER_ID + " = " + semesterID, null);

        return work;
    }



    public Cursor getAllUpcomingWork() {
        SQLiteDatabase db = this.getWritableDatabase();
        work = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + due_date + " >= date() AND " + points_earned + " = -1 ORDER BY " + due_date + " ASC", null);

        return work;
    }

    public Cursor getTodaysWork() {
        SQLiteDatabase db = this.getWritableDatabase();
        work = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + due_date + " <= date() AND " + points_earned + " = -1.0", null);

        return work;
    }

    //reads the text input and scans the database for an entry with the same input
    //used to get the id of the particular class to input categories and work correctly
    public int getClassID(String input, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        findClassID = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + class_name + " LIKE  '%" + input + "%' AND " + SEMESTER_ID + " = " + semesterID, null);
        findClassID.moveToFirst();
        return Integer.parseInt(findClassID.getString(0));
    }

    public boolean duplicateSemester(String input) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            findClassID = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_NAME + " LIKE  '%" + input + "%'", null);
            findClassID.moveToFirst();
            findClassID.getInt(0);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }
    }


    public boolean duplicateClass(String input, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            findClassID = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + class_name + " LIKE  '%" + input + "%' AND " + SEMESTER_ID + " = " + semesterID, null);
            findClassID.moveToFirst();
            findClassID.getInt(0);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean duplicateCategory(String input, int classID, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            findClassID = db.rawQuery("SELECT * FROM " + CATEGORY_TABLE + " WHERE " + category_name + " LIKE  '%" + input + "%' AND " + SEMESTER_ID + " = " + semesterID + " AND " + class_id + " = " + classID, null);
            findClassID.moveToFirst();
            findClassID.getInt(0);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean duplicateWork(String input, int categoryID, int classID, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            findClassID = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + work_name + " =  '%" + input + "%' AND " + SEMESTER_ID + " = " + semesterID + " AND " + class_id + " = " + classID
                    + " AND " + category_id + " = " + categoryID, null);
            findClassID.moveToFirst();
            findClassID.getInt(0);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }
    }

    public int getSemesterID(String input) {
        SQLiteDatabase db = this.getWritableDatabase();
        findSemesterID = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_NAME + " LIKE '%" + input + "%'", null);
        findSemesterID.moveToFirst();
        return Integer.parseInt(findSemesterID.getString(0));
    }

    public String getSemesterName(int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor semester = db.rawQuery("SELECT " + SEMESTER_NAME + " FROM " + SEMESTER_TABLE + " WHERE " + ID + " = " + semesterID, null);
        semester.moveToFirst();
        return semester.getString(0);
    }

    public int getCategoryID(String input, int classID, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        findCatID = db.rawQuery("SELECT * FROM " + CATEGORY_TABLE + " WHERE " + category_name + " LIKE '%" + input + "%' AND " + class_id + " = " + classID + " AND " + SEMESTER_ID + " = " + semesterID, null);
        findCatID.moveToFirst();
        return findCatID.getInt(0);
    }

    public int getWorkID(String input, int classID, int categoryID, int semesterID) {
        SQLiteDatabase db = this.getWritableDatabase();
        findWorkID = db.rawQuery("SELECT * FROM " + WORK_TABLE + " WHERE " + work_name + " LIKE '%" + input + "%' AND " + class_id + " = " + classID
                + " AND " + category_id + " = " + categoryID + " AND " + SEMESTER_ID + " = " + semesterID, null);
        findWorkID.moveToFirst();
        return Integer.parseInt(findWorkID.getString(0));
    }


    //gets the current semester based on date
    //if the current date is before the end date and after the start date it selects the appropriate semester
    public int getCurrentSemesterDate() {
        SQLiteDatabase db = this.getWritableDatabase();


        currentSemesterDate = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_END + " >= date() AND " + SEMESTER_START +
                " <= date()", null);
        currentSemesterDate.moveToFirst();

        return currentSemesterDate.getInt(0);
    }

    public Cursor getPreviousOrNextSemester() {
        SQLiteDatabase db = this.getWritableDatabase();


        currentSemesterDate = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_END + " < date()", null);

        return currentSemesterDate;

    }

    public boolean checkForPreviousSemester() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            currentSemesterDate = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_END + " < date()", null);
            currentSemesterDate.moveToFirst();
            currentSemesterDate.getInt(0);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }

    }

    public boolean checkForNextSemester() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            currentSemesterDate = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_START + " > date()", null);
            currentSemesterDate.moveToFirst();
            currentSemesterDate.getInt(0);
            return true;
        } catch (CursorIndexOutOfBoundsException e) {
            return false;
        }
    }

    public Cursor getNextSemester() {
        SQLiteDatabase db = this.getWritableDatabase();

        currentSemesterDate = db.rawQuery("SELECT * FROM " + SEMESTER_TABLE + " WHERE " + SEMESTER_START + " > date()", null);
        return currentSemesterDate;
    }


    /**
     * ------------------------------UPDATE SEMESTER-----------------------------------
     */
    public void updateSemester(int uID, String name, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

       if(name != null) contentValues.put(SEMESTER_NAME, name);
       if(startDate != null) contentValues.put(SEMESTER_START, startDate);
       if(endDate != null) contentValues.put(SEMESTER_END, endDate);

        db.update(SEMESTER_TABLE, contentValues, ID + " = " + uID, null);
    }


    /**
     * ------------------------------UPDATE CLASS------------------------------------
     */

    public void updateClassName(int uID, String name, Boolean type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (name != null) contentValues.put(class_name, name);
        if (type != null) contentValues.put(weighted, type);

        db.update(TABLE_NAME, contentValues, ID + " = " + uID, null);
    }


    /**
     * --------------------------------UPDATE CATEGORY---------------------------
     */

    public void updateCategoryName(int uID, String name, Double input) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (name != null) contentValues.put(category_name, name);
        if (input != null) contentValues.put(category_percent_points, input);

        db.update(CATEGORY_TABLE, contentValues, ID + " = " + uID, null);
    }


    /**
     * ----------------------------------UPDATE WORK-------------------------------
     */
    public void updateWork(int workID, Integer categoryID, String name, Integer pointsEarned, Integer maxPoints, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if (name != null) contentValues.put(work_name, name);
        if (pointsEarned != null) contentValues.put(points_earned, pointsEarned);
        if (maxPoints != null) contentValues.put(max_points, maxPoints);
        if (dueDate != null) contentValues.put(due_date, dueDate);
        if (categoryID != null) contentValues.put(category_id, categoryID);

        db.update(WORK_TABLE, contentValues, ID + " = " + workID, null);
    }


    /**
     * ---------------------------------------SHOW TUTORIALS------------------------------------------
     */

    public int getHomeScreen() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + HOME_SCREEN + " FROM " + EXTRAS_TABLE + " where " + ID + " = 1", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getCreateClass() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + CREATE_CLASS_SCREEN + " FROM " + EXTRAS_TABLE + " where " + ID + " = 1", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getGradeCalc() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + GRADE_CALCULATOR_SCREEN + " FROM " + EXTRAS_TABLE + " where " + ID + " = 1", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getUpcomingWork() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + UPCOMING_WORK_SCREEN + " FROM " + EXTRAS_TABLE + " where " + ID + " = 1", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getSemesterList() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + SEMESTER_LIST_SCREEN + " FROM " + EXTRAS_TABLE + " where " + ID + " = 1", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public int getAssignmentList() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + ASSIGNMENT_LIST_SCREEN + " FROM " + EXTRAS_TABLE + " where " + ID + " = 1", null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    /**
     * -------------------------------------HIDE TUTORIALS-------------------------------------
     */

    public void insertHome(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(HOME_SCREEN, value);
        //db.update(WORK_TABLE, contentValues, ID + " = " + workID, null);

        try {
            db.update(EXTRAS_TABLE, contentValues, ID + " = 1", null);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

    public void insertCreateClass(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(CREATE_CLASS_SCREEN, value);

        try {
            db.update(EXTRAS_TABLE, contentValues, ID + " = 1", null);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

    public void insertGradeCalc(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(GRADE_CALCULATOR_SCREEN, value);

        try {
            db.update(EXTRAS_TABLE, contentValues, ID + " = 1", null);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

    public void insertUpcomintWork(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(UPCOMING_WORK_SCREEN, value);

        try {
            db.update(EXTRAS_TABLE, contentValues, ID + " = 1", null);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

    public void insertSemesterList(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SEMESTER_LIST_SCREEN, value);

        try {
            db.update(EXTRAS_TABLE, contentValues, ID + " = 1", null);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

    public void insertAssignmentList(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ASSIGNMENT_LIST_SCREEN, value);

        try {
            db.update(EXTRAS_TABLE, contentValues, ID + " = 1", null);
            System.out.print("System inserted successfully");
        } catch (Exception e) {
            System.out.print("System was not inserted Database Class");
        }
    }

}

