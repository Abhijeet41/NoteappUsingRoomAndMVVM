package com.abhi41.noteapproomdb.UI.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi41.noteapproomdb.R;
import com.abhi41.noteapproomdb.UI.database.NotesDatabase;
import com.abhi41.noteapproomdb.UI.entities.Note;
import com.abhi41.noteapproomdb.UI.util.Dialogs;
import com.abhi41.noteapproomdb.UI.util.PrintMessage;
import com.abhi41.noteapproomdb.UI.util.RealPathUtil;
import com.abhi41.noteapproomdb.UI.viewmodel.CreateNoteViewModel;
import com.abhi41.noteapproomdb.databinding.ActivityCreateNoteBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import top.defaults.colorpicker.ColorPickerPopup;

public class CreateNoteActivity extends AppCompatActivity implements View.OnClickListener, Dialogs.AlertDialogListener {

    ActivityCreateNoteBinding binding;
    CreateNoteViewModel createNoteViewModel;
    private static final String TAG = "CreateNoteActivity";
    private String selectedNoteColor, selectedImagePath;
    private AlertDialog dialogAddUrl, dialogDeleteNote;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final int REQUEST_CODE_SELECT_CAMERA = 3;
    private static final int REQUEST_CODE_SELECT_CAMERA_CROP_PIC = 4;
    private Dialogs dialogs;
    private Note note_alredyavilable;
    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);
        createNoteViewModel = new CreateNoteViewModel(getApplication());
        binding.textDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a",
                Locale.getDefault()).format(new Date()));

        dialogs = new Dialogs(CreateNoteActivity.this);

        binding.imageSave.setOnClickListener(this);

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            note_alredyavilable = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdate();
        }

        binding.imageRemoveWebURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.textWebURL.setText(null);
                binding.layoutWebURL.setVisibility(View.GONE);
            }
        });

        binding.imageRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageNote.setImageBitmap(null);
                binding.imageNote.setVisibility(View.GONE);
                binding.imageRemoveImage.setVisibility(View.GONE);
                selectedImagePath = "";

            }
        });

        if (getIntent().getBooleanExtra("isFromQuickAction", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                    binding.imageNote.setImageBitmap(scaledBitmap);
                    binding.imageNote.setVisibility(View.VISIBLE);
                    binding.imageRemoveImage.setVisibility(View.VISIBLE);

                }else if (type.equals("URL")){

                    binding.textWebURL.setText(getIntent().getStringExtra("URL"));
                    binding.layoutWebURL.setVisibility(View.VISIBLE);

                }else if (type.equals("imageFromCamera")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");

                    byte[] byteArray = getIntent().getByteArrayExtra("byteArray");
                    Bitmap captureImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    int nh = (int) ( captureImage.getHeight() * (512.0 / captureImage.getWidth()) );
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(captureImage, 512, nh, true);

                    binding.imageNote.setImageBitmap(scaledBitmap);
                    binding.imageNote.setVisibility(View.VISIBLE);
                }
            }
        }

        initMiscellaneous();
        setSubtitleIndicator();

        binding.miscellaneous.txtColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerPopup.Builder(getApplicationContext())
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("Choose")
                        .cancelTitle("Cancel")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(v, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                v.setBackgroundColor(color);
                                String hexColor = String.format("#%06X", (0xFFFFFF & color));//converting color integer to hex string

                                selectedNoteColor = hexColor;
                                setSubtitleIndicator();
                                // binding.viewSubtitleIndicator.setBackgroundColor(color);
                            }

                        });
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.imageSave) {
            saveNote();
        }
    }


    private void setViewOrUpdate() {
        binding.inputNoteTitle.setText(note_alredyavilable.getTitle());
        binding.inputNoteSubTitle.setText(note_alredyavilable.getSubtitle());
        binding.inputNote.setText(note_alredyavilable.getNoteText());
        binding.textDateTime.setText(note_alredyavilable.getDateTime());

        if (note_alredyavilable.getImagePath() != null && !note_alredyavilable.getImagePath().trim().isEmpty()) {
            binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(note_alredyavilable.getImagePath()));
            binding.imageNote.setVisibility(View.VISIBLE);
            binding.imageRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = note_alredyavilable.getImagePath();
        }

        if (note_alredyavilable.getWeblink() != null && !note_alredyavilable.getWeblink().trim().isEmpty()) {
            binding.textWebURL.setText(note_alredyavilable.getWeblink());
            binding.layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote() {
        if (binding.inputNoteTitle.getText().toString().trim().isEmpty()) {
            PrintMessage.ToastMessage(CreateNoteActivity.this, "Note title can't be empty");
            return;
        } else if (binding.inputNoteTitle.getText().toString().trim().isEmpty()
                && binding.inputNote.getText().toString().trim().isEmpty()) {
            PrintMessage.ToastMessage(CreateNoteActivity.this, "Note can't be empty");
            return;
        }

        final Note note = new Note();
        note.setTitle(binding.inputNoteTitle.getText().toString());
        note.setSubtitle(binding.inputNoteSubTitle.getText().toString());
        note.setNoteText(binding.inputNote.getText().toString());
        note.setDateTime(binding.textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (binding.layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWeblink(binding.textWebURL.getText().toString());
        }

        if (note_alredyavilable != null) {
            note.setId(note_alredyavilable.getId());
        }

        // Room does't allow database operation on the main thread
        //that's why we are using async task to save note
/*
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {

                NotesDatabase.getDataBase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNoteTask().execute();*/


        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(createNoteViewModel.insertNote(note).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).doOnError(throwable -> {
                    Log.d(TAG, "throwable: " + throwable.getMessage());
                }).subscribe(() -> {

                    PrintMessage.LogD(TAG, "Data Inserted ");

                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();

                    compositeDisposable.dispose();
                }));

    }

    private void initMiscellaneous() {

        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(binding.miscellaneous.layoutMiscellaneous);

        binding.miscellaneous.textMiscellaneous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        binding.miscellaneous.viewColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                binding.miscellaneous.imageColor1.setImageResource(R.drawable.ic_done);
                binding.miscellaneous.imageColor2.setImageResource(0);
                binding.miscellaneous.imageColor3.setImageResource(0);
                binding.miscellaneous.imageColor4.setImageResource(0);
                binding.miscellaneous.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        binding.miscellaneous.viewColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                binding.miscellaneous.imageColor1.setImageResource(0);
                binding.miscellaneous.imageColor2.setImageResource(R.drawable.ic_done);
                binding.miscellaneous.imageColor3.setImageResource(0);
                binding.miscellaneous.imageColor4.setImageResource(0);
                binding.miscellaneous.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        binding.miscellaneous.viewColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                binding.miscellaneous.imageColor1.setImageResource(0);
                binding.miscellaneous.imageColor2.setImageResource(0);
                binding.miscellaneous.imageColor3.setImageResource(R.drawable.ic_done);
                binding.miscellaneous.imageColor4.setImageResource(0);
                binding.miscellaneous.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        binding.miscellaneous.viewColor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52FC";
                binding.miscellaneous.imageColor1.setImageResource(0);
                binding.miscellaneous.imageColor2.setImageResource(0);
                binding.miscellaneous.imageColor3.setImageResource(0);
                binding.miscellaneous.imageColor4.setImageResource(R.drawable.ic_done);
                binding.miscellaneous.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        binding.miscellaneous.viewColor5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                binding.miscellaneous.imageColor1.setImageResource(0);
                binding.miscellaneous.imageColor2.setImageResource(0);
                binding.miscellaneous.imageColor3.setImageResource(0);
                binding.miscellaneous.imageColor4.setImageResource(0);
                binding.miscellaneous.imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicator();
            }
        });

        if (note_alredyavilable != null && note_alredyavilable.getColor() != null && !note_alredyavilable.getColor().trim().isEmpty()) {
            switch (note_alredyavilable.getColor()) {
                case "#FDBE3B":
                    binding.miscellaneous.viewColor2.performClick();
                    break;

                case "#FF4842":
                    binding.miscellaneous.viewColor3.performClick();
                    break;

                case "#3A52FC":
                    binding.miscellaneous.viewColor4.performClick();
                    break;

                case "#000000":
                    binding.miscellaneous.viewColor5.performClick();
                    break;
            }
        }

        binding.miscellaneous.layoutAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                dialogs.showImagePickDialog(CreateNoteActivity.this);
            }
        });

        binding.miscellaneous.layoutAddUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialog();
            }
        });

        if (note_alredyavilable != null) {
            binding.miscellaneous.layoutDeleteNote.setVisibility(View.VISIBLE);
            binding.miscellaneous.layoutDeleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }

    private void setSubtitleIndicator() {
        try {
            GradientDrawable gradientDrawable = (GradientDrawable) binding.viewSubtitleIndicator.getBackground();
            gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {
             /*       try {

                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageNote.setImageBitmap(bitmap);
                        binding.imageNote.setVisibility(View.VISIBLE);
                        binding.imageRemoveImage.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }*/

                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(this);
                }
            }
        }else if (requestCode == REQUEST_CODE_SELECT_CAMERA && resultCode == RESULT_OK){
            try {
                if (data != null){

                    Bitmap captureImage = (Bitmap) data.getExtras().get("data");
                    Uri capturedImageUri = getImageUri(getApplicationContext(), captureImage);

                   // performCrop(capturedImageUri);

                    binding.imageNote.setImageBitmap(captureImage);
                    selectedImagePath = getPathFromUri(capturedImageUri);
                    binding.imageNote.setVisibility(View.VISIBLE);
                    binding.imageRemoveImage.setVisibility(View.VISIBLE);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = result.getUri();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    binding.imageNote.setImageBitmap(bitmap);
                    binding.imageNote.setVisibility(View.VISIBLE);
                    binding.imageRemoveImage.setVisibility(View.VISIBLE);

                    selectedImagePath = getPathFromUri(selectedImageUri);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }  /*else if (requestCode == REQUEST_CODE_SELECT_CAMERA_CROP_PIC && resultCode == RESULT_OK){
            //Bitmap captureImage = (Bitmap) data.getExtras().get("data");
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            Bitmap captureImage = extras.getParcelable("data");
            Uri capturedImageUri = getImageUri(getApplicationContext(), captureImage);

            binding.imageNote.setImageBitmap(captureImage);
            selectedImagePath = getPathFromUri(capturedImageUri);
            binding.imageNote.setVisibility(View.VISIBLE);
            binding.imageRemoveImage.setVisibility(View.VISIBLE);
        }*/
    }

    private void performCrop(Uri capturedImageUri) {
        // take care of exceptions
        try {
            // call the standard crop action intent (the user device may not
            // support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(capturedImageUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 2);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQUEST_CODE_SELECT_CAMERA_CROP_PIC);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath = new String();
        filePath = RealPathUtil.getRealPath(this, contentUri);
        //filePath = compressImage(filePath);

        return filePath;
    }

    public void showAddUrlDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, null);
        builder.setView(view);

        dialogAddUrl = builder.create();

        if (dialogAddUrl.getWindow() != null) {
            dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputUrl = view.findViewById(R.id.inputUrl);
        inputUrl.requestFocus();

        view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputUrl.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.enter_url, Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                    Toast.makeText(getApplicationContext(), R.string.enter_valid_url, Toast.LENGTH_SHORT).show();
                } else {
                    binding.textWebURL.setText(inputUrl.getText().toString());
                    binding.layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddUrl.dismiss();
                }
            }
        });

        view.findViewById(R.id.textCancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddUrl.dismiss();
            }
        });

        dialogAddUrl.show();
    }

    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, null);
            builder.setView(view);

            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CompositeDisposable compositeDisposable = new CompositeDisposable();
                    compositeDisposable.add(createNoteViewModel.deleteNote(note_alredyavilable)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                                Intent intent = new Intent();
                                intent.putExtra("isNoteDeleted", true);
                                setResult(RESULT_OK, intent);
                                finish();

                                compositeDisposable.dispose();
                            }));
                }
            });

            view.findViewById(R.id.textCancle).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }

    @Override
    public void onCameraClicked() {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent,REQUEST_CODE_SELECT_CAMERA);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    public void onGalleryClicked() {
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                            /*  Intent intent = new Intent(
                                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);*/
                      /*  if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(intent,
                                    REQUEST_CODE_SELECT_IMAGE);
                        }*/
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(CreateNoteActivity.this);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }


}