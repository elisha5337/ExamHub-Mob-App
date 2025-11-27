package com.example.examhubapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ExamEditor extends AppCompatActivity {

    private ArrayList<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exam_editor);

        // Initialize the questions list
        questions = new ArrayList<>();
        questions.add(new Question("What is the capital of France?", "Paris", new String[]{"London", "Berlin", "Madrid"}));
        questions.add(new Question("What is the largest ocean in the world?", "Pacific Ocean", new String[]{"Atlantic Ocean", "Indian Ocean", "Arctic Ocean"}));

        // Set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.list_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CustomAdapter adapter = new CustomAdapter(questions);
        recyclerView.setAdapter(adapter);
    }

    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<Question> arr;

        public CustomAdapter(ArrayList<Question> questions) {
            this.arr = questions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.question, parent, false); // Assuming a layout named question_item
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Question question = arr.get(position);
            holder.questionText.setText(question.getQuestion());
            holder.option1.setText(question.getAnswer()); // Assuming first option is the answer
            holder.option2.setText(question.getOptions()[0]);
            holder.option3.setText(question.getOptions()[1]);
            holder.option4.setText(question.getOptions()[2]);
        }

        @Override
        public int getItemCount() {
            return arr.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView questionText;
            public TextView option1;
            public TextView option2;
            public TextView option3;
            public TextView option4;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                questionText = itemView.findViewById(R.id.question);
                option1 = itemView.findViewById(R.id.option1);
                option2 = itemView.findViewById(R.id.option2);
                option3 = itemView.findViewById(R.id.option3);
                option4 = itemView.findViewById(R.id.option4);
            }
        }
    }

    private static class Question {
        private String question;
        private String answer;
        private String[] options;

        Question(String question, String answer, String[] options) {
            this.question = question;
            this.answer = answer;
            this.options = options;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        public String[] getOptions() {
            return options;
        }
    }
}
