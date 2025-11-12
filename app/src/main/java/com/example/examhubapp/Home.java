package com.example.examhubapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity {
    private String userId;
    private String firstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        DatabaseReference database= FirebaseDatabase.getInstance().getReference();
        ProgressDialog progressDialog=new ProgressDialog(Home.this);
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Bundle b = getIntent().getExtras();
        userId=b.getString("User UID");


        TextView name=findViewById(R.id.name);
        TextView totalQuestions=findViewById(R.id.totalQuestions);
        TextView totalPoints=findViewById(R.id.totalPoints);
        Button startQuiz=findViewById(R.id.startQuiz);
        Button create=findViewById(R.id.create);

        RelativeLayout solvedQuizesLayout=findViewById(R.id.solvedQuizesLayout);
        RelativeLayout yourQuizes=findViewById(R.id.yourQuizes);


        EditText quizId=findViewById(R.id.quizId);
        EditText Title=findViewById(R.id.Title);
        ImageView signout=findViewById(R.id.signout);

        ValueEventListener listener=new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot snapshot) {
                DataSnapshot userRef=snapshot.child("users").child(userId);
                firstName=userRef.child("fname").getValue().toString();

                if(userRef.hasChild("Total Points")){
                    String totalPointsValue=userRef.child("Total Points").getValue().toString();
                    int points=Integer.parseInt(totalPointsValue);
                     totalPoints.setText(String.format("%03d",points));
                }
                if(userRef.hasChild("Total Questions")){
                    String totalQuestionsValue=userRef.child("Total Questions").getValue().toString();
                    int questions=Integer.parseInt(totalQuestionsValue);
                    totalQuestions.setText(String.format("%03d",questions));
                }
                name.setText("Welcome "+firstName);
                progressDialog.dismiss();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error){
                Toast.makeText(Home.this,"can't connect",Toast.LENGTH_SHORT).show();

            }

        };
        signout.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            Intent i= new Intent(Home.this,MainActivity.class);
            startActivity(i);
            finish();
        });








        TextView signup=findViewById(R.id.signup);
        signup.setOnClickListener(v->{
            Intent i=new Intent(Home.this,Signup.class);
            startActivity(i);
        });
        }


    }
