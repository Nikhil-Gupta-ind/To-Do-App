package com.nikhilgupta.todoapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nikhilgupta.todoapp.database.AppDatabase;
import com.nikhilgupta.todoapp.database.TaskEntry;
/**
 * Here LiveData object task is casted in ViewModel
 */
public class AddTaskViewModel extends ViewModel {
    private LiveData<TaskEntry> task;

    public AddTaskViewModel(AppDatabase database, int taskId) {
        task = database.taskDao().loadTaskById(taskId);
    }

    public LiveData<TaskEntry> getTask() {
        return task;
    }
}
