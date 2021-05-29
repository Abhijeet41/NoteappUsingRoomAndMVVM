package com.abhi41.noteapproomdb.UI.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.abhi41.noteapproomdb.UI.dao.NoteDao;
import com.abhi41.noteapproomdb.UI.entities.Note;

@Database(entities = Note.class, version = 2, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;

    public static synchronized NotesDatabase getDataBase(Context context) {
        if (notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(context,
                    NotesDatabase.class, "notes_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao();

}
