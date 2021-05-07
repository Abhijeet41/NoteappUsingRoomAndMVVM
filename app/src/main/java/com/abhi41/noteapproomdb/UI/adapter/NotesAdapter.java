package com.abhi41.noteapproomdb.UI.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.abhi41.noteapproomdb.R;
import com.abhi41.noteapproomdb.UI.database.NotesDatabase;
import com.abhi41.noteapproomdb.UI.entities.Note;
import com.abhi41.noteapproomdb.UI.listeners.NotesListener;
import com.abhi41.noteapproomdb.UI.ui.MainActivity;
import com.abhi41.noteapproomdb.UI.util.ImageConverter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    public List<Note> noteList;
    private final NotesListener notesListener;
    private Timer timer;
    private List<Note> noteSource;
    private List<Note> selectedList = new ArrayList<>();
    private Activity contecxt;
    boolean isEnable = false;
    private NotesDatabase notesDatabase;

    public NotesAdapter(List<Note> notes, NotesListener notesListener, Activity contecxt) {
        this.noteList = notes;
        this.notesListener = notesListener;
        noteSource = notes;
        this.contecxt = contecxt;
        notesDatabase = NotesDatabase.getDataBase(contecxt);
    }

    @NonNull
    @Override
    public NotesAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_notes, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.NoteViewHolder holder, int position) {
        holder.setNote(noteList.get(position));

        if (noteList.get(position).isCheck()) {
            holder.ivCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.ivCheckBox.setVisibility(View.GONE);
        }

        holder.layoutNote.setOnClickListener(v -> {

            if (isEnable) {
                longClicked(holder);
            } else {
                notesListener.onNoteClicked(noteList.get(position), position);
                Toast.makeText(contecxt, "You clicked " + noteList.get(holder.getBindingAdapterPosition()), Toast.LENGTH_SHORT).show();
            }

        });

        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (!isEnable && holder.ivCheckBox.getVisibility() == View.GONE) {

                    ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            // Inflate a menu resource providing context menu items
                            MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(R.menu.menu_multi_select, menu);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            isEnable = true;
                            longClicked(holder);
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {


                            switch (item.getItemId()) {
                                case R.id.action_delete:
                                    showAlertDialog(mode);
                                    break;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {

                            isEnable = false;
                            removeAllClick();

                        }
                    };
                    ((AppCompatActivity) view.getContext()).startActionMode(mActionModeCallback);
                } else {
                    longClicked(holder);
                }
                return true;
            }
        });


    }


    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class NoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView textTitle, textSubtitle, textDateTime;
        private final LinearLayout layoutNote;
        private final ImageView imageNote;
        ImageView ivCheckBox;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);

            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
            ivCheckBox = itemView.findViewById(R.id.ivCheckBox);


        }

        void setNote(Note note) {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();

            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
                //imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                try {
                    Bitmap circularBitmap1 = ImageConverter.getRoundedCornerBitmap(BitmapFactory.decodeFile(note.getImagePath()), 90);

                    Glide.with(contecxt)
                            .load(circularBitmap1)
                            .centerCrop()
                            .apply(new RequestOptions().override(200, 200))
                            .into(imageNote);

                    //imageNote.setImageBitmap(circularBitmap1);
                    imageNote.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                imageNote.setVisibility(View.GONE);
            }

        }
    }

    public void searchNotes(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    noteList = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    noteList = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancleTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void longClicked(NoteViewHolder holder) {

        Note note = noteList.get(holder.getBindingAdapterPosition());
        if (holder.ivCheckBox.getVisibility() == View.GONE) {
            note.setCheck(true);
            holder.ivCheckBox.setVisibility(View.VISIBLE);
            selectedList.add(note);
        } else {
            note.setCheck(false);
            holder.ivCheckBox.setVisibility(View.GONE);
            selectedList.remove(note);
        }

    }

    private void removeAllClick() {
        selectedList.clear();

        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).isCheck()) {
                noteList.get(i).setCheck(false);
            }
        }
        notifyDataSetChanged();
        // Toast.makeText(contecxt, "onBack", Toast.LENGTH_SHORT).show();

    }

    private void showAlertDialog(ActionMode mode) {

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                contecxt);
        alertDialog2.setTitle("Confirm Delete...");
        alertDialog2.setMessage("Are you sure you want delete?");
        alertDialog2.setIcon(R.drawable.ic_delete);
        alertDialog2.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        for (Note note : selectedList) {
                            noteList.remove(note);

                            CompositeDisposable compositeDisposable = new CompositeDisposable();
                            compositeDisposable.add(notesDatabase.noteDao().deleteNote(note)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                                        Intent intent = new Intent();
                                        intent.putExtra("isNoteDeleted", true);
                                        contecxt.setResult(RESULT_OK, intent);

                                        //contecxt.finish();

                                        compositeDisposable.dispose();
                                    }));
                        }
                        mode.finish();


                        Toast.makeText(contecxt,
                                "You clicked on YES", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        alertDialog2.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        Toast.makeText(contecxt,
                                "You clicked on NO", Toast.LENGTH_SHORT)
                                .show();
                        dialog.cancel();
                    }
                });
        alertDialog2.show();
    }
}
