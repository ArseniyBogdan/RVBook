package com.example.recyclerviewtest.FirstSettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.MainActivity;
import com.example.recyclerviewtest.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private ConstraintLayout root_element_settings_activity;
    private DBManager dbManager;
    private TextInputEditText InputText_First_Name, InputText_Second_Name, InputText_Password, InputText_Password_Confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).hide();

        dbManager = new DBManager(this);
        dbManager.openDB();

        prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);

        root_element_settings_activity = findViewById(R.id.root_element_settings_activity);

        InputText_First_Name = findViewById(R.id.textInput_First_Name);
        InputText_Second_Name = findViewById(R.id.textInput_Second_Name);
        InputText_Password = findViewById(R.id.textInput_Password);
        InputText_Password_Confirm = findViewById(R.id.textInput_Password_Confirm);

        Button button_confirm = findViewById(R.id.button_confirm);

        button_confirm.setOnClickListener(view -> {
            String FirstName = Objects.requireNonNull(InputText_First_Name.getText()).toString();
            String SecondName = Objects.requireNonNull(InputText_Second_Name.getText()).toString();
            String Password = Objects.requireNonNull(InputText_Password.getText()).toString();
            String Password_Confirm = Objects.requireNonNull(InputText_Password_Confirm.getText()).toString();

            if(CheckCorrectnessOfInputName(FirstName)){
                Snackbar.make(root_element_settings_activity, "Неккоректно введено имя", Snackbar.LENGTH_SHORT).show();
            }
            if (CheckCorrectnessOfInputSurname(SecondName)){
                Snackbar.make(root_element_settings_activity, "Неккоректно введена фамилия", Snackbar.LENGTH_SHORT).show();
            }
            else if(Password.equals(Password_Confirm)){
                dbManager.AddDataAboutUser(FirstName, SecondName, Password);

                prefs.edit().putBoolean("firstRun", true).apply();

                Intent MainActivityIntent = new Intent(SettingsActivity.this, MainActivity.class); // Доделать переход на активити с запросом пароля и кнопочкой запомни меня
                startActivity(MainActivityIntent); //запускаем активити
            }
            else{
                Snackbar.make(root_element_settings_activity, "Пароли не совпадают", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private boolean CheckCorrectnessOfInputName(String Name){
        return Name.split(" ").length > 1 || Name.replace(" ", "").length() == 0;
    }

    private boolean CheckCorrectnessOfInputSurname(String Surname){
        return Surname.split(" ").length > 1 || Surname.replace(" ", "").length() == 0;
    }

    @Override
    protected void onDestroy(){ //закрытие базы во время закрытия приложения
        super.onDestroy();
        dbManager.DBClose();
    }
}