package com.example.prac7;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudentDao {
    @Insert
    void insertStudent(Student student);

    @Query("SELECT * FROM Student")
    List<Student> getAllStudents();

    @Delete
    void deleteStudent(Student student);

    @Update
    void updateStudent(Student student);

    @Query("SELECT * FROM student WHERE name LIKE :name")
    List<Student> searchStudentsByName(String name);
}
