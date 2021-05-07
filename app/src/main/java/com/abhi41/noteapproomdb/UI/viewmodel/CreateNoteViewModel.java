package com.abhi41.noteapproomdb.UI.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.abhi41.noteapproomdb.UI.database.NotesDatabase;
import com.abhi41.noteapproomdb.UI.entities.Note;

import io.reactivex.Completable;

public class CreateNoteViewModel extends AndroidViewModel {

    private NotesDatabase notesDatabase;

    public CreateNoteViewModel(@NonNull Application application) {
        super(application);
        notesDatabase = NotesDatabase.getDataBase(application);
    }

    public Completable insertNote(Note note){
        return notesDatabase.noteDao().insertNote(note);
    }

    public Completable deleteNote(Note note){
        return notesDatabase.noteDao().deleteNote(note);
    }

}
