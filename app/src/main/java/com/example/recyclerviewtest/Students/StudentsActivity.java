package com.example.recyclerviewtest.Students;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class StudentsActivity extends AppCompatActivity {

    public String name_group;

    private static StudentsActivity instance;

    private RecyclerView List_student;

    private AppBarLayout appBar;

    public StudentAdapter StudentAdapter;

    private LinearLayout students;

    private DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);
        Objects.requireNonNull(getSupportActionBar()).hide();

        instance = this;

        Intent IntentActivity = getIntent();

        if (IntentActivity.hasExtra(Intent.EXTRA_TEXT)) {
            //передача данных на это активити с button_activity
            name_group = IntentActivity.getStringExtra(Intent.EXTRA_TEXT);
        }

        TextView nameGroup = findViewById(R.id.NameGroupStudents);
        nameGroup.setText(name_group);

        dbManager = new DBManager(this);
        dbManager.openDB();

        appBar = findViewById(R.id.AppBarStudent);

        List_student = findViewById(R.id.rv_Student);

        ImageView imageView = findViewById(R.id.add_icon_Student);
        imageView.setOnClickListener(iconButtonOnCLickListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        List_student.setLayoutManager(layoutManager);

        SetListOfStudents();

        students = findViewById(R.id.Students);
        // линии между элементами списка
        List_student.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    public View.OnClickListener iconButtonOnCLickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // кастомное диалоговое окно для добавления студента
            AlertDialog.Builder a_builder = new AlertDialog.Builder(StudentsActivity.this);

            LayoutInflater inflater = LayoutInflater.from(StudentsActivity.this); //создаём некое представление
            View window_for_creation = inflater.inflate(R.layout.window_for_creation, null); // передаём айди нашего xml файла (для того, чтобы метод inflate() понимал из чего мы будем создавать новое представление; null - нет родителя;
            final TextInputEditText NameField = window_for_creation.findViewById(R.id.creationField); //помещаем ссылку на текстовое окно нашей карточки(CardView) в переменную для дальнейшей работы с этим полем.
            final TextInputLayout NameCage = window_for_creation.findViewById(R.id.NameGroup);
            NameCage.setHint("Фамилия и имя");
            a_builder.setView(window_for_creation); //устанавливаем получившийся вид в нашего строителя диалоговых окон

            a_builder.setCancelable(true) // добавляет возможность отменить диалоговое окно и мы ставим на true
                    .setPositiveButton("Добавить", (dialog, which) -> { // Перегружаем метод onClick для этой кнопки
                        String StudentName = Objects.requireNonNull(NameField.getText()).toString();
                        if(StudentName.replace(" ", "").length() ==  0
                                || !InputValidation(StudentName)){
                            Snackbar.make(students, "Некорретно введены данные", Snackbar.LENGTH_SHORT).show(); //Небольшое уведомление в нижней части экрана, в котором говорится, что пользователь не ввёл название группы
                            return;
                        }
                        if(TextUtils.isEmpty(StudentName)){ // проверка на отсутствие текста в Текстовом поле
                            Snackbar.make(students, "Введите ФИ ученика", Snackbar.LENGTH_SHORT).show(); //Небольшое уведомление в нижней части экрана, в котором говорится, что пользователь не ввёл название группы
                            return;
                        }
                        dbManager.insertToStudents(StudentName, name_group); // добавляем Введённый текст в список названий
                        DBReload();
                        SetListOfStudents();
                    })
                    .setNegativeButton("Отменить", (dialog, which) -> { //Перегружаем метод onClick для этой кнопки
                        dialog.cancel(); // закрываем диалоговое окно
                    });
            AlertDialog alert = a_builder.create(); // создаём диалоговое окно
            alert.setTitle("Вы хотите добавить нового ученика?"); // устанавливаем заголовок в диалоговом окне
            alert.show(); // показываем диалоговое окно
        }
    };

    public void RenameStudent(String StudentName){
        // функция для замены имени студента
        android.app.AlertDialog.Builder a_builder = new android.app.AlertDialog.Builder(StudentsActivity.this);
        LayoutInflater inflater = LayoutInflater.from(StudentsActivity.this);

        View window_for_creation = inflater.inflate(R.layout.window_for_creation, null);
        a_builder.setView(window_for_creation);

        TextInputEditText NameField = window_for_creation.findViewById(R.id.creationField);
        TextInputLayout NameFieldLayout = window_for_creation.findViewById(R.id.NameGroup);

        NameField.setText(StudentName);
        NameFieldLayout.setHint("Фамилия и имя");

        a_builder.setCancelable(true)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String NewStudentName = Objects.requireNonNull(NameField.getText()).toString();
                    if(!NewStudentName.equals(StudentName)){
                        // замена имени студента в БД
                        dbManager.RenameStudent(name_group, StudentName, NewStudentName);

                        DBReload();
                        SetListOfStudents();
                    }
                    else if(NewStudentName.isEmpty()){
                        Snackbar.make(students, "Введите имя ученика", Snackbar.LENGTH_SHORT).show();
                    }
                    else{
                        Snackbar.make(students, "Измените имя ученика", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отменить", (dialog, which) -> {
                    dialog.cancel();
                });
        android.app.AlertDialog alert = a_builder.create();
        alert.setTitle("Вы хотите переименовать ученика?");
        alert.show();
    }

    public void SetListOfStudents(){
        // функция для установки списка студентов
        StudentAdapter = new StudentAdapter((int) dbManager.GetStudentTableSize(name_group));
        StudentAdapter.setListOfName(dbManager.GetAllStudents(name_group));
        ArrayList<String> AverageScore = new ArrayList<>();
        for(String cn:dbManager.GetAllStudents(name_group)){
            AverageScore.add(dbManager.GetAverageScore(name_group, cn));
        }
        StudentAdapter.GetAverageScore(AverageScore);
        List_student.setAdapter(StudentAdapter);
    }

    public boolean InputValidation(String NameText){
        // проверка правильности ввода
        return NameText.matches("[a-zA-Zа-яА-Я\\s]*");
    }

    public static class StudentAppBarFragment extends Fragment {

        ImageView addImageView;
        TextView groupName;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // фрагмент для шапки студентов
            View view = inflater.inflate(R.layout.student_appbar_layout, null);

            addImageView = view.findViewById(R.id.add_icon_Student);
            addImageView.setOnClickListener(StudentsActivity.getInstance().iconButtonOnCLickListener);
            groupName = view.findViewById(R.id.NameGroupStudentsFragment);
            groupName.setText(StudentsActivity.getInstance().name_group);
            return view;
        }
    }


    public static  class ContextualAppBarFragment extends Fragment{

        StudentAdapter studentAdapter;
        String name_group = StudentsActivity.getInstance().name_group;
        DBManager dbManager;
        ImageView DeleteIcon, CancelIcon;
        TextView SelectedStudents;
        private static ContextualAppBarFragment instance;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // фрагмент для шапки students_activity (выделение студентов)
            instance = this;

            View view = inflater.inflate(R.layout.contextual_appbar_layout, null);
            DeleteIcon = view.findViewById(R.id.delete_icon_Student);
            CancelIcon = view.findViewById(R.id.cancel_icon_Delete_Student);
            SelectedStudents = view.findViewById(R.id.selected_students);
            dbManager = StudentsActivity.getInstance().GetDBManager();

            CancelIcon.setOnClickListener(v -> {
                StudentsActivity.getInstance().StudentAdapter = new StudentAdapter((int) dbManager.GetStudentTableSize(name_group)); // вводим кол-воэлементов списка
                StudentsActivity.getInstance().StudentAdapter.setListOfName(dbManager.GetAllStudents(name_group)); //Передаём список строк адаптеру
                ArrayList<String> AverageScore = new ArrayList<>();
                for(String cn : dbManager.GetAllStudents(name_group)){
                    AverageScore.add(dbManager.GetAverageScore(name_group, cn));
                }
                StudentsActivity.getInstance().StudentAdapter.GetAverageScore(AverageScore);
                StudentsActivity.getInstance().List_student.setAdapter(StudentsActivity.getInstance().StudentAdapter); // Устанавливаем на наш RecyclerView новый адаптер
                StudentsActivity.getInstance().SetStudentAppBarFragment();
            });

            DeleteIcon.setOnClickListener(v -> {
                studentAdapter = StudentsActivity.getInstance().GetStudentAdapter();

                android.app.AlertDialog.Builder a_builder = new android.app.AlertDialog.Builder(StudentsActivity.getInstance());

                a_builder.setCancelable(true) // добавляет возможность отменить диалоговое окно и мы ставим на true
                        .setPositiveButton("Подтвердить", (dialog, which) -> { // Перегружаем метод onClick для этой кнопки
                            ArrayList<String> SelectedStudents = new ArrayList<>(studentAdapter.GetSelectedStudents());

                            // удаление студентов
                            dbManager.DeleteStudent(name_group, SelectedStudents);
                            StudentsActivity.getInstance().DBReload();
                            dbManager = StudentsActivity.getInstance().GetDBManager();
                            CancelIcon.performClick();
                        })
                        .setNegativeButton("Отменить", (dialog, which) -> { //Перегружаем метод onClick для этой кнопки
                            dialog.cancel(); // закрываем диалоговое окно
                        });
                android.app.AlertDialog alert = a_builder.create(); // создаём диалоговое окно

                if(studentAdapter.GetCountOfSelectedStudents() >1){
                    // устанавливаем заголовок для диалогового окна
                    alert.setTitle("Вы точно хотите удалить этих учеников?");
                }
                else{
                    alert.setTitle("Вы точно хотите удалить этого ученика?");
                }
                alert.show();
            });

            return view;
        }

        public void SetTextSelectedStudents(int counter){
            // устанавливаем заголовок для фрагмента
            String text;
            if(counter % 10 == 1 && counter % 100 != 11){
                text = "Выбран " + counter + " ученик";
            }
            else if(counter > 1 && counter < 5){
                text = "Выбрано " + counter + " ученика";
            }
            else{
                text = "Выбрано " + counter + " учеников";
            }
            SelectedStudents.setText(text);
        }

        public static ContextualAppBarFragment getInstance(){
            return instance;
        }
    }

    public StudentAdapter GetStudentAdapter(){
        return StudentAdapter;
    }

    public DBManager GetDBManager(){
        return dbManager;
    }

    public void SetStudentAppBarFragment(){
        // функция установки фрагмента (без выделения)
        Fragment fragment;
        fragment = new StudentAppBarFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.Fragment_container_Students, fragment);
        ft.commit();
    }

    public void SetContextualAppBarFragment(){
        // функция установки фрагмента (с выделением)
        Fragment fragment;
        fragment = new ContextualAppBarFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.Fragment_container_Students, fragment);
        ft.commit();
        appBar.setEnabled(false);
    }

    public static StudentsActivity getInstance() {
        return instance;
    }

    private void DBReload(){
        // перезагрузка БД
        dbManager = new DBManager(StudentsActivity.getInstance());
        dbManager.openDB();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbManager.DBClose();
    }
}