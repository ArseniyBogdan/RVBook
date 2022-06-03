package com.example.recyclerviewtest.GetStudentMarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewtest.R;


import java.util.ArrayList;

public class GetStudentsMarksAdapter extends  RecyclerView.Adapter<GetStudentsMarksAdapter.StudentMarksViewHolder>{

    private final int numberNames;
    private final ArrayList<String> MarkTypesList = new ArrayList<>();
    private final ArrayList<String> MarksList = new ArrayList<>();
    private final ArrayList<String> DatesList = new ArrayList<>();
    private final ArrayList<String> DescriptionList = new ArrayList<>();

    public GetStudentsMarksAdapter(int numberStudents){ //Функция для получения кол-ва элементов списка
        this.numberNames = numberStudents;
    }

    @Override
    public GetStudentsMarksAdapter.StudentMarksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.student_get_marks_list_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new StudentMarksViewHolder(view);
    }

    public void setListOfDates(ArrayList<String> DateList){ //получаем список всех групп
        this.DatesList.addAll(DateList);
    }
    public void setListOfTypesMarks(ArrayList<String> TypeOfMarksList){
        this.MarkTypesList.addAll(TypeOfMarksList);
    }
    public void setListOfMarks(ArrayList<String> MarksList){ //получаем список всех групп
        this.MarksList.addAll(MarksList);
    }
    public void setListOfDescription(ArrayList<String> DescriptionList){
        this.DescriptionList.addAll(DescriptionList);
    }

    @Override
    public void onBindViewHolder(GetStudentsMarksAdapter.StudentMarksViewHolder holder, int position) {
        int color;
        if(position%2==0){
            color = GetStudentMarksActivity.getInstance().getResources().getColor(R.color.SCHEDULE_FRAGMENT2);
        }
        else{
            color = GetStudentMarksActivity.getInstance().getResources().getColor(R.color.SCHEDULE_FRAGMENT1);
        }
        holder.bind(DatesList.get(position), MarkTypesList.get(position), MarksList.get(position), DescriptionList.get(position), color);
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberNames;
    }

    class StudentMarksViewHolder extends RecyclerView.ViewHolder {

        TextView DateTextView;
        TextView MarkTypeView;
        TextView MarkView;
        TextView DescriptionView;
        View ourView;

        public StudentMarksViewHolder(View itemView) {
            super(itemView);

            ourView = itemView;
            DateTextView = itemView.findViewById(R.id.Date_TextView_Students_Marks);
            MarkTypeView = itemView.findViewById(R.id.MarkType_TextView_Students_Marks);
            MarkView = itemView.findViewById(R.id.Mark_TextView_Students_Marks);
            DescriptionView = itemView.findViewById(R.id.Description_TextView_Students_Marks);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int positionIndex = getAdapterPosition();
//                    // в рамботе
//                }
//            });
        }

        void bind(String Date, String MarkType, String Mark, String Description, int color){
            DateTextView.setText(Date);
            MarkTypeView.setText(MarkType);
            MarkView.setText(Mark);
            DescriptionView.setText(Description);

            ourView.setBackgroundColor(color);
        }
    }

}