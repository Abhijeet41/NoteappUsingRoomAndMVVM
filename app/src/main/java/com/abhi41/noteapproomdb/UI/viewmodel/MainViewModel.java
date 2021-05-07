package com.abhi41.noteapproomdb.UI.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.abhi41.noteapproomdb.UI.database.NotesDatabase;
import com.abhi41.noteapproomdb.UI.entities.Note;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;


public class MainViewModel extends AndroidViewModel {

    private NotesDatabase notesDatabase;

    public MainViewModel(@NonNull Application application) {
        super(application);
        notesDatabase = NotesDatabase.getDataBase(application);
    }

    public Flowable<List<Note>> loadAllNotes() {
        return notesDatabase.noteDao().getAllNotes();
    }

    public Completable deleteNote(Note note) {
        return notesDatabase.noteDao().deleteNote(note);
    }

}
