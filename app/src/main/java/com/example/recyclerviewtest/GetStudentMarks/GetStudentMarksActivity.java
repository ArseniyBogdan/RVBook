package com.example.recyclerviewtest.GetStudentMarks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class GetStudentMarksActivity extends AppCompatActivity {

    private static GetStudentMarksActivity instance;

    private String name_group, Datethis, fromDate = "", toDate = "";
    private RecyclerView rv_MarksList;
    private DBManager dbManager;
    private TextView titleText;

    private final ArrayList<String> StudentList = new ArrayList<>();

    private AutoCompleteTextView DateTextView, TypeOfWorkTextView, StudentTextView;
    private String[] PeriodsTypes, Types, Students;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_get_student_marks);

        instance = this;

        dbManager = new DBManager(this);
        dbManager.openDB();

        Intent IntentActivity = getIntent();

        titleText = findViewById(R.id.GetStudentMarks_Text);

        if (IntentActivity.hasExtra(Intent.EXTRA_TEXT)) {
            //передача данных на это активити
            name_group = IntentActivity.getStringExtra(Intent.EXTRA_TEXT);
            titleText.setText(name_group);
        }

        rv_MarksList = findViewById(R.id.rv_GetStudentMarks);
        DateTextView = findViewById(R.id.DateGetStudentActivity);
        TypeOfWorkTextView = findViewById(R.id.MarkType_GetStudentActivity);
        ImageView dateIcon = findViewById(R.id.icon_date_GetStudentMarks);
        StudentTextView = findViewById(R.id.Student_TextView);

        rv_MarksList.setLayoutManager(new LinearLayoutManager(this));

        dateIcon.setOnClickListener(DateOnClickListener);

        //получаю список периодов
        ArrayList<String> periodsList = new ArrayList<>(dbManager.GetAllPeriods());
        PeriodsTypes = new String[periodsList.size()];
        for (int i = 0; i < periodsList.size(); i++){
            PeriodsTypes[i] = periodsList.get(i);
        }
        //получаю список типов работ
        ArrayList<String> markList = new ArrayList<>(dbManager.GetAllTypesOfMarksOfGroups());
        Types = new String[markList.size()+2];
        Types[0] = "Все типы работ";
        Types[1] = "Долги";
        for (int i = 0; i < markList.size(); i++){
            Types[i+2] = markList.get(i);
        }
        //получаю список студентов
        StudentList.addAll(dbManager.GetAllStudents(name_group));
        Students = new String[StudentList.size()];
        for (int i = 0; i < StudentList.size(); i++){
            Students[i] = StudentList.get(i);
        }

        SetStudentsAdapter();
        SetDateAdapter();
        SetTypesAdapter();

        TypeOfWorkTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            if(!StudentTextView.getText().toString().isEmpty()){
                SetListOfMarks();
            }
        });

        // ставлю замену названия периода на дату, которому она соответствует
        DateTextView.setOnItemClickListener((parent, view, position, id) -> {
            if(!DateTextView.getText().toString().equals(" ") && !DateTextView.getText().toString().isEmpty()){
                String date = dbManager.GetPeriodDate(DateTextView.getText().toString());
                fromDate = date.split("-")[0];
                toDate = date.split("-")[1];
                DateTextView.setText(date);
                SetDateAdapter();
                if(!StudentTextView.getText().toString().isEmpty()){
                    SetListOfMarks();
                }
            }
        });

        StudentTextView.setOnItemClickListener((adapterView, view, i, l) -> SetListOfMarks());
    }

    public void SetListOfMarks(){
        // установка списка работ
        ArrayList<String> Dates = new ArrayList<>(dbManager.GetAllDatesByTypeOfMark(name_group, fromDate, toDate, TypeOfWorkTextView.getText().toString(), StudentTextView.getText().toString()));
        GetStudentsMarksAdapter getStudentsMarksAdapter = new GetStudentsMarksAdapter(Dates.size());
        getStudentsMarksAdapter.setListOfDates(Dates);
        getStudentsMarksAdapter.setListOfMarks(dbManager.GetAllMarksByDatesAndTypeOfMark(Dates, name_group,
                TypeOfWorkTextView.getText().toString(), StudentTextView.getText().toString()));
        getStudentsMarksAdapter.setListOfTypesMarks(dbManager.GetAllTypesOfMarksByDates(Dates, name_group, TypeOfWorkTextView.getText().toString(), StudentTextView.getText().toString()));
        getStudentsMarksAdapter.setListOfDescription(dbManager.GetAllDescriptionsByDates(Dates, name_group, TypeOfWorkTextView.getText().toString(), StudentTextView.getText().toString()));
        rv_MarksList.setAdapter(getStudentsMarksAdapter);
    }

    public void SetDateAdapter (){
        // установка адаптера с датами
        ArrayAdapter<String> periodsArrayAdapter = new ArrayAdapter(GetStudentMarksActivity.this, R.layout.list_item, PeriodsTypes);
        DateTextView.setAdapter(periodsArrayAdapter);
    }

    public void SetStudentsAdapter (){
        // установка адаптера со студентами
        ArrayAdapter<String> studentsArrayAdapter = new ArrayAdapter(GetStudentMarksActivity.this, R.layout.list_item, Students);
        StudentTextView.setAdapter(studentsArrayAdapter);
    }

    public void SetTypesAdapter (){
        // установка адаптера с типами работ
        ArrayAdapter<String> typesArrayAdapter = new ArrayAdapter(GetStudentMarksActivity.this, R.layout.list_item, Types);
        TypeOfWorkTextView.setAdapter(typesArrayAdapter);
    }

    // обработчик нажатий на иконку дат
    View.OnClickListener DateOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
                    subStr[1]=subStr[1].substring(0,subStr[1].length()-1); // убираем запятую после числа или даты
                    subStr[5]=subStr[5].substring(0,subStr[5].length()-1); // убираем запятую после 2 числа или даты
                }
                if(!dbManager.GetMonth(subStr[1]).equals("0")){
                    subStr[1] = dbManager.GetMonth(subStr[1]); //получаем месяц из русской раскладки
                    subStr[5] = dbManager.GetMonth(subStr[5]);
                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100); // Год 2021 сводим к формату 21
                    subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100);
                    Datethis = subStr[0] + "/" + subStr[1] + "/" + subStr[2] + " - " + subStr[4] + "/" + subStr[5] + "/" + subStr[6];
                    fromDate = subStr[0] + "/" + subStr[1] + "/" + subStr[2];
                    toDate = subStr[4] + "/" + subStr[5] + "/" + subStr[6];
                }
                else{
                    subStr[0] = dbManager.GetMonth(subStr[0]); // из строки получаем числовой порядок месяца
                    subStr[4] = dbManager.GetMonth(subStr[4]); // из строки получаем числовой порядок месяца 2
                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100); // Год 2021 сводим к формату 21
                    subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100); // Год 2021 сводим к формату 21
                    Datethis = subStr[1] + "/" + subStr[0] + "/" + subStr[2] + " - " + subStr[5] + "/" + subStr[4] + "/" + subStr[6];
                    fromDate = subStr[1] + "/" + subStr[0] + "/" + subStr[2];
                    toDate = subStr[5] + "/" + subStr[4] + "/" + subStr[6];
                }
                DateTextView.setText(Datethis);
                SetDateAdapter();
            });
            datePicker.show(getSupportFragmentManager(), datePicker.toString());
        }
    };

    public static GetStudentMarksActivity getInstance(){
        return instance;
    }
}