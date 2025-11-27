package com.example.examhubapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class QuestionAdapter extends ArrayAdapter<Question> {

    public QuestionAdapter(@NonNull Context context, @NonNull List<Question> questions) {
        super(context, 0, questions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_question, parent, false);
        }

        Question question = getItem(position);

        TextView questionText = convertView.findViewById(R.id.question_text);
        RadioGroup optionsGroup = convertView.findViewById(R.id.options_radiogroup);
        RadioButton option1 = convertView.findViewById(R.id.option1_radiobutton);
        RadioButton option2 = convertView.findViewById(R.id.option2_radiobutton);
        RadioButton option3 = convertView.findViewById(R.id.option3_radiobutton);
        RadioButton option4 = convertView.findViewById(R.id.option4_radiobutton);

        questionText.setText(question.getQuestion());
        option1.setText(question.getOption1());
        option2.setText(question.getOption2());
        option3.setText(question.getOption3());
        option4.setText(question.getOption4());

        optionsGroup.setOnCheckedChangeListener(null);
        optionsGroup.clearCheck();

        if (question.getSelectAnswer() != null) {
            if (question.getSelectAnswer().equals(question.getOption1())) {
                option1.setChecked(true);
            } else if (question.getSelectAnswer().equals(question.getOption2())) {
                option2.setChecked(true);
            } else if (question.getSelectAnswer().equals(question.getOption3())) {
                option3.setChecked(true);
            } else if (question.getSelectAnswer().equals(question.getOption4())) {
                option4.setChecked(true);
            }
        }

        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                question.setSelectAnswer(selectedRadioButton.getText().toString());
            }
        });

        return convertView;
    }
}
