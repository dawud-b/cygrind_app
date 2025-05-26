package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    private static int greenHex = 0xFF00FF00;
    private static int redHex = 0xFFFF0000;
    private Button restartBtn;
    private TextView resultMsg;
    private int currScore;
    private int highScore;
    boolean result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        restartBtn = findViewById(R.id.restart_btn);
        resultMsg = findViewById(R.id.result_msg);

        // get results from previous activity
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            resultMsg.setText("error");
        } else {
            int num = Integer.parseInt(extras.getString("entry"));
            result = num == extras.getInt("solution");
            if(result) {
                resultMsg.setTextColor(greenHex);
                resultMsg.setText("Correct!");
            } else {
                resultMsg.setTextColor(redHex);
                resultMsg.setText("Incorrect :(");
            }
            currScore = extras.getInt("currScore");
            highScore = extras.getInt("highScore");
        }

        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, GameActivity.class);
                intent.putExtra("result", result);
                intent.putExtra("currScore", currScore);
                intent.putExtra("highScore", highScore);
                startActivity(intent);
            }
        });
    }
}
