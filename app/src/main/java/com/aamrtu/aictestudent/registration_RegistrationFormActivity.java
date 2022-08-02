package com.aamrtu.aictestudent;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.regex.Pattern;

public class registration_RegistrationFormActivity extends AppCompatActivity {

    String studName, fatherName, email, studPhone, fatherPhone, password, rePassword, registrationYear, completionYear, course, term, batch;
    EditText studNameEditText, fatherNameEditText, emailEditText, studPhoneEditText, fatherPhoneEditText, passwordEditText, rePasswordEditText, registrationYearEditText, courseTermEditText, batchEditText;
    Button submitButton;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    void setFontSize() {
        TextView studNameTextView, fatherNameTextView, emailTextView, studPhoneTextView, fatherPhoneTextView, passwordTextView, rePasswordTextView, registrationYearTextView, courseTermTextView, batchTextView;

        studNameTextView = findViewById(R.id.studentNameTextView);
        fatherNameTextView = findViewById(R.id.studentFatherNameTextView);
        emailTextView = findViewById(R.id.studentEmailTextView);
        studPhoneTextView = findViewById(R.id.studentPhonenoTextView);
        fatherPhoneTextView = findViewById(R.id.fatherPhonenoTextView);
        passwordTextView = findViewById(R.id.studentPasswordTextView);
        rePasswordTextView = findViewById(R.id.studentReenterPasswordTextView);
        registrationYearTextView = findViewById(R.id.studentRegistrationYearTextView);
        courseTermTextView = findViewById(R.id.studentCourseTermTextView);
        batchTextView = findViewById(R.id.studentBatchTextView);

        float textSize = fatherPhoneTextView.getTextSize();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fatherPhoneTextView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
        }
        studNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        fatherNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        emailTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        studPhoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        fatherPhoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        passwordTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        rePasswordTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        registrationYearTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        courseTermTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        batchTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_form);

        setFontSize();

        Intent intent = getIntent();
        registrationYear = intent.getStringExtra("registrationYear");
        completionYear = "pursuing";
        course = intent.getStringExtra("course");
        term = intent.getStringExtra("term");
        batch = intent.getStringExtra("batch");

        studNameEditText = findViewById(R.id.studentName);
        fatherNameEditText = findViewById(R.id.studentFatherName);
        emailEditText = findViewById(R.id.studentEmail);
        studPhoneEditText = findViewById(R.id.studentPhoneno);
        fatherPhoneEditText = findViewById(R.id.fatherPhoneno);
        passwordEditText = findViewById(R.id.studentPassword);
        rePasswordEditText = findViewById(R.id.studentReenterPassword);
        courseTermEditText = findViewById(R.id.studentCourseTerm);
        registrationYearEditText = findViewById(R.id.studentRegistrationYear);
        batchEditText = findViewById(R.id.studentBatch);
        submitButton = findViewById(R.id.registrationFormSubmit);


        setDefaultFields();
        submitButton.setOnClickListener(new SubmitRegistrationForm());
    }

    void setDefaultFields() {
        registrationYearEditText.setText(registrationYear);
        courseTermEditText.setText((course + " " + term).toUpperCase());
        batchEditText.setText(batch);
    }

    class SubmitRegistrationForm implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                studName = studNameEditText.getText().toString().trim().toLowerCase();
                fatherName = fatherNameEditText.getText().toString().trim().toLowerCase();
                email = emailEditText.getText().toString().trim();
                studPhone = studPhoneEditText.getText().toString().trim();
                fatherPhone = fatherPhoneEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();
                rePassword = rePasswordEditText.getText().toString().trim();
            } catch (NullPointerException e) {
                Toast.makeText(registration_RegistrationFormActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
                return;
            }

            if (!(validateRegistrationDetail())) {
                return;
            }

            try {
                InputMethodManager hideKeyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                getCurrentFocus().clearFocus();
                hideKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (NullPointerException e) {
            }

            enterRegistrationPassword();
//            {
//
//                HashMap<String, String> studentDetail = new HashMap<>();
//                studentDetail.put("student_name", studName);
//                studentDetail.put("student_father_name", fatherName);
//                studentDetail.put("student_email", email);
//                studentDetail.put("student_mobile", studPhone);
//                studentDetail.put("student_father_mobile", fatherPhone);
//                studentDetail.put("password", password);
//                studentDetail.put("course_name", course);
//                studentDetail.put("term", term);
//                studentDetail.put("batch", batch);
//
//                FirebaseFirestore.getInstance().collection("STUDENT_REGISTRAR")
//                        .document("REGISTRATIONS")
//                        .collection(course + "_" + term + "_" + batch)
//                        .add(studentDetail)
//                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentReference> task) {
//                                if (task.isSuccessful()) {
//                                    Toast.makeText(registration_RegistrationFormActivity.this, "Registered Successfully", Toast.LENGTH_LONG).show();
//                                    finish();
//                                } else {
//                                    blockScreen(false);
//                                    Toast.makeText(registration_RegistrationFormActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        })
//                        .addOnFailureListener(registration_RegistrationFormActivity.this, new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                blockScreen(false);
//                                Toast.makeText(registration_RegistrationFormActivity.this, "No internet Connection", Toast.LENGTH_LONG).show();
//                            }
//                        });
//
//            }
        }
    }

    static class DataValidator {

        static boolean validateName(String name) {
            return Pattern.matches("^[a-zA-Z\\s]*$", name);
        }

        static boolean validateEmail(String email) {
            return Pattern.matches("[_a-zA-Z0-9]+(\\.[A-Za-z0-9]*)*@[A-Za-z0-9]+\\.[A-Za-z0-9]+(\\.[A-Za-z0-9]*)*", email);
        }

        static boolean validatePhone(String phone) {
            // The given argument to compile() method
            // is regular expression. With the help of
            // regular expression we can validate mobile
            // number.
            // 1) Begins with 0 or 91
            // 2) Then contains 7 or 8 or 9.
            // 3) Then contains 9 digits
            return Pattern.matches("(0/91)?[6-9][0-9]{9}", phone);
        }

        static boolean validatePassword(String password) {
            return password.length() >= 4;
        }
    }

    boolean validateRegistrationDetail() {

        boolean allCorrect = true;

        if (studName.isEmpty() || fatherName.isEmpty() || email.isEmpty() || studPhone.isEmpty() || fatherPhone.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            Toast.makeText(registration_RegistrationFormActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
            allCorrect = false;
        }

        if (!DataValidator.validateName(studName)) {
            studNameEditText.setError("Invalid Name");
            allCorrect = false;
        }

        if (!DataValidator.validateName(fatherName)) {
            fatherNameEditText.setError("Invalid Name");
            allCorrect = false;
        }

        if (!DataValidator.validateEmail(email)) {
            emailEditText.setError("Invalid Email");
            allCorrect = false;
        }

        if (!DataValidator.validatePhone(studPhone)) {
            studPhoneEditText.setError("Invalid Phone no.");
            allCorrect = false;
        }

        if (!DataValidator.validatePhone(fatherPhone)) {
            fatherPhoneEditText.setError("Invalid Phone no.");
            allCorrect = false;
        }

        if (!DataValidator.validatePassword(password)) {
            passwordEditText.setError("Minimum 4 character required");
            allCorrect = false;
        } else if (!password.equals(rePassword)) {
            rePasswordEditText.setError("Password Mismatch");
            allCorrect = false;
        }

        return allCorrect;


    }

    public void enterRegistrationPassword() {
        final EditText dialogPasswordEditText = new EditText(registration_RegistrationFormActivity.this);
        dialogPasswordEditText.setMaxLines(1);
        dialogPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        dialogPasswordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dialogPasswordEditText.setHint("Enter password");

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(registration_RegistrationFormActivity.this)
                .setView(dialogPasswordEditText)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String studentEnteredPassword = dialogPasswordEditText.getText().toString();
                        if (studentEnteredPassword.isEmpty()) {
                            Toast.makeText(getBaseContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkRegistrationPassword(studentEnteredPassword);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        alertBuilder.create().show();
    }

    void checkRegistrationPassword(final String studentEnteredPassword) {
        blockScreen(true);
        firebaseFirestore.collection("STUDENT_REGISTRAR")
                .document("detail")
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.isSuccessful()) {
                            blockScreen(false);
                            Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentSnapshot registrationData = task.getResult();
                        String registrationPassword = registrationData.get("password").toString();
                        if (registrationPassword.equals(studentEnteredPassword)) {
                            Toast.makeText(getBaseContext(), "Submitting...", Toast.LENGTH_SHORT).show();
                            generateIdAndUploadRegistrationData();
                        } else {
                            blockScreen(false);
                            enterRegistrationPassword();
                            Toast.makeText(getBaseContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(registration_RegistrationFormActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                        Log.d("Loog", Log.getStackTraceString(e));
                        blockScreen(false);
                    }
                });
    }

    public void generateIdAndUploadRegistrationData() {
        final DocumentReference docReference = FirebaseFirestore.getInstance().collection("STUDENT_REGISTRAR").document("lastAssignedId");
        docReference.update("lastID", FieldValue.increment(1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            docReference.get(Source.SERVER)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                final String generatedId = "arya" + task.getResult().get("lastID").toString();
                                                firebaseFirestore.collection("STUDENT")
                                                        .document(generatedId)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.getResult().exists()) {
                                                                    generateIdAndUploadRegistrationData();
                                                                    return;
                                                                } else {
                                                                    {
                                                                        HashMap<String, String> studentDetail = new HashMap<>();
                                                                        studentDetail.put("student_id", generatedId);
                                                                        studentDetail.put("student_name", studName);
                                                                        studentDetail.put("student_father_name", fatherName);
                                                                        studentDetail.put("student_email", email);
                                                                        studentDetail.put("student_mobile", studPhone);
                                                                        studentDetail.put("student_father_mobile", fatherPhone);
                                                                        studentDetail.put("student_password", password);
                                                                        studentDetail.put("registration_year", registrationYear);
                                                                        studentDetail.put("completion_year", completionYear);
                                                                        studentDetail.put("course_name", course);
                                                                        studentDetail.put("term", term);
                                                                        studentDetail.put("batch", batch);

                                                                        firebaseFirestore.collection("STUDENT")
                                                                                .document(generatedId)
                                                                                .set(studentDetail)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        blockScreen(false);
                                                                                        if (task.isSuccessful()) {

                                                                                            {// add student name and id it is only for future searching purpose
//                                                                                                HashMap<String, String> studIdNameMap = new HashMap<>();
//                                                                                                studIdNameMap.put(studName);
                                                                                                firebaseFirestore.collection("STUDENT")
                                                                                                        .document("StudentNameData")
                                                                                                        .update(studName, "");
                                                                                            }

                                                                                            Toast.makeText(registration_RegistrationFormActivity.this, "Registered Successfully", Toast.LENGTH_LONG).show();
                                                                                            AlertDialog.Builder alert = new AlertDialog.Builder(registration_RegistrationFormActivity.this)
                                                                                                    .setCancelable(false)
                                                                                                    .setTitle("Registered Successfully")
                                                                                                    .setMessage("Registration ID: " + generatedId)
                                                                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                                            setLoginDetailSharedPreference(generatedId, password);
                                                                                                            dialog.dismiss();
                                                                                                            finish();
                                                                                                        }
                                                                                                    });
                                                                                            alert.show();

                                                                                        } else {
                                                                                            Toast.makeText(registration_RegistrationFormActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(registration_RegistrationFormActivity.this, new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        blockScreen(false);
                                                                                        Toast.makeText(registration_RegistrationFormActivity.this, "No internet Connection", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                });

                                                                    }
                                                                }
                                                            }
                                                        }).addOnFailureListener(registration_RegistrationFormActivity.this, new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                                        blockScreen(false);
                                                    }
                                                });
                                                ;
                                            } else {

                                            }
                                        }
                                    }).addOnFailureListener(registration_RegistrationFormActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                    blockScreen(false);
                                }
                            });
                        } else {

                        }
                    }
                }).addOnFailureListener(registration_RegistrationFormActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                Log.d("Loog", Log.getStackTraceString(e));
                blockScreen(false);
            }
        });
    }


    void setLoginDetailSharedPreference(String userId, String userPassword) {
        SharedPrefLoginInfo sharedPrefLoginInfo = new SharedPrefLoginInfo(getApplicationContext());
        sharedPrefLoginInfo.setUserIdPassword(userId, userPassword);
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
