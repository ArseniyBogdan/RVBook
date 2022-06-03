package com.example.recyclerviewtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScheduleAdapter extends  RecyclerView.Adapter<ScheduleAdapter.ScheduleGroupViewHolder>{

    private final int numberGroups;
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> timeList = new ArrayList<>();
    private final ArrayList<String> SelectedGroups = new ArrayList<>();
    private final ArrayList<String> SelectedTimes = new ArrayList<>();
    int counter = 0;

    public ScheduleAdapter(int numberGroups){ //Функция для получения кол-ва элементов списка
        this.numberGroups = numberGroups;
    }

    @NonNull
    @Override
    public ScheduleGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //ф-ия, которой на вход передаём ViewGroup(компонент-контейнер), который выступает в роли самого RecyclerView,
        // следовательно все элементы будут поступать в него (создаём элемент списка)
        Context context = parent.getContext(); //получаем контекст RecyclerView
        int layoutIdForListItem = R.layout.schedule_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new ScheduleGroupViewHolder(view);
    }

    public void setListOfSubjects(ArrayList<String> nameList){
        //получаем список всех групп
        this.nameList.addAll(nameList);
        for(int i = 0; i < nameList.size(); i++){
            SelectedGroups.add("");
            SelectedTimes.add("");
        }
    }
    public void setListOfTimes(ArrayList<String> timeList){
        this.timeList.addAll(timeList);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ScheduleGroupViewHolder holder, int position) {
        // метод для обновления данных при прокрутке списка
        Drawable color;
        if(position%2==0){
            color = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
        }
        else{
            color = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
        }
        holder.bind(nameList.get(position), timeList.get(position), color);
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberGroups;
    }
    class ScheduleGroupViewHolder extends RecyclerView.ViewHolder {

        TextView SubjectTextView, TimeView; //переменная для нашего TextView из layout group_list_element

        int color = MainActivity.getInstance().getResources().getColor(R.color.DIVIDER_COLOR);
        Drawable color2;

        @SuppressLint("UseCompatLoadingForDrawables")
        public ScheduleGroupViewHolder(View itemView) { //графическая обёртка для элемента списка
            super(itemView);

            SubjectTextView = itemView.findViewById(R.id.SubjectTextView);
            TimeView = itemView.findViewById(R.id.TimeView);

            // обработчик нажатия на каждый из элементов списка
            // если в списке "" SelectedGroups и SelectedTimes, то этого студента ещё не выбирали, а если стоит его имя, то выбирали
            itemView.setOnLongClickListener(v -> {
                itemView.setBackgroundColor(color);

                int positionIndex = getAdapterPosition();

                if(SubjectTextView.getText().toString().equals(SelectedGroups.get(positionIndex))){
                    if(positionIndex%2==0){
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else{
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    itemView.setBackground(color2);
                    counter--;
                    SelectedGroups.set(positionIndex,"");
                    SelectedTimes.set(positionIndex,"");
                    if(counter == 0){
                        MainActivity.getInstance().SetScheduleAppBarFragment();
                    }
                    else {
                        MainActivity.ContextualAppBarFragment.getInstance().SetTextSelectedGroups(counter);
                    }
                }
                else{
                    SelectedGroups.set(positionIndex, SubjectTextView.getText().toString());
                    SelectedTimes.set(positionIndex, TimeView.getText().toString());

                    counter++;
                    if(counter == 1){
                        MainActivity.getInstance().SetContextualAppBarFragment();
                    }
                    else{
                        MainActivity.ContextualAppBarFragment.getInstance().SetTextSelectedGroups(counter);
                    }
                }

                return true;
            });


            itemView.setOnClickListener(v -> {
                int positionIndex = getAdapterPosition();
                if (counter == 0){
                    MainActivity.getInstance().SetTimeAndGroup(TimeView.getText().toString(), SubjectTextView.getText().toString());
                    MainActivity.getInstance().ScheduleOnClickListenerAdd.onClick(v);
                }
                else if(SubjectTextView.getText().toString().equals(SelectedGroups.get(positionIndex))){
                    if(positionIndex%2==0){
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else{
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    itemView.setBackground(color2);
                    counter--;
                    SelectedGroups.set(positionIndex,"");
                    SelectedTimes.set(positionIndex,"");
                    if(counter == 0){
                        MainActivity.getInstance().SetScheduleAppBarFragment();
                    }
                    else {
                        MainActivity.ContextualAppBarFragment.getInstance().SetTextSelectedGroups(counter);
                    }
                }
                else if(counter > 0){
                    SelectedGroups.set(positionIndex,SubjectTextView.getText().toString());
                    SelectedTimes.set(positionIndex,TimeView.getText().toString());
                    itemView.setBackgroundColor(color);
                    counter++;
                    MainActivity.ContextualAppBarFragment.getInstance().SetTextSelectedGroups(counter);
                }
            });
        }

        void bind(String NameGroup, String time, Drawable color){
            // функция для замены данных на созданных элементах списка
            SubjectTextView.setText(NameGroup);
            TimeView.setText(time.replace("-", " "));
            itemView.setBackground(color);
        }
    }

    public ArrayList<String> GetSelectedTimes(){
        return SelectedTimes;
    }
    public int GetCountOfSelectedGroups(){
        return counter;
    }

}
