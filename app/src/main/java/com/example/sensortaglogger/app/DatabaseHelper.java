package com.example.sensortaglogger.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.util.ArrayList;


/**
 * Created by Brendan on 11/06/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ActivityMon";

    // Contacts table name
    private static final String TABLE_SUMMARY = "summary";

    // Contacts Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_LENGTH = "length";
    private static final String KEY_STEPS = "steps";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACTIVITYMON_TABLE = "CREATE TABLE " + TABLE_SUMMARY + "("
                + KEY_NAME + " TEXT PRIMARY KEY," + KEY_LENGTH + " INTEGER,"
                + KEY_STEPS + " INTEGER" + ")";
        db.execSQL(CREATE_ACTIVITYMON_TABLE);

        // Populate initial data
        String INSERT_PREFIX = "INSERT INTO " + TABLE_SUMMARY + "(" + KEY_NAME + ", " + KEY_LENGTH
                + ", " + KEY_STEPS + ") VALUES";
        db.execSQL(INSERT_PREFIX + "('Sitting', 0, 0)");
        db.execSQL(INSERT_PREFIX + "('Standing', 0, 0)");
        db.execSQL(INSERT_PREFIX + "('Walking', 0, 0)");
        db.execSQL(INSERT_PREFIX + "('Running', 0, 0)");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUMMARY);

        // Create tables again
        onCreate(db);
    }

    public void addSummary(BehaviourSummary bs) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, bs.getName());
        values.put(KEY_LENGTH, bs.length);
        values.put(KEY_STEPS, bs.numSteps);

        // Inserting Row
        db.insert(TABLE_SUMMARY, null, values);
        db.close(); // Closing database connection
    }

    public int updateSummary(BehaviourSummary bs) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, bs.getName());
        values.put(KEY_LENGTH, bs.length);
        values.put(KEY_STEPS, bs.numSteps);

        return db.update(TABLE_SUMMARY, values, KEY_NAME + " = ?", new String[] {bs.getName()});
    }

    public ArrayList<BehaviourSummary> getAllSummaries() {
        ArrayList<BehaviourSummary> summaries = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_SUMMARY;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BehaviourSummary bs = new BehaviourSummary(cursor.getString(0),
                        Long.parseLong(cursor.getString(1)), Integer.parseInt(cursor.getString(2)));
                summaries.add(bs);
            } while (cursor.moveToNext());
        }

        return summaries;
    }

}
