package com.awozow.gymlog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awozow.gymlog.database.GymLogRepository;
import com.awozow.gymlog.database.entities.GymLog;
import com.awozow.gymlog.database.entities.User;
import com.awozow.gymlog.databinding.ActivityMainBinding;
import com.awozow.gymlog.viewHolders.GymLogAdapter;
import com.awozow.gymlog.viewHolders.GymLogViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "DAC_GYMLOG";
    private static final String MAIN_ACTIVITY_USER_ID = "com.awozow.gymlog.MAIN_ACTIVITY_USER_ID";
    static final String SHARED_PREFERENCE_USERID_KEY = "com.awozow.gymlog.SHARED_PREFERENCE_USERID_KEY";
    static final String SAVED_INSTANCE_STATE_USERID_KEY = "com.awozow.gymlog.SAVED_INSTANCE_STATE_USERID_KEY";
    private static final int LOGGED_OUT = -1;
    private ActivityMainBinding binding;
    private GymLogRepository repository;
    private GymLogViewModel gymLogViewModel;
    String mExercise = "";
    double mWeight = 0.0;
    int mReps = 0;

    private int loginUserID = -1;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gymLogViewModel = new ViewModelProvider(this).get(GymLogViewModel.class);


        RecyclerView recyclerView = binding.logDisplayRecyclerView;
        final GymLogAdapter adapter = new GymLogAdapter(new GymLogAdapter.GymLogDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        repository = GymLogRepository.getRepository(getApplication());
        loginUser(savedInstanceState);

        gymLogViewModel.getAllLogsById(loginUserID).observe(this,gymLogs -> {
            adapter.submitList(gymLogs);
        });

        //User is not logged in go to login screen
        if (loginUserID == -1){
            Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
            startActivity(intent);
        }
        updatedSharedPreference();

        //TODO remove this
        //binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());
        updateDisplay();

        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInformationFromDisplay();
                insertGymLogRecord();
             //   updateDisplay();
            }
        });

//        binding.exerciseInputEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               // updateDisplay();
//            }
//        });
    }

    private void loginUser(Bundle savedInstanceState) {

        // check shared preference for logged in user
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        loginUserID = sharedPreferences.getInt(getString(R.string.preference_userId_key), LOGGED_OUT);


        if (loginUserID == LOGGED_OUT &&
                savedInstanceState != null &&
                savedInstanceState.containsKey(SAVED_INSTANCE_STATE_USERID_KEY)) {

            loginUserID = savedInstanceState.getInt(
                    SAVED_INSTANCE_STATE_USERID_KEY,
                    LOGGED_OUT
            );
        }

        if (loginUserID == LOGGED_OUT) {
            loginUserID = getIntent().getIntExtra(
                    MAIN_ACTIVITY_USER_ID,
                    LOGGED_OUT
            );
        }

        if (loginUserID == LOGGED_OUT) {
            return;
        }

        LiveData<User> userObserver =
                repository.getUserByUserId(loginUserID);

        userObserver.observe(this, user -> {
            this.user = user;
            if (this.user != null) {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_INSTANCE_STATE_USERID_KEY, loginUserID);
        updatedSharedPreference();
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
        if (user == null){
            return false;
        }
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
        loginUserID = LOGGED_OUT;
        updatedSharedPreference();
        getIntent().putExtra(MAIN_ACTIVITY_USER_ID, LOGGED_OUT);
        startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
    }

    private void updatedSharedPreference(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putInt(getString(R.string.preference_userId_key), loginUserID);
        sharedPrefEditor.apply();
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

    @Deprecated
    private void updateDisplay(){
        ArrayList<GymLog> allLogs = repository.getAllLogsByUserId(loginUserID);
        if (allLogs.isEmpty()){
            //binding.logDisplayTextView.setText(R.string.nothing_to_show_time_to_hit_the_gym);
        }
        StringBuilder sb = new StringBuilder();
        for (GymLog log : allLogs){
            sb.append(log);
        }
        //binding.logDisplayTextView.setText(sb.toString());
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