package com.facebook.quire.models;

import android.util.Log;

import com.facebook.quire.QuiresAdapter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quire {

    // Properties
    private String qid;
    private String questionText;
    private String descriptionText;
    private long timestamp;
    private ArrayList<Choice> choices;
    private User user;
    private String uid;
    private String outcomeText;

    public long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    private long totalVotes;


    public ArrayList<String> getResultPicUrls() {
        return resultPicUrls;
    }

    public void setResultPicUrls(ArrayList<String> resultPicUrls) {
        this.resultPicUrls = resultPicUrls;
    }

    private ArrayList<String> resultPicUrls = new ArrayList<>();

    public ArrayList<String> getPicIds() {
        return picIds;
    }

    public void setPicIds(ArrayList<String> picIds) {
        this.picIds = picIds;
    }

    private ArrayList<String> picIds;






    private String status;
    private String createdAt;
    private long commentCount;
    private String location;
    private long choicesCount;
    private String outcome; // the string id for the final choice
    private int numAnsweredQuires;
    private int numAskedQuires;



    public long getInvertedTimestamp() {
        return invertedTimestamp;
    }

    public void setInvertedTimestamp(long invertedTimestamp) {
        this.invertedTimestamp = invertedTimestamp;
    }

    private long invertedTimestamp;

    // Getters

    public String getOutcome() {
        return outcome;
    }


    public String getQid() {
        return qid;
    }

    public long getTimestamp() {return timestamp;}

    public ArrayList<Choice> getChoices() {
        return choices;
    }

    public int getNumAnsweredQuires() {
        return numAnsweredQuires;
    }

    public int getNumAskedQuires() {
        return numAskedQuires;
    }

    public User getUser() {
        return user;
    }

    public String getUid(){ return uid; }

    public String getQuestionText() {
        return questionText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public long getChoicesCount() { return choicesCount;}

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public long getCommentCount() {
        return commentCount;
    }
    public String getLocation() {
        return location;
    }

    public String getOutcomeText() {
        return outcomeText;
    }

    //Setters
    public void setQid(String qid) {
        this.qid = qid;}

    public void setNumAnsweredQuires() {
        this.numAnsweredQuires = numAnsweredQuires;
    }

    public void setNumAskedQuires() {
        this.numAskedQuires = numAskedQuires;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (this.timestamp * 1000));
        setCreatedAt(date);}

    public void setChoices(ArrayList<Choice> choices) {
        this.choices = choices;
    }

    public void setChoicesCount(long choicesCount) {
        this.choicesCount = choicesCount;}


    public void setUid(String uid) {
        this.uid = uid;}

    public void setQuestionText(String questionText) {
        this.questionText = questionText;}

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;}

    public void setUser(User user) {
        this.user = user;}

    public void setStatus(String status) {
        this.status = status;}

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;}

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;}

    public void setLocation(String location) {
        this.location = location;}

    public void setOutcomeText(String outcomeText) {
        this.outcomeText = outcomeText;
    }

    //blank constructor for Parcelable
    public Quire() {
    }

    public Quire(String question) {
        this.questionText = question;
    }

    public static ArrayList<Quire> fromDataSnapshot(DataSnapshot snapshot) {
        ArrayList<Quire> quires = new ArrayList<>();

        Log.d("lalala", snapshot.getChildrenCount() + "");
//        for (int i = (int) snapshot.getChildrenCount(); i > 0; i--) {
        for (DataSnapshot item : snapshot.getChildren()) {
            Quire quire = Quire.fromSnapshot(item);
            Log.d("Quire", quire.getQuestionText());
            quires.add(quire);
        }

        return quires;
    }


    public static Quire fromSnapshot(DataSnapshot snapshot) {
        final Quire quire = new Quire();

        ArrayList<String> dogs = new ArrayList<>();
        quire.setPicIds(dogs);

        quire.setQuestionText(snapshot.child("questionText").getValue().toString());
        quire.setDescriptionText(snapshot.child("descriptionText").getValue().toString());
        quire.setCommentCount((long) snapshot.child("commentCount").getValue());
        quire.setStatus(snapshot.child("status").getValue().toString());
        String uid = snapshot.child("uid").getValue().toString();
        quire.setUid(uid);
        quire.setTotalVotes((long) snapshot.child("totalVotes").getValue());
        quire.setQid(snapshot.child("qid").getValue().toString());
        quire.setTimestamp((long) snapshot.child("timestamp").getValue());
        quire.setInvertedTimestamp((long) snapshot.child("invertedTimestamp").getValue());

        quire.setChoicesCount((long) snapshot.child("choicesCount").getValue());
        ArrayList<Choice> choices = Choice.fromDataSnapshot(snapshot.child("choices"));

        quire.setChoices(choices);






        if (quire.getStatus().toString().equals("closed")) {
            try {
                quire.setPicIds((ArrayList<String>) snapshot.child("picIds").getValue());
                quire.setOutcome(snapshot.child("outcome").getValue().toString());

                ArrayList<String> resultPicUrls = new ArrayList<>();
                for (DataSnapshot s : snapshot.child("resultPicUrls").getChildren()){
                    resultPicUrls.add(s.getValue().toString());
                }
                quire.setResultPicUrls(resultPicUrls);

            }catch(Exception e){

            }

        }

        return quire;
    }

    public static void fromIdDataSnapshot(DataSnapshot snapshot, final QuiresAdapter adapter) {
        final ArrayList<Quire> quires = new ArrayList<>();

        //DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        for (DataSnapshot item : snapshot.getChildren()) {
            Log.d("Key", item.getKey());
            Log.d("Value", item.getValue().toString());
            //Quire quire = Quire.fromIdSnapshot(item.getKey());
            String qid = item.getKey();

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

            mRef.child("quires").child(qid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Quire quire = Quire.fromSnapshot(dataSnapshot);
                    quires.add(quire);
                    adapter.setQuires(quires);
                    Log.d("Quire", quire.getQuestionText());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}












