package com.example.recyclerviewtest.GetMarks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.recyclerviewtest.AddDateMarks.AddMarksActivity;
import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.MainActivity;
import com.example.recyclerviewtest.MyHorizontalScrollView;
import com.example.recyclerviewtest.R;
import com.example.recyclerviewtest.Settings.TypesOfMarksAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class GetMarksActivity extends AppCompatActivity {

    private static GetMarksActivity instance;
    private String fromDate = "", toDate = "";
    private String Datethis, name_group;

    private DBManager dbManager;
    private TableLayout tableLayout;
    private AutoCompleteTextView dateText;


    private ArrayAdapter arrayAdapter;
    private ArrayList<String> NameList;
    private ArrayList<String> DateList;
    private final ArrayList<TextView> TextViews = new ArrayList<>();

    private ScrollView scrollY;
    private MyHorizontalScrollView scrollX;
    private GestureDetector gestureDetectorY;
    private RelativeLayout[][] sqContent;
    private int sizeI=0;
    private int sizeJ=0;
    private String[] Marks;
    private int LastI = 0, LastJ = 0, LastRowI=0;
    private String[] PeriodsTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appbar_layout);
        Objects.requireNonNull(getSupportActionBar()).hide();
        instance = this;
        Intent IntentActivity = getIntent();
        if(IntentActivity.hasExtra(Intent.EXTRA_TEXT)){
            name_group = IntentActivity.getStringExtra(Intent.EXTRA_TEXT);
        }

        Marks = getResources().getStringArray(R.array.MarksWithText);

        dateText = findViewById(R.id.Date_Text2);

        ImageView iconDate = findViewById(R.id.date_icon);

        dbManager = new DBManager(this);
        dbManager.openDB();

        dateText.setText(dbManager.GetPeriodByToday());
        if(!dateText.getText().toString().isEmpty()){
            fromDate = dateText.getText().toString().split("-")[0];
            toDate = dateText.getText().toString().split("-")[1];
        }

        ArrayList<String> periodsList = new ArrayList<>(dbManager.GetAllPeriods());
        PeriodsTypes = new String[periodsList.size()+1];
        for (int i = 0; i < periodsList.size(); i++){
            PeriodsTypes[i] = periodsList.get(i);
        }
        PeriodsTypes[PeriodsTypes.length - 1] = "За всё время";

        SetAdapter();

        dateText.setOnItemClickListener((parent, view, position, id) -> {
            if(!dateText.getText().toString().equals(" ") && !dateText.getText().toString().isEmpty()){
                String date;
                if(!dateText.getText().toString().equals("За всё время")){
                    date = dbManager.GetPeriodDate(dateText.getText().toString());
                    fromDate = date.split("-")[0];
                    toDate = date.split("-")[1];
                }
                else{
                    date = "";
                    fromDate = "";
                    toDate = "";
                }
                dateText.setText(date);
                CreateTableOfMarks();
                SetAdapter();
            }
        });

        iconDate.setOnClickListener(v -> {
            MaterialDatePicker datePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(Pair.create(
                                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                    MaterialDatePicker.todayInUtcMilliseconds()))
                            .setTitleText("Select date")
                            .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                String StrDate = datePicker.getHeaderText();

                Datethis = "";
                String[] subStr;
                subStr = StrDate.split(" ");
                if(subStr.length == 4){
                    String[] subStrBuffer = new String[7];
                    subStrBuffer[0] = subStr[0];
                    subStrBuffer[1] = subStr[1].substring(0, subStr[1].length()-3) + ",";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    String currentYear = sdf.format(new Date());
                    subStrBuffer[2] = currentYear;
                    subStrBuffer[3] = "-";
                    subStrBuffer[4] = subStr[2];
                    subStrBuffer[5] = subStr[3].substring(0, subStr[1].length()-3) + ",";
                    subStrBuffer[6] = subStrBuffer[2];
                    subStr=subStrBuffer.clone();
                }
                if(subStr.length == 5){
                    String[] subStrBuffer = new String[7];
                    subStrBuffer[0] = subStr[0];
                    subStrBuffer[1] = subStr[1] + ",";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                    String currentYear = sdf.format(new Date());
                    subStrBuffer[2] = currentYear;
                    subStrBuffer[3] = subStr[2];
                    subStrBuffer[4] = subStr[3];
                    subStrBuffer[5] = subStr[4] + ",";
                    subStrBuffer[6] = subStrBuffer[2];
                    subStr=subStrBuffer.clone();
                }
                if (subStr.length == 6){
                    String[] subStrBuffer = new String[7];
                    subStrBuffer[0] = subStr[0];
                    subStrBuffer[1] = subStr[1] + ",";
                    subStrBuffer[2] = subStr[5];
                    subStrBuffer[3] = subStr[2];
                    subStrBuffer[4] = subStr[3];
                    subStrBuffer[5] = subStr[4];
                    subStrBuffer[6] = subStrBuffer[2];
                    subStr=subStrBuffer.clone();
                }
                if(!subStr[1].equals("мая")){
                    subStr[1]=subStr[1].substring(0,subStr[1].length()-1);
                    subStr[5]=subStr[5].substring(0,subStr[5].length()-1);
                }
                if(!dbManager.GetMonth(subStr[1]).equals("0")){
                    subStr[1] = dbManager.GetMonth(subStr[1]);
                    subStr[5] = dbManager.GetMonth(subStr[5]);
                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100);
                    subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100);
                    Datethis = subStr[0] + "/" + subStr[1] + "/" + subStr[2] + " - " + subStr[4] + "/" + subStr[5] + "/" + subStr[6];
                    fromDate = subStr[0] + "/" + subStr[1] + "/" + subStr[2];
                    toDate = subStr[4] + "/" + subStr[5] + "/" + subStr[6];
                }
                else{
                    subStr[0] = dbManager.GetMonth(subStr[0]);
                    subStr[4] = dbManager.GetMonth(subStr[4]);
                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100);
                    subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100);
                    Datethis = subStr[1] + "/" + subStr[0] + "/" + subStr[2] + " - " + subStr[5] + "/" + subStr[4] + "/" + subStr[6];
                    fromDate = subStr[1] + "/" + subStr[0] + "/" + subStr[2];
                    toDate = subStr[5] + "/" + subStr[4] + "/" + subStr[6];
                }
                dateText.setText(Datethis);
                SetAdapter();
                CreateTableOfMarks();
            });
            datePicker.show(getSupportFragmentManager(), datePicker.toString());
        });

        CreateTableOfMarks();
    }

    public static GetMarksActivity getInstance() {
        return instance;
    }

    public void SetAdapter (){
        arrayAdapter = new ArrayAdapter(GetMarksActivity.this, R.layout.list_item, PeriodsTypes);
        dateText.setAdapter(arrayAdapter);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private View.OnClickListener getOnMarkClickListener() {
        return view -> {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(GetMarksActivity.this);
            LayoutInflater inflater = LayoutInflater.from(GetMarksActivity.this);
            View window_for_creation = inflater.inflate(R.layout.information_window, null);
            TextView NameField = window_for_creation.findViewById(R.id.NameField_InformationWindow);
            TextView DateField = window_for_creation.findViewById(R.id.DateField_InformationWindow);
            AutoCompleteTextView MarkField = window_for_creation.findViewById(R.id.MarkField_InformationWindow);
            TextView MarkTypeField = window_for_creation.findViewById(R.id.MarkTypeField_InformationWindow);
            TextView DescriptionField = window_for_creation.findViewById(R.id.DescriptionField_InformationWindow);
            TextView OkButton = window_for_creation.findViewById(R.id.button_ok);

            int IdStudent = ((view.getId())+1)/(DateList.size()+2)*(DateList.size()+2);
            int IdDate = (view.getId()%(DateList.size()+2));

            String date = TextViews.get(IdDate).getText().toString();
            String MarkStudent = TextViews.get(view.getId()).getText().toString();

            Drawable color;
            int i = (((view.getId())+1)/(DateList.size()+2));

            sqContent[i][IdDate].setBackground(GetMarksActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment_names));

            String WhichMarkTableForMark;
            if(TextViews.get(IdDate).getText().toString().equals(TextViews.get(IdDate + 1).getText().toString())){
                WhichMarkTableForMark = "1";
            }
            else if(TextViews.get(IdDate).getText().toString().equals(TextViews.get(IdDate - 1).getText().toString())){
                WhichMarkTableForMark = "2";
            }
            else{
                WhichMarkTableForMark = "IDK";
            }
            if(dbManager.GetImportantTypesOfWork()
                    .contains(dbManager.GetDateDescriptionAndTypeAndWhichMark(TextViews.get(IdDate).getText().toString(), name_group, WhichMarkTableForMark)
                            .get(0))
                    && LastI != 0 && LastJ!=0){
                color = getResources().getDrawable(R.drawable.cell_shape_fragment3);
            }
            else{
                if (LastI % 2 == 0){
                    color = getResources().getDrawable(R.drawable.cell_shape_fragment1);
                }
                else {
                    color = getResources().getDrawable(R.drawable.cell_shape_fragment2);
                }
            }
            sqContent[LastI][LastJ].setBackground(color);

            LastI = i;
            LastJ = IdDate;

            String WhichMarkTable;

            if(TextViews.get(IdDate).getText().toString().equals(TextViews.get(IdDate + 1).getText().toString())){
                WhichMarkTable = "1";
            }
            else if(TextViews.get(IdDate).getText().toString().equals(TextViews.get(IdDate - 1).getText().toString())){
                WhichMarkTable = "2";
            }
            else{
                WhichMarkTable = "IDK";
            }

            ArrayList<String> DesAndType = new ArrayList<>(dbManager.GetDateDescriptionAndTypeAndWhichMark(date, name_group, WhichMarkTable));

            NameField.setText("Ученик: " + NameList.get(IdStudent/(DateList.size() + 2) - 1));
            DateField.setText("Дата: " + date);
            MarkField.setText("Оценка: " + MarkStudent);
            arrayAdapter = new ArrayAdapter(GetMarksActivity.this, R.layout.list_item, Marks);
            MarkField.setAdapter(arrayAdapter);
            MarkTypeField.setText("Тип работы: " + DesAndType.get(0));
            DescriptionField.setText("Описание: " + DesAndType.get(1));

            a_builder.setView(window_for_creation);

            a_builder.setCancelable(true);
            AlertDialog alert = a_builder.create();
            OkButton.setOnClickListener(v -> {
                String Mark;
                if(MarkField.getText().toString().length()==9){
                    Mark = MarkField.getText().toString().substring(8);
                }
                else{
                    Mark = "";
                }
                if(!Mark.equals(MarkStudent)){
                    dbManager.updateMark(name_group,NameList.get(IdStudent/(DateList.size() + 2) - 1),date,DesAndType.get(0), DesAndType.get(1), DesAndType.get(2), Mark);
                    TextViews.get(view.getId()).setText(Mark);


                    String AverageScore = dbManager.GetAverageScore(name_group,NameList.get(IdStudent/(DateList.size() + 2) - 1));

                    int red=0, green=0, blue=0;

                    if(!AverageScore.equals("")){
                        green = (int)((Float.parseFloat(AverageScore)/5-0.1)*232);
                        blue = (int)((Float.parseFloat(AverageScore)/5)*126);
                        if (Float.parseFloat(AverageScore)>3.5){
                            red = (int)((1-Float.parseFloat(AverageScore)/5+0.5)*224);
                        }
                        else {
                            red = 224 - (int)((Float.parseFloat(AverageScore)/5)*30);
                        }

                        final ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(red, green, blue));
                        final SpannableStringBuilder text = new SpannableStringBuilder(AverageScore);
                        text.setSpan(style, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        TextViews.get((((view.getId())+1)/(DateList.size()+2)+1)*(DateList.size()+2)-1).setText(text);
                    }
                    else{
                        TextViews.get((((view.getId())+1)/(DateList.size()+2)+1)*(DateList.size()+2)-1).setText(AverageScore);
                    }

                }
                alert.cancel();
            });
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();
        };
    }

    private View.OnClickListener getDateOnClickListener() {
        return v -> {
            String WhichMarkTable;

            if(TextViews.get(v.getId()).getText().toString().equals(TextViews.get((v.getId()) + 1).getText().toString())){
                WhichMarkTable = "1";
            }
            else if(TextViews.get(v.getId()).getText().toString().equals(TextViews.get((v.getId()) - 1).getText().toString())){
                WhichMarkTable = "2";
            }
            else{
                WhichMarkTable = "IDK";
            }

            Intent AddMarksActivityIntent = new Intent(GetMarksActivity.this, AddMarksActivity.class);
            AddMarksActivityIntent.putExtra(Intent.EXTRA_TEXT, name_group);
            Log.d("MyLogs", String.valueOf(TextViews.get(v.getId()).getText()));
            AddMarksActivityIntent.putExtra(Intent.EXTRA_COMPONENT_NAME, String.valueOf(TextViews.get(v.getId()).getText()));
            AddMarksActivityIntent.putExtra(Intent.ACTION_INSERT, WhichMarkTable);
            startActivity(AddMarksActivityIntent);
        };
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private View.OnClickListener getNameOnClickListener() {
        return view -> {
            int i = (((view.getId())+1)/(DateList.size()+2));
            Drawable color;

            if (LastRowI == i) {

                for (int j = 0; j<DateList.size()+2; j++){

                    if (j!=0 && j<DateList.size()+1){
                        String WhichMarkTable;
                        if(TextViews.get(j).getText().toString().equals(TextViews.get(j + 1).getText().toString())){
                            WhichMarkTable = "1";
                        }
                        else if(TextViews.get(j).getText().toString().equals(TextViews.get(j - 1).getText().toString())){
                            WhichMarkTable = "2";
                        }
                        else{
                            WhichMarkTable = "IDK";
                        }
                        if(dbManager.GetImportantTypesOfWork()
                                .contains(dbManager.GetDateDescriptionAndTypeAndWhichMark(TextViews.get(j).getText().toString(), name_group, WhichMarkTable)
                                        .get(0))){
                            color = getResources().getDrawable(R.drawable.cell_shape_fragment3);
                        }
                        else{
                            if (LastRowI % 2 == 0){
                                color = getResources().getDrawable(R.drawable.cell_shape_fragment1);
                            }
                            else {
                                color = getResources().getDrawable(R.drawable.cell_shape_fragment2);
                            }
                        }
                    }
                    else if (LastRowI % 2 == 0){
                        color = getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else {
                        color = getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }

                    sqContent[LastRowI][j].setBackground(color);

                }
                LastRowI = 0;
            }
            else{
                for (int j = 0; j<DateList.size()+2; j++) {
                    sqContent[i][j].setBackground(GetMarksActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment_names));
                }
                for (int j = 0; j<DateList.size()+2; j++){
                    if (LastRowI % 2 == 0){
                        color = getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else {
                        color = getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    sqContent[LastRowI][j].setBackground(color);

                }
                LastRowI = i;
            }
        };
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public void CreateTableOfMarks(){
        TextViews.clear();
        int idCounter = 0;
        if (sizeI != 0 && sizeJ != 0){
            tableLayout.removeAllViews();
        }
        NameList = new ArrayList<>();
        DateList = new ArrayList<>();

        NameList = dbManager.GetAllStudents(name_group);
        DateList = dbManager.GetDatesFromTo(name_group, String.valueOf(fromDate), String.valueOf(toDate));

        sizeJ = DateList.size() + 2;
        sizeI = NameList.size() + 1;
        sqContent = new RelativeLayout[sizeI][sizeJ];
        scrollY = findViewById(R.id.field_y);
        scrollX = findViewById(R.id.field_x);
        gestureDetectorY = new GestureDetector(GetMarksActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                scrollX.smoothScrollBy((int) distanceX, 0);
                scrollY.smoothScrollBy(0, (int) (distanceY));
                return true;
            }
        });
        scrollY.setOnTouchListener((v, event) -> {
            gestureDetectorY.onTouchEvent(event);
            return true;
        });
        tableLayout = findViewById(R.id.table);
        TableRow[] row = new TableRow[sizeI];
        TableRow.LayoutParams paramsTableRow = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        paramsTableRow.setMargins(0, 50, 0, 50);
        paramsTableRow.weight = 1;

        for (int i = 0; i < sizeI; i++) {
            row[i] = new TableRow(GetMarksActivity.this);
            row[i].setLayoutParams(paramsTableRow);
            row[i].setWeightSum(sizeJ);
            ArrayList<String> markList = new ArrayList<>();
            if(i!=0){
                markList = dbManager.GetDatesMarksForStudent(name_group, NameList.get(i-1), DateList);
            }
            for (int j = 0; j < sizeJ; j++) {

                RelativeLayout relativeLayout = new RelativeLayout(GetMarksActivity.this);
                TextView textView = new TextView(GetMarksActivity.this);
                textView.setId(idCounter);
                idCounter += 1;

                if(j == 0 && i != 0){
                    if(NameList.get(i-1).split(" ").length==2){
                        textView.setText(NameList.get(i-1).split(" ")[0] + " "+ NameList.get(i-1).split(" ")[1].substring(0,1) + ".");
                    }
                    else if(NameList.get(i-1).split(" ").length==3){
                        textView.setText(NameList.get(i-1).split(" ")[0] + " "+ NameList.get(i-1).split(" ")[1].substring(0,1) + "."
                                + " "+ NameList.get(i-1).split(" ")[2].substring(0,1) + ".");
                    }
                    else {
                        textView.setText(NameList.get(i-1));
                    }
                }
                else if(i==0 && j==sizeJ-1){
                    textView.setText("Итог");
                }
                else if(j==sizeJ-1 && i!=0){
                    String AverageScore = dbManager.GetAverageScore(name_group, NameList.get(i-1), DateList);

                    int red, green, blue;

                    if(!AverageScore.equals("")){
                        green = (int)((Float.parseFloat(AverageScore)/5-0.1)*232);
                        blue = (int)((Float.parseFloat(AverageScore)/5)*126);
                        if (Float.parseFloat(AverageScore)>3.5){
                            red = (int)((1-Float.parseFloat(AverageScore)/5+0.5)*224);
                        }
                        else {
                            red = 224 - (int)((Float.parseFloat(AverageScore)/5)*30);
                        }

                        final ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(red, green, blue));
                        final SpannableStringBuilder text = new SpannableStringBuilder(AverageScore);
                        text.setSpan(style, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        textView.setText(text);
                    }
                    else{
                        textView.setText(AverageScore);
                    }
                }
                else if(i == 0 && j != 0){
                    textView.setText(DateList.get(j-1));
                }
                else if(i == 0 && j == 0){
                    textView.setText("");
                }
                else {
                    textView.setText(markList.get(j - 1));
                }

                textView.setTextSize(20);
                textView.setTextColor(getResources().getColor(R.color.black));
                if(i == 0 && j != 0 && j!=sizeJ-1){
                    textView.setOnClickListener(getDateOnClickListener());
                }
                if(i != 0 && j != 0 && j!=sizeJ-1){
                    textView.setOnClickListener(getOnMarkClickListener());
                }
                if(j == 0 && i != 0){
                    textView.setOnClickListener(getNameOnClickListener());
                }

                RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

                editTextParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                relativeLayout.setPadding(10,10,10,10);
                TextViews.add(textView);
                relativeLayout.addView(textView, editTextParams);
                relativeLayout.setBackgroundResource(R.drawable.cell_shape);
                sqContent[i][j] = relativeLayout;
                if (i % 2 == 0){
                    sqContent[i][j].setBackground(GetMarksActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1));
                }
                else {
                    sqContent[i][j].setBackground(GetMarksActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2));
                }

                if (i!=0 && j!=0 && j!=sizeJ-1){
                    String WhichMarkTable;
                    if(TextViews.get(j).getText().toString().equals(TextViews.get(j + 1).getText().toString())){
                        WhichMarkTable = "1";
                    }
                    else if(TextViews.get(j).getText().toString().equals(TextViews.get(j - 1).getText().toString())){
                        WhichMarkTable = "2";
                    }
                    else{
                        WhichMarkTable = "IDK";
                    }
                    if(dbManager.GetImportantTypesOfWork()
                            .contains(dbManager.GetDateDescriptionAndTypeAndWhichMark(TextViews.get(j).getText().toString(), name_group, WhichMarkTable)
                                    .get(0))){
                        sqContent[i][j].setBackground(GetMarksActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment3));
                    }
                }

                row[i].addView(sqContent[i][j], j);
            }
            tableLayout.addView(row[i], i);
        }
    }

    @Override
    protected void onDestroy(){ //закрытие базы во время закрытия приложения
        super.onDestroy();
        dbManager.DBClose();
    }
    @Override
    protected void onRestart(){ //Обновление данных при активации актвити
        super.onRestart();
        LastI = 0;
        LastJ = 0;
        CreateTableOfMarks();
    }
}
