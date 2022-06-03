package com.example.recyclerviewtest.DB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DBManager {

    private final DBHelper dbhelper;
    private SQLiteDatabase db;

    private String typeText, descriptionText;


    //шифрование
    private static final String DATAPASS_TABLE = "pass_table";
    private static String key = "pspspsps";
    public static String vector="4444fhfhdjdjfjfjfkfffwr4t45464";
    private static final String block="fhgodksr";

    public static byte[] getHash(String password) {
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        assert digest != null;
        digest.reset();
        return digest.digest(password.getBytes());
    }

    public static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
    }

    //шифровка
    public static String Encrypt(String str)
    {
        SecretKeySpec sks = null;
        try {
            sks = new SecretKeySpec(genKey(key), "AES");
        } catch (Exception e) {
            Log.e("Crypto", "AES secret key spec error-1");
        }
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParams = new IvParameterSpec(block.getBytes());
            c.init(Cipher.ENCRYPT_MODE, sks, ivParams);
            encodedBytes = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e("Crypto", "AES encryption error");
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);

    }

    public static String EncryptWithCustomKey(String str, String key)
    {
        SecretKeySpec sks = null;
        try {
            sks = new SecretKeySpec(genKey(key), "AES");
        } catch (Exception e) {
            Log.e("Crypto", "AES secret key spec error-1");
        }
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParams = new IvParameterSpec(block.getBytes());
            c.init(Cipher.ENCRYPT_MODE, sks, ivParams);
            encodedBytes = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e("Crypto", "AES encryption error");
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);

    }

    //дешифровка
    public static String Decrypt (String str) {
        SecretKeySpec sks = null;
        byte[] encryptedBytes=Base64.decode(str, Base64.DEFAULT);
        try {
            sks = new SecretKeySpec(genKey(key), "AES");
        } catch (Exception e) {
            Log.e("Crypto", "AES secret key spec error-2");
        }
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParams = new IvParameterSpec(block.getBytes());
            c.init(Cipher.DECRYPT_MODE, sks, ivParams);
            decodedBytes = c.doFinal(encryptedBytes);
        } catch (Exception e) {
            Log.e("Crypto", "AES decryption error");
        }
        return new String (decodedBytes, StandardCharsets.UTF_8);
    }

    public static String DecryptWithCustomKey (String str, String key){
        SecretKeySpec sks = null;
        byte[] encryptedBytes=Base64.decode(str, Base64.DEFAULT);
        try {
            sks = new SecretKeySpec(genKey(key), "AES");
        } catch (Exception e) {
            Log.e("Crypto", "AES secret key spec error-2");
        }
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParams = new IvParameterSpec(block.getBytes());
            c.init(Cipher.DECRYPT_MODE, sks, ivParams);
            decodedBytes = c.doFinal(encryptedBytes);
        } catch (Exception e) {
            Log.e("Crypto", "AES decryption error");
        }
        return new String (decodedBytes, StandardCharsets.UTF_8);
    }

    public static byte[] genKey(String str_key) {
        int iterationCount = 100;
        int keyLength = 128;
        int saltLength =  8;

        byte[] salt = new byte[saltLength];
        KeySpec keySpec = new PBEKeySpec(str_key.toCharArray(), salt,
                iterationCount, keyLength);
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] keyBytes = new byte[0];
        try {
            assert keyFactory != null;
            keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return keyBytes;
    }

    // замена шифра
    public void updateTablePass (String password_before, String password_after)
    {
        DecryptAndEncryptAllDB(password_before, password_after);
        ContentValues values = new ContentValues();
        values.put(DBConstants.COLUMN_HASH, bin2hex(getHash(password_after)));
        key = "pspspsps";
        values.put(DBConstants.COLUMN_PASS, Encrypt(password_after));
        key = password_after;


        db.update(DATAPASS_TABLE, values, DBConstants._ID + "=" + 1, null);

        // полная дешифровка БД старым ключом и шифровка новым

    }

    public void DecryptAndEncryptAllDB(String decrypt_key, String encrypt_key){
        ArrayList<String> GroupList = GetAllGroups();
        ArrayList<String> Reminders = GetAllReminders();

        // перезаписываем таблицу с расписанием
        for(int i = 0; i < 6 ;i++){
            switch (i){
                case 0: DecryptAndEncryptSchedule("ПН", encrypt_key); break;
                case 1: DecryptAndEncryptSchedule("ВТ", encrypt_key); break;
                case 2: DecryptAndEncryptSchedule("СР", encrypt_key); break;
                case 3: DecryptAndEncryptSchedule("ЧТ", encrypt_key); break;
                case 4: DecryptAndEncryptSchedule("ПТ", encrypt_key); break;
                case 5: DecryptAndEncryptSchedule("СБ", encrypt_key); break;
            }
        }

        //перезаписываем таблицу с типами работ
        ArrayList<String> TypesOfMarks = GetAllTypesOfMarksOfGroups();
        ArrayList<String> ValuesOfMarks = GetAllValuesOFMarks();

        db.execSQL(DBConstants.DROP_MARK_TYPES_TABLE);
        db.execSQL(DBConstants.MARK_TYPES_STRUCTURE);

        for (int i = 0; i<TypesOfMarks.size(); i++){
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.MARK_TYPE, EncryptWithCustomKey(TypesOfMarks.get(i), encrypt_key));
            cv.put(DBConstants.VALUE, EncryptWithCustomKey(ValuesOfMarks.get(i), encrypt_key));
            db.insert(DBConstants.MARKS_TYPES_TABLE, null, cv);
        }

        //перезаписываем таблицу с временными периодами
        ArrayList<String> DatesOfPeriods = GetAllDateOfPeriods();
        ArrayList<String> NameOfPeriods = GetAllPeriods();

        db.execSQL(DBConstants.DROP_PERIODS_TABLE);
        db.execSQL(DBConstants.PERIODS_STRUCTURE);

        for(int i = 0; i < DatesOfPeriods.size(); i++){
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.DATE, EncryptWithCustomKey(DatesOfPeriods.get(i), encrypt_key));
            cv.put(DBConstants.PERIOD, EncryptWithCustomKey(NameOfPeriods.get(i), encrypt_key));
            db.insert(DBConstants.PERIODS_TABLE, null, cv);
        }

        //перезаписываем таблицу с х-ми типов работ

        ContentValues cv1 = new ContentValues();
        cv1.put(DBConstants.MARK, EncryptWithCustomKey("5", encrypt_key));
        cv1.put(DBConstants.UPPER_BOUND, EncryptWithCustomKey(GetBound(DBConstants.UPPER_BOUND, "5"), encrypt_key));
        cv1.put(DBConstants.BOTTOM_BOUND, EncryptWithCustomKey(GetBound(DBConstants.BOTTOM_BOUND, "5"), encrypt_key));

        ContentValues cv2 = new ContentValues();
        cv2.put(DBConstants.MARK, EncryptWithCustomKey("4", encrypt_key));
        cv2.put(DBConstants.UPPER_BOUND, EncryptWithCustomKey(GetBound(DBConstants.UPPER_BOUND, "4"), encrypt_key));
        cv2.put(DBConstants.BOTTOM_BOUND, EncryptWithCustomKey(GetBound(DBConstants.BOTTOM_BOUND, "4"), encrypt_key));

        ContentValues cv3 = new ContentValues();
        cv3.put(DBConstants.MARK, EncryptWithCustomKey("3", encrypt_key));
        cv3.put(DBConstants.UPPER_BOUND, EncryptWithCustomKey(GetBound(DBConstants.UPPER_BOUND, "3"), encrypt_key));
        cv3.put(DBConstants.BOTTOM_BOUND, EncryptWithCustomKey(GetBound(DBConstants.BOTTOM_BOUND, "3"), encrypt_key));

        db.execSQL(DBConstants.DROP_BOUNDS_TABLE);
        db.execSQL(DBConstants.BOUNDS_STRUCTURE);

        db.insert(DBConstants.BOUNDS_TABLE, null, cv1);
        db.insert(DBConstants.BOUNDS_TABLE, null, cv2);
        db.insert(DBConstants.BOUNDS_TABLE, null, cv3);

        // перезапись таблицы с учениками
        for(int i = 0; i < GroupList.size(); i++){
            DecryptAndEncryptTablesOfGroup(GroupList.get(i), encrypt_key);
        }

        // перезаписываем таблицу с группами
        db.execSQL(DBConstants.DROP_GROUPS_TABLE);
        db.execSQL(DBConstants.GROUPS_STRUCTURE);

        for (int i = 0; i<GroupList.size(); i++){
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.GROUP_NAME, EncryptWithCustomKey(GroupList.get(i), encrypt_key));
            cv.put(DBConstants.REMINDER, EncryptWithCustomKey(Reminders.get(i), encrypt_key));
            db.insert(DBConstants.GROUPS_TABLE, null, cv);
        }
    }

    @SuppressLint("Range")
    public void DecryptAndEncryptTablesOfGroup(String name_group, String key){
        ArrayList<String> StudentNames = GetAllStudents(name_group);
        String Table_id = GetTableID(name_group);

        db.execSQL("DROP TABLE IF EXISTS " + "GROUP_TABLE_" + Table_id);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "GROUP_TABLE_" +
                Table_id + " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.STUDENT_NAMES + " TEXT)");

        for(int i = 0; i < StudentNames.size(); i++){
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.STUDENT_NAMES, EncryptWithCustomKey(StudentNames.get(i), key));
            db.insert("GROUP_TABLE_" + Table_id, null, cv);
        }

        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.DATE + " TEXT," +
                DBConstants.WHICHMARK + " TEXT,"+ DBConstants.MARK_TYPE + " TEXT," +
                DBConstants.DESCRIPTION + " TEXT)");

        // заполняем её именами
        for(int i = 0; i < StudentNames.size(); i++){
            db.execSQL("ALTER TABLE " + "MARK_TABLE_BUFFER" + " ADD COLUMN " + StudentNames.get(i).replace(" ", "_") + " TEXT");
        }

        Cursor cursor = db.query("MARK_TABLE_" + Table_id, null, null ,
                null ,null ,null ,null);

        if (cursor.moveToFirst()) { // запись оценок в существующие колонки
            ContentValues cv = new ContentValues();
            do {
                for (String cn : cursor.getColumnNames()) {
                    if(!cn.equals("id")){
                        cv.put(cn,  EncryptWithCustomKey(Decrypt(cursor.getString(cursor.getColumnIndex(cn))), key));
                    }
                }
                db.insert("MARK_TABLE_BUFFER", null, cv);

            } while (cursor.moveToNext());
            cursor.close();
        }

        db.execSQL("DROP TABLE " + "MARK_TABLE_" + Table_id); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + "MARK_TABLE_" + Table_id); // переименовываем буферную таблиццу
    }

    public void DecryptAndEncryptSchedule(String DayOfTheWeek, String key){
        ArrayList<String> Times = GetAllTimesInADay(DayOfTheWeek);
        ArrayList<String> Subjects = GetAllSubjectsInADay(DayOfTheWeek);
        switch (DayOfTheWeek){
            case "ПН":
                db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_MONDAY);
                db.execSQL(DBConstants.SCHEDULE_STRUCTURE_MONDAY);
                break;
            case "ВТ":
                db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_TUESDAY);
                db.execSQL(DBConstants.SCHEDULE_STRUCTURE_TUESDAY);
                break;
            case "СР":
                db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_WEDNESDAY);
                db.execSQL(DBConstants.SCHEDULE_STRUCTURE_WEDNESDAY);
                break;
            case "ЧТ":
                db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_THURSDAY);
                db.execSQL(DBConstants.SCHEDULE_STRUCTURE_THURSDAY);
                break;
            case "ПТ":
                db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_FRIDAY);
                db.execSQL(DBConstants.SCHEDULE_STRUCTURE_FRIDAY);
                break;
            case "СБ":
                db.execSQL(DBConstants.DROP_SCHEDULE_TABLE_SATURDAY);
                db.execSQL(DBConstants.SCHEDULE_STRUCTURE_SATURDAY);
                break;
        }

        for(int i = 0; i<Times.size(); i++){
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.TIME, EncryptWithCustomKey(Times.get(i), key));
            cv.put(DBConstants.GROUP_NAME, EncryptWithCustomKey(Subjects.get(i), key));
            db.insert(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, null, cv);
        }
    }

    public DBManager(Context context) { // ф-ия для получения контекста
        dbhelper = new DBHelper(context);
    }

    @SuppressLint("Range")
    public void openDB(){ // ф-ия для открытия базы данных
        db = dbhelper.getWritableDatabase();
        Cursor cursor = db.query(DATAPASS_TABLE, null, null ,
                null ,null ,null ,null);

        cursor.moveToFirst();
        if (key.equals("pspspsps")){
            key = Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_PASS)));
        }
        cursor.close();
    }

    public void insertToGroups(String name){ // Функция для добавления группы в основной список групп и создания таблицы группы
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.GROUP_NAME, Encrypt(name));
        cv.put(DBConstants.REMINDER, Encrypt(""));
        db.insert(DBConstants.GROUPS_TABLE, null, cv);
        String Table_id = GetTableID(name);
        Add_Table(Table_id);
        Add_Mark_Table(Table_id);
    }

    public void insertToStudents(String nameStudent, String nameGroup){ // Функция для добавления ученика в группу
        ContentValues cv = new ContentValues();
        String Table_id = GetTableID(nameGroup);
        cv.put(DBConstants.STUDENT_NAMES,  Encrypt(nameStudent));
        db.insert("GROUP_TABLE_" + Table_id, null, cv);
        db.execSQL("ALTER TABLE " + "MARK_TABLE_" + Table_id + " ADD COLUMN " + nameStudent.replace(" ", "_") + " TEXT"); //добавление столбца ученика в таблицу оценок
        SortTableStudents(nameGroup);
    }

    public void insertToMarks(String name_group, ArrayList<String> nameList, String data, String type, String description, String WhichMark, ArrayList<String> marks){ // ф-ия добавления оценок в таблицу группы(MARK_TABLE_id)
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.DATE,  Encrypt(data));
        cv.put(DBConstants.WHICHMARK, Encrypt(WhichMark));
        cv.put(DBConstants.MARK_TYPE,  Encrypt(type));
        cv.put(DBConstants.DESCRIPTION,  Encrypt(description));
        for(int i = 0; i<nameList.size(); i++){
            cv.put(nameList.get(i).replace(" ", "_"), Encrypt(marks.get(i)));
        }
        db.insert("MARK_TABLE_" + GetTableID(name_group), null, cv);
    }

    public void updateMarks(String name_group, ArrayList<String> nameList, String date, String type, String description, String WhichMark, ArrayList<String> marks){ // ф-ия обновления оценок таблицы группы(MARK_TABLE_id)
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.DATE,  Encrypt(date));
        cv.put(DBConstants.WHICHMARK, Encrypt(WhichMark));
        cv.put(DBConstants.MARK_TYPE,  Encrypt(type));
        cv.put(DBConstants.DESCRIPTION,  Encrypt(description));
        for(int i = 0; i<nameList.size(); i++){
            cv.put(nameList.get(i).replace(" ", "_"), Encrypt(marks.get(i))); // заменяю пробелы на _ (иначе будет ошибка в БД)
        }
        db.update("MARK_TABLE_" + GetTableID(name_group), cv, " id = " + GetDateAndWhichMarkId(date, name_group, WhichMark), null);
    }

    public void updateMark(String name_group, String StudentName, String date, String type, String description, String WhichMark, String mark){ // ф-ия обновления оценок таблицы группы(MARK_TABLE_id)
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.DATE,  Encrypt(date));
        cv.put(DBConstants.WHICHMARK, Encrypt(WhichMark));
        cv.put(DBConstants.MARK_TYPE,  Encrypt(type));
        cv.put(DBConstants.DESCRIPTION,  Encrypt(description));
        cv.put(StudentName.replace(" ", "_"), Encrypt(mark)); // заменяю пробелы на _ (иначе будет ошибка в БД)
        db.update("MARK_TABLE_" + GetTableID(name_group), cv, " id = " + GetDateAndWhichMarkId(date, name_group, WhichMark), null);
    }

    public ArrayList<String> GetAllGroups() { // ф-ия для получения списка групп
        ArrayList<String> GroupList = new ArrayList<>();
        Cursor cursor = db.query(DBConstants.GROUPS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DBConstants.GROUP_NAME));
            GroupList.add(Decrypt(name));
        }

        cursor.close();
        return GroupList;
    }

    public ArrayList<String> GetAllStudents(String nameGroup){ // ф-ия для получения списка учеников
        ArrayList<String> GroupList = new ArrayList<>();
        Cursor cursor = db.query("GROUP_TABLE_" + GetTableID(nameGroup), null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DBConstants.STUDENT_NAMES));
            GroupList.add(Decrypt(name));
        }

        cursor.close();
        return GroupList;
    }

    public long GetStudentTableSize(String name){ // ф-ия для получения размера таблицы с учениками
        return DatabaseUtils.queryNumEntries(db, "GROUP_TABLE_" + GetTableID(name));
    }

    @SuppressLint("Range")
    public void RenameStudent(String name_group, String NameBefore, String NameAfter){
        ContentValues cv = new ContentValues();
        String Table_id = GetTableID(name_group);
        cv.put(DBConstants.STUDENT_NAMES, Encrypt(NameAfter));
        db.update("GROUP_TABLE_" + Table_id, cv, " id = " + GetStudentID(name_group, NameBefore), null);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.DATE + " TEXT," +
                DBConstants.WHICHMARK + " TEXT,"+ DBConstants.MARK_TYPE + " TEXT," +
                DBConstants.DESCRIPTION + " TEXT)");

        ArrayList<String> NameList = GetAllStudents(name_group);
        // заполняем её именами
        for(int i = 0; i < NameList.size(); i++){
            db.execSQL("ALTER TABLE " + "MARK_TABLE_BUFFER" + " ADD COLUMN " + NameList.get(i).replace(" ", "_") + " TEXT");
        }


        Cursor cursor = db.query("MARK_TABLE_" + Table_id, null, null ,
                null ,null ,null ,null);

        if (cursor.moveToFirst()) { // запись оценок в существующие колонки
            cv = new ContentValues();
            do {
                for (String cn : cursor.getColumnNames()) {
                    if (NameBefore.equals(cn.replace("_"," "))){
                        cv.put(NameAfter.replace(" ", "_"),  cursor.getString(cursor.getColumnIndex(cn)));
                    }
                    if(!cn.equals("id") && !NameBefore.equals(cn.replace("_", " "))){
                        cv.put(cn,  cursor.getString(cursor.getColumnIndex(cn)));
                    }
                }
                db.insert("MARK_TABLE_BUFFER", null, cv);

            } while (cursor.moveToNext());
            cursor.close();
        }

        db.execSQL("DROP TABLE " + "MARK_TABLE_" + Table_id); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + "MARK_TABLE_" + Table_id); // переименовываем буферную таблиццу
    }

    public long GetGroupsSize(){ // ф-ия для получения размера таблицы с группами
        return DatabaseUtils.queryNumEntries(db, DBConstants.GROUPS_TABLE);
    }

    public void Delete_DateMarks(String id, String name_group){
        db.delete("MARK_TABLE_" + GetTableID(name_group), " id = " + id, null);
    }

    @SuppressLint("Range")
    public String GetDateAndWhichMarkId(String Date, String name_group, String WhichMark){
        int index, index2;
        Date = Encrypt(Date);
        WhichMark = Encrypt(WhichMark);
        String id ="", DateSearch, WhichMarkSearch;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            index2 = cursor.getColumnIndex(DBConstants.WHICHMARK);
            id = String.valueOf(cursor.getInt(cursor.getColumnIndex(DBConstants._ID)));
            DateSearch = cursor.getString(index);
            WhichMarkSearch = cursor.getString(index2);

            if (DateSearch.equals(Date) && WhichMarkSearch.equals(WhichMark)) {
                break;
            }
        }
        cursor.close();
        return id;
    }

    public void DeleteScheduleGroupsByTime(String DayOfTheWeek, ArrayList<String> TimeList){
        for(String time: TimeList){
            db.delete(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, " " + DBConstants.TIME + " = ?", new String[] {Encrypt(time.replace(" ","-"))});
        }
    }

    @SuppressLint("Range")
    public void DeleteStudent(String GroupName, ArrayList<String> NameSelectedStudents){
        String Tableid = GetTableID(GroupName);
        for(String Name:NameSelectedStudents){
            if(!Name.equals("")){
                db.delete("GROUP_TABLE_" + Tableid, " id = " + GetStudentID(GroupName, Name), null);
            }
//            db.execSQL("ALTER TABLE " + "MARK_TABLE_" + id +" DROP COLUMN " + Name.replace(" ", "_"));
        }

        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.DATE + " TEXT," +
                DBConstants.WHICHMARK + " TEXT,"+ DBConstants.MARK_TYPE + " TEXT," +
                DBConstants.DESCRIPTION + " TEXT)");

        ArrayList<String> NameList = GetAllStudents(GroupName);
        // заполняем её именами
        for(int i = 0; i < NameList.size(); i++){
            db.execSQL("ALTER TABLE " + "MARK_TABLE_BUFFER" + " ADD COLUMN " + NameList.get(i).replace(" ", "_") + " TEXT");
        }

        Cursor cursor = db.query("MARK_TABLE_" + Tableid, null, null ,
                null ,null ,null ,null);

        if (cursor.moveToFirst()) { // запись оценок в существующие колонки
            ContentValues cv = new ContentValues();
            do {
                for (String cn : cursor.getColumnNames()) {
                    if(!cn.equals("id") && !NameSelectedStudents.contains(cn.replace("_", " "))){
                        cv.put(cn,  Encrypt(cursor.getString(cursor.getColumnIndex(cn))));
                    }
                }
                db.insert("MARK_TABLE_BUFFER", null, cv);

            } while (cursor.moveToNext());
            cursor.close();
        }
        db.execSQL("DROP TABLE " + "MARK_TABLE_" + Tableid); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + "MARK_TABLE_" + Tableid); // переименовываем буферную таблиццу
    }

    public void Delete_Table(String name){ // удаляем таблицу и строку из таблицы groups_name
        String id = GetTableID(name);
        db.execSQL("DROP TABLE IF EXISTS " + "GROUP_TABLE_" + id);
        db.execSQL("DROP TABLE IF EXISTS " + "MARK_TABLE_" + id);
        db.delete(DBConstants.GROUPS_TABLE,  " id = " + id, null);
    }

    public String GetTableID(String name){ // ф-ия для получения id таблицы по названию группы
        int index, index2;
        String id ="", name2;
        Cursor cursor = db.query(DBConstants.GROUPS_TABLE, null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.GROUP_NAME);
            index2 = cursor.getColumnIndex(DBConstants._ID);
            id = String.valueOf(cursor.getInt(index2));
            name2 = cursor.getString(index);
            if (name2.equals(Encrypt(name))) {
                break;
            }
        }
        cursor.close();
        return id;
    }

    public String GetStudentID(String GroupName, String StudentName){
        int index, index2;
        String id ="", name2;
        StudentName = Encrypt(StudentName);
        Cursor cursor = db.query("GROUP_TABLE_" + GetTableID(GroupName), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.STUDENT_NAMES);
            index2 = cursor.getColumnIndex(DBConstants._ID);
            id = String.valueOf(cursor.getInt(index2));
            name2 = cursor.getString(index);
            if (name2.equals(StudentName)) {
                break;
            }
        }
        cursor.close();
        return id;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetDateMarks(String date, String name_group, ArrayList<String> Name_List, String WhichMark){ // ф-ия для получения оценок из таблицы оценок какой-то группы
        ArrayList<String> marks = new ArrayList<>();
        int index, index2;
        date = Encrypt(date);
        WhichMark = Encrypt(WhichMark);
        String dateSearch, WhichMarkSearch;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            index2 = cursor.getColumnIndex(DBConstants.WHICHMARK);
            dateSearch = String.valueOf(cursor.getString(index));
            WhichMarkSearch = String.valueOf(cursor.getString(index2));
            if (dateSearch.equals(date) && (WhichMarkSearch.equals(WhichMark) || WhichMark.equals("IDK"))) {
                break;
            }
        }
        index = cursor.getColumnIndex(DBConstants.DATE);
        this.typeText = Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.MARK_TYPE)));
        this.descriptionText = Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.DESCRIPTION)));

        for(int i=0; i<Name_List.size(); i++){
            marks.add(Decrypt(cursor.getString(index + 4 + i)));
        }
        cursor.close();
        return marks;
    }

    public String GetWhichMark(String date, String name_group){
        String WhichMark = "", dateSearch;
        int index, index2;
        date = Encrypt(date);
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        index = cursor.getColumnIndex(DBConstants.DATE);
        index2 = cursor.getColumnIndex(DBConstants.WHICHMARK);
        while(cursor.moveToNext()){
            dateSearch = String.valueOf(cursor.getString(index));
            WhichMark = String.valueOf(cursor.getString(index2));
            if (dateSearch.equals(date)) {
                break;
            }
        }
        WhichMark = Decrypt(WhichMark);

        return WhichMark;
    }

    @SuppressLint("Range")
    public String GetDateMarkStudent(String date, String name_group, String NameStudent){ // ф-ия для получения оценок из таблицы оценок какой-то группы
        String mark;
        int index;
        date = Encrypt(date);
        String dateSearch;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            dateSearch = String.valueOf(cursor.getString(index));
            if (dateSearch.equals(date)) {
                break;
            }
        }

        mark = Decrypt(cursor.getString(cursor.getColumnIndex(NameStudent.replace(" ", "_"))));
        cursor.close();
        return mark;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetDateDescriptionAndTypeAndWhichMark(String date, String name_group, String WhichMark){ // ф-ия для получения оценок из таблицы оценок какой-то группы
        ArrayList<String> DesAndType = new ArrayList<>();
        date = Encrypt(date);
        WhichMark = Encrypt(WhichMark);
        int index, index2;
        String dateSearch, WhicMarkSearch;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            index2 = cursor.getColumnIndex(DBConstants.WHICHMARK);
            dateSearch = String.valueOf(cursor.getString(index));
            WhicMarkSearch = String.valueOf(cursor.getString(index2));
            if (dateSearch.equals(date) && (WhicMarkSearch.equals(WhichMark) || WhichMark.equals(Encrypt("IDK")))) {
                break;
            }
        }

        DesAndType.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.MARK_TYPE))));
        DesAndType.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.DESCRIPTION))));
        DesAndType.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.WHICHMARK))));

        cursor.close();
        return DesAndType;
    }

    public String GetType(){ // ф-ия для получения типа оценки (вызывать строго после функции GetDateMarks())
        return typeText;
    } // ф-ия, возвращяющая тип оценки

    public String GetDescription(){ // ф-ия для получения описания оценки (вызывать строго после функции GetDateMarks())
        return descriptionText;
    } // ф-ия, возвращяющая описание оценки

    @SuppressLint("Range")
    public String GetReminderGroup(String NameGroup){
        Cursor cursor = db.query(DBConstants.GROUPS_TABLE, null, null ,
                null ,null ,null ,null);
        int index = cursor.getColumnIndex(DBConstants.GROUP_NAME);
        String NameSearch;
        NameGroup = Encrypt(NameGroup);
        while(cursor.moveToNext()){
            NameSearch = String.valueOf(cursor.getString(index));
            if (NameSearch.equals(NameGroup)) {
                break;
            }
        }
        return Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.REMINDER)));
    }

    @SuppressLint("Range")
    public ArrayList<String> GetAllReminders(){
        ArrayList<String> ReminderGroup = new ArrayList<>();
        Cursor cursor = db.query(DBConstants.GROUPS_TABLE, null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            ReminderGroup.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.REMINDER))));
        }
        return ReminderGroup;
    }

    public boolean CheckDate(String date, String name_group, String WhichMark){ // ф-ия для проверки даты(есть в базе или нет)
        int index, index2;
        date = Encrypt(date);
        WhichMark = Encrypt(WhichMark);
        String dateSearch, WhichMarkSearch;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            index2 = cursor.getColumnIndex(DBConstants.WHICHMARK);
            dateSearch = cursor.getString(index);
            WhichMarkSearch = cursor.getString(index2);
            if (dateSearch.equals(date) && (WhichMarkSearch.equals(WhichMark) || WhichMark.equals("IDK"))) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public boolean CheckGroup(String name_group){
        int index;
        String name_group_search;
        name_group = Encrypt(name_group);
        Cursor cursor = db.query(DBConstants.GROUPS_TABLE, null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.GROUP_NAME);
            name_group_search = cursor.getString(index);
            if(name_group_search.equals(name_group)){
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public ArrayList<String> GetAllDates(String name_group){ // ф-ия для возврата всех дат
        ArrayList<String> dates = new ArrayList<>();
        int index;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            dates.add(Decrypt(cursor.getString(index)));
        }
        cursor.close();
        return dates;
    }

    public ArrayList<String> GetAllSubjectsInADay (String DayOfTheWeek){
        ArrayList<String> SubjectList = new ArrayList<>();

        Cursor cursor = db.query(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, null, null ,
                null ,null ,null ,null);
        int index = cursor.getColumnIndex(DBConstants.GROUP_NAME);
        while(cursor.moveToNext()){
            SubjectList.add(Decrypt(cursor.getString(index)));
        }
        cursor.close();
        return SubjectList;
    }

    public ArrayList<String> GetAllTimesInADay (String DayOfTheWeek){
        ArrayList<String> TimeList = new ArrayList<>();

        Cursor cursor = db.query(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, null, null ,
                null ,null ,null ,null);
        int index = cursor.getColumnIndex(DBConstants.TIME);
        while(cursor.moveToNext()){
            TimeList.add(Decrypt(cursor.getString(index)));
        }
        cursor.close();
        return TimeList;
    }

    public ArrayList<String> GetAllTypesOfMarks (String name_group){
        ArrayList<String> TypeList = new ArrayList<>();

        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        int index = cursor.getColumnIndex(DBConstants.MARK_TYPE);
        while(cursor.moveToNext()){
            TypeList.add(Decrypt(cursor.getString(index)));
        }
        cursor.close();
        return TypeList;
    }

    @SuppressLint("Range")
    public String GetNameGroupByTime(String DayOfTheWeek, String Time){
        String NameGroup = "";
        Time = Encrypt(Time);
        Cursor cursor = db.query(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, null, null ,
                null ,null ,null ,null);
        int index = cursor.getColumnIndex(DBConstants.TIME);
        while(cursor.moveToNext()){
            if(cursor.getString(index).equals(Time)){
                NameGroup = cursor.getString(cursor.getColumnIndex(DBConstants.GROUP_NAME));
                break;
            }
        }
        NameGroup = Decrypt(NameGroup);

        return NameGroup;
    }

    public long GetSizeSubjectsInADay (String DayOfTheWeek){
        return DatabaseUtils.queryNumEntries(db, DBConstants.SCHEDULE_TABLE + DayOfTheWeek);
    }

    public void InsertToScheduleTimeAndGroup(String DayOfTheWeek, String Group, String Hours1, String Hours2, String Minutes1, String Minutes2){
        ContentValues cv = new ContentValues();
        String time;

        if (Integer.parseInt(Hours1)<10 && !Hours1.equals("00")){ // проверка на нули вначале чисел
            Hours1 = "0" + Hours1;
        }
        if (Integer.parseInt(Minutes1)<10 && !Minutes1.equals("00")){ // проверка на нули вначале чисел
            Minutes1 = "0" + Minutes1;
        }
        if (Integer.parseInt(Hours2)<10 && !Hours2.equals("00")){ // проверка на нули вначале чисел
            Hours2 = "0" + Hours2;
        }
        if (Integer.parseInt(Minutes2)<10 && !Minutes2.equals("00")){ // проверка на нули вначале чисел
            Minutes2 = "0" + Minutes2;
        }

        if(Integer.parseInt(Hours1)*60 + Integer.parseInt(Minutes1) <= Integer.parseInt(Hours2)*60 + Integer.parseInt(Minutes2)){
            time = Hours1 + ":" + Minutes1 + "-" + Hours2 + ":" + Minutes2;
        }
        else{
            time = Hours2 + ":" + Minutes2 + "-" + Hours1 + ":" + Minutes1;
        }
//        new ServerManager().execute("INSERT_INTO_SCHEDULE", "Olezha", GetDayOfTheWeek(DayOfTheWeek), Group, time);
        cv.put(DBConstants.GROUP_NAME,  Encrypt(Group));
        cv.put(DBConstants.TIME, Encrypt(time));
        db.insert(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, null, cv);
        SortScheduleByTime(DayOfTheWeek);
    }

    public String GetDayOfTheWeek(String DayOfTheWeek){
        switch (DayOfTheWeek){
            case "ПН": return "Monday";
            case "ВТ": return "Tuesday";
            case "СР": return "Wednesday";
            case "ЧТ": return "Thursday";
            case "ПТ": return "Friday";
            case "Сб": return "Saturday";
        }
        return "WithoutDay";
    }

    public ArrayList<String> GetAllStudentMarks(String name_group, String StudentName){ // ф-ия для возврата всех оценок студента
        ArrayList<String> Marks = new ArrayList<>();
        int index;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(StudentName.replace(" ", "_"));
            Marks.add(Decrypt(cursor.getString(index)));
        }
        cursor.close();
        return Marks;
    }

    @SuppressLint("Range")
    public String GetAverageScore(String name_group, String StudentName, ArrayList<String> DateList){ // ф-ия для возврата среднего балла
        DecimalFormat f = new DecimalFormat("##.00");
        String AverageScore = "";
        double SrB;
        int index, SrBallSummator = 0, counter = 0, Datecounter=0;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(StudentName.replace(" ", "_"));
            if(Datecounter!=DateList.size() && Encrypt(DateList.get(Datecounter)).equals(cursor.getString(cursor.getColumnIndex(DBConstants.DATE))) ){
                if (cursor.getString(index) == null){
                    Datecounter++;
                    continue;
                }
                else if( cursor.getString(index).equals(Encrypt("2")) || cursor.getString(index).equals(Encrypt("3")) ||
                        cursor.getString(index).equals(Encrypt("4")) || cursor.getString(index).equals(Encrypt("5"))){ // Проверяем оценки и суммируем их для подсчёта ср балла

                    SrBallSummator += Integer.parseInt(Decrypt(cursor.getString(index)));
                    counter += 1;
                }
                else if(cursor.getString(index).equals(".")){ // Проверяем точки
                    SrBallSummator += 1;
                    counter += 1;
                }
                Datecounter++;
            }
        }
        if(counter != 0){ // проверяем чтобы оценки были
            SrB = ((float) SrBallSummator / counter);
            return f.format(SrB).replace(",", ".");
        }
        cursor.close();
        return AverageScore;
    }

    public String GetAverageScore(String name_group, String StudentName){ // вторая ф-ия для возврата всех дат
        DecimalFormat f = new DecimalFormat("##.00");
        String AverageScore = " ";
        double SrB;
        int index, SrBallSummator = 0, counter = 0;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(StudentName.replace(" ", "_"));

            if(index == -1 || !cursor.getColumnName(index).replace("_", " ").equals(StudentName)){
                continue;
            }
            else if (cursor.getString(index) == null){
                continue;
            }
            else if( cursor.getString(index).equals(Encrypt("2")) || cursor.getString(index).equals(Encrypt("3")) ||
                    cursor.getString(index).equals(Encrypt("4")) || cursor.getString(index).equals(Encrypt("5"))){ // Проверяем оценки и суммируем их для подсчёта ср балла

                SrBallSummator += Integer.parseInt(Decrypt(cursor.getString(index)));
                counter += 1;
            }
            else if(cursor.getString(index).equals(Encrypt("."))){ // Проверяем точки
                SrBallSummator += 1;
                counter += 1;
            }
        }
        if(counter != 0){ // проверяем чтобы оценки были
            SrB = ((float) SrBallSummator / counter);
            return f.format(SrB).replace(",", ".");
        }
        return AverageScore;
    }

    public ArrayList<String> GetAllGroupAverageScore(){
        DecimalFormat f = new DecimalFormat("##.00");
        ArrayList<String> AverageScore = new ArrayList<>();
        ArrayList<String> GroupList = GetAllGroups();
        for(String name_group: GroupList){
            double SrB = 0;
            String Table_id = GetTableID(name_group);
            int index,  counterNames = 0;
            ArrayList<String> NameList = GetAllStudents(name_group);
            for(String StudentName: NameList){
                int SrBallSummator = 0, counter = 0;
                Cursor cursor = db.query("MARK_TABLE_" + Table_id, null, null ,
                    null ,null ,null ,null);
                index = cursor.getColumnIndex(StudentName.replace(" ", "_"));
                while(cursor.moveToNext()){
                    if (cursor.getString(index) == null){
                        continue;
                    }
                    else if( cursor.getString(index).equals(Encrypt("2")) || cursor.getString(index).equals(Encrypt("3")) ||
                            cursor.getString(index).equals(Encrypt("4")) || cursor.getString(index).equals(Encrypt("5"))){ // Проверяем оценки и суммируем их для подсчёта ср балла

                        SrBallSummator += Integer.parseInt(Decrypt(cursor.getString(index)));
                        counter += 1;
                    }
                    else if(cursor.getString(index).equals(".")){ // Проверяем точки
                        SrBallSummator += 1;
                        counter += 1;
                    }
                }
                if(counter != 0){
                    SrB += (float) SrBallSummator / counter;
                    counterNames++;
                }
                cursor.close();
            }
            if(counterNames != 0){ // проверяем чтобы оценки были
                AverageScore.add(f.format((float)SrB / counterNames).replace(",", "."));
            }
            else {
                AverageScore.add("0.00");
            }
        }
        return AverageScore;
    }

    public void Add_Mark_Table(String id){ // ф-ия для добавления таблицы оценок к группе
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_" +
                id + " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.DATE + " TEXT," +
                DBConstants.WHICHMARK + " TEXT," +
                DBConstants.MARK_TYPE + " TEXT," + DBConstants.DESCRIPTION + " TEXT)");
    }

    public void Add_Table(String id){ // ф-ия для создания таблицы с учениками
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "GROUP_TABLE_" +
                id + " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.STUDENT_NAMES + " TEXT)");
    }

    @SuppressLint("Range")
    private void DecryptStudentsTable(String TableName){
        String table_id = GetTableID(TableName);
        Cursor c = db.query("GROUP_TABLE_" + table_id, null, null, null, null, null, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," +
                DBConstants.STUDENT_NAMES + " TEXT)");

        while(c.moveToNext()){
            ContentValues cv = new ContentValues();
            cv.put(DBConstants.STUDENT_NAMES, Decrypt(c.getString(c.getColumnIndex(DBConstants.STUDENT_NAMES))));
            db.insert("MARK_TABLE_BUFFER", null, cv);
        }
        db.execSQL("DROP TABLE " + "GROUP_TABLE_" + table_id); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + "GROUP_TABLE_" + table_id);
    }

    @SuppressLint("Range")
    public void SortTableStudents(String TableName){ // сортировка в таблице имён студентов
        String Table_id = GetTableID(TableName);
        DecryptStudentsTable(TableName);
        Cursor c = db.query("GROUP_TABLE_" + Table_id, null, null, null, null, null, DBConstants.STUDENT_NAMES);
        Cursor ch = db.query("GROUP_TABLE_" + Table_id, null, null, null, null, null, null);
        if (c != null){
            if (c.moveToFirst()) {
                ch.moveToFirst();
                ContentValues cv = new ContentValues();
                do {
                    cv.put(DBConstants.STUDENT_NAMES,  Encrypt(c.getString(c.getColumnIndex(DBConstants.STUDENT_NAMES))));
                    db.update("GROUP_TABLE_" + Table_id, cv, DBConstants._ID + " = ?", new String[] {ch.getString(ch.getColumnIndex("id"))});
                    ch.moveToNext();
                } while (c.moveToNext());
                c.close();
                ch.close();
            }
        }
        SortTableMarksByNames(TableName);
    }

    @SuppressLint("Range")
    public void SortTableMarksByNames(String TableName){ // ф-ия сортировки таблицы оценок по именам
        // создаём буферную таблицу для сортировки
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.DATE + " TEXT," +
                DBConstants.WHICHMARK + " TEXT," +
                DBConstants.MARK_TYPE + " TEXT," + DBConstants.DESCRIPTION + " TEXT)");
        String id = GetTableID(TableName);
        ArrayList<String> NameList = GetAllStudents(TableName);
        // заполняем её именами
        for(String Name: NameList){
            db.execSQL("ALTER TABLE " + "MARK_TABLE_BUFFER" + " ADD COLUMN " + Name.replace(" ", "_") + " TEXT");
        }

        Cursor cursor = db.query("MARK_TABLE_" + id, null, null, null, null, null, null);

        if (cursor.moveToFirst()) { // запись оценок в существующие колонки
            ContentValues cv = new ContentValues();
            do {
                for (String cn : cursor.getColumnNames()) {
                    if(!cn.equals("id")){
                        cv.put(cn,  cursor.getString(cursor.getColumnIndex(cn)));
                    }
                }
                db.insert("MARK_TABLE_BUFFER", null, cv);

            } while (cursor.moveToNext());
            cursor.close();
        }
        db.execSQL("DROP TABLE " + "MARK_TABLE_" + id); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + "MARK_TABLE_" + id); // переименовываем буферную таблиццу
    }

    public void SortScheduleByTime(String DayOfTheWeek){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.GROUP_NAME + " TEXT," + DBConstants.TIME+ " TEXT)");
        ContentValues cv = new ContentValues();
        ArrayList<String> Times1;
        String delimeter = "-";
        String delimeter2 = ":";
        Times1 = GetAllTimesInADay(DayOfTheWeek);
        String[][] left = new String[Times1.size()][];

        for(int i = 0; i< Times1.size(); i++){
            left[i] = Times1.get(i).split(delimeter);
        }

        int[] SortTimes = new int[Times1.size()];

        for(int i = 0; i< left.length; i++){
            String[] subStr;
            subStr = left[i][0].split(delimeter2);

            if (subStr[0].substring(0, 1).equals("0")){ // проверка на нули вначале чисел
                subStr[0] = subStr[0].substring(1,2);
            }
            if (subStr[1].substring(0, 1).equals("0")){
                subStr[1] = subStr[1].substring(1,2);
            }

            SortTimes[i] = Integer.parseInt(subStr[0])*60 + Integer.parseInt(subStr[1]); // перевожу их в минуты
        }

        ArrayList<String> SortedLeftTimes = new ArrayList<>();
        Arrays.sort(SortTimes); // сортирую

        for (int sortTime : SortTimes) {
            int hours = sortTime / 60;
            int minutes = sortTime % 60;
            if (minutes == 0) {
                String StrHours = String.valueOf(hours);
                if (hours < 10) {
                    StrHours = "0" + hours;
                }
                SortedLeftTimes.add(StrHours + ":" + "00");
            } else {
                String StrHours, StrMinutes;
                if (hours < 10) {
                    StrHours = "0" + hours;
                } else {
                    StrHours = String.valueOf(hours);
                }
                if (minutes < 10) {
                    StrMinutes = "0" + minutes;
                } else {
                    StrMinutes = String.valueOf(minutes);
                }
                SortedLeftTimes.add(StrHours + ":" + StrMinutes);
            }
        }

        for (int i = 0; i < SortedLeftTimes.size(); i++){
            for (int j = 0; j < SortedLeftTimes.size(); j++){
                if((SortedLeftTimes.get(i)).equals(left[j][0])){
                    cv.put(DBConstants.TIME, Encrypt(left[j][0] + "-" + left[j][1])); //записываю время
                    cv.put(DBConstants.GROUP_NAME, Encrypt(GetNameGroupByTime(DayOfTheWeek, left[j][0] + "-" + left[j][1]))); //записываю название урока
                    db.insert("MARK_TABLE_BUFFER", null, cv);
                }
            }
        }
        db.execSQL("DROP TABLE " + DBConstants.SCHEDULE_TABLE + DayOfTheWeek); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + DBConstants.SCHEDULE_TABLE + DayOfTheWeek); // переименовываем буферную таблиццу
    }

    @SuppressLint("Range")
    public void updateSchedule(String DayOfTheWeek, String Hours1, String Hours2,
                               String Minutes1, String Minutes2, String time, String Group){
        Cursor cursor = db.query(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, null, null, null, null, null, null);

        if (Integer.parseInt(Hours1)<10 && !Hours1.equals("00")){ // проверка на нули вначале чисел
            Hours1 = "0" + Hours1;
        }
        if (Integer.parseInt(Minutes1)<10 && !Minutes1.equals("00")){ // проверка на нули вначале чисел
            Minutes1 = "0" + Minutes1;
        }
        if (Integer.parseInt(Hours2)<10 && !Hours2.equals("00")){ // проверка на нули вначале чисел
            Hours2 = "0" + Hours2;
        }
        if (Integer.parseInt(Minutes2)<10 && !Minutes2.equals("00")){ // проверка на нули вначале чисел
            Minutes2 = "0" + Minutes2;
        }

        String time2 = Hours1 + ":" + Minutes1 + "-" + Hours2 + ":" + Minutes2;
        while(cursor.moveToNext()){
            if(cursor.getString(cursor.getColumnIndex(DBConstants.TIME)).equals(Encrypt(time.replace(" ", "-")))){
                break;
            }
        }

//        new ServerManager().execute("UPDATE_SCHEDULE", "Olezha", GetDayOfTheWeek(DayOfTheWeek), Group, time2, time.replace(" ","-"));
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.TIME, Encrypt(time2));
        cv.put(DBConstants.GROUP_NAME, Encrypt(Group));
        db.update(DBConstants.SCHEDULE_TABLE + DayOfTheWeek, cv, " id = " + cursor.getString(cursor.getColumnIndex(DBConstants._ID)), null);
        SortScheduleByTime(DayOfTheWeek);
    }

    @SuppressLint("Range")
    public ArrayList<String> GetTimeAndDay(String NameGroup){
        boolean marker = false;
        ArrayList<String> ClassAndTime = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int DayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String[] DaysOfTheWeek = {"ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ"};
        String[] NewDaysOfTheWeek = new String[7];

        for(int i = 0; i<DaysOfTheWeek.length; i++){
            if(7 - DayOfTheWeek > i){
                NewDaysOfTheWeek[i] = DaysOfTheWeek[(DayOfTheWeek)+i];
            }
            else{
                NewDaysOfTheWeek[i] = DaysOfTheWeek[(DayOfTheWeek-1)-(6-i)];
            }
        }

        Date date = new Date(); // получаю настоящее время
        String[] sBr = date.toString().split(" ");
        int time = Integer.parseInt(sBr[3].split(":")[0])*60 + Integer.parseInt(sBr[3].split(":")[1]) + 180;
        String[] TimeOfTheClass;

        boolean flag = true;
        String FirstTime = "";

        for (String Day:NewDaysOfTheWeek){
            if(!Day.equals("ВС")){
                Cursor c = db.query(DBConstants.SCHEDULE_TABLE + Day, null, null,
                        null, null, null, null);
                while(c.moveToNext()){
                    if(c.getString(c.getColumnIndex(DBConstants.GROUP_NAME)).equals(Encrypt(NameGroup))){ // ищем упоминание группы в расписании

                        TimeOfTheClass = Decrypt(c.getString(c.getColumnIndex(DBConstants.TIME))).split("-")[0].split(":");

                        int TimeOfTheClassI = Integer.parseInt(TimeOfTheClass[0])*60 + Integer.parseInt(TimeOfTheClass[1]);
                        if(Day.equals(NewDaysOfTheWeek[0])){ // отметаем уроки за сегодняшний день, время которых уже пролшло
                            if(TimeOfTheClassI >= time){
                                ClassAndTime.add(Day);
                                ClassAndTime.add(Decrypt(c.getString(c.getColumnIndex(DBConstants.TIME))));
                                marker = true;
                                break;
                            }
                            else if (flag){ // сохраняем данные об первом времени урока в сегодняшний, время которого уже прошло (если уроков больше нет)
                                FirstTime = Decrypt(c.getString(c.getColumnIndex(DBConstants.TIME)));
                                flag = false;
                            }
                        }
                        else{
                            ClassAndTime.add(Day);
                            ClassAndTime.add(Decrypt(c.getString(c.getColumnIndex(DBConstants.TIME))));
                            marker = true;
                            break;
                        }
                    }
                }
            }
            if(marker){
                break;
            }
        }

        if(ClassAndTime.isEmpty()){
            ClassAndTime.add(NewDaysOfTheWeek[0]);
            ClassAndTime.add(FirstTime);
        }
        return ClassAndTime;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetDatesMarksForStudent(String NameTable, String NameStudent, ArrayList<String> Dates){ // ф-ия для возврата оценок студента по датам
        ArrayList<String> Marks = new ArrayList<>();
        int index, j=0;
        String dateSearch;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(NameTable), null, null ,
                null ,null ,null ,null);
        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            dateSearch = cursor.getString(index);
            if (!Dates.isEmpty() && dateSearch.equals(Encrypt(Dates.get(j)))) {
                for (j = 0; j<Dates.size(); j++){
                    Marks.add(Decrypt(cursor.getString(cursor.getColumnIndex(NameStudent.replace(" ", "_")))));
                    cursor.moveToNext();
                }
                break;
            }
        }
        cursor.close();
        return Marks;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetAllMarksByDatesAndTypeOfMark(ArrayList<String> Dates, String name_group, String TypeOfWork, String NameStudent){
        ArrayList<String> Marks = new ArrayList<>();
        int index, index_type_of_mark, j=0;
        String dateSearch;

        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        index_type_of_mark = cursor.getColumnIndex(DBConstants.MARK_TYPE);

        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            dateSearch = cursor.getString(index);
            if (!Dates.isEmpty() && dateSearch.equals(Encrypt(Dates.get(j)))) {
                if (cursor.getString(index_type_of_mark).equals(Encrypt(TypeOfWork)) || TypeOfWork.isEmpty() || TypeOfWork.equals("Все типы работ")
                        || (TypeOfWork.equals("Долги") && cursor.getString(cursor.getColumnIndex(NameStudent.replace(" ", "_"))).equals(Encrypt(".")))){

                    Marks.add(Decrypt(cursor.getString(cursor.getColumnIndex(NameStudent.replace(" ", "_")))));
                    j++;
                }
                if(j == Dates.size()){
                    break;
                }
            }
        }
        cursor.close();
        return Marks;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetAllTypesOfMarksByDates(ArrayList<String> Dates, String name_group, String TypeOfWork, String StudentName){
        ArrayList<String> TypesOfMarks = new ArrayList<>();
        int index, index_type_of_mark, j=0;
        String dateSearch;

        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        index_type_of_mark = cursor.getColumnIndex(DBConstants.MARK_TYPE);

        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            dateSearch = cursor.getString(index);
            if (!Dates.isEmpty() && dateSearch.equals(Encrypt(Dates.get(j)))) {
                if (cursor.getString(index_type_of_mark).equals(Encrypt(TypeOfWork)) || TypeOfWork.isEmpty() || TypeOfWork.equals("Все типы работ") ||
                        (TypeOfWork.equals("Долги") && cursor.getString(cursor.getColumnIndex(StudentName.replace(" ", "_"))).equals(Encrypt(".")))){

                    TypesOfMarks.add(Decrypt(cursor.getString(index_type_of_mark)));
                    j++;
                }
                if(j == Dates.size()){
                    break;
                }
            }
        }
        cursor.close();
        return TypesOfMarks;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetAllDescriptionsByDates(ArrayList<String> Dates, String name_group, String TypeOfWork, String StudentName){
        ArrayList<String> Descriptions = new ArrayList<>();
        int index, index_type_of_mark, j=0;
        String dateSearch;

        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        index_type_of_mark = cursor.getColumnIndex(DBConstants.MARK_TYPE);

        while(cursor.moveToNext()){
            index = cursor.getColumnIndex(DBConstants.DATE);
            dateSearch = cursor.getString(index);
            if (!Dates.isEmpty() && dateSearch.equals(Encrypt(Dates.get(j)))) {
                if (cursor.getString(index_type_of_mark).equals(Encrypt(TypeOfWork)) || TypeOfWork.isEmpty() || TypeOfWork.equals("Все типы работ") ||
                        (TypeOfWork.equals("Долги") && cursor.getString(cursor.getColumnIndex(StudentName.replace(" ", "_"))).equals(Encrypt(".")))){

                    Descriptions.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.DESCRIPTION))));
                    j++;
                }
                if(j == Dates.size()){
                    break;
                }
            }
        }
        cursor.close();
        return Descriptions;

    }

    public ArrayList<String> GetDatesFromTo(String TableName ,String DateFrom, String DateTo){ // получаем даты за определённый период
        int DateFromInteger, DateToInteger;
        String delimeter = "/";
        if(DateFrom.equals("")){ //устанавливаю нижнюю границу
            DateFromInteger = 0;
        }
        else{
            String[] subStr;
            subStr = DateFrom.split(delimeter);
            DateFromInteger = Integer.parseInt(subStr[0]) + Integer.parseInt(subStr[1])*30 + (Integer.parseInt(subStr[2])%100)*365;
        }
        if(DateTo.equals("")){ // устанавливаю верхнуюю границу
            DateToInteger = 300000000;
        }
        else{
            String[] subStr;
            subStr = DateTo.split(delimeter);
            DateToInteger = Integer.parseInt(subStr[0]) + Integer.parseInt(subStr[1])*30 + (Integer.parseInt(subStr[2])%100)*365;
        }

        ArrayList<String> Dates = GetAllDates(TableName); // считываю все даты
        int[] SortDates = new int[Dates.size()];

        for(int i = 0; i< Dates.size(); i++){
            String[] subStr;
            subStr = Dates.get(i).split(delimeter);
            SortDates[i] = Integer.parseInt(subStr[2])*365;
            SortDates[i] += GetCountMonth(Integer.parseInt(subStr[1]), Integer.parseInt(subStr[2]));
            SortDates[i] += Integer.parseInt(subStr[0]); // перевожу их в дни
        }

        ArrayList<String> SortedDates = new ArrayList<>();
        Arrays.sort(SortDates); // сортирую
        for (int sortDate : SortDates) {
            if (sortDate >= DateFromInteger && DateToInteger >= sortDate) {
                int years = sortDate / 365;
                int months = GetMonthByCount((sortDate - years * 365), years);
                int days = ((sortDate - years * 365) - GetCountMonth(months, years));
                SortedDates.add(days + "/" + months + "/" + years);
            }
        }
        return SortedDates;
    }

    public void SortTableMarksByDates(String TableName){ // ф-ия для сортировки таблицы оценок по датам
        // создаём буферную таблицу для сортировки
        String delimeter = "/";

        db.execSQL("CREATE TABLE IF NOT EXISTS " + "MARK_TABLE_BUFFER" +
                " (" + DBConstants._ID + " INTEGER PRIMARY KEY," + DBConstants.DATE + " TEXT," +
                DBConstants.WHICHMARK + " TEXT," +
                DBConstants.MARK_TYPE + " TEXT," + DBConstants.DESCRIPTION + " TEXT)");
        String id = GetTableID(TableName);
        ArrayList<String> NameList = GetAllStudents(TableName);
        // заполняем её именами
        for(String Name: NameList){
            db.execSQL("ALTER TABLE " + "MARK_TABLE_BUFFER" + " ADD COLUMN " + Name.replace(" ", "_") + " TEXT");
        }

        ArrayList<String> Dates = GetAllDates(TableName); // считываю все даты
        int[] SortDates = new int[Dates.size()];

        for(int i = 0; i< Dates.size(); i++){
            String[] subStr;
            subStr = Dates.get(i).split(delimeter);
            SortDates[i] = Integer.parseInt(subStr[2])*365;
            SortDates[i] += GetCountMonth(Integer.parseInt(subStr[1]), Integer.parseInt(subStr[2]));
            SortDates[i] += Integer.parseInt(subStr[0]); // перевожу их в дни
        }

        Arrays.sort(SortDates); // сортирую
        ArrayList<String> SortedDates = new ArrayList<>();
        for (int sortDate : SortDates) {
            // обратно перевожу в формат даты
            int years = sortDate / 365;
            int months = GetMonthByCount((sortDate - years * 365), years);
            int days = ((sortDate - years * 365) - GetCountMonth(months, years));
            SortedDates.add(days + "/" + months + "/" + years);
        }

        int counter = 0;
        String LastCn = " ";
        // Запись в таблицу с учётом даты и номера оценки
        ContentValues cv = new ContentValues();
        for (String cn : SortedDates) {
            for (int j = 1; j<3; j++){
                if(!LastCn.equals(cn)){
                    counter = 0;
                }
                if(CheckDate(cn, TableName, String.valueOf(j)) && counter!=2){
                    ArrayList<String> Marks;
                    Marks = GetDateMarks(cn, TableName, GetAllStudents(TableName), String.valueOf(j));
                    cv.put(DBConstants.DATE, Encrypt(cn)); // запись в cv
                    cv.put(DBConstants.MARK_TYPE, Encrypt(typeText));
                    cv.put(DBConstants.DESCRIPTION, Encrypt(descriptionText));
                    cv.put(DBConstants.WHICHMARK, Encrypt(String.valueOf(j)));
                    for(int i = 0; i < Marks.size(); i ++){
                        cv.put(NameList.get(i).replace(" ", "_"), Encrypt(Marks.get(i)));
                    }
                    db.insert("MARK_TABLE_BUFFER", null, cv); // запись в буферную таблицу
                    LastCn = cn;
                    counter++;
                }
            }
        }

        db.execSQL("DROP TABLE " + "MARK_TABLE_" + id); // удаляем существующую таблицу
        db.execSQL("ALTER TABLE MARK_TABLE_BUFFER RENAME TO " + "MARK_TABLE_" + id); // переименовываем буферную таблицу
    }

    public int GetMonthByCount(int count, int wisokosnyi){
        if(wisokosnyi % 4 == 0){
            wisokosnyi = 1;
        }
        if(count<=31){
            return 1;
        }
        else if (count <= 59 + wisokosnyi){
            return 2;
        }
        else if (count <= 90 + wisokosnyi){
            return 3;
        }
        else if (count <= 120 + wisokosnyi){
            return 4;
        }
        else if (count <= 151 + wisokosnyi){
            return 5;
        }
        else if (count <= 181 + wisokosnyi){
            return 6;
        }
        else if (count <= 212 + wisokosnyi){
            return 7;
        }
        else if (count <= 243 + wisokosnyi){
            return 8;
        }
        else if (count <= 273 + wisokosnyi){
            return 9;
        }
        else if (count <= 304 + wisokosnyi){
            return 10;
        }
        else if (count <= 334 + wisokosnyi){
            return 11;
        }
        else if (count <= 365 + wisokosnyi){
            return 12;
        }
        return 0;
    }

    public int GetCountMonth(int count, int wisokosnyi){
        if(wisokosnyi %4 == 0){
            wisokosnyi = 1;
        }
        switch(count){
            case 1: return 0;
            case 2: return 31;
            case 3: return 59 + wisokosnyi;
            case 4: return 90 + wisokosnyi;
            case 5: return 121 + wisokosnyi;
            case 6: return 151 + wisokosnyi;
            case 7: return 181 + wisokosnyi;
            case 8: return 212 + wisokosnyi;
            case 9: return 243 + wisokosnyi;
            case 10: return 273 + wisokosnyi;
            case 11: return 304 + wisokosnyi;
            case 12: return 334 + wisokosnyi;
        }
        return 0;
    }

    public void ResetNameGroup(String FirstName, String SecondName){
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.GROUP_NAME, Encrypt(SecondName));
        db.update(DBConstants.GROUPS_TABLE, cv, " id = " + GetTableID(FirstName), null);
    }

    public void ResetReminderGroup(String NameGroup, String Reminder){
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.REMINDER, Encrypt(Reminder));
        db.update(DBConstants.GROUPS_TABLE, cv, " id = " + GetTableID(NameGroup), null);
    }

    public void ClearBuffer(){
        db.execSQL("DROP TABLE " + "MARK_TABLE_BUFFER");
    }

    public void CreateNewTables(){
        db.execSQL(DBConstants.BOUNDS_STRUCTURE);
    }

    public void DBClose(){
        dbhelper.close();
    } // ф-ия для закрытия базы данных

    //button activity functions

    public int GetCountOfExcellentStudents(String name_group){
        int CountOfExcellentStudents = 0;
        ArrayList<String> NameList = new ArrayList<>(GetAllStudents(name_group));
        for (String i: NameList){
            String averageScore = GetAverageScore(name_group, i);
            double bound = Double.parseDouble(GetBound(DBConstants.UPPER_BOUND, "5"));
            double bound_bottom = Double.parseDouble(GetBound(DBConstants.BOTTOM_BOUND, "5"));
            String averageScoreOfImportantWorks = GetAverageScoreOfImportantWorks(name_group, i);
            if (averageScore.equals(" ")){
                continue;
            }
            else if(Double.parseDouble(averageScore) >= bound){ // заменить на оценка + тип
                CountOfExcellentStudents++;
            }
            else if (Double.parseDouble(averageScore) < bound && Double.parseDouble(averageScore) >= bound_bottom
                    && !averageScoreOfImportantWorks.isEmpty()){
                if (Double.parseDouble(averageScoreOfImportantWorks)>=bound ){
                    CountOfExcellentStudents++;
                }
            }
            else if (Double.parseDouble(averageScore) < bound && Double.parseDouble(averageScore) >= bound_bottom
                    && averageScoreOfImportantWorks.isEmpty()){
                CountOfExcellentStudents++;
            }
        }
        return CountOfExcellentStudents;
    }

    public int GetCountOfGoodStudents(String name_group){
        int CountOfGoodStudents = 0;
        ArrayList<String> NameList = new ArrayList<>(GetAllStudents(name_group));
        for (String i: NameList){
            String averageScore = GetAverageScore(name_group, i);
            double upperBound4 = Double.parseDouble(GetBound(DBConstants.UPPER_BOUND, "4"));
            double bottomBound4 = Double.parseDouble(GetBound(DBConstants.BOTTOM_BOUND, "4"));
            double bottomBound5 = Double.parseDouble(GetBound(DBConstants.BOTTOM_BOUND, "5"));
            double upperBound5 = Double.parseDouble(GetBound(DBConstants.UPPER_BOUND, "5"));
            String averageScoreOfImportantWorks = GetAverageScoreOfImportantWorks(name_group, i);
            if (averageScore.equals(" ")){
                continue;
            }
            else if(Double.parseDouble(averageScore) >= upperBound4
                    && Double.parseDouble(averageScore) < bottomBound5){ // заменить на оценка + тип
                CountOfGoodStudents++;
            }
            else if (Double.parseDouble(averageScore) < upperBound5 && Double.parseDouble(averageScore) >= bottomBound5
                    && !averageScoreOfImportantWorks.isEmpty()){
                if (!(Double.parseDouble(averageScoreOfImportantWorks) >= bottomBound5)){
                    CountOfGoodStudents++;
                }
            }
            else if (Double.parseDouble(averageScore) < upperBound4 && Double.parseDouble(averageScore) >= bottomBound4
                    && !averageScoreOfImportantWorks.isEmpty()){
                if (Double.parseDouble(averageScoreOfImportantWorks) >= bottomBound4){
                    CountOfGoodStudents++;
                }
            }
            else if (Double.parseDouble(averageScore) < upperBound4 && Double.parseDouble(averageScore) >= bottomBound4
                    && averageScoreOfImportantWorks.isEmpty()){
                CountOfGoodStudents++;
            }
        }
        return CountOfGoodStudents;
    }

    public int GetCountOfBadStudents(String name_group){
        int CountOfBadStudents = 0;
        ArrayList<String> NameList = new ArrayList<>(GetAllStudents(name_group));
        for (String i: NameList){
            String averageScore = GetAverageScore(name_group, i);
            double upperBound3 = Double.parseDouble(GetBound(DBConstants.UPPER_BOUND, "3"));
            double bottomBound3 = Double.parseDouble(GetBound(DBConstants.BOTTOM_BOUND, "3"));
            String averageScoreOfImportantWorks = GetAverageScoreOfImportantWorks(name_group, i);
            if (GetAverageScore(name_group, i).equals(" ")){
                continue;
            }
            else if(Double.parseDouble(averageScore) < bottomBound3){ // заменить на оценка + тип
                CountOfBadStudents++;
            }
            else if (Double.parseDouble(averageScore) >= bottomBound3
                    && Double.parseDouble(averageScore) < upperBound3
                    && !averageScoreOfImportantWorks.isEmpty()){
                if (Double.parseDouble(averageScoreOfImportantWorks) <= bottomBound3){
                    CountOfBadStudents++;
                }
            }
        }
        return CountOfBadStudents;
    }

    public long GetCountDates (String name_group){
        return DatabaseUtils.queryNumEntries(db, "MARK_TABLE_" + GetTableID(name_group));
    }

    public int GetCountOfImportantWorks(String name_group){
        int CountOfImportantWorks = 0;
        ArrayList<String> TypeList = GetAllTypesOfMarks(name_group);
        for (String Work: TypeList){
            for (String ImportantWork: GetImportantTypesOfWork())
            {
                if (Work.equals(ImportantWork)){
                    CountOfImportantWorks++;
                }
            }

        }
        return CountOfImportantWorks;
    }

    @SuppressLint("Range")
    public String GetAverageScoreOfImportantWorks(String name_group, String StudentName){
        int DateCount = (int) GetCountDates(name_group);

        ArrayList<String> TypesOfWorks = GetImportantTypesOfWork();

        DecimalFormat f = new DecimalFormat("##.00");
        String AverageScore = "";
        double SrB;
        int index, SrBallSummator = 0, counter = 0, Datecounter=0;
        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);
        index = cursor.getColumnIndex(StudentName.replace(" ", "_"));
        while(cursor.moveToNext()){
            if(Datecounter!=DateCount){
                for (String ImportantWork: TypesOfWorks){
                    if (cursor.getString(cursor.getColumnIndex(DBConstants.MARK_TYPE)).equals(Encrypt(ImportantWork))){
                        if (cursor.getString(index) == null || cursor.getString(index).equals(Encrypt("."))){ // Проверяем точки
                            SrBallSummator += 1;
                            counter += 1;
                        }
                        else if((cursor.getString(index).equals(Encrypt("2")) || cursor.getString(index).equals(Encrypt("3")) ||
                                cursor.getString(index).equals(Encrypt("4")) || cursor.getString(index).equals(Encrypt("5")))){ // Проверяем оценки и суммируем их для подсчёта ср балла

                            SrBallSummator += Integer.parseInt(Decrypt(cursor.getString(index)));
                            counter += 1;
                        }
                    }
                    Datecounter++;
                }
            }
        }
        if(counter != 0){ // проверяем чтобы оценки были
            SrB = ((float) SrBallSummator / counter);
            return f.format(SrB).replace(",", ".");
        }
        cursor.close();

        return AverageScore;
    }

    public String GetAverageScoreOfAllImportantWorks(String name_group){
        String AverageScoreOfAllImportantWorks;
        DecimalFormat f = new DecimalFormat("##.00");
        double SrB = 0;
        int   counterNames = 0;
        ArrayList<String> NameList = GetAllStudents(name_group);
        for(String StudentName: NameList) {
            if(!GetAverageScoreOfImportantWorks(name_group, StudentName).isEmpty()){
                SrB += Double.parseDouble(GetAverageScoreOfImportantWorks(name_group, StudentName));
                counterNames++;
            }
        }
        if(counterNames != 0){ // проверяем чтобы оценки были
            AverageScoreOfAllImportantWorks = (f.format((float)SrB / counterNames).replace(",", "."));
        }
        else {
            AverageScoreOfAllImportantWorks = "0.00";
        }
        return AverageScoreOfAllImportantWorks;
    }

    public int GetCountArrearagesOfGroup(String name_group){
        int CountOfArrearages = 0;
        ArrayList<String> NameList = new ArrayList<>(GetAllStudents(name_group));
        for (String i: NameList){
            CountOfArrearages += GetCountArrearageOfStudent(name_group,i);
        }
        return CountOfArrearages;
    }

    public int GetCountArrearageOfStudent(String name_group, String StudentName){
        int CountOfArrearages = 0;

        Cursor cursor = db.query("MARK_TABLE_" + GetTableID(name_group), null, null ,
                null ,null ,null ,null);

        int index = cursor.getColumnIndex(StudentName.replace(" ", "_"));
        while(cursor.moveToNext()){
            if (cursor.getString(index) == null){
                continue;
            }
            else if(cursor.getString(index).equals(Encrypt("."))){
                CountOfArrearages++;
            }
        }
        return CountOfArrearages;
    }

    // для статистики

    public void SetValueOfCheckBoxes(ArrayList<String> ValuesOfCheckBoxes){
        ContentValues cv = new ContentValues();
        int id=1;
        for(String i:ValuesOfCheckBoxes){
            cv.put(DBConstants.VALUE, i);
            db.update(DBConstants.VALUES_TABLE, cv, " id = " + id, null);
            id++;
        }
    }

    public ArrayList<String> GetValueOfCheckBoxes(){
        ArrayList<String> ValuesOfCheckBoxes = new ArrayList<>();

        Cursor cursor = db.query(DBConstants.VALUES_TABLE, null, null ,
                null ,null ,null ,null);

        int index = cursor.getColumnIndex(DBConstants.VALUE);
        while(cursor.moveToNext()){
            ValuesOfCheckBoxes.add(cursor.getString(index));
        }

        return  ValuesOfCheckBoxes;
    }

    public long GetSizeOfMarksTypesTable(){
        return DatabaseUtils.queryNumEntries(db, DBConstants.MARKS_TYPES_TABLE);
    }

    public ArrayList<String> GetAllTypesOfMarksOfGroups(){ // ф-ия для получения списка групп
        ArrayList<String> TypesList = new ArrayList<>();
        Cursor cursor = db.query(DBConstants.MARKS_TYPES_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String TypeOfMark = Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.MARK_TYPE)));
            TypesList.add(TypeOfMark);
        }

        cursor.close();
        return TypesList;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetAllValuesOFMarks(){ // ф-ия для получения списка групп
        ArrayList<String> ValueList = new ArrayList<>();
        Cursor cursor = db.query(DBConstants.MARKS_TYPES_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            ValueList.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.VALUE))));
        }

        cursor.close();
        return ValueList;
    }

    public void InsertTypesOfMarks(String TypesOfMarks){
        ContentValues cv = new ContentValues();

        cv.put(DBConstants.MARK_TYPE, Encrypt(TypesOfMarks));
        cv.put(DBConstants.VALUE, Encrypt("false"));

        db.insert(DBConstants.MARKS_TYPES_TABLE, null, cv);
    }

    public void UpdateTypesOfMarks(ArrayList<String> TypesOfMarks, ArrayList<String> ValueList){
        ContentValues cv = new ContentValues();

        for(int i = 0; i<TypesOfMarks.size(); i++){
            cv.put(DBConstants.MARK_TYPE, Encrypt(TypesOfMarks.get(i)));
            cv.put(DBConstants.VALUE, Encrypt(ValueList.get(i)));
            db.update(DBConstants.MARKS_TYPES_TABLE, cv, " id = " + GetMarkTypeId(TypesOfMarks.get(i)), null);
        }
    }

    @SuppressLint("Range")
    public int GetMarkTypeId(String MarkType){
        int id;

        Cursor cursor = db.query(DBConstants.MARKS_TYPES_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String TypeOfMark = cursor.getString(cursor.getColumnIndex(DBConstants.MARK_TYPE));
            if (TypeOfMark.equals(Encrypt(MarkType))){
                break;
            }
        }

        id = cursor.getInt(cursor.getColumnIndex(DBConstants._ID));
        cursor.close();
        return id;
    }

    @SuppressLint("Range")
    public ArrayList<String> GetImportantTypesOfWork(){
        ArrayList<String> ImportantTypesOfWork = new ArrayList<>();

        Cursor cursor = db.query(DBConstants.MARKS_TYPES_TABLE, null, null ,
                null ,null ,null ,null);

        String ValueOfMark;
        while(cursor.moveToNext()){
            ValueOfMark = cursor.getString(cursor.getColumnIndex(DBConstants.VALUE));
            if (ValueOfMark.equals(Encrypt("true"))){
                ImportantTypesOfWork.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.MARK_TYPE))));
            }
        }
        cursor.close();

        return ImportantTypesOfWork;
    }

    public void RenameTypeOfWork(String TypeOfWork, String TypeOfWorkBefore){
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.MARK_TYPE, Encrypt(TypeOfWork));
        db.update(DBConstants.MARKS_TYPES_TABLE, cv, " id = " + GetMarkTypeId(TypeOfWorkBefore), null);
        RenameTypeOfWorkInDB(TypeOfWork, TypeOfWorkBefore);
    }

    @SuppressLint("Range")
    public void RenameTypeOfWorkInDB(String TypeOfWork, String TypeOfWorkBefore){
        ArrayList<String> GroupList = GetAllGroups();
        int index;
        for(String name_group: GroupList){
            String Table_id = GetTableID(name_group);
            Cursor cursor = db.query("MARK_TABLE_" + Table_id, null, null ,
                    null ,null ,null ,null);

            index = cursor.getColumnIndex(DBConstants.MARK_TYPE);
            while(cursor.moveToNext()){
                if (Encrypt(TypeOfWorkBefore).equals(cursor.getString(index))){
                    ContentValues cv = new ContentValues();
                    cv.put(DBConstants.MARK_TYPE, Encrypt(TypeOfWork));
                    db.update("MARK_TABLE_" + Table_id, cv, " id = " + cursor.getInt(cursor.getColumnIndex(DBConstants._ID)), null);
                }
            }
            cursor.close();
        }
    }

    public void DeleteTypeOfMark(ArrayList<String> TypesOfMarks){
        for (String TypeOfMark: TypesOfMarks){
            if(!TypeOfMark.isEmpty()){
                db.delete(DBConstants.MARKS_TYPES_TABLE, " id = " + GetMarkTypeId(TypeOfMark), null);
            }
        }

    }

    public boolean CheckTypeOfWork(String TypeOfWork){
        int index;

        Cursor cursor = db.query(DBConstants.MARKS_TYPES_TABLE, null, null ,
                null ,null ,null ,null);

        index = cursor.getColumnIndex(DBConstants.MARK_TYPE);
        while(cursor.moveToNext()){
            if (Encrypt(TypeOfWork).equals(cursor.getString(index))){
                cursor.close();
                return false;
            }
        }
        cursor.close();
        return true;
    }

    public long GetSizeOfPeriodsTable(){
        return DatabaseUtils.queryNumEntries(db, DBConstants.PERIODS_TABLE);
    }

    public void InsertToPeriods(String PeriodName, String Date){
        ContentValues cv = new ContentValues();

        cv.put(DBConstants.PERIOD, Encrypt(PeriodName));
        cv.put(DBConstants.DATE, Encrypt(Date));

        db.insert(DBConstants.PERIODS_TABLE, null, cv);
    }

    @SuppressLint("Range")
    public ArrayList<String> GetAllPeriods(){ // ф-ия для получения списка групп
        ArrayList<String> PeriodList = new ArrayList<>();
        Cursor cursor = db.query(DBConstants.PERIODS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            PeriodList.add(Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.PERIOD))));
        }

        cursor.close();
        return PeriodList;
    }

    public ArrayList<String> GetAllDateOfPeriods(){ // ф-ия для получения списка групп
        ArrayList<String> DateList = new ArrayList<>();
        Cursor cursor = db.query(DBConstants.PERIODS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String DateOfPeriod = cursor.getString(cursor.getColumnIndex(DBConstants.DATE));
            DateList.add(Decrypt(DateOfPeriod).replace("-", " "));
        }

        cursor.close();
        return DateList;
    }

    public boolean CheckPeriod(String Period){
        int index;

        Cursor cursor = db.query(DBConstants.PERIODS_TABLE, null, null ,
                null ,null ,null ,null);

        index = cursor.getColumnIndex(DBConstants.PERIOD);
        while(cursor.moveToNext()){
            if (Encrypt(Period).equals(cursor.getString(index))){
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public boolean CheckPeriodDate(String PeriodTime){
        int index;

        Cursor cursor = db.query(DBConstants.PERIODS_TABLE, null, null ,
                null ,null ,null ,null);

        index = cursor.getColumnIndex(DBConstants.DATE);
        while(cursor.moveToNext()){
            if (Encrypt(PeriodTime).equals(cursor.getString(index))){
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public String GetMonth(String Month){
        String month = "0";
        switch(Month){
            case "Jan":
            case "янв":
                month = "1"; break;
            case "Feb":
            case "февр":
                month = "2"; break;
            case "Mar":
            case "мар":
                month = "3"; break;
            case "Apr":
            case "апр":
                month = "4"; break;
            case "May":
            case "мая":
                month = "5"; break;
            case "Jun":
            case "июн":
                month = "6"; break;
            case "Jul":
            case "июл":
                month = "7"; break;
            case "Aug":
            case "авг":
                month = "8"; break;
            case "Sep":
            case "сент":
                month = "9"; break;
            case "Oct":
            case "окт":
                month = "10"; break;
            case "Nov":
            case "нояб":
                month = "11"; break;
            case "Dec":
            case "дек":
                month = "12"; break;
        }
        return month;
    }

    public void RenamePeriodDate(String PeriodBefore, String Period, String Date){
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.PERIOD, Encrypt(Period));
        cv.put(DBConstants.DATE, Encrypt(Date));
        db.update(DBConstants.PERIODS_TABLE, cv, " id = " + GetPeriodDateId(PeriodBefore), null);
    }

    @SuppressLint("Range")
    public int GetPeriodDateId(String Period){
        int id;

        Cursor cursor = db.query(DBConstants.PERIODS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            String PeriodUnknown = cursor.getString(cursor.getColumnIndex(DBConstants.PERIOD));
            if (PeriodUnknown.equals(Encrypt(Period))){
                break;
            }
        }

        id = cursor.getInt(cursor.getColumnIndex(DBConstants._ID));
        cursor.close();
        return id;
    }

    @SuppressLint("Range")
    public String GetPeriodDate(String Period){
        String date;

        if (Period.equals("За всё время")){
            return "";
        }

        Cursor cursor = db.query(DBConstants.PERIODS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            String PeriodUnknown = cursor.getString(cursor.getColumnIndex(DBConstants.PERIOD));
            if (PeriodUnknown.equals(Encrypt(Period))){
                break;
            }
        }

        date = Decrypt(cursor.getString(cursor.getColumnIndex(DBConstants.DATE)));
        cursor.close();
        return date;
    }

    public void DeletePeriods(ArrayList<String> PeriodsList){
        for (String Period: PeriodsList){
            if(!Period.isEmpty()){
                db.delete(DBConstants.PERIODS_TABLE, " id = " + GetPeriodDateId(Period), null);
            }
        }
    }

    // Bounds_Table

    @SuppressLint("Range")
    public String GetBound(String TypeOfBound, String Mark){
        String Bound = "";
        Cursor cursor = db.query(DBConstants.BOUNDS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            String MarkUnknown = cursor.getString(cursor.getColumnIndex(DBConstants.MARK));
            if (MarkUnknown.equals(Encrypt(Mark))){
                Bound = Decrypt(cursor.getString(cursor.getColumnIndex(TypeOfBound)));
                break;
            }
        }

        return Bound;
    }

    public void UpdateBound(String TypeOfBound, String Mark, String AverageScore){
        ContentValues cv = new ContentValues();
        cv.put(TypeOfBound, Encrypt(AverageScore));
        db.update(DBConstants.BOUNDS_TABLE, cv, " id = " + GetBoundId(Mark), null);
    }

    @SuppressLint("Range")
    public int GetBoundId(String Mark){
        int id;

        Cursor cursor = db.query(DBConstants.BOUNDS_TABLE, null, null ,
                null ,null ,null ,null);

        while(cursor.moveToNext()){
            String MarkUnknown = cursor.getString(cursor.getColumnIndex(DBConstants.MARK));
            if (MarkUnknown.equals(Encrypt(Mark))){
                break;
            }
        }

        id = cursor.getInt(cursor.getColumnIndex(DBConstants._ID));
        cursor.close();
        return id;
    }

    public String GetPeriodByToday(){ // получаем даты за определённый период
        String ThisPeriod="";
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);

        int DateFromInteger,DateToInteger, DateThisInteger;
        ArrayList<String> PeriodList = GetAllDateOfPeriods();

        String delimeter2 = " ";
        String delimeter = "/";


        for(String Period: PeriodList){
            String DateFrom = Period.split(delimeter2)[0];
            String DateTo = Period.split(delimeter2)[1];

            String[] subStrThisDate;
            subStrThisDate = dateText.split(delimeter);
            DateThisInteger = Integer.parseInt(subStrThisDate[0]) + Integer.parseInt(subStrThisDate[1])*30 + (Integer.parseInt(subStrThisDate[2])%100)*365;

            if(DateFrom.equals("")){ //устанавливаю нижнюю границу
                DateFromInteger = 0;
            }
            else{
                String[] subStr;
                subStr = DateFrom.split(delimeter);
                DateFromInteger = Integer.parseInt(subStr[0]) + Integer.parseInt(subStr[1])*30 + (Integer.parseInt(subStr[2])%100)*365;
            }
            if(DateTo.equals("")){ // устанавливаю верхнуюю границу
                DateToInteger = 300000000;
            }
            else{
                String[] subStr;
                subStr = DateTo.split(delimeter);
                DateToInteger = Integer.parseInt(subStr[0]) + Integer.parseInt(subStr[1])*30 + (Integer.parseInt(subStr[2])%100)*365;
            }

            if(DateThisInteger<= DateToInteger && DateThisInteger >=DateFromInteger){
                ThisPeriod = Period;
                break;
            }
        }

        return ThisPeriod.replace(" ", "-");
    }

    public ArrayList<String> GetAllDatesByTypeOfMark(String name_group, String DateFrom, String DateTo, String TypeOfWork, String StudentName){

        int DateFromInteger, DateToInteger;
        String delimeter = "/";
        if(DateFrom.equals("")){ //устанавливаю нижнюю границу
            DateFromInteger = 0;
        }
        else{
            String[] subStr;
            subStr = DateFrom.split(delimeter);
            DateFromInteger = Integer.parseInt(subStr[0]) + Integer.parseInt(subStr[1])*30 + (Integer.parseInt(subStr[2])%100)*365;
        }
        if(DateTo.equals("")){ // устанавливаю верхнуюю границу
            DateToInteger = 300000000;
        }
        else{
            String[] subStr;
            subStr = DateTo.split(delimeter);
            DateToInteger = Integer.parseInt(subStr[0]) + Integer.parseInt(subStr[1])*30 + (Integer.parseInt(subStr[2])%100)*365;
        }

        String Table_id = GetTableID(name_group);

        ArrayList<String> dates = new ArrayList<>();
        if(TypeOfWork.equals("Долги")){
            int index, index_student;

            Cursor cursor = db.query("MARK_TABLE_" + Table_id, null, null ,
                    null ,null ,null ,null);
            index_student = cursor.getColumnIndex(StudentName.replace(" ", "_"));

            while(cursor.moveToNext()){
                index = cursor.getColumnIndex(DBConstants.DATE);
                if(cursor.getString(index_student).equals(Encrypt("."))){
                    dates.add(cursor.getString(index));
                }
            }
            cursor.close();
        }
        else {
            int index, index_type_of_mark;
            Cursor cursor = db.query("MARK_TABLE_" + Table_id, null, null,
                    null, null, null, null);
            index = cursor.getColumnIndex(DBConstants.DATE);
            index_type_of_mark = cursor.getColumnIndex(DBConstants.MARK_TYPE);
            while (cursor.moveToNext()) {
                if (cursor.getString(index_type_of_mark).equals(Encrypt(TypeOfWork)) || TypeOfWork.isEmpty() || TypeOfWork.equals("Все типы работ")) {
                    dates.add(Decrypt(cursor.getString(index)));
                }
            }
            cursor.close();
        }

        int[] SortDates = new int[dates.size()];

        for(int i = 0; i< dates.size(); i++){
            String[] subStr;
            subStr = dates.get(i).split(delimeter);
            SortDates[i] = Integer.parseInt(subStr[2])*365;
            SortDates[i] += GetCountMonth(Integer.parseInt(subStr[1]), Integer.parseInt(subStr[2]));
            SortDates[i] += Integer.parseInt(subStr[0]); // перевожу их в дни
        }

        ArrayList<String> SortedDates = new ArrayList<>();
        Arrays.sort(SortDates); // сортирую
        for (int sortDate : SortDates) {
            if (sortDate >= DateFromInteger && DateToInteger >= sortDate) {
                int years = sortDate / 365;
                int months = GetMonthByCount((sortDate - years * 365), years);
                int days = ((sortDate - years * 365) - GetCountMonth(months, years));
                SortedDates.add(days + "/" + months + "/" + years);
            }
        }

        return SortedDates;
    }

    public void AddDataAboutUser(String First_Name, String Second_Name, String Password){
        ContentValues values = new ContentValues();

        //сохраняю хэш пароля, после чего этим же хэшем шифрую данные о пользователе и его пароль
        db.execSQL(DBConstants.DROP_DATAPASS_TABLE);
        db.execSQL(DBConstants.DATAPASS_CREATE);

        values.put(DBConstants.COLUMN_PASS, Encrypt(Password));
        values.put(DBConstants.COLUMN_HASH, bin2hex(getHash(Password)));
        key = Password;
        values.put(DBConstants.COLUMN_NAME, Encrypt(First_Name));
        values.put(DBConstants.COLUMN_SURNAME, Encrypt(Second_Name));
        db.insert(DATAPASS_TABLE, null, values);

        for(int i = 0; i<3; i++){ // заполняю таблицу по х-кам итоговых отметок
            ContentValues cv = new ContentValues();
            if(i == 0){
                cv.put(DBConstants.MARK, DBManager.Encrypt("5"));
                cv.put(DBConstants.UPPER_BOUND, DBManager.Encrypt("4.75"));
                cv.put(DBConstants.BOTTOM_BOUND, DBManager.Encrypt("4.50"));
            }
            else if(i == 1){
                cv.put(DBConstants.MARK, DBManager.Encrypt("4"));
                cv.put(DBConstants.UPPER_BOUND, DBManager.Encrypt("3.75"));
                cv.put(DBConstants.BOTTOM_BOUND, DBManager.Encrypt("3.50"));
            }
            else {
                cv.put(DBConstants.MARK, DBManager.Encrypt("3"));
                cv.put(DBConstants.UPPER_BOUND, DBManager.Encrypt("2.75"));
                cv.put(DBConstants.BOTTOM_BOUND, DBManager.Encrypt("2.50"));
            }

            db.insert(DBConstants.BOUNDS_TABLE, null, cv);
        }
    }

    public boolean CheckPassword(String password){
        Cursor cursor =db.query(DATAPASS_TABLE, null, null,
                null, null, null, null);
        cursor.moveToFirst();
        @SuppressLint("Range") String hash = cursor.getString(cursor.getColumnIndex(DBConstants.COLUMN_HASH));
        cursor.close();
        String password_Hash = bin2hex(getHash(password));
        if(hash.equals(password_Hash)){
            key = password;
            return true;
        }
        else{
            return false;
        }
    }

}
