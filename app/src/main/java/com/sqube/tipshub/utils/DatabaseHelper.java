package com.sqube.tipshub.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.sqube.tipshub.utils.CommonsKt.BTTS;
import static com.sqube.tipshub.utils.CommonsKt.CLASSIC;
import static com.sqube.tipshub.utils.CommonsKt.NEWS;
import static com.sqube.tipshub.utils.CommonsKt.OVER;
import static com.sqube.tipshub.utils.CommonsKt.WONGAMES;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DATABASE_NAME = "tipshubDatabase.db";

    private final String TABLE_NAME = "tips_table";
    private final String ID = "_id";
    private final String MARKET = "market";
    private final String TIP_STRING = "tips";

    public DatabaseHelper(Context context) {
        super(context,DATABASE_NAME , null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createDb = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY, " +
                MARKET + " TEXT UNIQUE, " +
                TIP_STRING + " TEXT)";

        sqLiteDatabase.execSQL(createDb);
        insertInitialData(sqLiteDatabase);
    }

    private void insertInitialData(SQLiteDatabase db) {
        ContentValues value = new ContentValues();

        value.put(MARKET, CLASSIC);
        value.put(TIP_STRING, "");
        db.insert(TABLE_NAME, null, value);
        value.clear();

        value.put(MARKET, OVER);
        value.put(TIP_STRING, "");
        db.insert(TABLE_NAME, null, value);
        value.clear();

        value.put(MARKET, BTTS);
        value.put(TIP_STRING, "");
        db.insert(TABLE_NAME, null, value);
        value.clear();

        value.put(MARKET, WONGAMES);
        value.put(TIP_STRING, "");
        db.insert(TABLE_NAME, null, value);
        value.clear();

        value.put(MARKET, NEWS);
        value.put(TIP_STRING, "");
        db.insert(TABLE_NAME, null, value);
        value.clear();
    }

    public void updateTip(SQLiteDatabase db, String market, String tip){
        ContentValues value = new ContentValues();
        value.put(MARKET, market);
        value.put(TIP_STRING, tip);

        db.update(TABLE_NAME, value, MARKET+ "=?", new String[] {market});

    }

    public String getTip(SQLiteDatabase db, String market){
        String Selection = MARKET + " =?";
        String[] SelectionArgs = {market};
        Cursor cursor = db.query(TABLE_NAME, new String[] {TIP_STRING}, Selection, SelectionArgs, null, null, null);
        if(!cursor.moveToFirst())
            return  "";
        else
            return cursor.getString(0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}
