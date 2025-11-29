package com.awozow.gymlog;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.awozow.gymlog.databinding.ActivityMainBinding;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DAC_GYMLOG";
    private ActivityMainBinding binding;
    String mExercise = "";
    double mWeight = 0.0;
    int mReps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());

        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                updateDisplay();
            }
        });
    }

    private void updateDisplay(){
        String currentInfo = binding.logDisplayTextView.getText().toString();
        String newDisplay = String.format(Locale.ENGLISH,"Exercise:%s%nWeight:%.2f%nReps:%d%n=-=-=-=%n%s", mExercise,mWeight,mReps,currentInfo);
        binding.logDisplayTextView.setText(newDisplay);
    }

    private void getInformationFromDisplay(){
        mExercise = binding.exerciseInputEditText.getText().toString();
        try {
            mWeight = Double.parseDouble(binding.weightInputEditText.getText().toString());
        } catch (NumberFormatException e){
            Log.d(TAG, "Error reading value from weight edit text.");
        }
        try {
            mReps = Integer.parseInt(binding.repInputEditText.getText().toString());
        } catch (NumberFormatException e){
            Log.d(TAG, "Error reading value from reps edit text.");
        }
    }
}