package com.example.examhubapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SolvedQuestionAdapter extends RecyclerView.Adapter<SolvedQuestionAdapter.ViewHolder> {

    private List<AnsweredQuestion> answeredQuestions;

    public SolvedQuestionAdapter(List<AnsweredQuestion> answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solved_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnsweredQuestion answeredQuestion = answeredQuestions.get(position);
        Question question = answeredQuestion.getQuestion();
        Context context = holder.itemView.getContext();

        holder.questionTextView.setText(question.getQuestion());
        holder.yourAnswerTextView.setText("Your Answer: " + answeredQuestion.getSelectedAnswer());
        holder.correctAnswerTextView.setText("Correct Answer: " + question.getCorrectAnswer());
        holder.descriptionTextView.setText("Description: " + question.getDescription());

        if (answeredQuestion.isCorrect()) {
            holder.yourAnswerTextView.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.yourAnswerTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return answeredQuestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView questionTextView;
        public TextView yourAnswerTextView;
        public TextView correctAnswerTextView;
        public TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.question_text_view);
            yourAnswerTextView = itemView.findViewById(R.id.your_answer_text_view);
            correctAnswerTextView = itemView.findViewById(R.id.correct_answer_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
        }
    }
}
