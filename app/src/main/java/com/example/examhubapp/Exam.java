package com.example.examhubapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.security.PublicKey;

public class Exam extends AppCompatActivity {

    private Question[] data;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        ListView listView=findViewById(R.id.list_item);
        Button submit=findViewById(R.id.submit);
        TextView title=findViewById(R.id.title);




    }

    public class ListAdapter extends BaseAdapter {
        Question[] arr;
        ListAdapter(Question[] arr2){
            arr=arr2;
        }
        @Override
        public int getCount(){
            return arr.length;
        }
        @Override
        public Object getItem(int i){
            return arr[i];
        }
        @Override
        public long getItemId(int i){
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup){
            LayoutInflater inflater=getLayoutInflater();
            View v=inflater.inflate(R.layout.question,null);
            TextView question=findViewById(R.id.question);
            RadioButton option1=findViewById(R.id.option1);
            RadioButton option2=findViewById(R.id.option2);
            RadioButton option3=findViewById(R.id.option3);
            RadioButton option4=findViewById(R.id.option4);

            question.setText(data[i].getQuestion());
            option1.setText(data[i].getOption1());
            option2.setText(data[i].getOption2());
            option3.setText(data[i].getOption3());
            option4.setText(data[i].getOption4());

            option1.setOnCheckedChangeListener((compoundButton,b)->{
                if(b){data[i].setSelectAnswer("1");}
            });

            option2.setOnCheckedChangeListener((compoundButton,b)->{
                if(b){data[i].setSelectAnswer("2");}
            });

            option3.setOnCheckedChangeListener((compoundButton,b)->{
                if(b){data[i].setSelectAnswer("3");}
            });

            option4.setOnCheckedChangeListener((compoundButton,b)->{
                if(b){data[i].setSelectAnswer("4");}
            });

            switch (data[i].getCorrectAnswer()){
                case "1":
                    option1.setChecked(true);
                    break;
                case "2":
                    option2.setChecked(true);
                    break;
                case "3":
                    option3.setChecked(true);
                    break;
                case "4":
                    option4.setChecked(true);
                    break;


            }


            return null;
        }

    }
}