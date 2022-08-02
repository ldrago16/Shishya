package com.aamrtu.aictestudent;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.io.File;

public class login_MainActivity extends AppCompatActivity {

    File filePath;
    EditText idEditText, passwordEditText;
    Button submitButton;
    TextView registerStudent;

    SharedPrefLoginInfo sharedPrefLog;

    @Override
    protected void onResume() {
        super.onResume();
        checkUserAlreadyLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        idEditText = findViewById(R.id.idEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        submitButton = findViewById(R.id.submitIdPassword);
        registerStudent = findViewById(R.id.studentRegisterText);


        {
            //remove it after use
            FirebaseFirestore.getInstance().collection("APPSECURITY")
                    .document("security")
                    .get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().exists()) {
                                String key = task.getResult().get("key").toString();
                                if (key.equals(getResources().getString(R.string.key))) {
                                    Toast.makeText(login_MainActivity.this, "WELCOME", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(login_MainActivity.this, "This version no Longer Available", Toast.LENGTH_LONG).show();
                                    System.exit(0);
                                }
                            } else {
                                Toast.makeText(login_MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                                System.exit(0);
                            }
                        }
                    })
                    .addOnFailureListener(login_MainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(login_MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                            System.exit(0);
                        }
                    });
        }

        sharedPrefLog = new SharedPrefLoginInfo(getApplicationContext());
        class OnClickSubmitButton implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                String studId = idEditText.getText().toString().trim();
                String studPassword = passwordEditText.getText().toString().trim();
                checkStudIdPasswordInDataBase(studId, studPassword);
            }
        }
        submitButton.setOnClickListener(new OnClickSubmitButton());

       // FUTURE FEATURE FOR STUDENT REGISTRATION
        class OnRegisterButtonClick implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                checkRegistrationOpenThenOpenActivity();
//                final EditText dialogPasswordEditText = new EditText(login_MainActivity.this);
//                dialogPasswordEditText.setMaxLines(1);
//                dialogPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                dialogPasswordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
//                dialogPasswordEditText.setHint("Enter password");
//                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(login_MainActivity.this)
//                        .setView(dialogPasswordEditText)
//                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                final String studentEnteredPassword = dialogPasswordEditText.getText().toString();
//                                if (studentEnteredPassword.isEmpty()) {
//                                    Toast.makeText(getBaseContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
//                                    return;
//                                }
//                                checkRegistrationPassword(studentEnteredPassword);
//                            }
//                        })
//                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                alertBuilder.create().show();
            }
        }
        registerStudent.setOnClickListener(new OnRegisterButtonClick());


    }

    //FUTURE FEATURE FOR STUDENT REGISTRATION
    void checkRegistrationOpenThenOpenActivity() {
        blockScreen(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("STUDENT_REGISTRAR")
                .document("detail")
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        blockScreen(false);
                        if(!task.isSuccessful()){
                            Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentSnapshot registrationData = task.getResult();

                        try {
                            String registrationStatus = registrationData.get("status").toString();
                            if (registrationStatus.equals("close")) {
                                throw new NullPointerException();
                            }
                            Intent registrationActivity = new Intent(login_MainActivity.this, registration_RegistrationFormActivity.class);
                            registrationActivity.putExtra("course", registrationData.get("course").toString());
                            registrationActivity.putExtra("term", registrationData.get("term").toString());
                            registrationActivity.putExtra("batch", registrationData.get("batch").toString());
                            registrationActivity.putExtra("registrationYear", registrationData.get("registration_year").toString());
                            startActivity(registrationActivity);

                        } catch (NullPointerException e) {
                            Toast.makeText(getBaseContext(), "Registration Closed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                })
                .addOnFailureListener(login_MainActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                        Log.d("Loog", Log.getStackTraceString(e));
                        blockScreen(false);
                    }
                });
    }


    //method check user already login or not by checking the credential file exist in file manager or not.
    void checkUserAlreadyLogin() {

        if(sharedPrefLog.readLoginStatus()){
            String studId = sharedPrefLog.getUserId();
            String studPassword = sharedPrefLog.getUserPassword();
            Log.d("Looog", studId + " " + studPassword);
            checkStudIdPasswordInDataBase(studId, studPassword);    //check if login id or name is in database or not
        }

        /*

        //filePath location used to store student login id and name to make student login until not logout
        filePath = new File(getExternalCacheDir(), "credential.sys");
        //check if login filePath exist or not if exist student directly login without entering login id or login name
        if (filePath.exists()) {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                Scanner sn = new Scanner(fis);
                String studId = sn.nextLine();      //fetch login student id
                String studName = sn.nextLine();    //fetch login student name
                Log.d("Looog", studId + " " + studName);
                checkStudIdPasswordInDataBase(studId, studName);    //check if login id or name is in database or not
            } catch (Exception e) {
                Log.d("Looog", Log.getStackTraceString(e));
            }
        }

        */
    }

    private void checkStudIdPasswordInDataBase(final String id, final String password) {
//        if(!NetworkConnectivityChecker.isNetworkConnected(login_MainActivity.this))
//        {
//            Toast.makeText(login_MainActivity.this, "Check Network Connectivity", Toast.LENGTH_SHORT).show();
//            return;
//        }
        blockScreen(true);
        Log.d("Looog", "Check");
        try {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("STUDENT")
                    .document(id)
                    .get(Source.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                if (!documentSnapshot.exists()) {       //if giving is is not exist in databases
                                    Log.d("Looog", "Invalid ID");
                                    idEditText.setError("Invalid ID");
                                    Toast.makeText(login_MainActivity.this, "Invalid ID", Toast.LENGTH_SHORT).show();
                                } else if(!("pursuing".equals(documentSnapshot.get("completion_year").toString()))){
                                    Toast.makeText(login_MainActivity.this, "Course finished", Toast.LENGTH_SHORT).show();
                                    sharedPrefLog.clearUserIdNameDetail();
                                } else if (!password.equals(documentSnapshot.get("student_password").toString())) {     //if given password is not match with given id
                                    Log.d("Looog", "Incorrect Password");
                                    passwordEditText.setError("Incorrect Password");
                                    Toast.makeText(login_MainActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                } else {    //if id and password match and student data found
                                    Log.d("Looog", "Student FOUND " + documentSnapshot.get("student_name"));
                                    StudentDetailHolder studentDetailHolder = StudentDetailHolder.getReference();
                                    studentDetailHolder.setStudentFireBaseInfo(documentSnapshot);   //store the logged student information

                                    sharedPrefLog.setUserIdPassword(id, password);

//                                try (FileOutputStream fos = new FileOutputStream(filePath, false)) {
//                                    filePath.createNewFile();   //create a file on given path for storing successfully login student id and name
//                                    fos.write(id.getBytes());
//                                    fos.write("\n".getBytes());
//                                    fos.write(name.getBytes());
//                                } catch (Exception e) {
//                                    Log.d("Looog", Log.getStackTraceString(e));
//                                }
                                    Intent intent = new Intent(login_MainActivity.this, studcontainer_StudentMainContainerActivity.class);  //jump on studentPanel
                                    startActivity(intent);
                                    finish();
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(login_MainActivity.this, "error:Some fields is missing from your account",Toast.LENGTH_LONG).show();
                            }
                            blockScreen(false);
                        }
                    })
                    .addOnFailureListener(login_MainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(login_MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder noAlertBuilder = new AlertDialog.Builder(login_MainActivity.this);
                            AlertDialog alertDialog = noAlertBuilder
                                    .setMessage("Turn on mobile net or connect to wifi")
                                    .setCancelable(false)
                                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                dialog.dismiss();
                                                login_MainActivity.this.finish();
                                            }
                                            return true;
                                        }
                                    })
                                    .setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            login_MainActivity.this.checkUserAlreadyLogin();
                                            dialog.dismiss();
                                        }
                                    }).create();
                            alertDialog.show();


                            Log.d("Looog", "Fail " + Log.getStackTraceString(e));
                            blockScreen(false);
                        }
                    });
        } catch (Exception e) {
            idEditText.setError("Invalid ID");
            passwordEditText.setError("Invalid Password");
            Log.d("Looog", "Exception in checkStudIdPasswordInDataBase " + Log.getStackTraceString(e));
            blockScreen(false);
        }
    }

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
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
