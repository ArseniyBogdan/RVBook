package com.example.recyclerviewtest.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewtest.MainActivity;
import com.example.recyclerviewtest.R;

import java.util.ArrayList;

public class TypesOfMarksAdapter extends  RecyclerView.Adapter<TypesOfMarksAdapter.TypesOfMarksViewHolder>{

    private final int numberNames;
    private final ArrayList<String> TypesOfMarks = new ArrayList<>();
    int counter = 0;

    private final ArrayList<String> ValueTypesOfMarks = new ArrayList<>();
    private final ArrayList<String> SelectedTypesOfMarks = new ArrayList<>();

    public TypesOfMarksAdapter(int numberTypesOfMarks){ //Функция для получения кол-ва элементов списка
        this.numberNames = numberTypesOfMarks;
    }

    @NonNull
    @Override
    public TypesOfMarksAdapter.TypesOfMarksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.mark_types_list_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new TypesOfMarksViewHolder(view);
    }

    public void setListTypesOfMarks(ArrayList<String> TypesOfMarks){
        this.TypesOfMarks.addAll(TypesOfMarks);
        for(String ignored : TypesOfMarks){
            SelectedTypesOfMarks.add("");
        }
    }

    public void setListValuesOfMarks(ArrayList<String> SelectedTypesOfMarks){
        this.ValueTypesOfMarks.addAll(SelectedTypesOfMarks);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull TypesOfMarksAdapter.TypesOfMarksViewHolder holder, int position) {
        Drawable color;
        if(position%2==0){
            color = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
        }
        else{
            color = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
        }

        holder.bind(TypesOfMarks.get(position), ValueTypesOfMarks.get(position), color);
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberNames;
    }

    class TypesOfMarksViewHolder extends RecyclerView.ViewHolder {

        TextView MarkTypeView;

        CheckBox CheckBox;

        int color = MainActivity.getInstance().getResources().getColor(R.color.DIVIDER_COLOR);
        Drawable color2;

        @SuppressLint("UseCompatLoadingForDrawables")
        public TypesOfMarksViewHolder(View itemView) {
            super(itemView);

            MarkTypeView = itemView.findViewById(R.id.MarkType_TextView_Settings);
            CheckBox = itemView.findViewById(R.id.value_of_types_checkbox);

            // обработчик нажатия на каждый из элементов списка
            // если в списке "", то этого студента ещё не выбрали, а если стоит его имя, то выбрали

            itemView.setOnLongClickListener(v -> {
                itemView.setBackgroundColor(color);

                int positionIndex = getAdapterPosition();

                if (MarkTypeView.getText().toString().equals(SelectedTypesOfMarks.get(positionIndex))){
                    if(positionIndex%2==0){
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else{
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    itemView.setBackground(color2);
                    counter--;
                    SelectedTypesOfMarks.set(positionIndex,"");
                    if(counter == 0){
                        MainActivity.getInstance().SetTypeMarksAppBarFragment();
                    }
                    else {
                        MainActivity.getInstance().SetTextSelectedTypesOfMarks(counter);
                    }
                }
                else {
                    SelectedTypesOfMarks.set(positionIndex, MarkTypeView.getText().toString());

                    counter++;
                    if(counter == 1){
                        MainActivity.getInstance().SetTypeMarksContextualAppBarFragment();
                    }
                    else{
                        MainActivity.getInstance().SetTextSelectedTypesOfMarks(counter);
                    }
                }
                return true;
            });

            itemView.setOnClickListener(v -> {
                int positionIndex = getAdapterPosition();
                if (counter == 0){
                    MainActivity.getInstance().GetDeleteAndRenameDialog.onClick(itemView);
                }
                else if(MarkTypeView.getText().toString().equals(SelectedTypesOfMarks.get(positionIndex))){
                    if(positionIndex%2==0){
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment1);
                    }
                    else{
                        color2 = MainActivity.getInstance().getResources().getDrawable(R.drawable.cell_shape_fragment2);
                    }
                    itemView.setBackground(color2);
                    counter--;
                    SelectedTypesOfMarks.set(positionIndex,"");
                    if(counter == 0){
                        MainActivity.getInstance().SetTypeMarksAppBarFragment();
                    }
                    else {
                        MainActivity.getInstance().SetTextSelectedTypesOfMarks(counter);
                    }
                }
                else if(counter > 0){
                    SelectedTypesOfMarks.set(positionIndex,MarkTypeView.getText().toString());
                    itemView.setBackgroundColor(color);
                    counter++;
                    MainActivity.getInstance().SetTextSelectedTypesOfMarks(counter);
                }
            });

            // считывание выбора
            CheckBox.setOnClickListener(v -> {
                int positionIndex = getAdapterPosition();
                if (counter == 0){
                    ValueTypesOfMarks.set(positionIndex, String.valueOf(CheckBox.isChecked()));
                }
            });
        }

        void bind(String MarkType, String ValueTypeOfMark, Drawable color){
            MarkTypeView.setText(MarkType);
            CheckBox.setChecked(ValueTypeOfMark.equals("true"));
            itemView.setBackground(color);
        }
    }

    public ArrayList<String> GetSelectedValueTypesOfMarks(){
        return ValueTypesOfMarks;
    }

    public ArrayList<String> GetSelectedTypesOfMarks(){
        return SelectedTypesOfMarks;
    }

    public int GetCountOfSelectedTypesOfMarks(){
        return counter;
    }

}
