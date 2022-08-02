package com.aamrtu.aictestudent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class studcontainer_StudentMainContainerActivity extends AppCompatActivity {

    public static FragmentManager fragmentManager;
    ImageView profileOption, timetableOption, notesOption, messageOption;      //images options
    ArrayList<ImageView> optionList = new ArrayList<>();    //store list of option (profileImageView..etc) to set Enable or Disable
    Toolbar toolbar;
    studcontainer_StudentMainContainerActivity context;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main_container);

        context = this;

        fragmentManager = getSupportFragmentManager();

        profileOption = findViewById(R.id.profileImageView);
        timetableOption = findViewById(R.id.timetableImageView);
        notesOption = findViewById(R.id.noteImageView);
        messageOption = findViewById(R.id.messageImageView);

//        set Listener on all option
        OnClickOption onClickAction = new OnClickOption();
        profileOption.setOnClickListener(onClickAction);
        timetableOption.setOnClickListener(onClickAction);
        notesOption.setOnClickListener(onClickAction);
        messageOption.setOnClickListener(onClickAction);

//        check if activity is starting for the first time, if we not do this check then we face fragment override problem
        if (savedInstanceState == null) {
//            set profile fragment in fragmentContainer
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, new option_StudentProfileFragment());
            fragmentTransaction.commit();
            profileOption.setEnabled(false);
            setToolBar();
        }
    }

    void setToolBar() {
        toolbar = findViewById(R.id.dot_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dot_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.resetPassword) {
            //add code for LogOut
            resetPassword();
        }
        if (item.getItemId() == R.id.about) {
            Intent about_intent = new Intent(studcontainer_StudentMainContainerActivity.this, AboutActivity.class);
            startActivity(about_intent);
        }
        if (item.getItemId() == R.id.logout) {
            //add code for LogOut
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    void resetPassword() {
        ViewGroup parent = findViewById(android.R.id.content); // this is root most view group which android provide by default
        final View view = LayoutInflater.from(context).inflate(R.layout.reset_password_layout, parent, false);
        final EditText oldPassEditText = view.findViewById(R.id.oldPassEditText);
        final EditText newPassEditText = view.findViewById(R.id.newPassEditText);
        final EditText rePassEditText = view.findViewById(R.id.reEnterPassEditText);
        Button passChangeButton = view.findViewById(R.id.passwordChangeButton);
        Button passCancelButton = view.findViewById(R.id.cancelButton);

        final AlertDialog.Builder resetPasswordDialogBuilder = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false);
        final AlertDialog resetPasswordAlertDialog = resetPasswordDialogBuilder.create();

        passChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOldPassword = oldPassEditText.getText().toString().trim();
                String enteredNewPassword = newPassEditText.getText().toString().trim();
                String enteredReEnterPassword = rePassEditText.getText().toString().trim();
                if (enteredOldPassword.isEmpty() || enteredNewPassword.isEmpty() || enteredReEnterPassword.isEmpty()) {
                    EditText emptyEditText = enteredOldPassword.isEmpty() ? oldPassEditText : enteredNewPassword.isEmpty() ? newPassEditText : enteredReEnterPassword.isEmpty() ? rePassEditText : null;
                    emptyEditText.setError("Empty Field");
                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(enteredOldPassword.equals(enteredNewPassword)){
                    newPassEditText.setError("New password same as old password");
                    return;
                }else if (enteredNewPassword.length() < 4) {
                    newPassEditText.setError("Minimum 4 character required");
                    return;
                } else if (!enteredNewPassword.equals(enteredReEnterPassword)) {
                    rePassEditText.setError("Password Mismatch");
                    return;
                } else {
                    resetPasswordAlertDialog.cancel();
                    checkAndResetPassword(enteredOldPassword, enteredNewPassword);
                }
            }
        });

        passCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPasswordAlertDialog.cancel();
            }
        });


//                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        StudentDetailHolder studentDetailHolder = StudentDetailHolder.getReference();
//                        EditText oldPassEditText =  view.findViewById(R.id.oldPassEditText);
//                        EditText newPassEditText =  view.findViewById(R.id.newPassEditText);
//                        EditText rePassEditText =  view.findViewById(R.id.reEnterPassEditText);
//
//                        final String oldPassword = oldPassEditText.getText().toString().trim();
//                        String newPassword = newPassEditText.getText().toString().trim();
//                        String rePassword = rePassEditText.getText().toString().trim();
//
//                        if(oldPassword.isEmpty() || newPassword.isEmpty() || rePassword.isEmpty())
//                        {
//                            Toast.makeText(context, "Fill all fields", Toast.LENGTH_LONG).show();
//                            resetPassword();
//                            return;
//                        }
//                        if (oldPassword.length()<4) {
//                            Toast.makeText(context, "Minimum 4 character required in password", Toast.LENGTH_LONG).show();
//                            resetPassword();
//                            return;
//                        } else if (!newPassword.equals(rePassword)) {
//                            rePasswordEditText.setError("Password Mismatch");
//                            allCorrect = false;
//                        }
//                        blockScreen(true);
//                        firebaseFirestore.collection("STUDENT")
//                                .document(studentDetailHolder.getStudentId())
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                        blockScreen(false);
//                                        if(task.isSuccessful()){
//                                            String databaseOldPassword = task.getResult().get("student_password").toString();
//                                            if(databaseOldPassword.equals(oldPassword)){
//                                                Toast.makeText(context, "Match", Toast.LENGTH_LONG).show();
//                                            }
//                                            else {
//                                                Toast.makeText(context, "Not Match", Toast.LENGTH_LONG).show();
//                                            }
//                                        }
//
//                                    }
//                                });
//
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
        resetPasswordAlertDialog.show();
    }

    void checkAndResetPassword(final String oldPassword, final String newPassword) {
        final StudentDetailHolder studentDetailHolder = StudentDetailHolder.getReference();
        blockScreen(true);

//        try { // used to hide keyboard
//            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }catch (NullPointerException e){}

        firebaseFirestore.collection("STUDENT")
                .document(studentDetailHolder.getStudentId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String databaseOldPassword = task.getResult().get("student_password").toString();
                            if (databaseOldPassword.equals(oldPassword)) {
                                Toast.makeText(context, "Changing...", Toast.LENGTH_LONG).show();
                                firebaseFirestore.collection("STUDENT")
                                        .document(studentDetailHolder.getStudentId())
                                        .update("student_password", newPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                blockScreen(false);
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, "Password Changed", Toast.LENGTH_LONG).show();
                                                    new SharedPrefLoginInfo(context).setUserIdPassword(studentDetailHolder.getStudentId(), newPassword); //set New Password in sharedPref
                                                }
                                                else
                                                    Toast.makeText(context, "Password Change Operation Failed", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(context, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                blockScreen(false);
                                                Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
                                                Log.d("LOOOG", Log.getStackTraceString(e));
                                            }
                                        });
                            } else {
                                blockScreen(false);
                                Toast.makeText(context, "Old Password Not Correct", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        blockScreen(false);
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
                        Log.d("LOOOG", Log.getStackTraceString(e));
                    }
                });
    }

    void logout() {

        SharedPrefLoginInfo sharedPrefLoginInfo = new SharedPrefLoginInfo(getApplicationContext());
        sharedPrefLoginInfo.clearUserIdNameDetail();

//        File filePath = new File(getExternalCacheDir(), "credential.sys");
//        //check if login filePath exist or not if exist st udent directly login without entering login id or login name
//        try {
//            if(filePath.exists()){
//                Toast.makeText(studcontainer_StudentMainContainerActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
//                filePath.delete();
//            }
//        } catch (Exception e) {
//            Log.d("Looog", Log.getStackTraceString(e));
//        }
        Intent transferTologin = new Intent(studcontainer_StudentMainContainerActivity.this, login_MainActivity.class);
        startActivity(transferTologin);
        studcontainer_StudentMainContainerActivity.this.finish();
        StudentDetailHolder.clearStudentData();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(studcontainer_StudentMainContainerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            studcontainer_StudentMainContainerActivity.this.finish();
        }
    }

    class OnClickOption implements View.OnClickListener {

        FragmentManager fragmentManager = studcontainer_StudentMainContainerActivity.fragmentManager;

        @Override
        public void onClick(View view) {
//            check which View Is Clicked and set fragment accordingly

            int clickedViewId = view.getId();
            switch (clickedViewId) {
                case R.id.profileImageView:
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new option_StudentProfileFragment()).commit();
                    break;
                case R.id.timetableImageView:
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new option_StudentTimeTableFragment()).commit();
                    break;
                case R.id.noteImageView:
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new option_StudentNoteFragment()).commit();
                    break;
                case R.id.messageImageView:
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainer, new option_StudentMessageFragment()).commit();
                    break;
            }

            selectedOptionDisabler(view);
        }

        //        disable givenParameter View
        public void selectedOptionDisabler(View selectedView) {
            optionList.add(profileOption);
            optionList.add(timetableOption);
            optionList.add(notesOption);
            optionList.add(messageOption);

            for (ImageView imageView : optionList) {
                if (imageView.getId() == selectedView.getId()) {
                    imageView.setEnabled(false);
                } else {
                    imageView.setEnabled(true);
                }
            }
        }
    }

    //    block whole screen for processing
    public void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);
        ProgressBar progressBar = findViewById(R.id.submitProgressBar);

        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
