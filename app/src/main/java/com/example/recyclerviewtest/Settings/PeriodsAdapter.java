package com.example.recyclerviewtest.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewtest.MainActivity;
import com.example.recyclerviewtest.R;

import java.util.ArrayList;

public class PeriodsAdapter extends  RecyclerView.Adapter<PeriodsAdapter.PeriodsViewHolder>{

    private final int numberNames;
    private final ArrayList<String> PeriodsList = new ArrayList<>();
    int counter = 0;

    private final ArrayList<String> PeriodsTimeList = new ArrayList<>();
    private final ArrayList<String> SelectedPeriods = new ArrayList<>();

    public PeriodsAdapter(int numberTypesOfMarks){ //Функция для получения кол-ва элементов списка
        this.numberNames = numberTypesOfMarks;
    }

    @NonNull
    @Override
    public PeriodsAdapter.PeriodsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.periods_window_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new PeriodsViewHolder(view);
    }

    public void setListPeriods(ArrayList<String> PeriodsList){
        this.PeriodsList.addAll(PeriodsList);
        for(String ignored : PeriodsList){
            SelectedPeriods.add("");
        }
    }

    public void setListTimes(ArrayList<String> PeriodsTimeList){
        this.PeriodsTimeList.addAll(PeriodsTimeList);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull PeriodsAdapter.PeriodsViewHolder holder, int position) {
        Drawable color;
        if(position%2==0){
            color = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
        }
        else{
            color = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
        }

        holder.bind(PeriodsList.get(position), PeriodsTimeList.get(position).replace("-", " "), color);
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberNames;
    }

    class PeriodsViewHolder extends RecyclerView.ViewHolder {

        TextView PeriodView;

        TextView DateView;

        int color = MainActivity.getInstance().getResources().getColor(R.color.DIVIDER_COLOR);
        Drawable color2;

        @SuppressLint("UseCompatLoadingForDrawables")
        public PeriodsViewHolder(View itemView) {
            super(itemView);

            PeriodView = itemView.findViewById(R.id.Periods_TextView_Settings);
            DateView = itemView.findViewById(R.id.Time_Period);

            // обработчик нажатия на каждый из элементов списка
            // если в списке "", то этого студента ещё не выбрали, а если стоит его имя, то выбрали
            itemView.setOnLongClickListener(v -> {
                itemView.setBackgroundColor(color);

                int positionIndex = getAdapterPosition();

                if (PeriodView.getText().toString().equals(SelectedPeriods.get(positionIndex))){
                    if(positionIndex%2==0){
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else{
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    itemView.setBackground(color2);
                    counter--;
                    SelectedPeriods.set(positionIndex,"");
                    if(counter == 0){
                        MainActivity.getInstance().SetTypeMarksAppBarFragment();
                    }
                    else {
                        MainActivity.getInstance().SetTextSelectedPeriods(counter);
                    }
                }
                else {
                    SelectedPeriods.set(positionIndex, PeriodView.getText().toString());

                    counter++;
                    if(counter == 1){
                        MainActivity.getInstance().SetTypeMarksContextualAppBarFragment();
                    }
                    else{
                        MainActivity.getInstance().SetTextSelectedPeriods(counter);
                    }
                }



                return true;
            });

            itemView.setOnClickListener(v -> {
                int positionIndex = getAdapterPosition();
                if (counter == 0){
                    MainActivity.getInstance().GetAndRenameDialogPeriods.onClick(itemView);
                }
                else if(PeriodView.getText().toString().equals(SelectedPeriods.get(positionIndex))){
                    if(positionIndex%2==0){
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else{
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    itemView.setBackground(color2);
                    counter--;
                    SelectedPeriods.set(positionIndex,"");
                    if(counter == 0){
                        MainActivity.getInstance().SetTypeMarksAppBarFragment();
                    }
                    else {
                        MainActivity.getInstance().SetTextSelectedPeriods(counter);
                    }
                }
                else if(counter > 0){
                    SelectedPeriods.set(positionIndex,PeriodView.getText().toString());
                    itemView.setBackgroundColor(color);
                    counter++;
                    MainActivity.getInstance().SetTextSelectedPeriods(counter);
                }
            });


        }

        void bind(String Period, String date, Drawable color){
            PeriodView.setText(Period);
            DateView.setText(date);
            itemView.setBackground(color);
        }
    }

    public ArrayList<String> GetSelectedPeriods(){
        return SelectedPeriods;
    }

    public int GetCountOfSelectedPeriods(){
        return counter;
    }
}
