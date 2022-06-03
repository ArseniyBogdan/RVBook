package com.example.recyclerviewtest.Students;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewtest.R;

import java.util.ArrayList;

public class StudentAdapter extends  RecyclerView.Adapter<StudentAdapter.NameStudentViewHolder>{

    private final int numberNames;
    private final ArrayList<String> nameList = new ArrayList<>();
    int counter = 0;
    private ArrayList<String> AverageScore = new ArrayList<>();
    private final ArrayList<String> SelectedStudents = new ArrayList<>();

    public StudentAdapter(int numberStudents){ //Функция для получения кол-ва элементов списка
        this.numberNames = numberStudents;
    }

    @NonNull
    @Override
    public NameStudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.students_list_element;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new NameStudentViewHolder(view);
    }

    public void setListOfName(ArrayList<String> nameList){
        this.nameList.addAll(nameList);
        for(String i : nameList){
            SelectedStudents.add("");
        }
    }

    @Override
    public void onBindViewHolder(NameStudentViewHolder holder, int position) { // метод для обновления данных при прокрутке списка
        holder.bind(nameList.get(position), AverageScore.get(position), position); //передаём данные в функцию bind(), для устанавливки текста в TextView элемента списка //+
    }

    @Override
    public int getItemCount() { //функция возвращает кол-во элементов списка
        return numberNames;
    }

    class NameStudentViewHolder extends RecyclerView.ViewHolder {

        TextView NameStudentView; //переменная для нашего Text View из layout group_list_element
        TextView StudentMark; //переменная для вывода среднего балла студента
        TextView IndexStudent;
        View ourView;
        int color = StudentsActivity.getInstance().getResources().getColor(R.color.DIVIDER_COLOR);
        int color2 = StudentsActivity.getInstance().getResources().getColor(R.color.white);

        public NameStudentViewHolder(View itemView) { //что-то вроде графической обёртки для элемента списка
            super(itemView); //itemView - это элемент списка, который был сгенерирован из layout group_list_element

            ourView = itemView;
            NameStudentView = itemView.findViewById(R.id.Student_name); // поместили в переменную ссылку на наш TextView (операция поиска по id очень затратная, следовательно её мы проводим один раз)
            StudentMark = itemView.findViewById(R.id.Student_sr_mark);
            IndexStudent = itemView.findViewById(R.id.Student_position);

            // обработчик нажатия на каждый из элементов списка
            itemView.setOnLongClickListener(v -> {
                int positionIndex = getAdapterPosition();
                if(NameStudentView.getText().toString().equals(SelectedStudents.get(positionIndex))){
                    itemView.setBackgroundColor(color2);
                    counter--;
                    SelectedStudents.set(positionIndex,"");
                    if(counter == 0){
                        StudentsActivity.getInstance().SetStudentAppBarFragment();
                    }
                    else {
                        StudentsActivity.ContextualAppBarFragment.getInstance().SetTextSelectedStudents(counter);
                    }
                }
                else{
                    itemView.setBackgroundColor(color);

                    // если в списке "", то этого студента ещё не выбирали, а если стоит его имя, то выбирали
                    SelectedStudents.set(positionIndex,NameStudentView.getText().toString());

                    counter++;
                    if(counter == 1){
                        StudentsActivity.getInstance().SetContextualAppBarFragment();
                    }
                    else {
                        StudentsActivity.ContextualAppBarFragment.getInstance().SetTextSelectedStudents(counter);
                    }
                }
                return true;
            });
            itemView.setOnClickListener(v -> {
                int positionIndex = getAdapterPosition();
                if (counter == 0){
                    StudentsActivity.getInstance().RenameStudent(NameStudentView.getText().toString());
                }
                if(NameStudentView.getText().toString().equals(SelectedStudents.get(positionIndex))){
                    itemView.setBackgroundColor(color2);
                    counter--;
                    SelectedStudents.set(positionIndex,"");
                    if(counter == 0){
                        StudentsActivity.getInstance().SetStudentAppBarFragment();
                    }
                    else {
                        StudentsActivity.ContextualAppBarFragment.getInstance().SetTextSelectedStudents(counter);
                    }
                }
                else if(counter > 0){
                    SelectedStudents.set(positionIndex,NameStudentView.getText().toString());
                    itemView.setBackgroundColor(color);
                    counter++;
                    StudentsActivity.ContextualAppBarFragment.getInstance().SetTextSelectedStudents(counter);
                }
            });
        }

        void bind(String Name, String AverageScore, int position){ // функция для установки текста в элемент списка, на вход подаётся название группы
            String index;
            if(position+1<10){
                index = "0" + (position+1) + ":";
            }
            else{
                index = (position+1) + ":";
            }
            NameStudentView.setText(Name); // устанавливаем текст в наш TextView

            int red, green, blue; // делаю градиент для оценок (чтобы каждая оценка имела свой оттенок) (пока что идеальная конфигурация)

            if(!AverageScore.equals(" ")){
                green = (int)((Float.parseFloat(AverageScore)/5-0.1)*232);
                blue = (int)((Float.parseFloat(AverageScore)/5)*126);
                if (Float.parseFloat(AverageScore)>3.5){
                    red = (int)((1-Float.parseFloat(AverageScore)/5+0.5)*224);
                }
                else {
                    red = 224 - (int)((Float.parseFloat(AverageScore)/5)*30);
                }

                final ForegroundColorSpan style = new ForegroundColorSpan(Color.rgb(red, green, blue)); // устанавливаю для среднего балла цвета (в одном TextView)
                final SpannableStringBuilder text = new SpannableStringBuilder(AverageScore);
                text.setSpan(style, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                StudentMark.setText(text);
            }
            else{
                StudentMark.setText(AverageScore);
            }

            IndexStudent.setText(index);
            if(Name.equals(SelectedStudents.get(position))){
                ourView.setBackgroundColor(color);
            }
        }
    }

    public ArrayList<String> GetSelectedStudents(){
        return SelectedStudents;
    }

    public int GetCountOfSelectedStudents(){
        return counter;
    }

    public void GetAverageScore(ArrayList<String> AverageScore){
        this.AverageScore = AverageScore;
    }
}
