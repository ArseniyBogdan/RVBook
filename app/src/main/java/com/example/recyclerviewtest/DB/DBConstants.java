package com.example.recyclerviewtest.DB;

public class DBConstants {
    public static final String GROUPS_TABLE = "Groups";
    public static final String SCHEDULE_TABLE = "Schedule_";
    public static final String _ID = "id";
    public static final String GROUP_NAME = "Group_name";
    public static final String DB_NAME = "database_for_diary";
    public static final String STUDENT_NAMES = "student_name";
    public static final String DATE = "date";
    public static final String DESCRIPTION = "description";
    public static final String MARK_TYPE = "type";
    public static final String REMINDER = "reminder";
    public static final String WHICHMARK = "mark";
    public static final String MONDAY = "ПН";
    public static final String TUESDAY = "ВТ";
    public static final String WEDNESDAY = "СР";
    public static final String THURSDAY = "ЧТ";
    public static final String FRIDAY = "ПТ";
    public static final String SATURDAY = "СБ";
    public static final String TIME = "time";
    public static final String VALUES_TABLE = "Value";
    public static final String MARKS_TYPES_TABLE = "Marks_Types";
    public static final String PERIODS_TABLE = "Periods";
    public static final String BOUNDS_TABLE = "Bounds";
    public static final String UPPER_BOUND = "Upper_bound";
    public static final String BOTTOM_BOUND = "Bottom_bound";
    public static final String PERIOD = "period";
    public static final String VALUE = "value";
    public static final String MARK = "mark";
    private static final String DATAPASS_TABLE = "pass_table";
    public static final String COLUMN_HASH = "HASH";
    public static final String COLUMN_PASS = "Pass";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SURNAME = "surname";
    public static final int DB_VERSION = 1;

    public static final String DATAPASS_CREATE = "create table IF NOT EXISTS "
            + DATAPASS_TABLE + "(" + _ID + " integer primary key, " +
            COLUMN_NAME + " text not null, " + COLUMN_SURNAME + " text not null, " +
            COLUMN_HASH + " text not null, " + COLUMN_PASS + " text not null " + ");";

    public static final String GROUPS_STRUCTURE = "CREATE TABLE IF NOT EXISTS " +
            GROUPS_TABLE + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + REMINDER + " TEXT" + ")";

    public static final String SCHEDULE_STRUCTURE_MONDAY = "CREATE TABLE IF NOT EXISTS " +
            SCHEDULE_TABLE + MONDAY + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + TIME + " TEXT)";

    public static final String SCHEDULE_STRUCTURE_TUESDAY = "CREATE TABLE IF NOT EXISTS " +
            SCHEDULE_TABLE + TUESDAY + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + TIME+ " TEXT)";

    public static final String SCHEDULE_STRUCTURE_WEDNESDAY = "CREATE TABLE IF NOT EXISTS " +
            SCHEDULE_TABLE + WEDNESDAY + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + TIME+ " TEXT)";

    public static final String SCHEDULE_STRUCTURE_THURSDAY = "CREATE TABLE IF NOT EXISTS " +
            SCHEDULE_TABLE + THURSDAY + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + TIME + " TEXT)";

    public static final String SCHEDULE_STRUCTURE_FRIDAY = "CREATE TABLE IF NOT EXISTS " +
            SCHEDULE_TABLE + FRIDAY + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + TIME + " TEXT)";

    public static final String SCHEDULE_STRUCTURE_SATURDAY = "CREATE TABLE IF NOT EXISTS " +
            SCHEDULE_TABLE + SATURDAY + " (" + _ID + " INTEGER PRIMARY KEY," + GROUP_NAME + " TEXT," + TIME + " TEXT)";

    public static final String VALUES_STRUCTURE_CHECKBOXES = "CREATE TABLE IF NOT EXISTS " +
            VALUES_TABLE + " (" + _ID + " INTEGER PRIMARY KEY," + VALUE + " TEXT)";

    public static final String MARK_TYPES_STRUCTURE = "CREATE TABLE IF NOT EXISTS " +
            MARKS_TYPES_TABLE + " (" + _ID + " INTEGER PRIMARY KEY," + MARK_TYPE + " TEXT," + VALUE+ " TEXT)";

    public static final String PERIODS_STRUCTURE = "CREATE TABLE IF NOT EXISTS " +
            PERIODS_TABLE + " (" + _ID + " INTEGER PRIMARY KEY," + PERIOD + " TEXT," + DATE+ " TEXT)";

    public static final String BOUNDS_STRUCTURE = "CREATE TABLE IF NOT EXISTS " +
            BOUNDS_TABLE + " (" + _ID + " INTEGER PRIMARY KEY," + MARK + " TEXT," + UPPER_BOUND + " TEXT," + BOTTOM_BOUND + " TEXT)";



    public static final String DROP_DATAPASS_TABLE = "DROP TABLE IF EXISTS " + DATAPASS_TABLE;
    public static final String DROP_GROUPS_TABLE = "DROP TABLE IF EXISTS " + GROUPS_TABLE;
    public static final String DROP_SCHEDULE_TABLE_MONDAY = "DROP TABLE IF EXISTS " + SCHEDULE_TABLE + MONDAY;
    public static final String DROP_SCHEDULE_TABLE_TUESDAY = "DROP TABLE IF EXISTS " + SCHEDULE_TABLE + TUESDAY;
    public static final String DROP_SCHEDULE_TABLE_WEDNESDAY = "DROP TABLE IF EXISTS " + SCHEDULE_TABLE + WEDNESDAY;
    public static final String DROP_SCHEDULE_TABLE_THURSDAY = "DROP TABLE IF EXISTS " + SCHEDULE_TABLE + THURSDAY;
    public static final String DROP_SCHEDULE_TABLE_FRIDAY = "DROP TABLE IF EXISTS " + SCHEDULE_TABLE + FRIDAY;
    public static final String DROP_SCHEDULE_TABLE_SATURDAY = "DROP TABLE IF EXISTS " + SCHEDULE_TABLE + SATURDAY;
    public static final String DROP_VALUES_TABLE = "DROP TABLE IF EXISTS " + VALUES_TABLE;
    public static final String DROP_MARK_TYPES_TABLE = "DROP TABLE IF EXISTS " + MARKS_TYPES_TABLE;
    public static final String DROP_PERIODS_TABLE = "DROP TABLE IF EXISTS " + PERIODS_TABLE;
    public static final String DROP_BOUNDS_TABLE = "DROP TABLE IF EXISTS " + BOUNDS_TABLE;
}
