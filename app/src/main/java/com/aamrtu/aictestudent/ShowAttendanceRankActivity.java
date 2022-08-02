package com.aamrtu.aictestudent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShowAttendanceRankActivity extends AppCompatActivity {

    static SwipeRefreshLayout swipeRefreshLayout;
    static ShowAttendanceRankActivity showAttendanceRankRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendance_rank);

        showAttendanceRankRef = ShowAttendanceRankActivity.this;
        swipeRefreshLayout = findViewById(R.id.pullToRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                option_StudentProfileFragment.profileFragment.fetchAllStudentAttendance();
            }
        });

        calculateAndSetTopAttendees(StudentDetailHolder.getReference().getStudentBatchAttendanceData());
    }

    static void setRefreshingFalse(){
        if(swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(false);
            showAttendanceRankRef.calculateAndSetTopAttendees(StudentDetailHolder.getReference().getStudentBatchAttendanceData());
        }
    }

    void calculateAndSetTopAttendees(@NonNull Task<QuerySnapshot> task){
        int prevAttendRankDecider = Integer.MIN_VALUE;
        int rankGenerator = 0;
        int position = 0;
        int studentRank = 0;
        ArrayList<String> tempStudentNameList = new ArrayList<>();
        ArrayList<String> tempStudentRankList = new ArrayList<>();

        String rankDeciderFieldName = "attend_rank_decider";
        for (QueryDocumentSnapshot studentData : task.getResult()) {
//            if ((studentData.get(rankDeciderFieldName)).equals("null")) {
//                continue;
//            }
            int currentAttendRankDecider = Integer.parseInt(("" + studentData.get(rankDeciderFieldName)));
            if (currentAttendRankDecider != prevAttendRankDecider) {
                rankGenerator = position;
                rankGenerator++;
                prevAttendRankDecider = currentAttendRankDecider;
            }
            studentRank = rankGenerator;
            position++;
            tempStudentNameList.add(studentData.get("student_name").toString());
            tempStudentRankList.add(String.valueOf(studentRank));
        }
        RecyclerView topAttendeesRecyclerView = findViewById(R.id.topAttendeesList);
        topAttendeesRecyclerView.setLayoutManager(new LinearLayoutManager(ShowAttendanceRankActivity.this));
        topAttendeesRecyclerView.setAdapter(new ShowAttendRankAdapter(tempStudentNameList, tempStudentRankList));
    }
}


class ShowAttendRankAdapter extends RecyclerView.Adapter<ShowAttendRankAdapter.AttendRankAdapterViewHolder>{
    ArrayList<String> studentNameList;
    ArrayList<String> studentRankList;

    ShowAttendRankAdapter(ArrayList<String> studentName, ArrayList<String> rank){
        studentNameList = studentName;
        studentRankList = rank;
    }

    @NonNull
    @Override
    public AttendRankAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_all_attendees_rank_list_layout,parent, false);
        return new AttendRankAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendRankAdapterViewHolder holder, int position) {
        holder.studentSrNo.setText(String.valueOf(position+1));
        holder.studentName.setText(studentNameList.get(position).toUpperCase());
        holder.studentRank.setText(studentRankList.get(position));
    }

    @Override
    public int getItemCount() {
        return studentNameList.size();
    }

    class AttendRankAdapterViewHolder extends RecyclerView.ViewHolder{
        TextView studentSrNo, studentName, studentRank;
        public AttendRankAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            studentSrNo = itemView.findViewById(R.id.srNo);
            studentName = itemView.findViewById(R.id.studentName);
            studentRank = itemView.findViewById(R.id.studentRank);
        }
    }
}