package com.example.recyclerviewtest.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATAPASS_TABLE = "pass_table";

    public DBHelper(@Nullable Context context) {
        super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConstants.DATAPASS_CREATE);
        db.execSQL(DBConstants.GROUPS_STRUCTURE);
        db.execSQL(DBConstants.SCHEDULE_STRUCTURE_MONDAY);
        db.execSQL(DBConstants.SCHEDULE_STRUCTURE_TUESDAY);
        db.execSQL(DBConstants.SCHEDULE_STRUCTURE_WEDNESDAY);
        db.execSQL(DBConstants.SCHEDULE_STRUCTURE_THURSDAY);
        db.execSQL(DBConstants.SCHEDULE_STRUCTURE_FRIDAY);
        db.execSQL(DBConstants.SCHEDULE_STRUCTURE_SATURDAY);
        db.execSQL(DBConstants.VALUES_STRUCTURE_CHECKBOXES);
        db.execSQL(DBConstants.MARK_TYPES_STRUCTURE);
        db.execSQL(DBConstants.PERIODS_STRUCTURE);
        db.execSQL(DBConstants.BOUNDS_STRUCTURE);

        ContentValues values = new ContentValues();

        values.put(DBConstants.COLUMN_PASS, DBManager.Encrypt("pspspsps"));
        values.put(DBConstants.COLUMN_HASH, DBManager.bin2hex(DBManager.getHash("pspspsps")));
        values.put(DBConstants.COLUMN_NAME, DBManager.Encrypt("Pashalocha"));
        values.put(DBConstants.COLUMN_SURNAME, DBManager.Encrypt("A_Mozhet_Net"));
        db.insert(DATAPASS_TABLE, null, values);

        for(int i = 0; i<10; i++){ // заполняю таблицу по статистике
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.VALUE, "true");
            db.insert(DBConstants.VALUES_TABLE, null, cv);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBConstants.DROP_DATAPASS_TABLE);
        db.execSQL(DBConstants.DROP_GROUPS_TABLE);
        db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_MONDAY);
        db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_TUESDAY);
        db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_WEDNESDAY);
        db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_THURSDAY);
        db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_FRIDAY);
        db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_SATURDAY);
        db.execSQL(DBConstants.DROP_VALUES_TABLE);
        db.execSQL(DBConstants.DROP_MARK_TYPES_TABLE);
        db.execSQL(DBConstants.DROP_PERIODS_TABLE);
        db.execSQL(DBConstants.DROP_BOUNDS_TABLE);

        onCreate(db);
    }

}
