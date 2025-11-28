package com.example.examhubapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Changed from TextView
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExamEditor extends AppCompatActivity {

    private ArrayList<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exam_editor);

        // --- Fix 1: Correctly handle window insets for edge-to-edge display ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

    // --- Fix 2: Changed ViewHolder to use Buttons for options, which is more appropriate ---
    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<Question> questionList;

        public CustomAdapter(ArrayList<Question> questions) {
            this.questionList = questions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // --- Fix 3: Changed layout name to be more descriptive ---
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.question, parent, false); // Use 'question_item.xml'
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Question question = questionList.get(position);
            holder.questionText.setText(question.getQuestion());

            // --- Fix 4: Safely get and set options to prevent crashes ---
            // The Question's constructor now creates a shuffled list of all choices.
            List<String> options = question.getShuffledOptions();
            holder.option1.setText(options.get(0));
            holder.option2.setText(options.get(1));
            holder.option3.setText(options.get(2));
            holder.option4.setText(options.get(3));
        }

        @Override
        public int getItemCount() {
            return questionList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView questionText;
            public Button option1; // Changed to Button
            public Button option2; // Changed to Button
            public Button option3; // Changed to Button
            public Button option4; // Changed to Button

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Ensure these IDs match your 'question_item.xml'
                questionText = itemView.findViewById(R.id.question);
                option1 = itemView.findViewById(R.id.option1);
                option2 = itemView.findViewById(R.id.option2);
                option3 = itemView.findViewById(R.id.option3);
                option4 = itemView.findViewById(R.id.option4);
            }
        }
    }

    // --- Fix 5: Improved Question class to handle options more safely ---
    private static class Question {
        private String question;
        private String answer;
        private List<String> shuffledOptions; // Use a List for flexibility

        Question(String question, String answer, String[] otherOptions) {
            this.question = question;
            this.answer = answer;

            // Create a combined list of all options
            this.shuffledOptions = new ArrayList<>();
            this.shuffledOptions.add(answer); // Add the correct answer
            Collections.addAll(this.shuffledOptions, otherOptions); // Add the other options

            // Shuffle the list so the correct answer is not always the first option
            Collections.shuffle(this.shuffledOptions);
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        // Return the pre-shuffled list of 4 options
        public List<String> getShuffledOptions() {
            return shuffledOptions;
        }
    }
}
