package com.example.recyclerviewtest;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GroupAdapter extends  RecyclerView.Adapter<GroupAdapter.NameGroupViewHolder>{

    private final int numberGroups;
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> AverageScores = new ArrayList<>();
    private final ArrayList<String> Reminders = new ArrayList<>();

    public GroupAdapter(int numberGroups){ //Функция для получения кол-ва элементов списка
        this.numberGroups = numberGroups;
    }

    @NonNull
    @Override
    public NameGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.group_list_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new NameGroupViewHolder(view);
    }

    public void setListOfName(ArrayList<String> nameList){ //получаем список всех групп
        this.nameList.addAll(nameList);
    }
    public void setAverageScore(ArrayList<String> AverageScores){
        this.AverageScores.addAll(AverageScores);
    }
    public void setListOfReminders(ArrayList<String> Reminders){
        this.Reminders.addAll(Reminders);
    }
    @Override
    public void onBindViewHolder(NameGroupViewHolder holder, int position) {
        // метод для обновления данных при прокрутке списка
        holder.bind(nameList.get(position), AverageScores.get(position), Reminders.get(position));
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberGroups;
    }
    class NameGroupViewHolder extends RecyclerView.ViewHolder {

        TextView NameGroupView, AverageScore_group, Reminder, DayOfTheWeek, Time_OfThe_Class;
        ImageButton Settings_Group_Button;

        public NameGroupViewHolder(View itemView) { //графическая обёртка для элемента списка
            super(itemView);

            NameGroupView = itemView.findViewById(R.id.groupName);
            AverageScore_group = itemView.findViewById(R.id.AverageScore_group);
            Settings_Group_Button = itemView.findViewById(R.id.settings_group_button);
            Reminder = itemView.findViewById(R.id.reminder);
            DayOfTheWeek = itemView.findViewById(R.id.day_ofThe_week);
            Time_OfThe_Class = itemView.findViewById(R.id.time_ofThe_class);

            // обработчик нажатия на каждый из элементов списка
            itemView.setOnClickListener(v -> {
                int positionIndex = getAdapterPosition();
                String Group_name = nameList.get(positionIndex);

                // переход на Button_Activity
                MainActivity.getInstance().StartActivityGroup(Group_name, positionIndex);
            });
            Settings_Group_Button.setOnClickListener(v -> {
                String NameGroup = NameGroupView.getText().toString();
                MainActivity.getInstance().showPopupMenu(v, NameGroup);
            });
        }

        void bind(String NameGroup, String AverageScore, String reminder){
            ArrayList<String> ClassAndTime = MainActivity.getInstance().GetDayAndTime(NameGroup);

            // делаю градиент для оценок (чтобы каждая оценка имела свой оттенок)
            int red, green, blue;
            green = (int)((Float.parseFloat(AverageScore)/5-0.1)*232);
            blue = (int)((Float.parseFloat(AverageScore)/5)*126);
            if (Float.parseFloat(AverageScore)>3.5){
                red = (int)((1-Float.parseFloat(AverageScore)/5+0.5)*224);
            }
            else {
                red = 224 - (int)((Float.parseFloat(AverageScore)/5)*30);
            }

            final ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(red, green, blue)); // устанавливаю для среднего балла цвета (в одном TextView)
            final SpannableStringBuilder text = new SpannableStringBuilder("Средний балл по классу: " + AverageScore);
            text.setSpan(style, 24, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            AverageScore_group.setText(text);
            NameGroupView.setText(NameGroup);
            Reminder.setText(reminder);
            if(ClassAndTime.get(1).equals("")){
                DayOfTheWeek.setText("Следующий урок");
                Time_OfThe_Class.setText("Время урока");
            }
            else{
                // устанавливаем день недели и время следующего урока
                DayOfTheWeek.setText(ClassAndTime.get(0));
                Time_OfThe_Class.setText(ClassAndTime.get(1));
            }

        }
    }

}
