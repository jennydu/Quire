package com.facebook.quire.models;

import android.util.Log;

import com.facebook.quire.CommentsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class Comment {
    private String text;
    private String quireId;
    private String userId;
    private String createdAt;
    private long timestamp;
    private User user;
    private String commentId;
    private long invertedTimestamp;

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setQuireId(String quireId) {
        this.quireId = quireId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setInvertedTimestamp(long invertedTimestamp) {
        this.invertedTimestamp = invertedTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(this.timestamp*1000));
        setCreatedAt(date);
    }

    public User getUser() {
        return user;
    }

    public long getInvertedTimestamp() {
        return invertedTimestamp;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public String getQuireId() {
        return quireId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCommentId() {
        return commentId;
    }

    public Comment() {

    }

    public Comment(String text, String quireId, String userId, String createdAt) {
        this.text = text;
        this.quireId = quireId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static ArrayList<Comment> fromDataSnapshot(DataSnapshot snapshot) {
        ArrayList<Comment> comments = new ArrayList<>();

        for (DataSnapshot item : snapshot.getChildren()) {
            Comment comment = Comment.fromSnapshot(item);
            comments.add(comment);
        }

        return comments;
    }


    public static Comment fromSnapshot(DataSnapshot snapshot) {
        final Comment comment = new Comment();

        comment.setText(snapshot.child("text").getValue().toString());
        comment.setQuireId(snapshot.child("quireId").getValue().toString());
        comment.setTimestamp((long) snapshot.child("timestamp").getValue());
        comment.setUserId(snapshot.child("userId").getValue().toString());

        return comment;
    }

    public static void fromIdDataSnapshot(DataSnapshot snapshot, final CommentsAdapter adapter) {
        final ArrayList<Comment> comments = new ArrayList<>();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        for (DataSnapshot item : snapshot.getChildren()) {
            //Log.d("Key", item.getKey());
            //Log.d("Value", item.getValue().toString());
            //Quire quire = Quire.fromIdSnapshot(item.getKey());
            String commentId = item.getKey();
            mRef.child("comments").child(commentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Comment comment = Comment.fromSnapshot(dataSnapshot);
                    comments.add(comment);
                    adapter.setComments(comments);
                    Log.d("Comment", comment.getText());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
