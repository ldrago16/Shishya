package com.aamrtu.aictestudent;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class option_StudentMessageFragment extends Fragment {


    public option_StudentMessageFragment() {
        // Required empty public constructor
    }

    static View messageView;
    SwipeRefreshLayout swipeRefreshLayout;
    studcontainer_StudentMainContainerActivity containerActivityContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        containerActivityContext = (studcontainer_StudentMainContainerActivity) container.getContext();
        messageView = inflater.inflate(R.layout.option_fragment_student_message, container, false);

        swipeRefreshLayout = messageView.findViewById(R.id.pullToRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchStudentMessageData();
            }
        });


        if (StudentDetailHolder.getReference().getStudentMessageData() != null)
            studentMessageDataFound(StudentDetailHolder.getReference().getStudentMessageData());
        else {
            containerActivityContext.blockScreen(true);
            fetchStudentMessageData();
        }
        return messageView;
    }

    void fetchStudentMessageData() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        final Task<QuerySnapshot> studentMessageData = firebaseFirestore.collection("STUDENT")
                .document(StudentDetailHolder.getReference().getStudentId())
                .collection("ANNOUNCEMENT")
                .orderBy("time", Query.Direction.DESCENDING)
                .get(Source.SERVER);
        studentMessageData.continueWith(new Continuation<QuerySnapshot, Nullable>() {
            @Override
            public Nullable then(@NonNull Task<QuerySnapshot> task) throws Exception {
                containerActivityContext.blockScreen(false);
                if (!task.isSuccessful() && task.getResult().isEmpty()) {
                    Toast.makeText(containerActivityContext, "Error:No Message Avail", Toast.LENGTH_SHORT).show();
                    return null;
                }
                StudentDetailHolder.getReference().setStudentMessageData(task);
                studentMessageDataFound(task);
                swipeRefreshLayout.setRefreshing(false);
                return null;
            }
        });
        studentMessageData.addOnFailureListener(containerActivityContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                containerActivityContext.blockScreen(false);
                Toast.makeText(containerActivityContext, "No internet connection", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void studentMessageDataFound(Task<QuerySnapshot> task) {
        ArrayList<String> sentByTeacherNameList = new ArrayList<>();
        ArrayList<String> sentTimeList = new ArrayList<>();
        final ArrayList<String> messageList = new ArrayList<>();

        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
            if (documentSnapshot.exists()) {
                sentByTeacherNameList.add("" + documentSnapshot.get("sender"));
                sentTimeList.add("" + documentSnapshot.get("time"));
                messageList.add("" + documentSnapshot.get("text"));
            }
        }

        Button clearAllMessage = messageView.findViewById(R.id.clearAllMessageButton);
        clearAllMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder msgClearAlert = new AlertDialog.Builder(getContext())
                        .setTitle("Warning!")
                        .setMessage("All messages will be cleared")
                        .setPositiveButton("clear", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                for (QueryDocumentSnapshot documentSnapshot : StudentDetailHolder.getReference().getStudentMessageData().getResult()) {
                                    firebaseFirestore.collection("STUDENT")
                                            .document(StudentDetailHolder.getReference().getStudentId())
                                            .collection("ANNOUNCEMENT")
                                            .document(documentSnapshot.getId())
                                            .delete();
                                }
                                fetchStudentMessageData();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                if(!messageList.isEmpty()) {
                    msgClearAlert.create().show();
                }else {
                    Toast.makeText(containerActivityContext, "no message to delete", Toast.LENGTH_LONG).show();
                }
            }
        });

        RecyclerView messageRecyclerView = messageView.findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(containerActivityContext));
        messageRecyclerView.setAdapter(new StudentMessageRecyclerAdapter(sentByTeacherNameList, sentTimeList, messageList));
    }

}

class StudentMessageRecyclerAdapter extends RecyclerView.Adapter<StudentMessageRecyclerAdapter.StudentMessageViewHolder> {

    static ArrayList<String> teacherNameList, timeList, messageList;

    StudentMessageRecyclerAdapter(ArrayList<String> teacherName, ArrayList<String> time, ArrayList<String> message) {
        teacherNameList = teacherName;
        timeList = time;
        messageList = message;
    }

    @NonNull
    @Override
    public StudentMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_message_layout, parent, false);
        return new StudentMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentMessageViewHolder holder, int position) {
        holder.sentByTeacherText.setText(teacherNameList.get(position));
        holder.sentTimeText.setText(timeList.get(position));
        holder.sentMessage.setText(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return teacherNameList.size();
    }

    public void clear(){
        teacherNameList.clear();
        timeList.clear();
        messageList.clear();
        notifyDataSetChanged();
    }

    class StudentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView sentByTeacherText, sentTimeText, sentMessage;

        public StudentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentByTeacherText = itemView.findViewById(R.id.messageSentByName);
            sentTimeText = itemView.findViewById(R.id.noteSentTime);
            sentMessage = itemView.findViewById(R.id.sentMessage);
        }
    }
}
