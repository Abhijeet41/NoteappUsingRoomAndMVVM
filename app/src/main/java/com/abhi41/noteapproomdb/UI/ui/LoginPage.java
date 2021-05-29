package com.abhi41.noteapproomdb.UI.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.abhi41.noteapproomdb.R;
import com.abhi41.noteapproomdb.UI.util.PrintMessage;
import com.abhi41.noteapproomdb.databinding.ActivityLoginPageBinding;
import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;
import com.truecaller.android.sdk.TruecallerSDK;
import com.truecaller.android.sdk.TruecallerSdkScope;

import org.jetbrains.annotations.NotNull;

public class LoginPage extends AppCompatActivity {
    private static final String TAG = "LoginPage";
    ActivityLoginPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_page);


        binding.loginWithTruecaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TruecallerSDK.getInstance().isUsable()) {
                    TruecallerSDK.getInstance().getUserProfile(LoginPage.this);
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
                    dialogBuilder.setMessage("Truecaller App not installed.");

                    dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                                Log.d(TAG, "onClick: Closing dialog");

                                dialog.dismiss();
                            }
                    );

                    dialogBuilder.setIcon(R.drawable.com_truecaller_icon);
                    dialogBuilder.setTitle(" ");

                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        TruecallerSdkScope sdkScope = new TruecallerSdkScope.Builder(this, new ITrueCallback() {
            @Override
            public void onSuccessProfileShared(@NonNull @NotNull TrueProfile trueProfile) {
                PrintMessage.LogD(TAG, trueProfile.firstName + " " + trueProfile.lastName);
                startActivity(new Intent(getApplicationContext(), MainActivity.class)
                        .putExtra("profile", trueProfile));
                finish();
            }

            @Override
            public void onFailureProfileShared(@NonNull @NotNull TrueError trueError) {
                PrintMessage.LogD(TAG, trueError.toString());
            }

            @Override
            public void onVerificationRequired(TrueError trueError) {
                PrintMessage.LogD(TAG, trueError.toString());
            }
        })
                .consentMode(TruecallerSdkScope.CONSENT_MODE_POPUP)
                .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
                .footerType(TruecallerSdkScope.FOOTER_TYPE_SKIP).build();

        TruecallerSDK.init(sdkScope);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TruecallerSDK.getInstance().onActivityResultObtained(LoginPage.this,requestCode,resultCode,data);
    }
}