package com.example.recyclerviewtest.FirstSettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.recyclerviewtest.DB.DBManager;
import com.example.recyclerviewtest.MainActivity;
import com.example.recyclerviewtest.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;


import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private CheckBox remember_me;
    private ConstraintLayout root_element_login_activity;
    private TextInputEditText Password_EditText;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_login);
        remember_me = findViewById(R.id.CheckBox_RememberMe);
        root_element_login_activity = findViewById(R.id.root_element_login_activity);
        Button button_confirm = findViewById(R.id.button_Login_confirm);

        dbManager = new DBManager(this);
        dbManager.openDB();

        prefs = getSharedPreferences("pref", MODE_PRIVATE);

        Password_EditText = findViewById(R.id.textInput_LoginPassword);

        button_confirm.setOnClickListener(view -> {
            if(dbManager.CheckPassword(Objects.requireNonNull(Password_EditText.getText()).toString())){
                if(remember_me.isChecked()){
                    prefs.edit().putBoolean("Start_Login_activity", true).apply();
                }
                Intent MainActivityIntent = new Intent(LoginActivity.this, MainActivity.class); // Доделать переход на активити с запросом пароля и кнопочкой запомни меня
                startActivity(MainActivityIntent); //запускаем активити
                finish();
            }
            else{
                Snackbar.make(root_element_login_activity, "Введён неверный пароль", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy(){ //закрытие базы во время закрытия приложения
        super.onDestroy();
        dbManager.DBClose();
    }
}