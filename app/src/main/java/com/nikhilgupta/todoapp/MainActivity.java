package com.nikhilgupta.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nikhilgupta.todoapp.database.AppDatabase;
import com.nikhilgupta.todoapp.database.TaskEntry;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    private AppDatabase mDb;

    private TextView textView2;
    private static final String FIRST_LAUNCH_KEY = "launchKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);

        textView2 = findViewById(R.id.textView2);

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int monthInt = calendar.get(Calendar.MONTH);

        String day = "";
        String month = "";
        switch (dayOfWeek) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
        }
        switch (monthInt) {
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;
            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                day = "July";
                break;
            case 7:
                month = "August";
                break;
            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;
            case 11:
                month = "December";
                break;
        }
        textView2.setText(day + ", " + date + " " + month);

        // Checking if the app is launched for the first time
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(FIRST_LAUNCH_KEY, true)) {
//            TestUtil.insertFakeData(database);
            insertTutorialData();
            sp.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply();
        }

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

//        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
//        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Here you'll implement swipe to delete
                AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<TaskEntry> tasks = mAdapter.getTaskEntries();
                        mDb.taskDao().deleteTask(tasks.get(position));
//                retrieveTasks(); removed this since now LiveData handle it.
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(view -> {
            // Create a new intent to start an AddTaskActivity
            Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(addTaskIntent);
        });

        final int state[] = new int[1];
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                state[0] = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 10 /*&& (state[0]==0 || state[0]==2)*/) {
//                    hideToolbar();
                    fabButton.hide();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }
                } else if (dy < 0) {
//                    showToolbar();
                    fabButton.show();
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().show();
                    }
                }
            }
        });

        mDb = AppDatabase.getInstance(this);
        setupViewModel();
    }

    private void insertTutorialData() {
        Date date = new Date();
        final TaskEntry taskEntry = new TaskEntry("Add Task with + button", AddTaskActivity.PRIORITY_HIGH, date);
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            mDb = AppDatabase.getInstance(this);
            mDb.taskDao().insertTask(taskEntry);
        });
        final TaskEntry taskEntry2 = new TaskEntry("Swipe to delete the task", AddTaskActivity.PRIORITY_MEDIUM, date);
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            mDb = AppDatabase.getInstance(this);
            mDb.taskDao().insertTask(taskEntry2);
        });
        final TaskEntry taskEntry3 = new TaskEntry("Tap task to edit", AddTaskActivity.PRIORITY_LOW, date);
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            mDb = AppDatabase.getInstance(this);
            mDb.taskDao().insertTask(taskEntry3);
        });

    }

    private void setupViewModel() {

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(List<TaskEntry> taskEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setTasks(taskEntries);
            }
        });
        /**
         *  LiveData casted in ViewModel so code replaced as above
         */
        /*final LiveData<List<TaskEntry>> tasks = mDb.taskDao().loadAllTasks();
        tasks.observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(List<TaskEntry> taskEntries) {
                Log.d(TAG, "Receiving database update from LiveData");
                mAdapter.setTasks(taskEntries);
            }
        });*/
        /**
         * Removed executor since using liveData object task we are calling observe method which
         * runs on background thread by default.
         * Removed runOnUiThread() because we onChanged method runs on UI thread by default.
         * Hence code replaced as above.
         */
        /*AppExecutors.getInstance().getDiskIO().execute(()->{
            final List<TaskEntry> tasks = mDb.taskDao().loadAllTasks();
            runOnUiThread(()->{
                // We'll be able to simplify this once we learn more
                // about Android Architecture Component
                mAdapter.setTasks(tasks);
            });
        });*/
    }

    @Override
    public void onItemCLickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }
}