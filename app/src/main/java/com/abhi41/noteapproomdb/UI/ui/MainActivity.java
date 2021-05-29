package com.abhi41.noteapproomdb.UI.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.abhi41.noteapproomdb.R;
import com.abhi41.noteapproomdb.UI.adapter.NotesAdapter;
import com.abhi41.noteapproomdb.UI.entities.Note;
import com.abhi41.noteapproomdb.UI.listeners.NotesListener;
import com.abhi41.noteapproomdb.UI.util.Dialogs;
import com.abhi41.noteapproomdb.UI.util.MyApplication;
import com.abhi41.noteapproomdb.UI.util.PrintMessage;
import com.abhi41.noteapproomdb.UI.util.RealPathUtil;
import com.abhi41.noteapproomdb.UI.viewmodel.MainViewModel;
import com.abhi41.noteapproomdb.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements NotesListener, Dialogs.AlertDialogListener, MyApplication.LogoutListener, PaymentResultListener {

    ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private MainViewModel mainViewModel;
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTES = 3;
    private static final int REQUEST_CODE_SELECT_IMAGE_SHORTCUT = 4;
    private static final int REQUEST_CODE_SELECT_CAMERA = 5;

    AlertDialog dialogAddUrl;
    private int noteClickedPosition = -1;
    private List<Note> noteList = new ArrayList<>();
    private NotesAdapter notesAdapter;
    Dialogs dialogs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Checkout.preload(getApplicationContext());

        mainViewModel = new MainViewModel(getApplication());
        dialogs = new Dialogs(MainActivity.this);

        binding.imageAddNoteMain.setOnClickListener(v ->
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE));

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notesAdapter.cancleTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(editable.toString());
                }
            }
        });

        binding.imageAddNote.setOnClickListener(view -> startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
        ));

        binding.imageAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //selectImage();
            dialogs.showImagePickDialog(MainActivity.this);

            }
        });

        binding.imageAddWebLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddUrlDialog();
            }
        });
        binding.ivDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });

    }

    private String getPathFromUri(Uri contentUri) {
        String realPath = new String();

        realPath = RealPathUtil.getRealPath(this, contentUri);

        return realPath;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }


    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

 /*       class GetNotesTask extends AsyncTask<Void, Void, List<Note>>
        {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDataBase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                PrintMessage.LogD(TAG,notes.toString());
            }
        }
        new GetNotesTask().execute();*/

        // insted of asynk task use Rxjava to fetch from roomDb

        notesAdapter = new NotesAdapter(noteList, this,MainActivity.this);
        binding.notesRecyclerview.setHasFixedSize(true);
        binding.notesRecyclerview.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        binding.notesRecyclerview.setAdapter(notesAdapter);

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(mainViewModel.loadAllNotes().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notes -> {
                    PrintMessage.LogD(TAG, notes.toString());
                    //noteList.clear();

                    if (requestCode == REQUEST_CODE_SHOW_NOTES) {

                        noteList.clear();
                        noteList.addAll(notes);
                        notesAdapter.notifyDataSetChanged();

                    } else if (requestCode == REQUEST_CODE_ADD_NOTE) {

                        noteList.add(0, notes.get(0)); // adding list on 0th position
                        notesAdapter.notifyItemInserted(0); //notify adapter item inserted at 0th position
                        binding.notesRecyclerview.smoothScrollToPosition(0);

                    } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {

                        noteList.remove(noteClickedPosition);

                        if (isNoteDeleted) {
                            notesAdapter.notifyItemRemoved(noteClickedPosition);
                        } else {
                            noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                            notesAdapter.notifyItemChanged(noteClickedPosition);
                        }

                    }
                    compositeDisposable.dispose();
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (resultCode != RESULT_CANCELED) {
            if (requestCode == REQUEST_CODE_SELECT_IMAGE_SHORTCUT && resultCode == RESULT_OK) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        String selectedPath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedPath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    }
                }
            }else if (requestCode == REQUEST_CODE_SELECT_CAMERA && resultCode == RESULT_OK){
                try {
                    if (data != null){
                        Bitmap captureImage = (Bitmap) data.getExtras().get("data");

                        //Convert to byte array to pass bitmap through intent
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        captureImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        Uri capturedImageUri = getImageUri(getApplicationContext(), captureImage);
                        String selectedPath = getPathFromUri(capturedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("quickActionType", "imageFromCamera");
                        intent.putExtra("imagePath", selectedPath);
                        intent.putExtra("byteArray", byteArray);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_" + System.currentTimeMillis(), null);
        return Uri.parse(path);
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
                    dialogAddUrl.dismiss();
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickAction", true);
                    intent.putExtra("quickActionType", "URL");
                    intent.putExtra("URL", inputUrl.getText().toString());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

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
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(intent,
                                    REQUEST_CODE_SELECT_IMAGE_SHORTCUT);
                        }
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
    protected void onResume() {
        super.onResume();
        //Set Listener to receive events
        MyApplication.registerSessionListener(this);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        MyApplication.resetSession();

    }

    @Override
    public void onSessionLogout() {
        Log.d(TAG, "onSessionLogout: ");
    }

    public void startPayment() {

        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_XKH7DtCqkT78Yh");
        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.razorpay);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "NIIT");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
          //  options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", "50000");//pass amount in currency subunits //500*100
            options.put("prefill.email", "abhijeetmule46@gmail.com");
            options.put("prefill.contact","8355942271");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onPaymentSuccess: "+razorpayPaymentId);
    }

    @Override
    public void onPaymentError(int i, String response) {
        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onPaymentError: "+response);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Checkout.clearUserData(this);
    }
}