package com.example.prac7;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput, ageInput;
    private Button addButton;
    private AppDatabase db;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;

    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "student_db").build();

        nameInput = findViewById(R.id.nameInput);
        ageInput = findViewById(R.id.ageInput);
        addButton = findViewById(R.id.addButton);
        searchInput = findViewById(R.id.searchInput);

        addButton.setOnClickListener(view -> {
            String name = nameInput.getText().toString();
            int age = Integer.parseInt(ageInput.getText().toString());
            insertStudent(name, age);
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadAllStudents();

    }

    private void insertStudent(String name, int age) {
        new Thread(() -> {
            Student student = new Student();
            student.setName(name);
            student.setAge(age);
            db.studentDao().insertStudent(student);

            runOnUiThread(this::loadAllStudents);
        }).start();
    }

    private void loadAllStudents() {
        new Thread(() -> {
            List<Student> studentList = db.studentDao().getAllStudents();
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new StudentAdapter(studentList, this::showUpdateDeleteDialog);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.updateData(studentList);
                }
            });
        }).start();
    }

    private void showUpdateDeleteDialog(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_delete, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editAge = dialogView.findViewById(R.id.editAge);
        Button updateButton = dialogView.findViewById(R.id.updateButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        editName.setText(student.getName());
        editAge.setText(String.valueOf(student.getAge()));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        updateButton.setOnClickListener(v -> {
            String updatedName = editName.getText().toString();
            int updatedAge = Integer.parseInt(editAge.getText().toString());
            updateStudent(student.getId(), updatedName, updatedAge);
            dialog.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            deleteStudent(student);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateStudent(int id, String name, int age) {
        new Thread(() -> {
            Student student = new Student();
            student.setId(id);
            student.setName(name);
            student.setAge(age);
            db.studentDao().updateStudent(student);

            runOnUiThread(this::loadAllStudents);
        }).start();
    }

    private void deleteStudent(Student student) {
        new Thread(() -> {
            db.studentDao().deleteStudent(student);
            runOnUiThread(this::loadAllStudents);
        }).start();
    }

    private void searchStudents(String query) {
        new Thread(() -> {
            List<Student> studentList;
            if (query.isEmpty()) {
                studentList = db.studentDao().getAllStudents();
            } else {
                studentList = db.studentDao().searchStudentsByName(query);
            }
            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateData(studentList);
                }
            });
        }).start();
    }

}
