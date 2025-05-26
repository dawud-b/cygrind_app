package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private int currentScore;
    private int highScore;

    private TextView highScoreTxt;
    private TextView problemText;
    private Button submitBtn;
    private EditText answerEntry;
    private TextView currScoreTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        submitBtn = findViewById(R.id.game_btn);
        answerEntry = findViewById(R.id.game_entry);
        problemText = findViewById(R.id.game_problem_text);
        highScoreTxt = findViewById(R.id.high_score);
        currScoreTxt = findViewById(R.id.curr_score);

        Random rand = new Random();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            currentScore = extras.getInt("currScore");
            highScore = extras.getInt("highScore");
        }

        int a = rand.nextInt(10);
        int b = rand.nextInt(10);
        String problemTxt = String.format("%d + %d", a, b);
        problemText.setText(problemTxt);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = answerEntry.getText().toString();

                Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                intent.putExtra("solution", a + b);
                intent.putExtra("entry", answer);
                intent.putExtra("highScore", highScore);
                intent.putExtra("currScore", currentScore);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if(extras.getBoolean("result")) {
                currentScore += 1;
                if(currentScore > highScore) {
                    highScore = currentScore;
                }
            } else {
                currentScore = 0;
            }
        }

        String msg = "High Score: " + highScore;
        highScoreTxt.setText(msg);
        msg = "Current Score: " + currentScore;
        currScoreTxt.setText(msg);
    }

}
