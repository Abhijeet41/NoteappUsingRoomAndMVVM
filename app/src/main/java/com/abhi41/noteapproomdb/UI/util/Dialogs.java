package com.abhi41.noteapproomdb.UI.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi41.noteapproomdb.R;

public class Dialogs {

    private  AlertDialog dialogAddUrl;

    private AlertDialogListener callBack;


    public Dialogs(Context context) {
        callBack = (AlertDialogListener) context;
    }

    public void showAlertDialog(Context context, TextView textWebUrl){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_add_url, null);
        builder.setView(view);

        dialogAddUrl = builder.create();

        if (dialogAddUrl.getWindow() != null){
            dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final EditText inputUrl = view.findViewById(R.id.inputUrl);
        inputUrl.requestFocus();

        view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputUrl.getText().toString().trim().isEmpty()){
                    Toast.makeText(context, R.string.enter_url, Toast.LENGTH_SHORT).show();
                }else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()){
                    Toast.makeText(context, R.string.enter_valid_url, Toast.LENGTH_SHORT).show();
                }else {
                    textWebUrl.setText(inputUrl.getText().toString());
                }
            }
        });
    }



    public void showImagePickDialog(Activity activity){

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_select_image);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthLcl = (int) (displayMetrics.widthPixels*0.9f);
        int heightLcl = (int) (displayMetrics.heightPixels*0.9f);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = widthLcl;
        layoutParams.height = heightLcl;
        dialog.getWindow().setAttributes(layoutParams);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        final ImageView imge_camera = dialog.findViewById(R.id.imge_camera);
        final ImageView imge_gallery = dialog.findViewById(R.id.imge_gallery);

        imge_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.onCameraClicked();
                dialog.dismiss();
            }
        });

        imge_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.onGalleryClicked();
                dialog.dismiss();
            }
        });

   /*   WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialogImage.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogImage.getWindow().setAttributes(layoutParams);*/



        dialog.show();
    }


    public interface AlertDialogListener
    {
        public void onCameraClicked();
        public void onGalleryClicked();

    }

}
