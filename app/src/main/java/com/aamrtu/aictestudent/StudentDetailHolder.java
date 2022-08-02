package com.aamrtu.aictestudent;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public final class StudentDetailHolder {
    private String studentId, studentName, studentFatherName, studentEmail, studentPhone, studentCourse, studentTerm, studentBatch;
    private DocumentSnapshot studentFireBaseInfo;
    private static StudentDetailHolder studentDetailholderReference;
    private ArrayList<String> subjectListInCourse = new ArrayList<>();
    private Task<QuerySnapshot> studentBatchAttendanceData;
    private Task<QuerySnapshot> studentMessageData;

    private StudentDetailHolder(){

    }

    public static StudentDetailHolder getReference(){
        if(studentDetailholderReference != null) {
            return studentDetailholderReference;
        }

        studentDetailholderReference = new StudentDetailHolder();
        return studentDetailholderReference;
    }

    //call only when student logout
    public static void clearStudentData(){
        studentDetailholderReference = null;
    }

    private void setStudentDetail(){
        studentId = (""+studentFireBaseInfo.get("student_id"));
        studentName = (""+studentFireBaseInfo.get("student_name"));
        studentFatherName = (""+studentFireBaseInfo.get("student_father_name"));
        studentEmail = (""+studentFireBaseInfo.get("student_email"));
        studentPhone = (""+studentFireBaseInfo.get("student_mobile"));
        studentCourse = (""+studentFireBaseInfo.get("course_name"));
        studentTerm = (""+studentFireBaseInfo.get("term"));
        studentBatch = (""+studentFireBaseInfo.get("batch"));
    }

    void setStudentFireBaseInfo(DocumentSnapshot studInfo){
        studentFireBaseInfo = studInfo;
        if(studentFireBaseInfo != null)
            setStudentDetail();
    }

    public DocumentSnapshot getStudentFireBaseInfo(){
        return studentFireBaseInfo;
    }

    public String getStudentId(){
        return studentId;
    }

    public String getStudentName(){
        return studentName;
    }

    public String getStudentFatherName() {
        return studentFatherName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public String getStudentCourse() {
        return studentCourse;
    }

    public String getStudentTerm() {
        return studentTerm;
    }

    public String getStudentBatch() {
        return studentBatch;
    }

    public ArrayList<String> getSubjectListInCourse() {
        return subjectListInCourse;
    }

    public void setSubjectListInCourse(ArrayList<String> subjectListInCourse) {
        this.subjectListInCourse = subjectListInCourse;
    }

    public Task<QuerySnapshot> getStudentBatchAttendanceData() {
        return studentBatchAttendanceData;
    }

    public void setStudentBatchAttendanceData(Task<QuerySnapshot> studentBatchAttendanceData) {
        this.studentBatchAttendanceData = studentBatchAttendanceData;
    }

    public Task<QuerySnapshot> getStudentMessageData() {
        return studentMessageData;
    }

    public void setStudentMessageData(Task<QuerySnapshot> studentMessageData) {
        this.studentMessageData = studentMessageData;
    }
}
