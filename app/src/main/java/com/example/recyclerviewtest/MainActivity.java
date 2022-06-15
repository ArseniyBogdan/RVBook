 package com.example.recyclerviewtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.recyclerviewtest.DB.DBConstants;
import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.Settings.PeriodsAdapter;
import com.example.recyclerviewtest.Settings.TypesOfMarksAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DBManager dbManager;

    private static MainActivity instance;

    private RecyclerView GroupList, ScheduleList, MarkTypesList;
     private FloatingActionButton addButton;
    private AppBarLayout appBar;
    private ScheduleAdapter scheduleAdapter;
    private String SelectedTab = "ПН", time, Group, Datethis;
    private TextView CountOfSelectedTypesOfMarks;

    private CardView rootElementPeriods, rootElementFinalMarks;

    public AppBarLayout appBarLayout;

    private TypesOfMarksAdapter typesOfMarksAdapter;
    private PeriodsAdapter periodsAdapter;

    public int marker = 0;

    private ConstraintLayout root;

    private TextView UpperBound5, UpperBound4, UpperBound3 , BottomBound5, BottomBound4, BottomBound3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

//        this.deleteDatabase("database_for_diary"); //удаление базы данных


        instance = this;
        dbManager = new DBManager(this);
        dbManager.openDB();

//        dbManager.CreateNewTables(); // добавляем новые таблицы
//        dbManager.ClearBuffer(); // очищаем буфер

        ScheduleList = findViewById(R.id.ScheduleList);
        appBar = findViewById(R.id.AppBar);
        GroupList = findViewById(R.id.rv_Groups);
        addButton = findViewById(R.id.fab);
        ImageView settingsIcon = findViewById(R.id.ic_settings);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        addButton.setOnClickListener(GroupOnClickListenerAdd);
        bottomNavigationView.setOnItemSelectedListener(mOnNavigationItemSelectedListener);
        settingsIcon.setOnClickListener(onSettingsIconClickListener);

        //задаём стиль отображения, как в LinearLayout для RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        GroupList.setLayoutManager(layoutManager);

        // переменная для роддительского элемента, на который будем выводить SnackBars
        root = findViewById(R.id.root_element);


        GroupCreate();
    }

    // ClickListener для создания диалогового окна с добавлением группы
    private final View.OnClickListener GroupOnClickListenerAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //создание кастомного диалогового окна
            AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View window_for_creation = inflater.inflate(R.layout.window_for_creation, null);
            a_builder.setView(window_for_creation);

            final TextInputEditText NameField = window_for_creation.findViewById(R.id.creationField);

            a_builder.setCancelable(true)
                    .setPositiveButton("Добавить", (dialog, which) -> {
                        String GroupName = Objects.requireNonNull(NameField.getText()).toString();
                        if(TextUtils.isEmpty(GroupName)){
                            Snackbar.make(root, "Введите название группы", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        else if(dbManager.CheckGroup(GroupName)){
                            Snackbar.make(root, "Такое название группы уже существует", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        // запись в БД новую группу
                        dbManager.insertToGroups(GroupName);
                        GroupCreate();
                    })
                    .setNegativeButton("Отменить", (dialog, which) -> {
                        dialog.cancel();
                    });
            AlertDialog alert = a_builder.create();
            alert.setTitle("Вы хотите создать новую группу?");
            alert.show();
        }
    };

    // Один ClickListener для двух добавления и изменения записи в расписании
    public View.OnClickListener ScheduleOnClickListenerAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // создание диалогового окна
            ScheduleList = findViewById(R.id.ScheduleList);
            AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View window_for_creation = inflater.inflate(R.layout.window_for_creation_schedule, null);
            AutoCompleteTextView NameField = window_for_creation.findViewById(R.id.creationField2);
            TextInputEditText Hours1 = window_for_creation.findViewById(R.id.Hours1);
            TextInputEditText Hours2 = window_for_creation.findViewById(R.id.Hours2);
            TextInputEditText Minutes1 = window_for_creation.findViewById(R.id.Minutes1);
            TextInputEditText Minutes2 = window_for_creation.findViewById(R.id.Minutes2);

            ArrayList<String> GroupNames = new ArrayList<>(dbManager.GetAllGroups());
            ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.getInstance(), R.layout.list_item, GroupNames);

            // marker = 0 => новая запись, marker = 1 => перезапись
            if(marker == 0){
                NameField.setAdapter(arrayAdapter);
            }
            else{
                NameField.setText(Group);
                NameField.setAdapter(arrayAdapter);

                String[] SrB = time.split(" ");

                Hours1.setText(SrB[0].split(":")[0]);
                Hours2.setText(SrB[1].split(":")[0]);
                Minutes1.setText(SrB[0].split(":")[1]);
                Minutes2.setText(SrB[1].split(":")[1]);
            }
            a_builder.setView(window_for_creation);

            a_builder.setCancelable(true)
                    .setPositiveButton("Сохранить", (dialog, which) -> { // Перегружаем метод onClick для этой кнопки
                        String GroupName = Objects.requireNonNull(NameField.getText()).toString();
                        String Hour1 = Objects.requireNonNull(Hours1.getText()).toString();
                        String Hour2 = Objects.requireNonNull(Hours2.getText()).toString();
                        String Minute1 = Objects.requireNonNull(Minutes1.getText()).toString();
                        String Minute2 = Objects.requireNonNull(Minutes2.getText()).toString();

                        if(TextUtils.isEmpty(GroupName)){
                            Snackbar.make(root, "Введите название урока", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        else if(TextUtils.isEmpty(Hour1) && TextUtils.isEmpty(Hour2) && TextUtils.isEmpty(Minute1) && TextUtils.isEmpty(Minute2)){
                            Snackbar.make(root, "Введите время", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        if (CheckCorrectEntry(Hour1, Hour2,
                                Minute1, Minute2)){
                            Snackbar.make(root, "Данные введены некорректно", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        else if(marker==0){
                            // запись новых групп в расписание
                            dbManager.InsertToScheduleTimeAndGroup(SelectedTab, GroupName, Hour1, Hour2,
                                    Minute1, Minute2);
                        }
                        else{
                            // перезапись
                            dbManager.updateSchedule(SelectedTab, Hour1, Hour2,
                                    Minute1, Minute2, time, GroupName);
                        }
                        ScheduleCreate(SelectedTab);
                        marker = 0;
                    })
                    .setNegativeButton("Отменить", (dialog, which) -> {
                        dialog.cancel();
                        marker = 0;
                    });
            AlertDialog alert2 = a_builder.create();
            alert2.setTitle("Вы хотите добавить урок?");
            alert2.show();
        }
    };

    // проверка на правильность ввода в методе ScheduleOnClickListenerAdd
    public boolean CheckCorrectEntry(String hours1, String hours2, String minutes1, String minutes2){
        if(IsNumeric(hours1) && IsNumeric(hours2) && IsNumeric(minutes1) && IsNumeric(minutes2)){
            if(!(Integer.parseInt(hours1)>=0 && Integer.parseInt(hours1) <=23)){
                return true;
            }
            if(!(Integer.parseInt(hours2)>=0 && Integer.parseInt(hours2) <=23)){
                return true;
            }
            if(!(Integer.parseInt(minutes1)>=0 && Integer.parseInt(minutes1) <=59)){
                return true;
            }
            return !(Integer.parseInt(minutes2) >= 0 && Integer.parseInt(minutes2) <= 59);
        }
        return true;
    }

    // перезапись типа работы
    public View.OnClickListener GetDeleteAndRenameDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // создание кастомного диалогового окна
            AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

            View window_for_creation = inflater.inflate(R.layout.window_for_creation, null);
            a_builder.setView(window_for_creation);

            TextInputEditText NameField = window_for_creation.findViewById(R.id.creationField);
            TextInputLayout NameFieldLayout = window_for_creation.findViewById(R.id.NameGroup);
            TextView TextFieldBeforeRename = v.findViewById(R.id.MarkType_TextView_Settings);

            NameField.setText(TextFieldBeforeRename.getText().toString());
            NameFieldLayout.setHint("Вид работы");

            a_builder.setCancelable(true)
                    .setPositiveButton("Сохранить", (dialog, which) -> {
                        String Type_Of_Work = Objects.requireNonNull(NameField.getText()).toString();
                        String TypeOfWorkBeforeRename = TextFieldBeforeRename.getText().toString();


                        if(!Type_Of_Work.equals(TypeOfWorkBeforeRename) && dbManager.CheckTypeOfWork(Type_Of_Work)){ // проверка на отсутствие текста в Текстовом поле
                            // перезапись
                            dbManager.RenameTypeOfWork(Type_Of_Work, TypeOfWorkBeforeRename);

                            //обновление списка работ
                            typesOfMarksAdapter = new TypesOfMarksAdapter((int) dbManager.GetSizeOfMarksTypesTable());
                            typesOfMarksAdapter.setListTypesOfMarks(dbManager.GetAllTypesOfMarksOfGroups()); //Передаём список строк адаптеру
                            typesOfMarksAdapter.setListValuesOfMarks(dbManager.GetAllValuesOFMarks());
                            MarkTypesList.setAdapter(typesOfMarksAdapter);
                        }
                        else if(Type_Of_Work.isEmpty()){
                            Snackbar.make(root, "Введите вид работы", Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            Snackbar.make(root, "Измените вид работы", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отменить", (dialog, which) -> {
                        dialog.cancel();
                    });
            AlertDialog alert = a_builder.create();
            alert.setTitle("Вы хотите переименовать вид работы?");
            alert.show();
        }
    };

    //проверка на числовой формат ввода
    public static boolean IsNumeric(String str){
        try{
            Integer.valueOf(str);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    //
    public View.OnClickListener GetAndRenameDialogPeriods = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder a_builder_fab = new AlertDialog.Builder(MainActivity.this); // привязываем строителя диалоговых окон к переменной
            LayoutInflater inflater_fab = LayoutInflater.from(MainActivity.this); //создаём некое представление
            View window_for_creation_fab = inflater_fab.inflate(R.layout.window_for_creation_periods, null); // передаём айди нашего xml файла (для того, чтобы метод inflate() понимал из чего мы будем создавать новое представление; null - нет родителя;

            a_builder_fab.setView(window_for_creation_fab); //устанавливаем получившийся вид в нашего строителя диалоговых окон

            TextInputEditText NameField = window_for_creation_fab.findViewById(R.id.creationField); //помещаем ссылку на текстовое окно нашей карточки(CardView) в переменную для дальнейшей работы с этим полем.
            TextInputEditText DatePeriod = window_for_creation_fab.findViewById(R.id.TextDatePeriods);
            TextInputLayout DatePeriodLayout = window_for_creation_fab.findViewById(R.id.Date_TextView_Periods);

            TextView PeriodBefore = v.findViewById(R.id.Periods_TextView_Settings); // устанавливаем старые данные
            TextView DateBefore = v.findViewById(R.id.Time_Period);
            NameField.setText(PeriodBefore.getText().toString());
            DatePeriod.setText(DateBefore.getText().toString().replace(" ", "-"));

            DatePeriodLayout.setEndIconOnClickListener(v1 -> {
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
                         subStr[1]=subStr[1].substring(0,subStr[1].length()-1).replace(".", ""); // убираем запятую после числа или даты
                         subStr[5]=subStr[5].substring(0,subStr[5].length()-1).replace(".", ""); // убираем запятую после 2 числа или даты
                     }
                     if(!dbManager.GetMonth(subStr[1]).equals("0")){
                         subStr[1] = dbManager.GetMonth(subStr[1]); //получаем месяц из русской раскладки
                         subStr[5] = dbManager.GetMonth(subStr[5]);
                         subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100); // Год 2021 сводим к формату 21
                         subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100);
                         Datethis = subStr[0] + "/" + subStr[1] + "/" + subStr[2] + "-" + subStr[4] + "/" + subStr[5] + "/" + subStr[6];
                     }
                     else{
                         subStr[0] = dbManager.GetMonth(subStr[0]); // из строки получаем числовой порядок месяца
                         subStr[4] = dbManager.GetMonth(subStr[4]); // из строки получаем числовой порядок месяца 2
                         subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100); // Год 2021 сводим к формату 21
                         subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100); // Год 2021 сводим к формату 21
                         Datethis = subStr[1] + "/" + subStr[0] + "/" + subStr[2] + "-" + subStr[5] + "/" + subStr[4] + "/" + subStr[6];
                     }
                     DatePeriod.setText(Datethis);
                 });
                 datePicker.show(getSupportFragmentManager(), datePicker.toString());
             });

             a_builder_fab.setCancelable(true)
                     .setPositiveButton("Сохранить", (dialog, which) -> {
                         String PeriodNow_Text = Objects.requireNonNull(NameField.getText()).toString();
                         String PeriodBefore_Text = Objects.requireNonNull(PeriodBefore.getText()).toString();
                         String DatePeriod_Text = Objects.requireNonNull(DatePeriod.getText()).toString();
                         String DateBefore_Text = Objects.requireNonNull(DateBefore.getText()).toString();

                         if (PeriodNow_Text.equals(PeriodBefore_Text) && dbManager.CheckPeriod(PeriodNow_Text)
                                 && DatePeriod_Text.equals(DateBefore_Text.replace(" ", "-")) && dbManager.CheckPeriodDate(DatePeriod_Text)){
                             Snackbar.make(rootElementPeriods, "Такое название периода уже существует или такие даты соответствуют уже существующему периоду", Snackbar.LENGTH_SHORT).show();
                         }
                         else if(TextUtils.isEmpty(PeriodNow_Text)){
                             Snackbar.make(rootElementPeriods, "Введите название периода", Snackbar.LENGTH_SHORT).show();
                         }
                         else if(TextUtils.isEmpty(DatePeriod_Text)){
                             Snackbar.make(rootElementPeriods, "Введите временной промежуток", Snackbar.LENGTH_SHORT).show();
                         }
                         else if (!dbManager.CheckPeriod(PeriodNow_Text)
                                 || !dbManager.CheckPeriodDate(DatePeriod_Text)){
                             // перезапись временного периода
                             dbManager.RenamePeriodDate(PeriodBefore_Text, PeriodNow_Text, DatePeriod_Text);

                             periodsAdapter = new PeriodsAdapter((int) dbManager.GetSizeOfPeriodsTable());
                             periodsAdapter.setListPeriods(dbManager.GetAllPeriods());
                             periodsAdapter.setListTimes(dbManager.GetAllDateOfPeriods());
                             MarkTypesList.setAdapter(periodsAdapter);
                         }
                         else{
                             Snackbar.make(rootElementPeriods, "Такой период уже существует  ", Snackbar.LENGTH_SHORT).show(); //Небольшое уведомление в нижней части экрана, в котором говорится, что пользователь не ввёл название группы
                         }
                     })
                     .setNegativeButton("Отменить", (dialog, which) -> {
                         dialog.cancel();
                     });
             AlertDialog alert = a_builder_fab.create();
             alert.setTitle("Вы хотите изменить данные этого периода?");
             alert.show();

         }
     };

    public static class GroupFragment extends Fragment {

        RecyclerView GroupList;
        DBManager dbManager;
        GroupAdapter groupAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.group_fragment, null);
            GroupList = view.findViewById(R.id.rv_FragmenGroups);
            dbManager = MainActivity.getInstance().getDBManager();

            groupAdapter = new GroupAdapter((int) dbManager.GetGroupsSize());
            groupAdapter.setListOfName(dbManager.GetAllGroups());
            groupAdapter.setAverageScore(dbManager.GetAllGroupAverageScore());
            groupAdapter.setListOfReminders(dbManager.GetAllReminders());
            GroupList.setAdapter(groupAdapter);

            return view;
        }
    }

    public static  class ScheduleFragment extends Fragment{

        RecyclerView ScheduleList;
        ScheduleAdapter scheduleAdapter;
        DBManager dbManager;
        TabLayout tabLayout;
        TabLayout.Tab FirstTab;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            dbManager = MainActivity.getInstance().getDBManager();

            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.schedule_fragment, null);
            tabLayout = view.findViewById(R.id.Tab_Layout);
            FirstTab =  tabLayout.getTabAt(0);
            ScheduleList = view.findViewById(R.id.ScheduleList);

            tabLayout.addOnTabSelectedListener(MainActivity.getInstance().TabListener);
            FirstTab.view.setBackgroundColor(getResources().getColor(R.color.SCHEDULE_DAY_SELECTED));
            LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.getInstance());
            ScheduleList.setLayoutManager(layoutManager);

            scheduleAdapter = new ScheduleAdapter((int) dbManager.GetSizeSubjectsInADay("ПН"));
            scheduleAdapter.setListOfTimes(dbManager.GetAllTimesInADay("ПН"));
            scheduleAdapter.setListOfSubjects(dbManager.GetAllSubjectsInADay("ПН"));
            ScheduleList.setAdapter(scheduleAdapter);
            MainActivity.getInstance().scheduleAdapter = scheduleAdapter;

            return view;
        }
    }

    public void SetTimeAndGroup(String time, String Group){
        this.time = time;
        this.Group = Group;
        marker = 1;
        Log.d("My_Log", dbManager.GetTimeAndDay(Group).get(0) + " Группа"); // проверка
        Log.d("My_Log", dbManager.GetTimeAndDay(Group).get(1)+ " Время");
    }

    public ArrayList<String> GetDayAndTime(String NameGroup){
        ArrayList<String> ClassAndTime = new ArrayList<>(dbManager.GetTimeAndDay(NameGroup));
        if(!ClassAndTime.isEmpty()){
            switch (ClassAndTime.get(0)){
                case "ПН": ClassAndTime.set(0, "Понедельник");break;
                case "ВТ": ClassAndTime.set(0, "Вторник");break;
                case "СР": ClassAndTime.set(0, "Среда");break;
                case "ЧТ": ClassAndTime.set(0, "Четверг");break;
                case "ПТ": ClassAndTime.set(0, "Пятница");break;
                case "СБ": ClassAndTime.set(0, "Суббота");break;
            }
        }
        return ClassAndTime;
    }

    public DBManager getDBManager(){
        return dbManager;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbManager.DBClose();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void StartActivityGroup(String name, int position){
        // Переход на Button_Activity
        Intent ButtonActivityIntent = new Intent(MainActivity.this, Button_Activity.class);
        ButtonActivityIntent.putExtra(Intent.EXTRA_CHOSEN_COMPONENT, String.valueOf(position));
        ButtonActivityIntent.putExtra(Intent.EXTRA_TEXT, name);
        startActivity(ButtonActivityIntent);
    }

    @Override
    protected void onRestart(){
        //Обновление данных при активации активити
        super.onRestart();
        GroupCreate();
    }


    private final TabLayout.OnTabSelectedListener TabListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            // замена фрагмента
            SelectedTab = Objects.requireNonNull(tab.getText()).toString();
            tab.view.setBackgroundColor(getResources().getColor(R.color.SCHEDULE_DAY_SELECTED));
            ScheduleCreate(SelectedTab);
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            //установка другого цвета на tab
            tab.view.setBackgroundColor(getResources().getColor(R.color.SCHEDULE_DAYS));
        }
        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };

    private final NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = new NavigationBarView.OnItemSelectedListener() { // обработчик нажатий для меню снизу с заменой фрагмента
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()){
                case R.id.action_Group:
                    fragment = new GroupFragment();
                    addButton.setOnClickListener(GroupOnClickListenerAdd);
                    GroupCreate();
                    break;
                case R.id.action_Schedule:
                    fragment = new ScheduleFragment();
                    appBar.setEnabled(false);
                    addButton.setOnClickListener(ScheduleOnClickListenerAdd);
                    break;
            }

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
            return true;
        }
    };

    //вызов SettingsMenu
    public View.OnClickListener onSettingsIconClickListener = this::showSettingsMenu;

    @SuppressLint("NonConstantResourceId")
    public void showSettingsMenu(View v){
        // меню для работы с настройками приложения (отображение статистики, типы работ, временные периоды, итоговые отметки, замена пароля)
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.settings_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.itemStatics:
                    //кастомное диалоговое окно
                    AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View window_for_creation = inflater.inflate(R.layout.statistics_window, null);
                    a_builder.setView(window_for_creation);

                    CheckBox checkBox1 = window_for_creation.findViewById(R.id.count_of_excellent_students_checkbox);
                    CheckBox checkBox2 = window_for_creation.findViewById(R.id.count_of_good_students_checkbox);
                    CheckBox checkBox3 = window_for_creation.findViewById(R.id.count_of_middle_students_checkbox);
                    CheckBox checkBox4 = window_for_creation.findViewById(R.id.count_of_bad_students_checkbox);
                    CheckBox checkBox5 = window_for_creation.findViewById(R.id.count_of_marks_checkbox);
                    CheckBox checkBox6 = window_for_creation.findViewById(R.id.count_of_important_work_checkbox);
                    CheckBox checkBox7 = window_for_creation.findViewById(R.id.count_of_arrearages_checkbox);
                    CheckBox checkBox8 = window_for_creation.findViewById(R.id.AverageScore_For_class_checkbox);
                    CheckBox checkBox9 = window_for_creation.findViewById(R.id.AverageScore_of_importantWorks_checkbox);

                    ArrayList<String> SetCheckBoxesValues = new ArrayList<>(dbManager.GetValueOfCheckBoxes());

                    //устанавливаю значения сохранённые в БД
                    checkBox1.setChecked(SetCheckBoxesValues.get(0).equals("true"));
                    checkBox2.setChecked(SetCheckBoxesValues.get(1).equals("true"));
                    checkBox3.setChecked(SetCheckBoxesValues.get(2).equals("true"));
                    checkBox4.setChecked(SetCheckBoxesValues.get(3).equals("true"));
                    checkBox5.setChecked(SetCheckBoxesValues.get(4).equals("true"));
                    checkBox6.setChecked(SetCheckBoxesValues.get(5).equals("true"));
                    checkBox7.setChecked(SetCheckBoxesValues.get(6).equals("true"));
                    checkBox8.setChecked(SetCheckBoxesValues.get(7).equals("true"));
                    checkBox9.setChecked(SetCheckBoxesValues.get(8).equals("true"));


                    TextView ButtonOk = window_for_creation.findViewById(R.id.button_ok_statistics);
                    TextView ButtonCancel = window_for_creation.findViewById(R.id.button_cancel_statistics);

                    a_builder.setCancelable(true);
                    AlertDialog alert = a_builder.create();
                    alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // делаю задний фон диалогового окна прозрачным
                    alert.show();

                    ButtonOk.setOnClickListener(v112 -> {
                        ArrayList<String> CheckBoxesValue = new ArrayList<>();

                        CheckBoxesValue.add(String.valueOf(checkBox1.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox2.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox3.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox4.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox5.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox6.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox7.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox8.isChecked()));
                        CheckBoxesValue.add(String.valueOf(checkBox9.isChecked()));

                        // изменение настроек вывода статистики в БД
                        dbManager.SetValueOfCheckBoxes(CheckBoxesValue);
                        alert.cancel();
                    });
                    ButtonCancel.setOnClickListener(v1 -> alert.cancel());

                    return true;
                case R.id.itemImportantWorks:
                    // кастомное диалоговое окно для типов работ
                    AlertDialog.Builder a_builder2 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater2 = LayoutInflater.from(MainActivity.this);
                    View window_for_creation2 = inflater2.inflate(R.layout.types_of_works_window, null);

                    MarkTypesList = window_for_creation2.findViewById(R.id.MarksTypes_List);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    MarkTypesList.setLayoutManager(layoutManager);

                    appBarLayout = window_for_creation2.findViewById(R.id.AppBar_TypesOfWorks);
                    ImageView btnCancel = window_for_creation2.findViewById(R.id.cancel_icon_Delete_TypeOfMarks);
                    ImageView btnDelete = window_for_creation2.findViewById(R.id.delete_icon_TypesOfMarks);
                    CountOfSelectedTypesOfMarks = window_for_creation2.findViewById(R.id.selected_TypesOfMarks);
                    CardView rootElementMarks = window_for_creation2.findViewById(R.id.root_element_TypesOfWorks);

                    a_builder2.setView(window_for_creation2);

                    typesOfMarksAdapter = new TypesOfMarksAdapter((int) dbManager.GetSizeOfMarksTypesTable());
                    typesOfMarksAdapter.setListTypesOfMarks(dbManager.GetAllTypesOfMarksOfGroups());
                    typesOfMarksAdapter.setListValuesOfMarks(dbManager.GetAllValuesOFMarks());
                    MarkTypesList.setAdapter(typesOfMarksAdapter);

                    a_builder2.setCancelable(true);
                    AlertDialog alert2 = a_builder2.create();
                    alert2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert2.show();

                    TextView textViewOk = window_for_creation2.findViewById(R.id.button_ok_statistics);
                    TextView textViewCancel = window_for_creation2.findViewById(R.id.button_cancel_statistics);

                    FloatingActionButton Fab = window_for_creation2.findViewById(R.id.fab_settings);

                    // добавление типа работ
                    Fab.setOnClickListener(v12 -> {
                        AlertDialog.Builder a_builder_fab = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater_fab = LayoutInflater.from(MainActivity.this);
                        View window_for_creation_fab = inflater_fab.inflate(R.layout.window_for_creation, null);

                        a_builder_fab.setView(window_for_creation_fab);

                        TextInputEditText NameField = window_for_creation_fab.findViewById(R.id.creationField);
                        TextInputLayout NameFieldLayout = window_for_creation_fab.findViewById(R.id.NameGroup);

                        NameFieldLayout.setHint("Вид работы");

                        a_builder_fab.setCancelable(true)
                                .setPositiveButton("Добавить", (dialog, which) -> {
                                    String NameOfImportantWork = Objects.requireNonNull(NameField.getText()).toString();

                                    if(TextUtils.isEmpty(NameOfImportantWork)){
                                        Snackbar.make(rootElementMarks, "Введите название типа работ", Snackbar.LENGTH_SHORT).show();
                                    }
                                    else if (dbManager.CheckTypeOfWork(NameOfImportantWork)){
                                        // добавляем введённый текст в список названий
                                        dbManager.InsertTypesOfMarks(NameOfImportantWork);

                                        typesOfMarksAdapter = new TypesOfMarksAdapter((int) dbManager.GetSizeOfMarksTypesTable());
                                        typesOfMarksAdapter.setListTypesOfMarks(dbManager.GetAllTypesOfMarksOfGroups());
                                        typesOfMarksAdapter.setListValuesOfMarks(dbManager.GetAllValuesOFMarks());
                                        MarkTypesList.setAdapter(typesOfMarksAdapter);
                                    }
                                    else{
                                        Snackbar.make(rootElementMarks, "Такой вид работы уже существует  ", Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Отменить", (dialog, which) -> {
                                    dialog.cancel();
                                });
                        AlertDialog alert1 = a_builder_fab.create();
                        alert1.setTitle("Вы хотите добавить новый вид работы?");
                        alert1.show();
                    });

                    // удаление типов работ
                    btnDelete.setOnClickListener(v110 -> {
                        AlertDialog.Builder a_builder12 = new AlertDialog.Builder(MainActivity.getInstance());
                        a_builder12.setCancelable(true)
                                .setPositiveButton("Подтвердить", (dialog, which) -> {
                                    ArrayList<String> SelectedTypeOfMarks = new ArrayList<>(typesOfMarksAdapter.GetSelectedTypesOfMarks());

                                    dbManager.DeleteTypeOfMark(SelectedTypeOfMarks);
                                    btnCancel.performClick();
                                })
                                .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                        AlertDialog alert14 = a_builder12.create();

                        if(typesOfMarksAdapter.GetCountOfSelectedTypesOfMarks() >1){
                            alert14.setTitle("Вы точно хотите удалить эти типы работ?");
                        }
                        else{
                            alert14.setTitle("Вы точно хотите удалить этот тип работ?");
                        }
                        alert14.show();
                    });

                    // кнопка отмены (снятие выделения)
                    btnCancel.setOnClickListener(v13 -> {
                        typesOfMarksAdapter = new TypesOfMarksAdapter((int) dbManager.GetSizeOfMarksTypesTable());
                        typesOfMarksAdapter.setListTypesOfMarks(dbManager.GetAllTypesOfMarksOfGroups());
                        typesOfMarksAdapter.setListValuesOfMarks(dbManager.GetAllValuesOFMarks());
                        MarkTypesList.setAdapter(typesOfMarksAdapter);
                        MainActivity.getInstance().SetTypeMarksAppBarFragment();
                    });

                    // кнопка обновления типов работ (срезоваая работа или нет)
                    textViewOk.setOnClickListener(v14 -> {
                        ArrayList<String> TypesOfMarks = new ArrayList<>(dbManager.GetAllTypesOfMarksOfGroups());
                        ArrayList<String> ValueList = new ArrayList<>(typesOfMarksAdapter.GetSelectedValueTypesOfMarks());

                        dbManager.UpdateTypesOfMarks(TypesOfMarks, ValueList);
                        alert2.cancel();
                    });
                    textViewCancel.setOnClickListener(v111 -> alert2.cancel());

                    return true;
                case R.id.itemTimePeriods:
                    // кастомное диалоговое окно
                    AlertDialog.Builder a_builder3 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater3 = LayoutInflater.from(MainActivity.this);
                    View window_for_creation3 = inflater3.inflate(R.layout.periods_window, null);

                    MarkTypesList = window_for_creation3.findViewById(R.id.Periods_List);
                    layoutManager = new LinearLayoutManager(MainActivity.this);
                    MarkTypesList.setLayoutManager(layoutManager);
                    appBarLayout = window_for_creation3.findViewById(R.id.AppBar_Periods);
                    ImageView btnCancelPeriods = window_for_creation3.findViewById(R.id.cancel_icon_Delete_Periods);
                    ImageView btnDeletePeriods = window_for_creation3.findViewById(R.id.delete_icon_Periods);
                    CountOfSelectedTypesOfMarks = window_for_creation3.findViewById(R.id.selected_Periods);
                    rootElementPeriods = window_for_creation3.findViewById(R.id.root_element_Periods);

                    a_builder3.setView(window_for_creation3);

                    periodsAdapter = new PeriodsAdapter((int) dbManager.GetSizeOfPeriodsTable());
                    periodsAdapter.setListPeriods(dbManager.GetAllPeriods());
                    periodsAdapter.setListTimes(dbManager.GetAllDateOfPeriods());
                    MarkTypesList.setAdapter(periodsAdapter);

                    a_builder3.setCancelable(true);
                    AlertDialog alert3 = a_builder3.create();
                    alert3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert3.show();

                    TextView textViewCancelPeriods = window_for_creation3.findViewById(R.id.button_cancel_statistics);

                    FloatingActionButton Fab3 = window_for_creation3.findViewById(R.id.fab_Periods);

                    // кнопка добавления периода
                    Fab3.setOnClickListener(v15 -> {
                        AlertDialog.Builder a_builder_fab = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater_fab = LayoutInflater.from(MainActivity.this);
                        View window_for_creation_fab = inflater_fab.inflate(R.layout.window_for_creation_periods, null);

                        a_builder_fab.setView(window_for_creation_fab);

                        TextInputEditText NameField = window_for_creation_fab.findViewById(R.id.creationField);
                        TextInputEditText DatePeriod = window_for_creation_fab.findViewById(R.id.TextDatePeriods);
                        TextInputLayout DatePeriodLayout = window_for_creation_fab.findViewById(R.id.Date_TextView_Periods);

                        // календарь
                        DatePeriodLayout.setEndIconOnClickListener(v151 -> {
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
                                    if(subStr[5].equals("г.")){
                                        // для русской версии ПО
                                        // из формата с (пример: 15, июня 2021) к формату (15/6/2021)
                                        String[] subStrBuffer = new String[7];
                                        subStrBuffer[0] = subStr[0];
                                        subStrBuffer[1] = subStr[1].substring(0, subStr[1].length()-3) + ",";
                                        subStrBuffer[2] = subStr[4];
                                        subStrBuffer[3] = "-";
                                        subStrBuffer[4] = subStr[2];
                                        subStrBuffer[5] = subStr[3].substring(0, subStr[1].length()-3) + ",";
                                        subStrBuffer[6] = subStrBuffer[2];
                                        subStr=subStrBuffer.clone();
                                    }
                                    else{ // для английской версии ПО (такой же перевод данных)
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
                                }
                                if(!subStr[1].equals("мая")){
                                    // после любого из месяцев, кроме мая, ставится точка (связано с сокращением от полного слова)
                                    subStr[1]=subStr[1].substring(0,subStr[1].length()-1).replace(".",""); // убираем запятую после числа или даты
                                    subStr[5]=subStr[5].substring(0,subStr[5].length()-1).replace(".",""); // убираем запятую после 2 числа или даты
                                }
                                if(!dbManager.GetMonth(subStr[1]).equals("0")){
                                    subStr[1] = dbManager.GetMonth(subStr[1]); //получаем месяц из русской раскладки
                                    subStr[5] = dbManager.GetMonth(subStr[5]);
                                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100); // Год 2021 сводим к формату 21
                                    subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100);
                                    Datethis = subStr[0] + "/" + subStr[1] + "/" + subStr[2] + "-" + subStr[4] + "/" + subStr[5] + "/" + subStr[6];
                                }
                                else{
                                    subStr[0] = dbManager.GetMonth(subStr[0]); // из строки получаем числовой порядок месяца
                                    subStr[4] = dbManager.GetMonth(subStr[4]); // из строки получаем числовой порядок месяца 2
                                    subStr[2] = String.valueOf(Integer.parseInt(subStr[2])%100); // Год 2021 сводим к формату 21
                                    subStr[6] = String.valueOf(Integer.parseInt(subStr[6])%100); // Год 2021 сводим к формату 21
                                    Datethis = subStr[1] + "/" + subStr[0] + "/" + subStr[2] + "-" + subStr[5] + "/" + subStr[4] + "/" + subStr[6];
                                }
                                //возвращаем дату в текстовое поле
                                DatePeriod.setText(Datethis);
                            });
                            datePicker.show(getSupportFragmentManager(), datePicker.toString());
                        });

                        a_builder_fab.setCancelable(true)
                                .setPositiveButton("Добавить", (dialog, which) -> {
                                    String NameOfPeriod = Objects.requireNonNull(NameField.getText()).toString();
                                    String DateOfPeriod = Objects.requireNonNull(DatePeriod.getText()).toString();

                                    if(TextUtils.isEmpty(NameOfPeriod)){
                                        Snackbar.make(rootElementPeriods, "Введите название периода", Snackbar.LENGTH_SHORT).show();
                                    }
                                    else if(TextUtils.isEmpty(DateOfPeriod)){
                                        Snackbar.make(rootElementPeriods, "Введите временной промежуток", Snackbar.LENGTH_SHORT).show();
                                    }
                                    else if (!dbManager.CheckPeriod(NameOfPeriod) && !dbManager.CheckPeriodDate(DateOfPeriod)){
                                        // запись периода в БД
                                        dbManager.InsertToPeriods(NameOfPeriod, DateOfPeriod);

                                        periodsAdapter = new PeriodsAdapter((int) dbManager.GetSizeOfPeriodsTable());
                                        periodsAdapter.setListPeriods(dbManager.GetAllPeriods());
                                        periodsAdapter.setListTimes(dbManager.GetAllDateOfPeriods());
                                        MarkTypesList.setAdapter(periodsAdapter);
                                    }
                                    else{
                                        Snackbar.make(rootElementPeriods, "Такой период уже существует  ", Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                        AlertDialog alert12 = a_builder_fab.create();
                        alert12.setTitle("Вы хотите добавить новый период?");
                        alert12.show();
                    });

                    // удаление периодов
                    btnDeletePeriods.setOnClickListener(v16 -> {
                        AlertDialog.Builder a_builder1 = new AlertDialog.Builder(MainActivity.getInstance());

                        a_builder1.setCancelable(true)
                                .setPositiveButton("Подтвердить", (dialog, which) -> {
                                    ArrayList<String> SelectedPeriods = new ArrayList<>(periodsAdapter.GetSelectedPeriods());

                                    dbManager.DeletePeriods(SelectedPeriods);
                                    btnCancelPeriods.performClick();
                                })
                                .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                        AlertDialog alert13 = a_builder1.create();

                        if(periodsAdapter.GetCountOfSelectedPeriods() >1){
                            alert13.setTitle("Вы точно хотите удалить эти периоды?");
                        }
                        else{
                            alert13.setTitle("Вы точно хотите удалить этот период?");
                        }
                        alert13.show();
                    });

                    // снятие выделения
                    btnCancelPeriods.setOnClickListener(v17 -> {
                        periodsAdapter = new PeriodsAdapter((int) dbManager.GetSizeOfPeriodsTable());
                        periodsAdapter.setListPeriods(dbManager.GetAllPeriods());
                        periodsAdapter.setListTimes(dbManager.GetAllDateOfPeriods());
                        MarkTypesList.setAdapter(periodsAdapter);
                        MainActivity.getInstance().SetTypeMarksAppBarFragment();
                    });

                    textViewCancelPeriods.setOnClickListener(v18 -> alert3.cancel());

                    return true;
                case R.id.itemFinalMarks:
                    // замена и установка новых данных в окне итоговых отметок
                    AlertDialog.Builder a_builder4 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater_4 = LayoutInflater.from(MainActivity.this);
                    View window_for_creation_4 = inflater_4.inflate(R.layout.final_marks_window, null);

                    rootElementFinalMarks = window_for_creation_4.findViewById(R.id.root_element_FinalMarks);

                    TextView CancelButton = window_for_creation_4.findViewById(R.id.button_cancel_statistics);

                    UpperBound5 = window_for_creation_4.findViewById(R.id.TextViewUpperBound5);
                    BottomBound5 = window_for_creation_4.findViewById(R.id.TextViewBottomBound5);
                    UpperBound4 = window_for_creation_4.findViewById(R.id.TextViewUpperBound4);
                    BottomBound4 = window_for_creation_4.findViewById(R.id.TextViewBottomBound4);
                    UpperBound3 = window_for_creation_4.findViewById(R.id.TextViewUpperBound3);
                    BottomBound3 = window_for_creation_4.findViewById(R.id.TextViewBottomBound3);

                    SetBounds();

                    UpperBound5.setOnClickListener(FinalMarksOnClickListener);
                    BottomBound5.setOnClickListener(FinalMarksOnClickListener);
                    UpperBound4.setOnClickListener(FinalMarksOnClickListener);
                    BottomBound4.setOnClickListener(FinalMarksOnClickListener);
                    UpperBound3.setOnClickListener(FinalMarksOnClickListener);
                    BottomBound3.setOnClickListener(FinalMarksOnClickListener);

                    a_builder4.setView(window_for_creation_4);
                    a_builder4.setCancelable(true);

                    AlertDialog alert4 = a_builder4.create();
                    alert4.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alert4.show();

                    CancelButton.setOnClickListener(v19 -> alert4.cancel());

                    return true;
                case R.id.itemResetPassword:
                    // кастомное диалоговое окно для замены пароля
                    AlertDialog.Builder a_builder5 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater_5 = LayoutInflater.from(MainActivity.this);
                    View window_for_creation_5 = inflater_5.inflate(R.layout.window_for_reset_password, null);


                    TextInputEditText before_password = window_for_creation_5.findViewById(R.id.BeforePassword_EditText);
                    TextInputEditText after_password = window_for_creation_5.findViewById(R.id.AfterPassword_EditText);
                    TextInputEditText after_password_confirm = window_for_creation_5.findViewById(R.id.AfterPassword_EditText2);

                    a_builder5.setView(window_for_creation_5);
                    a_builder5.setCancelable(true)
                            .setPositiveButton("Сохранить", (dialogInterface, i) -> {
                                String Before_passwordText = Objects.requireNonNull(before_password.getText()).toString();
                                String after_passwordText = Objects.requireNonNull(after_password.getText()).toString();
                                String after_passwordText_confirm = Objects.requireNonNull(after_password_confirm.getText()).toString();

                                if(!dbManager.CheckPassword(Before_passwordText)){
                                    Snackbar.make(root, "Неверно введён старый пароль", Snackbar.LENGTH_SHORT).show();
                                }
                                else if (TextUtils.isEmpty(after_passwordText)){
                                    Snackbar.make(root, "Введите новый пароль", Snackbar.LENGTH_SHORT).show();
                                }
                                else if(after_passwordText.equals(after_passwordText_confirm)){
                                    SharedPreferences prefs = getSharedPreferences("pref", MODE_PRIVATE);
                                    prefs.edit().putBoolean("Start_Login_activity", false).apply();
                                    dbManager.updateTablePass(Before_passwordText, after_passwordText);
                                    Snackbar.make(root, "Пароль сохранён", Snackbar.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("Отменить", (dialogInterface, i) -> dialogInterface.cancel());

                    AlertDialog alert5 = a_builder5.create();
                    alert5.setTitle("Вы хотите поменять праоль?");
                    alert5.show();
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    @SuppressLint("NonConstantResourceId")
    public void showPopupMenu(View v, String NameGroup) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.group_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.item1:
                    // диалоговое окно для переименования группы
                    AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View window_for_creation = inflater.inflate(R.layout.window_for_creation, null);
                    a_builder.setView(window_for_creation);
                    TextInputEditText NameField = window_for_creation.findViewById(R.id.creationField);
                    NameField.setText(NameGroup);

                    a_builder.setCancelable(true)
                            .setPositiveButton("Добавить", (dialog, which) -> {
                                String NameOFGroup = Objects.requireNonNull(NameField.getText()).toString();
                                if(TextUtils.isEmpty(NameOFGroup)){
                                    Snackbar.make(root, "Название группы не должно быть пустым", Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                dbManager.ResetNameGroup(NameGroup, NameOFGroup);
                                GroupCreate();
                            })
                            .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                    AlertDialog alert = a_builder.create();
                    alert.setTitle("Вы хотите изменить название группы?");
                    alert.show();
                    return true;
                case R.id.item2:
                    // кастомное диалоговое окно для установки и замены напоминания
                    AlertDialog.Builder a_builder2 = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater2 = LayoutInflater.from(MainActivity.this);
                    View window_for_creation2 = inflater2.inflate(R.layout.window_for_creation, null);
                    TextInputEditText NameField2 = window_for_creation2.findViewById(R.id.creationField);
                    TextInputLayout NameLayout = window_for_creation2.findViewById(R.id.NameGroup);
                    NameLayout.setHint("Напоминание");

                    NameField2.setText(dbManager.GetReminderGroup(NameGroup));
                    a_builder2.setView(window_for_creation2);

                    a_builder2.setCancelable(true)
                            .setPositiveButton("Добавить", (dialog, which) -> {
                                String Reminder = Objects.requireNonNull(NameField2.getText()).toString();
                                dbManager.ResetReminderGroup(NameGroup, Reminder);
                                GroupCreate();
                            })
                            .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                    AlertDialog alert2 = a_builder2.create();
                    alert2.setTitle("Вы хотите поставить напоминание?");
                    alert2.show();
                    return true;
                case R.id.item3:
                    // кнопочка для удаления группы
                    AlertDialog.Builder a_builder3 = new AlertDialog.Builder(MainActivity.this);

                    a_builder3.setCancelable(true)
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                dbManager.Delete_Table(NameGroup);
                                GroupCreate();
                            })
                            .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                    AlertDialog alert3 = a_builder3.create();
                    alert3.setTitle("Вы точно хотите удалить эту группу?");
                    alert3.show();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    public void SetTextSelectedTypesOfMarks(int counter){
        // подсчёт кол-ва выбранных типов работ
        String text;
        if(counter % 10 == 1 && counter % 100 != 11){
            text = "Выбран " + counter + " тип работ";
        }
        else if(counter > 1 && counter < 5){
            text = "Выбрано " + counter + " типа работ";
        }

        else{
            text = "Выбрано " + counter + " типов работ";
        }
        CountOfSelectedTypesOfMarks.setText(text);
    }

     public void SetTextSelectedPeriods(int counter){
        // подчёт кол-ва выбранных периодов
         String text;
         if(counter % 10 == 1 && counter % 100 != 11){
             text = "Выбран " + counter + " период";
         }
         else if(counter > 1 && counter < 5){
             text = "Выбрано " + counter + " периода";
         }

         else{
             text = "Выбрано " + counter + " периодов";
         }
         CountOfSelectedTypesOfMarks.setText(text);
     }

     @SuppressLint("NonConstantResourceId")
     View.OnClickListener FinalMarksOnClickListener = v -> {
        // диалоговоая развилка для окна с финальными отметками
         switch (v.getId()){
             case R.id.TextViewUpperBound5:
                 CreateDialogFinalMarks("5", "Upper_bound");
                 break;
             case R.id.TextViewBottomBound5:
                 CreateDialogFinalMarks("5", "Bottom_bound");
                 break;
             case R.id.TextViewUpperBound4:
                 CreateDialogFinalMarks("4", "Upper_bound");
                 break;
             case R.id.TextViewBottomBound4:
                 CreateDialogFinalMarks("4", "Bottom_bound");
                 break;
             case R.id.TextViewUpperBound3:
                 CreateDialogFinalMarks("3", "Upper_bound");
                 break;
             case R.id.TextViewBottomBound3:
                 CreateDialogFinalMarks("3", "Bottom_bound");
                 break;
         }
     };

     public static boolean IsDouble(String str){
         // проверка на тип double
         try{
             Double.valueOf(str);
             return true;
         }catch (NumberFormatException e){
             return false;
         }
     }

    private void CreateDialogFinalMarks(String Mark, String TypeOfBound){
         // кастомное диалоговое окно в диалоговом окне FinalMarks
        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View window_for_creation = inflater.inflate(R.layout.window_for_creation, null);
        TextInputEditText NameField = window_for_creation.findViewById(R.id.creationField);
        TextInputLayout NameLayout = window_for_creation.findViewById(R.id.NameGroup);
        NameLayout.setHint("Средний балл");
        NameLayout.setHelperText("Пример: 4.77");

        NameField.setText(dbManager.GetBound(TypeOfBound, Mark));
        a_builder.setView(window_for_creation);

        a_builder.setCancelable(true)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String AverageScoreForFinalMarks = Objects.requireNonNull(NameField.getText()).toString();
                    if (!IsDouble(AverageScoreForFinalMarks)){
                        Snackbar.make(rootElementFinalMarks, "Средний балл введён не корректно", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    final double a = Double.parseDouble(AverageScoreForFinalMarks.replace(",", ".")) * 100;
                    if (!TextUtils.isEmpty(AverageScoreForFinalMarks) && Math.round(a)/100 >= 2
                            && Math.round(a)/100 <= 5){
                        dbManager.UpdateBound(TypeOfBound, Mark, String.valueOf((double) Math.round(a)/100));
                        SetBounds();
                    }
                    else{
                        Snackbar.make(rootElementFinalMarks, "Средний балл введён не корректно", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
        AlertDialog alert = a_builder.create();
        alert.setTitle("Вы хотите изменить верхнюю границу (допустимые значения от 2 до 5)?");
        alert.show();
     }

     //фрагмент для шапки расписания
    public static class ScheduleAppBarFragment extends Fragment {

        TabLayout tabLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.schedule_appbar_fragment, null);
            tabLayout = view.findViewById(R.id.Tab_Layout_Fragment);
            tabLayout.addOnTabSelectedListener(MainActivity.getInstance().TabListener);
            return view;
        }
    }

    // фрагмент шапки для расписания (удаление записей)
    public static  class ContextualAppBarFragment extends Fragment{

        ScheduleAdapter ScheduleAdapter;
        DBManager dbManager;
        ImageView DeleteIcon, CancelIcon;
        TextView SelectedGroups;
        private static MainActivity.ContextualAppBarFragment instance;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            instance = this;

            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.contextual_appbar_layout, null);
            DeleteIcon = view.findViewById(R.id.delete_icon_Student);
            CancelIcon = view.findViewById(R.id.cancel_icon_Delete_Student);
            SelectedGroups = view.findViewById(R.id.selected_students);
            dbManager = MainActivity.getInstance().getDBManager();

            SelectedGroups.setText("Выбрано 1 занятие");

            CancelIcon.setOnClickListener(v -> {
                MainActivity.getInstance().ScheduleCreate(MainActivity.getInstance().SelectedTab);
                MainActivity.getInstance().SetScheduleAppBarFragment();
            });

            DeleteIcon.setOnClickListener(v -> {
                // удаляем выделенные записи

                ScheduleAdapter = MainActivity.getInstance().GetScheduleAdapter();

                AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.getInstance());

                a_builder.setCancelable(true)
                        .setPositiveButton("Подтвердить", (dialog, which) -> {
                            ArrayList<String> SelectedTimes = new ArrayList<>(ScheduleAdapter.GetSelectedTimes());

                            dbManager.DeleteScheduleGroupsByTime(MainActivity.getInstance().SelectedTab, SelectedTimes);
                            CancelIcon.performClick();
                        })
                        .setNegativeButton("Отменить", (dialog, which) -> dialog.cancel());
                AlertDialog alert = a_builder.create();

                if(ScheduleAdapter.GetCountOfSelectedGroups() >1){
                    alert.setTitle("Вы точно хотите удалить этих учеников?");
                }
                else{
                    alert.setTitle("Вы точно хотите удалить этого ученика?");
                }
                alert.show();
            });

            return view;
        }

        public void SetTextSelectedGroups(int counter){
            // установка на шапку кол-ва выбранных групп
            String text;
            if(counter % 10 == 1 && counter % 100 != 11){
                text = "Выбрано " + counter + " занятие";
            }
            else if(counter > 1 && counter < 5){
                text = "Выбрано " + counter + " занятия";
            }
            else{
                text = "Выбрано " + counter + " занятий";
            }
            SelectedGroups.setText(text);
        }

        public static MainActivity.ContextualAppBarFragment getInstance(){
            return instance;
        }
    }

    public ScheduleAdapter GetScheduleAdapter (){
        return scheduleAdapter;
    }

    public void SetContextualAppBarFragment(){
         // установка фрагмента на шапку расписания
         Fragment fragment;
         fragment = new ContextualAppBarFragment();
         FragmentManager fm = getSupportFragmentManager();
         FragmentTransaction ft = fm.beginTransaction();
         ft.replace(R.id.Tab_Layout_Schedule_Fragment, fragment);
         ft.commit();
         appBar.setEnabled(false);
    }

    public void SetScheduleAppBarFragment(){
        // установка фрагмента на шапку расписания
        Fragment fragment;
        fragment = new ScheduleAppBarFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.Tab_Layout_Schedule_Fragment, fragment);
        ft.commit();
        appBar.setEnabled(false);
    }

    public void SetTypeMarksContextualAppBarFragment(){
         // выключение включение шапки для типов работ
        appBarLayout.setEnabled(true);
        appBarLayout.setVisibility(View.VISIBLE);
    }

    public void SetTypeMarksAppBarFragment(){
        // выключение включение шапки для типов работ
        appBarLayout.setEnabled(false);
        appBarLayout.setVisibility(View.INVISIBLE);
    }

    public void ScheduleCreate (String DayOfTheWeek){
         // создание расписания
        ScheduleList = findViewById(R.id.ScheduleList);
        scheduleAdapter = new ScheduleAdapter((int) dbManager.GetSizeSubjectsInADay(DayOfTheWeek));
        scheduleAdapter.setListOfTimes(dbManager.GetAllTimesInADay(DayOfTheWeek));
        scheduleAdapter.setListOfSubjects(dbManager.GetAllSubjectsInADay(DayOfTheWeek));
        ScheduleList.setAdapter(scheduleAdapter);
    }

    public void GroupCreate(){
         // создание списка групп
        GroupAdapter groupAdapter = new GroupAdapter((int) dbManager.GetGroupsSize()); // вводим кол-воэлементов списка
        groupAdapter.setListOfName(dbManager.GetAllGroups()); //Передаём список строк адаптеру

        groupAdapter.setAverageScore(dbManager.GetAllGroupAverageScore());
        groupAdapter.setListOfReminders(dbManager.GetAllReminders());
        GroupList.setAdapter(groupAdapter);
    }

    private void SetBounds(){
         // установка границ в окне FinalMarks
        UpperBound5.setText(dbManager.GetBound(DBConstants.UPPER_BOUND, "5"));
        BottomBound5.setText(dbManager.GetBound(DBConstants.BOTTOM_BOUND, "5"));
        UpperBound4.setText(dbManager.GetBound(DBConstants.UPPER_BOUND, "4"));
        BottomBound4.setText(dbManager.GetBound(DBConstants.BOTTOM_BOUND, "4"));
        UpperBound3.setText(dbManager.GetBound(DBConstants.UPPER_BOUND, "3"));
        BottomBound3.setText(dbManager.GetBound(DBConstants.BOTTOM_BOUND, "3"));
    }

}

