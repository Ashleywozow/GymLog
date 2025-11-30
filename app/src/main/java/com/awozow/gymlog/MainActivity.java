package com.awozow.gymlog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.awozow.gymlog.database.GymLogRepository;
import com.awozow.gymlog.database.entities.GymLog;
import com.awozow.gymlog.database.entities.User;
import com.awozow.gymlog.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "DAC_GYMLOG";
    private static final String MAIN_ACTIVITY_USER_ID = "com.awozow.gymlog.MAIN_ACTIVITY_USER_ID";
    private ActivityMainBinding binding;
    private GymLogRepository repository;
    String mExercise = "";
    double mWeight = 0.0;
    int mReps = 0;

    //TODO: Add login info
    private int loginUserID = -1;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginUser();
        invalidateOptionsMenu();

        if (loginUserID == -1){
            Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
            startActivity(intent);
        }

        repository = GymLogRepository.getRepository(getApplication());

        binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());
        updateDisplay();

        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                insertGymLogRecord();
                updateDisplay();
            }
        });

        binding.exerciseInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDisplay();
            }
        });
    }

    private void loginUser() {
        //TODO: Make login method functional
        user = new User("Ash", "password");
        loginUserID = getIntent().getIntExtra(MAIN_ACTIVITY_USER_ID,-1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.logoutMenuItem);
        item.setVisible(true);
        item.setTitle(user.getUsername());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                showLogoutDialog();
                return false;
            }
        });
        return true;
    }

    private void showLogoutDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog alertDialog = alertBuilder.create();

        alertBuilder.setMessage("Logout?");
        alertBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertBuilder.create().show();
    }

    private void logout() {
        //TODO: Finish logout work
        startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
    }

    static Intent mainActivityIntentFactory(Context context, int userID){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userID);
        return intent;
    }

    private void insertGymLogRecord(){
        if (mExercise.isEmpty()){
            return;
        }
        GymLog log = new GymLog(mExercise, mWeight, mReps, loginUserID );
        repository.insertGymLog(log);
    }

    private void updateDisplay(){
        ArrayList<GymLog> allLogs = repository.getAllLogs();
        if (allLogs.isEmpty()){
            binding.logDisplayTextView.setText(R.string.nothing_to_show_time_to_hit_the_gym);
        }
        StringBuilder sb = new StringBuilder();
        for (GymLog log : allLogs){
            sb.append(log);
        }
        binding.logDisplayTextView.setText(sb.toString());
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