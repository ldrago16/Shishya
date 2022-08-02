package com.aamrtu.aictestudent;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class option_StudentProfileFragment extends Fragment {


    public option_StudentProfileFragment() {
        // Required empty public constructor
    }

    static View inflatedProfileView;
    studcontainer_StudentMainContainerActivity containerActivityContext;   //context of main container activity which hold this fragment
    static option_StudentProfileFragment profileFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedProfileView = inflater.inflate(R.layout.option_fragment_student_profile, container, false);
        containerActivityContext = (studcontainer_StudentMainContainerActivity) inflater.getContext();
        if (StudentDetailHolder.getReference().getStudentBatchAttendanceData() == null) {
            fetchAllStudentAttendance();
        } else {
            calculateAndSetStudentAttendanceRank(StudentDetailHolder.getReference().getStudentBatchAttendanceData());
        }
        profileFragment = option_StudentProfileFragment.this;
        loadStudentPersonalDetail(StudentDetailHolder.getReference());
        return inflatedProfileView;
    }
    // fetch student attandance for calculate rank

    void fetchAllStudentAttendance() {
        final StudentDetailHolder studentDetail = StudentDetailHolder.getReference();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> task = firebaseFirestore.collection("ATTENDANCE")
                .whereEqualTo("course_name", studentDetail.getStudentCourse())
                .whereEqualTo("term", studentDetail.getStudentTerm())
                .whereEqualTo("batch", studentDetail.getStudentBatch())
                .orderBy("attend_rank_decider", Query.Direction.DESCENDING)
                .get(Source.SERVER);

        task.continueWith(new Continuation<QuerySnapshot, Nullable>() {
            @Override
            public Nullable then(@NonNull Task<QuerySnapshot> task) throws Exception {
                return calculateAndSetStudentAttendanceRank(task);
            }
        });

        task.addOnFailureListener(containerActivityContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ShowAttendanceRankActivity.setRefreshingFalse();    //when ShowAttendanceRankActivity class request for refresh or fetch studend attendance data so after fetching done setRefreshing false
                Toast.makeText(containerActivityContext, "No internet connection", Toast.LENGTH_SHORT).show();
                Log.d("Looog", Log.getStackTraceString(e));
            }
        });
    }

    Nullable calculateAndSetStudentAttendanceRank(@NonNull Task<QuerySnapshot> task) {
        TextView studentRankView = inflatedProfileView.findViewById(R.id.showStudentRank);
        TextView totalStudentView = inflatedProfileView.findViewById(R.id.showTotalStudent);
        if (!task.isSuccessful()) {
            studentRankView.setText("Not Avail Now");
            return null;
        }

        StudentDetailHolder.getReference().setStudentBatchAttendanceData(task);
        ShowAttendanceRankActivity.setRefreshingFalse();        //when ShowAttendanceRankActivity class request for refresh or fetch studend attendance data so after fetching done setRefreshing false
        int prevAttendRankDecider = Integer.MIN_VALUE;
        int rankGenerator = 0;
        int position = 0;
        int studentRank = 0;

        String rankDeciderFieldName = "attend_rank_decider";
        String totalStudents = String.valueOf(task.getResult().size());
        for (QueryDocumentSnapshot studentData : task.getResult()) {
            if ((studentData.get(rankDeciderFieldName)).equals("null") && !studentData.exists()) {
                continue;
            }

            int currentAttendRankDecider = Integer.parseInt(("" + studentData.get(rankDeciderFieldName)));
            Log.d("Looog", "" + currentAttendRankDecider);
            if (currentAttendRankDecider != prevAttendRankDecider) {
                rankGenerator = position;
                rankGenerator++;
                prevAttendRankDecider = currentAttendRankDecider;
            }
            studentRank = rankGenerator;
            position++;
            if ((studentData.get("student_name").toString().equals(StudentDetailHolder.getReference().getStudentName()))) {
                break;
            }
        }
        switch (studentRank) {
            case 0:
                studentRankView.setText("\u2639");  //sad emogi unicode
                break;
            case 1:
                studentRankView.setText("1st \uD83E\uDD47");
                break;
            case 2:
                studentRankView.setText("2nd \uD83E\uDD48");
                break;
            case 3:
                studentRankView.setText("3rd \uD83E\uDD49");
                break;
            default:
                studentRankView.setText(String.valueOf(studentRank));
                break;
        }
        totalStudentView.setText(totalStudents);
        if (studentRank > 0) {
            calculateAndSetTopThreeAttendees(task);
        }
        return null;
    }

    void calculateAndSetTopThreeAttendees(@NonNull Task<QuerySnapshot> task) {
        ArrayList<DocumentSnapshot> topThree = new ArrayList<>(3);
        int topThreeAttendCounter = 0;
        String rankField = MyCalendar.getCalendarMonth() + "_" + MyCalendar.getCalendarYear() + "_attend_rank_decider";
        for (QueryDocumentSnapshot studentData : task.getResult()) {
            if (topThreeAttendCounter < 3) {
                topThree.add(studentData);
                topThreeAttendCounter++;
            } else {
                break;
            }
        }
        TextView first = inflatedProfileView.findViewById(R.id.attendFirstRankText);
        TextView second = inflatedProfileView.findViewById(R.id.attendSecondRankText);
        TextView third = inflatedProfileView.findViewById(R.id.attendThirdRankText);

        try {
            first.setText(("" + topThree.get(0).get("student_name")).toUpperCase());
            second.setText(("" + topThree.get(1).get("student_name")).toUpperCase());
            third.setText(("" + topThree.get(2).get("student_name")).toUpperCase());

            Button allAttendRank = inflatedProfileView.findViewById(R.id.allAttendRankButton);
            allAttendRank.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent allAttendRankIntent = new Intent(containerActivityContext, ShowAttendanceRankActivity.class);
                    containerActivityContext.startActivity(allAttendRankIntent);
                }
            });
        } catch (IndexOutOfBoundsException e) {
        }
    }


    //set Logged student personal Profile Detail like name, fathername etc.
    void loadStudentPersonalDetail(StudentDetailHolder storedStudentDetail) {
        EditText studName = inflatedProfileView.findViewById(R.id.showStudentName);
        EditText id = inflatedProfileView.findViewById(R.id.showStudentId);
        EditText fatherName = inflatedProfileView.findViewById(R.id.showStudentFatherName);
        EditText email = inflatedProfileView.findViewById(R.id.showStudentEmail);
        EditText phone = inflatedProfileView.findViewById(R.id.showStudentPhoneno);
        EditText courseYear = inflatedProfileView.findViewById(R.id.showStudentCourse);
        EditText batch = inflatedProfileView.findViewById(R.id.showStudentBatch);

        studName.setText(storedStudentDetail.getStudentName().toUpperCase());
        id.setText(storedStudentDetail.getStudentId());
        fatherName.setText(storedStudentDetail.getStudentFatherName().toUpperCase());
        email.setText(storedStudentDetail.getStudentEmail());
        phone.setText(storedStudentDetail.getStudentPhone());
        courseYear.setText((storedStudentDetail.getStudentCourse() + " " + storedStudentDetail.getStudentTerm()).toUpperCase());
        batch.setText(storedStudentDetail.getStudentBatch());
    }

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        containerActivityContext.blockScreen(state);
    }
}
