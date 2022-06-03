package com.example.recyclerviewtest.AddDateMarks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewtest.R;

import java.util.ArrayList;

public class MarksAdapter extends  RecyclerView.Adapter<MarksAdapter.MarkViewHolder>{

    private final int numberNames;
    private final ArrayList<String> nameList = new ArrayList<>();
    private ArrayList<String> marks = new ArrayList<>();
    private final String[] Marks_list = AddMarksActivity.getInstance().getResources().getStringArray(R.array.Marks);

    public void SetSizeMarks(int numberStudents){
        this.marks = new ArrayList<>();
        for (int i = 0; i<numberStudents ; i++){
            this.marks.add(" ");
        }
    }

    public void SetMarks(ArrayList<String> GetMarks){
        this.marks = GetMarks;
    }

    public ArrayList<String> GetMarks(){
        return this.marks;
    }

    public MarksAdapter(int numberStudents){ //Функция для получения кол-ва элементов списка
        this.numberNames = numberStudents;
    }

    @Override
    public MarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.mark_list_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new MarkViewHolder(view);
    }

    public void setListOfName(ArrayList<String> nameList){ //получаем список всех групп
        this.nameList.addAll(nameList);
    }

    @Override
    public void onBindViewHolder(MarkViewHolder holder, int position) {
        holder.bind(nameList.get(position), position);
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberNames;
    }
    class MarkViewHolder extends RecyclerView.ViewHolder {

        TextView NameStudentView, IndexStudent;
        AutoCompleteTextView spinnerMark;

        public MarkViewHolder(View itemView) {
            super(itemView);

            NameStudentView = itemView.findViewById(R.id.Student);
            spinnerMark = itemView.findViewById(R.id.Mark);
            IndexStudent = itemView.findViewById(R.id.Position);

            spinnerMark.setOnItemClickListener((parent, view, position, id) -> {
                if(!spinnerMark.getText().toString().equals(" ")){
                    int positionAdapter = getAdapterPosition();
                    marks.set(positionAdapter, spinnerMark.getText().toString());
                }
            });
        }

        @SuppressLint("SetTextI18n")
        void bind(String Name, int position){
            String index;
            if(position+1<10){
                index = "0" + (position+1) + ":";
            }
            else{
                index = (position+1) + ":";
            }
            IndexStudent.setText(index);
            if(Name.split(" ").length==2){
                NameStudentView.setText(Name.split(" ")[0] + " "+ Name.split(" ")[1].substring(0,1) + ".");
            }
            else if(Name.split(" ").length==3){
                NameStudentView.setText(Name.split(" ")[0] + " "+ Name.split(" ")[1].substring(0,1) + "."
                        + " "+ Name.split(" ")[2].substring(0,1) + ".");
            }
            else{
                NameStudentView.setText(Name); // устанавливаем текст в наш TextView в формате Фамилия И.
            }

            ArrayAdapter arrayAdapter = new ArrayAdapter(AddMarksActivity.getInstance(), R.layout.list_item, Marks_list); // каждый раз создаём новые адаптеры, т.к. иначе ломается выпадающий список
            spinnerMark.setText(marks.get(position)); // устанавливаем первую отметку, которая находится в буфере
            spinnerMark.setAdapter(arrayAdapter); //обновляем адаптер
        }
    }
}
