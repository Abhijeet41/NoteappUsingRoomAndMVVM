package com.abhi41.noteapproomdb.UI.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.abhi41.noteapproomdb.UI.entities.Note;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface NoteDao {

   /* @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();*/

    @Query("SELECT * FROM notes ORDER BY id DESC")
    Flowable<List<Note>>getAllNotes();

  /*  @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertNote(Note note);

    @Delete
    Completable deleteNote(Note note);



}
