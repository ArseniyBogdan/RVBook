package com.example.recyclerviewtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.recyclerviewtest.AddDateMarks.AddMarksActivity;
import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.GetMarks.GetMarksActivity;
import com.example.recyclerviewtest.GetStudentMarks.GetStudentMarksActivity;
import com.example.recyclerviewtest.Students.StudentsActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Objects;

public class Button_Activity extends AppCompatActivity {

    private String name_group;
    private int PositionElement;

    private LinearLayout StudentLayout, AddMarksLayout, GetMarksLayout;

    private final RelativeLayout[] sqContent = new RelativeLayout[10];

    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_cards);
        Objects.requireNonNull(getSupportActionBar()).hide();
        TextView sa_group = findViewById(R.id.groupNameTitle);
        Intent IntentActivity = getIntent();

        dbManager = new DBManager(this);
        dbManager.openDB();

        StudentLayout = findViewById(R.id.LinearLayout_Students);
        AddMarksLayout = findViewById(R.id.LinearLayout_AddMarks);
        GetMarksLayout = findViewById(R.id.LinearLayout_GetMarks);

        CardView addStudentCard = findViewById(R.id.Students_card);
        CardView addMarkCard = findViewById(R.id.AddMarks_card);
        CardView getMarksCard = findViewById(R.id.GetMarks_card);
        CardView getStudentMarksLayout = findViewById(R.id.GetStudentMarks_card);

        TextInputEditText addStudentText = findViewById(R.id.Students_card_Text);
        TextInputEditText addMarkText = findViewById(R.id.AddMarks_card_Text);
        TextInputEditText getMarkText = findViewById(R.id.GetMarks_card_Text);
        TextInputEditText getStudentMarkText = findViewById(R.id.GetStudentMarks_card_Text);

        if(IntentActivity.hasExtra(Intent.EXTRA_TEXT)){ //передача данных на это активити
            sa_group.setText(IntentActivity.getStringExtra(Intent.EXTRA_TEXT));
            name_group = IntentActivity.getStringExtra(Intent.EXTRA_TEXT);
            PositionElement = Integer.parseInt(IntentActivity.getStringExtra(Intent.EXTRA_CHOSEN_COMPONENT));
        }

        SetStatistics();

        // установка обработчиков нажатий
        addStudentCard.setOnClickListener(OnClickListenerStudentsCard);
        addStudentText.setOnClickListener(OnClickListenerStudentsCard);
        addMarkCard.setOnClickListener(OnClickListenerAddMarksCard);
        addMarkText.setOnClickListener(OnClickListenerAddMarksCard);
        getMarksCard.setOnClickListener(OnClickListenerGetMarksCard);
        getMarkText.setOnClickListener(OnClickListenerGetMarksCard);
        getStudentMarksLayout.setOnClickListener(onClickListenerGetStudentMarksCard);
        getStudentMarksLayout.setOnClickListener(onClickListenerGetStudentMarksCard);
    }

    @SuppressLint("SetTextI18n")
    private void SetStatistics(){
        // установка статистики на карточки
        StudentLayout.removeAllViews();
        AddMarksLayout.removeAllViews();
        GetMarksLayout.removeAllViews();
        ArrayList<String> SetValueOfCheckBoxes = new ArrayList<>(dbManager.GetValueOfCheckBoxes());

        // установка статистики на карточку со студентами
        for (int IdCounter = 3; IdCounter>-1; IdCounter--){

            RelativeLayout relativeLayout = new RelativeLayout(Button_Activity.this);
            TextView textView = new TextView(Button_Activity.this);
            textView.setId(IdCounter); // устанавливаем свой айдишник для каждой ячейки текста (потом будем к ним обращаться по этим айдишникам)


            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==0){
                textView.setText("Кол-во учеников: " + dbManager.GetAllStudents(name_group).size());
            }
            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==1){
                textView.setText("Отличников: " + dbManager.GetCountOfExcellentStudents(name_group));
            }
            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==2){
                textView.setText("Хорошистов: " + dbManager.GetCountOfGoodStudents(name_group));
            }
            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==3){
                textView.setText("Неаттестованных: " + dbManager.GetCountOfBadStudents(name_group));
            }
            textView.setTextSize(14);
            textView.setTextColor(getResources().getColor(R.color.black));
            RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams( // параметры положения для EditText
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            editTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT); //выравнивание по центру
            relativeLayout.setPadding(25,0,0,0);

            relativeLayout.addView(textView, editTextParams); //добавление в RelativeLayout
            sqContent[IdCounter] = relativeLayout;
            if (SetValueOfCheckBoxes.get(IdCounter).equals("true")){
                StudentLayout.addView(sqContent[IdCounter], 0);
            }
        }

        // установка статистики на карточку с добавлением отметок
        for (int IdCounter = 6; IdCounter>3; IdCounter--){
            RelativeLayout relativeLayout = new RelativeLayout(Button_Activity.this);
            TextView textView = new TextView(Button_Activity.this);
            textView.setId(IdCounter);


            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==4){
                textView.setText("Кол-во работ: " + dbManager.GetAllDates(name_group).size());
            }
            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==5){
                textView.setText("Кол-во срезовых: " + dbManager.GetCountOfImportantWorks(name_group));
            }
            textView.setTextSize(14);
            textView.setTextColor(getResources().getColor(R.color.black));
            RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            editTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            relativeLayout.setPadding(25,0,0,0);

            relativeLayout.addView(textView, editTextParams);
            sqContent[IdCounter] = relativeLayout;
            if (SetValueOfCheckBoxes.get(IdCounter).equals("true")){
                AddMarksLayout.addView(sqContent[IdCounter],0);
            }
        }

        // установка статистики на карточку с выводом отметок
        for (int IdCounter = 8; IdCounter>5; IdCounter--){
            RelativeLayout relativeLayout = new RelativeLayout(Button_Activity.this);
            TextView textView = new TextView(Button_Activity.this);
            textView.setId(IdCounter);


            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==6){
                textView.setText("Ср. балл класса: " + dbManager.GetAllGroupAverageScore().get(PositionElement));
            }
            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==7){
                textView.setText("Ср. балл по срезовым работам: " + dbManager.GetAverageScoreOfAllImportantWorks(name_group));
            }
            if(SetValueOfCheckBoxes.get(IdCounter).equals("true") && IdCounter==8){
                textView.setText("Кол-во долгов: " + dbManager.GetCountArrearagesOfGroup(name_group));
            }
            textView.setTextSize(14);
            textView.setTextColor(getResources().getColor(R.color.black));
            RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            editTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            relativeLayout.setPadding(25,0,0,0);

            relativeLayout.addView(textView, editTextParams);
            sqContent[IdCounter] = relativeLayout;
            if (SetValueOfCheckBoxes.get(IdCounter).equals("true")){
                GetMarksLayout.addView(sqContent[IdCounter],0);
            }
        }
    }

    private final View.OnClickListener OnClickListenerStudentsCard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent StudentActivityIntent = new Intent(Button_Activity.this, StudentsActivity.class); // Создаём намерение
            StudentActivityIntent.putExtra(Intent.EXTRA_TEXT, name_group); // передаём данные на активити Students
            startActivity(StudentActivityIntent); //запускаем активити
        }
    };

    private final View.OnClickListener OnClickListenerAddMarksCard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent AddMarksActivityIntent = new Intent(Button_Activity.this, AddMarksActivity.class); // Создаём намерение
            AddMarksActivityIntent.putExtra(Intent.EXTRA_TEXT, name_group); // передаём данные на активити AddMarks
            startActivity(AddMarksActivityIntent); //запускаем активити
        }
    };

    private final View.OnClickListener OnClickListenerGetMarksCard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent GetMarksActivityIntent = new Intent(Button_Activity.this, GetMarksActivity.class); // Создаём намерение
            GetMarksActivityIntent.putExtra(Intent.EXTRA_TEXT, name_group); // передаём данные на активити GetMarks
            startActivity(GetMarksActivityIntent); //запускаем активити
        }
    };

    private final View.OnClickListener onClickListenerGetStudentMarksCard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent GetStudentMarksActivityIntent = new Intent(Button_Activity.this, GetStudentMarksActivity.class); // Создаём намерение
            GetStudentMarksActivityIntent.putExtra(Intent.EXTRA_TEXT, name_group); // передаём данные на активити GetMarks
            startActivity(GetStudentMarksActivityIntent); //запускаем активити
        }
    };

    @Override
    protected void onRestart(){
        //Обновление данных при активации активити
        super.onRestart();
        dbManager.DBClose();
        dbManager = new DBManager(this);
        dbManager.openDB();
        SetStatistics();
    }

    @Override
    protected void onDestroy(){
        //закрытие базы во время закрытия приложения
        super.onDestroy();
        dbManager.DBClose();
    }
}