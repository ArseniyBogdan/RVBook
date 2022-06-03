package com.example.recyclerviewtest.AddDateMarks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.MainActivity;
import com.example.recyclerviewtest.R;
import com.example.recyclerviewtest.Settings.TypesOfMarksAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class AddMarksActivity extends AppCompatActivity {


    private ImageView deleteDateMarks;
    private String name_group, WhichMark = "1";
    private TextInputEditText dateText;
    private static AddMarksActivity instance;
    private TextInputEditText description;
    private DBManager dbManager;
    private MarksAdapter marksAdapter;
    private RecyclerView List_Marks;
    private LinearLayout mLayout;
    private final ArrayList<String> MarkList = new ArrayList<>();
    private AutoCompleteTextView markType;
    private String Date;
    private ArrayAdapter arrayAdapter;
    private String[] Types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marks);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Intent IntentActivity = getIntent();
        dbManager = new DBManager(this);
        dbManager.openDB();



        if(IntentActivity.hasExtra(Intent.EXTRA_TEXT)){
            name_group = IntentActivity.getStringExtra(Intent.EXTRA_TEXT);
        }

        instance = this;

        TextView nameGroupText = findViewById(R.id.AddMarks_Text);
        nameGroupText.setText(name_group);

        ImageView addMarksButton = findViewById(R.id.add_icon_AddMarks);
        addMarksButton.setOnClickListener(SetOnIconAddMarksClickListener);

        deleteDateMarks = findViewById(R.id.icon_Delete_Date_Marks);

        BottomNavigationView bottomNavigationView = findViewById(R.id.MarksNavigation);
        bottomNavigationView.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        mLayout = findViewById(R.id.MlinearLayout);
        List_Marks = findViewById(R.id.rv_Marks);
        List_Marks.setNestedScrollingEnabled(false);

        BottomNavigationItemView mark1 = findViewById(R.id.action_Mark1);
        BottomNavigationItemView mark2 = findViewById(R.id.action_Mark2);

        TextInputLayout dateTextViewMateial = findViewById(R.id.Date_TextView_Material);
        dateText = findViewById(R.id.TextDate);

        description = findViewById(R.id.Description_multiline);
        markType = findViewById(R.id.MarkType);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        List_Marks.setLayoutManager(layoutManager);

        SetListOfMarks();
        setMarkTypes();

        if(IntentActivity.hasExtra(Intent.EXTRA_COMPONENT_NAME) && IntentActivity.hasExtra(Intent.ACTION_INSERT)){
            WhichMark = IntentActivity.getStringExtra(Intent.ACTION_INSERT);
            Date = IntentActivity.getStringExtra(Intent.EXTRA_COMPONENT_NAME);
            dateText.setText(Date);

            if(WhichMark.equals("IDK")){
                WhichMark=dbManager.GetWhichMark(Date, name_group);
            }
            if(WhichMark.equals("1")){
                mark1.performClick();
            }
            else if(WhichMark.equals("2")){
                mark2.performClick();
            }
        }

        dateTextViewMateial.setEndIconOnClickListener(v -> {
            MaterialDatePicker datePicker =
                    MaterialDatePicker.Builder.datePicker()
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .setTitleText("Select date")
                            .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                String StrDate = datePicker.getHeaderText();
                Date = "";
                String[] subStr;
                subStr = StrDate.split(" ");
                if(!subStr[1].equals("мая")){
                    if (subStr[1].endsWith(",")
                            || subStr[1].endsWith(".")){
                        subStr[1]=subStr[1].substring(0,subStr[1].length()-1);
                    }
                    else{
                        subStr[1] = GetMonth(subStr[1]);
                    }
                }
                if(!GetMonth(subStr[1]).equals("0")){
                    subStr[1] = GetMonth(subStr[1]);
                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100);
                    Date = subStr[0] + "/" + subStr[1] + "/" + subStr[2];
                }
                else{
                    subStr[0] = GetMonth(subStr[0]);
                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100);
                    Date = subStr[1] + "/" + subStr[0] + "/" + subStr[2];
                }

                if(dbManager.CheckDate(Date, name_group, WhichMark)){
                    marksAdapter.SetSizeMarks((int) dbManager.GetStudentTableSize(name_group));
                    marksAdapter.SetMarks(dbManager.GetDateMarks(Date, name_group, dbManager.GetAllStudents(name_group), WhichMark));
                    List_Marks.setAdapter(marksAdapter);
                    markType.setText(dbManager.GetType());
                    arrayAdapter = new ArrayAdapter(AddMarksActivity.this, R.layout.list_item, Types);
                    markType.setAdapter(arrayAdapter);
                    description.setText(dbManager.GetDescription());
                    deleteDateMarks.setVisibility(View.VISIBLE);
                    deleteDateMarks.setOnClickListener(SetOnIconDeleteMarksClickListener);
                }
                else {
                    deleteDateMarks.setVisibility(View.INVISIBLE);
                    ClearActivity();
                }
                dateText.setText(Date);
            });
            datePicker.show(getSupportFragmentManager(), datePicker.toString());
        });

        markType.setOnItemClickListener((adapterView, view, i, l) -> {
            if(markType.getText().toString().equals("Добавить тип")){
                AlertDialog.Builder a_builder_fab = new AlertDialog.Builder(AddMarksActivity.this);
                LayoutInflater inflater_fab = LayoutInflater.from(AddMarksActivity.this);
                View window_for_creation_fab = inflater_fab.inflate(R.layout.window_for_creation, null);

                a_builder_fab.setView(window_for_creation_fab);

                TextInputEditText NameField = window_for_creation_fab.findViewById(R.id.creationField);
                TextInputLayout NameFieldLayout = window_for_creation_fab.findViewById(R.id.NameGroup);

                NameFieldLayout.setHint("Вид работы");

                a_builder_fab.setCancelable(true)
                        .setPositiveButton("Добавить", (dialog, which) -> {
                            String NameOfImportantWork = Objects.requireNonNull(NameField.getText()).toString();

                            if(TextUtils.isEmpty(NameOfImportantWork)){
                                Snackbar.make(mLayout, "Введите название типа работ", Snackbar.LENGTH_SHORT).show();
                            }
                            else if (dbManager.CheckTypeOfWork(NameOfImportantWork)){
                                // добавляем введённый текст в список названий
                                dbManager.InsertTypesOfMarks(NameOfImportantWork);
                                markType.setText("");
                                setMarkTypes();
                            }
                            else{
                                Snackbar.make(mLayout, "Такой вид работы уже существует  ", Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отменить", (dialog, which) -> {
                            dialog.cancel();
                        });
                AlertDialog alert1 = a_builder_fab.create();
                alert1.setTitle("Вы хотите добавить новый вид работы?");
                alert1.show();
            }
        });
    }

    public View.OnClickListener SetOnIconDeleteMarksClickListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            android.app.AlertDialog.Builder a_builder = new android.app.AlertDialog.Builder(AddMarksActivity.this);

            a_builder.setCancelable(true)
                    .setPositiveButton("Подтвердить", (dialog, which) -> {
                        dbManager.Delete_DateMarks(dbManager.GetDateAndWhichMarkId(Date, name_group, WhichMark), name_group);
                        deleteDateMarks.setVisibility(View.INVISIBLE);

                        ClearAllWithoutDate();
                    })
                    .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());

            android.app.AlertDialog alert = a_builder.create();
            alert.setTitle("Вы точно хотите удалить отметки за эту работу");
            alert.show();
        }
    };

    // обработчик нажатий для меню снизу
    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()){
                    case R.id.action_Mark1:
                        WhichMark = "1";
                        SetMarksIfTheyArePresent();
                        break;
                    case R.id.action_Mark2:
                        WhichMark = "2";
                        SetMarksIfTheyArePresent();
                        break;
                }
                return true;
            };

    public void setMarkTypes(){
        MarkList.clear();
        MarkList.addAll(dbManager.GetAllTypesOfMarksOfGroups());
        Types = new String[MarkList.size() + 1];
        for (int i = 0; i < MarkList.size(); i++){
            Types[i] = MarkList.get(i);
        }

        Types[Types.length - 1] = "Добавить тип";

        arrayAdapter = new ArrayAdapter(this, R.layout.list_item, Types);
        markType.setAdapter(arrayAdapter);
    }

    public void SetMarksIfTheyArePresent(){
        if(Date == null){return;}
        else if(dbManager.CheckDate(Date, name_group, WhichMark)){
            marksAdapter.SetSizeMarks((int) dbManager.GetStudentTableSize(name_group));
            marksAdapter.SetMarks(dbManager.GetDateMarks(Date, name_group, dbManager.GetAllStudents(name_group), WhichMark));
            List_Marks.setAdapter(marksAdapter);
            markType.setText(dbManager.GetType());
            arrayAdapter = new ArrayAdapter(this, R.layout.list_item, Types);
            markType.setAdapter(arrayAdapter);
            description.setText(dbManager.GetDescription());
            deleteDateMarks.setVisibility(View.VISIBLE);
            deleteDateMarks.setOnClickListener(SetOnIconDeleteMarksClickListener);
        }
        else {
            deleteDateMarks.setVisibility(View.INVISIBLE);
            ClearAllWithoutDate();
        }
    }

    public View.OnClickListener SetOnIconAddMarksClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String DateText = Objects.requireNonNull(dateText.getText()).toString();
            String MarkType = Objects.requireNonNull(markType.getText()).toString();
            String Description = Objects.requireNonNull(description.getText()).toString();

            if(TextUtils.isEmpty(DateText)){
                Snackbar.make(mLayout , "Введите дату", Snackbar.LENGTH_SHORT).show(); //Небольшое уведомление в нижней части экрана, в котором говорится, что пользователь не ввёл название группы
                dateText.setHintTextColor(Color.RED);
            }
            else{
                if(dbManager.CheckDate(Date, name_group, WhichMark)){
                    if(!CheckChangeMarks(marksAdapter.GetMarks(), dbManager.GetDateMarks(Date, name_group, dbManager.GetAllStudents(name_group), WhichMark)) && !CheckChangeType() && !CheckChangeDescription()){
                        Snackbar.make(mLayout , "Измените данные оценок для сохранения", Snackbar.LENGTH_SHORT).show();
                    }
                    else{
                        dbManager.updateMarks(name_group, dbManager.GetAllStudents(name_group), DateText,
                                MarkType, Description, WhichMark, marksAdapter.GetMarks());
                        Snackbar.make(mLayout , "Изменения сохранены", Snackbar.LENGTH_SHORT).show();
                    }
                }
                else {
                    dbManager.insertToMarks(name_group, dbManager.GetAllStudents(name_group), DateText,
                            MarkType, Description, WhichMark, marksAdapter.GetMarks());
//                            dbManager.ClearBuffer(); // очистить буфурную таблицу если что-то пойдёт не так
                    dbManager.SortTableMarksByDates(name_group);
                    Snackbar.make(mLayout , "Данные сохранены", Snackbar.LENGTH_SHORT).show();
                    deleteDateMarks.setVisibility(View.VISIBLE);
                    deleteDateMarks.setOnClickListener(SetOnIconDeleteMarksClickListener);
                }
            }
        }
    };

//    public String getMark(int itemPosition){ //ф-ия для получения выбранной оценки
//        String[] choose = getResources().getStringArray(R.array.Marks);
//        return choose[itemPosition];
//    }
//
//    public int getPositionType(String type){
//        String[] choose = getResources().getStringArray(R.array.Types);
//        for(int i = 0; i<choose.length; i++){
//            if(choose[i].equals(type)){
//                return i;
//            }
//        }
//        return 0;
//    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbManager.DBClose();
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

    public boolean CheckChangeMarks(ArrayList<String> marks, ArrayList<String> date_marks){
        for(int i = 0; i<marks.size(); i++){
            String mark = marks.get(i);
            String DateMark = date_marks.get(i);
            if(mark == null &&  DateMark != null){
                return true;
            }
            else {
                assert mark != null;
                if(mark.equals(DateMark)){
                    continue;
                }
                else if(!marks.get(i).equals(date_marks.get(i))){
                    return true;
                }
            }

        }
        return false;
    }

    public boolean CheckChangeType(){
        return !markType.getText().toString().equals(dbManager.GetType());
    }

    public boolean CheckChangeDescription(){
        return !Objects.requireNonNull(description.getText()).toString().equals(dbManager.GetDescription());
    }

    public void ClearActivity(){
        marksAdapter.SetSizeMarks((int) dbManager.GetStudentTableSize(name_group));
        List_Marks.setAdapter(marksAdapter);
        markType.setText("");
        arrayAdapter = new ArrayAdapter(AddMarksActivity.this, R.layout.list_item, Types);
        markType.setAdapter(arrayAdapter);
        description.setText("");
        dateText.setText("");
    }

    public void ClearAllWithoutDate(){
        marksAdapter.SetSizeMarks((int) dbManager.GetStudentTableSize(name_group));
        List_Marks.setAdapter(marksAdapter);
        markType.setText("");
        arrayAdapter = new ArrayAdapter(AddMarksActivity.this, R.layout.list_item, Types);
        markType.setAdapter(arrayAdapter);
        description.setText("");
    }

    public void SetListOfMarks(){
        marksAdapter = new MarksAdapter((int) dbManager.GetStudentTableSize(name_group));
        marksAdapter.setListOfName(dbManager.GetAllStudents(name_group));
        marksAdapter.SetSizeMarks((int) dbManager.GetStudentTableSize(name_group));
        List_Marks.setAdapter(marksAdapter);
        List_Marks.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public static AddMarksActivity getInstance() {
        return instance;
    }
}