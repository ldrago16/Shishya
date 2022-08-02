package com.aamrtu.aictestudent;


import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class option_StudentNoteFragment extends Fragment {


    public option_StudentNoteFragment() {
        // Required empty public constructor
    }


    static View noteView;
    Spinner subjectSpin;    //subject spinner
    ArrayList<String> tempSubjectList;
    studcontainer_StudentMainContainerActivity containerActivityContext;    //context of main container activity which hold this fragment
    StudentDetailHolder studentStoredDetail = StudentDetailHolder.getReference();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        noteView = inflater.inflate(R.layout.option_fragment_student_note, container, false);
        containerActivityContext = (studcontainer_StudentMainContainerActivity) inflater.getContext();
        subjectSpin = noteView.findViewById(R.id.selectSubjectNoteSpinner);

        if (ActivityCompat.checkSelfPermission( containerActivityContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(containerActivityContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if(studentStoredDetail.getSubjectListInCourse().size() != 0) {
            setSubjectSpinnerItem(studentStoredDetail.getSubjectListInCourse());
        }
        else {
            fetchSubjectInCourse(studentStoredDetail.getStudentCourse(), studentStoredDetail.getStudentTerm());
        }
        return noteView;
    }

//    fetch all subject in course in particular term
    void fetchSubjectInCourse(String course, String term){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> task =  firebaseFirestore.collection("SUBJECT")
                .whereEqualTo("course_name", course)
                .whereEqualTo("term", term)
                .get(Source.SERVER);
        task.continueWith(new Continuation<QuerySnapshot, Nullable>() {
            @Override
            public Nullable then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if(!task.isSuccessful() && task.getResult() == null)  {
                    Toast.makeText(containerActivityContext, "Error:No Notes Avail", Toast.LENGTH_SHORT).show();
                    return null;
                }
                ArrayList<String> subjectList = new ArrayList<>();
                for(QueryDocumentSnapshot subjectItem : task.getResult()){
                    if(!(""+subjectItem.get("subject_name")).equals("null"))
                        subjectList.add(""+subjectItem.get("subject_name"));
                }
                studentStoredDetail.setSubjectListInCourse(subjectList);
                return setSubjectSpinnerItem(subjectList);
            }
        });
        task.addOnFailureListener(containerActivityContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Looog", Log.getStackTraceString(e));
                Toast.makeText(containerActivityContext, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    Nullable setSubjectSpinnerItem(ArrayList<String> subjectList){
        tempSubjectList = new ArrayList<>();
        tempSubjectList.addAll(subjectList);
        tempSubjectList.add(0, "SELECT SUBJECT");
        tempSubjectList.add(tempSubjectList.size(), "other");
        //ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(containerActivityContext, android.R.layout.simple_spinner_dropdown_item,tempSubjectList);
        subjectSpin.setAdapter(new MySpinnerAdapter(containerActivityContext,tempSubjectList));
        //subjectSpin.setAdapter(subjectAdapter);
        subjectSpin.setOnItemSelectedListener(new SubjectSpinnnerOnSelectListener());
        return null;
    }

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        containerActivityContext.blockScreen(state);
    }

    class SubjectSpinnnerOnSelectListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, final View view, int position, long id) {
            if(!NetworkConnectivityChecker.isNetworkConnected(containerActivityContext))
            {
                Toast.makeText(containerActivityContext, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            if(position == 0){
                recyclerViewPopulateWithNotesData(new ArrayList<HashMap<String, String>>(), "");
                return;
            }
            blockScreen(true);
            String courseTerm = studentStoredDetail.getStudentCourse()+studentStoredDetail.getStudentTerm();
            final String subject = tempSubjectList.get(position);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Task<QuerySnapshot> getNoteTask = firestore.collection("NOTES")
                    .document(courseTerm)
                    .collection(subject)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .get();

            getNoteTask.continueWith(new Continuation<QuerySnapshot, Nullable>() {
                @Override
                public Nullable then(@NonNull Task<QuerySnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        Toast.makeText(containerActivityContext, "No Notes Found", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    ArrayList<HashMap<String,String>> tempNotesDetail = new ArrayList<>();

                    for(QueryDocumentSnapshot noteSnap : task.getResult()){
                        if(!noteSnap.exists())
                            continue;

                        HashMap<String,String> noteData = new HashMap<>();
                        noteData.put("name", ""+noteSnap.get("note_name"));
                        noteData.put("description", ""+noteSnap.get("description"));
                        noteData.put("teacher", ""+noteSnap.get("teacher_name"));
                        noteData.put("time", ""+noteSnap.get("time"));
                        noteData.put("url", ""+noteSnap.get("url"));
                        noteData.put("fileExtension", "."+noteSnap.get("file_extension"));
                        noteData.put("fileContentType", ""+noteSnap.get("content_type"));
                        tempNotesDetail.add(noteData);
                    }

                   recyclerViewPopulateWithNotesData(tempNotesDetail, subject);
                    return null;
                }
            });

            getNoteTask.addOnFailureListener(containerActivityContext, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(containerActivityContext, "No Notes Found", Toast.LENGTH_SHORT).show();
                    Log.d("Looog", Log.getStackTraceString(e));
                }
            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
    public void recyclerViewPopulateWithNotesData(ArrayList<HashMap<String,String>> fetchedNotesDetail, String subject){
        RecyclerView noteRecycleView = noteView.findViewById(R.id.notesRecyclerView);
        noteRecycleView.setLayoutManager(new LinearLayoutManager(containerActivityContext));
        noteRecycleView.setAdapter(new StudentNoteRecyclerAdapter(fetchedNotesDetail, containerActivityContext, subject));
        blockScreen(false);
    }
}

class StudentNoteRecyclerAdapter extends RecyclerView.Adapter<StudentNoteRecyclerAdapter.StudentNoteViewholder>{

    private ArrayList<HashMap<String,String>> notesData;
    private studcontainer_StudentMainContainerActivity containerActivityContext;
    private String subject;

    StudentNoteRecyclerAdapter(ArrayList<HashMap<String,String>> tempNotesData, studcontainer_StudentMainContainerActivity context, String selectedSubject){
        notesData = tempNotesData;
        containerActivityContext = context;
        subject = selectedSubject;
    }
    @NonNull
    @Override
    public StudentNoteViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_note_layout, parent,false);
        return new StudentNoteViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentNoteViewholder holder, final int position) {
        HashMap<String, String> noteDataAtPosition = notesData.get(position);
        final String name = noteDataAtPosition.get("name");
        final String time = noteDataAtPosition.get("time");
        final String description = noteDataAtPosition.get("description");
        final String teacher = noteDataAtPosition.get("teacher");
        final String url = noteDataAtPosition.get("url");
        final String file_extension = noteDataAtPosition.get("fileExtension");
        final String file_contentType = noteDataAtPosition.get("fileContentType");

        holder.name.setText(name);
        holder.time.setText(time);
        holder.description.setText(description);
        holder.teacher.setText(teacher);

        holder.downloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(containerActivityContext, "downloading...", Toast.LENGTH_SHORT).show();
                final Uri noteUri = Uri.parse(url);



                DownloadManager downloadManager = (DownloadManager) containerActivityContext.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(noteUri);
                request.setTitle(name);
                request.setDescription(description);
                request.setMimeType(file_contentType);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                final String fileName = name +"_by_"+ teacher + file_extension;

                StudentDetailHolder studentDetails = StudentDetailHolder.getReference();
                String courseTerm = studentDetails.getStudentCourse()+studentDetails.getStudentTerm();

                String filePath = courseTerm+"/"+subject+"/"+fileName;

                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS ,filePath);
               // final File excelDestination = new File(containerActivityContext.getExternalFilesDir("Excel")+"/"+dateSpinnerSelected+ "_" +monthSpinnerSelected+ "_" + yearSpinnerSelected, fileName);
                //request.setDestinationUri(Uri.fromFile(excelDestination));
                downloadManager.enqueue(request);
                Toast.makeText(containerActivityContext, "Download location: "+new File(Environment.DIRECTORY_DOWNLOADS, filePath).toString(), Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return notesData.size();
    }

    public class StudentNoteViewholder extends RecyclerView.ViewHolder{

        TextView name, time, description, teacher;
        ImageView downloadIcon;

        public StudentNoteViewholder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.noteName);
            time = itemView.findViewById(R.id.noteTime);
            downloadIcon = itemView.findViewById(R.id.downloadNoteIcon);
            description = itemView.findViewById(R.id.noteDescription);
            teacher = itemView.findViewById(R.id.noteTeacherName);
        }
    }
}
