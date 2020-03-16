package com.example.bustravel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteDatabaseHelper extends SQLiteOpenHelper {

    public static  final String DATABASE_NAME =  "Distance.db";
    public static  final String TABLE_NAME =  "Calculate";


    public SqliteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table "+ TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT ,PLACE TEXT, DISTANCE TEXT, BUS_NO TEXT) ");
        Log.w("Oncreate Warning", "Oncreate: ");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
        Log.w("This is Upgrade warning", "onUpgrade: ");
    }
    public void insertDate(String place, String dist, String busno){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PLACE", place);
        contentValues.put("DISTANCE", dist);
        contentValues.put("BUS_NO", busno);
        long result = db.insert(TABLE_NAME, null, contentValues);
    }
    public long getProfilesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

    public Cursor getValByBusno(String busno){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =   db.rawQuery("select PLACE, DISTANCE, BUS_NO from "+TABLE_NAME+" where BUS_NO"+"="+ busno, null);
        return res;
    }
}