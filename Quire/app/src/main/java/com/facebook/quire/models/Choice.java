package com.facebook.quire.models;

import com.google.firebase.database.DataSnapshot;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class Choice {

    private String text;
    private long votes;
    private String cid;
    private String imgSrc;
    private ArrayList<User> users;

    //constructors
    public Choice(String choice) {
        this.text = choice;
    }

    //empty for parcelable
    public Choice() {
    }


    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }


// Choice id: cid
    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }


// Text: text
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


// Number of votes: votes
    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }


// Image source: imgSrc
    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }



    /**
    public static Choice fromJSON(JSONObject json){
        Choice c = new Choice();
        try{
            c.text = json.getString("text");
            c.votes = json.getInt("votes");
            c.cid = json.getString("cid");
//            c.imgSrc = json.getString("imgSrc");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return c;
    }

    // Choice.fromJSONArray([ {...}, {...}, {...} ]) to list
    public static ArrayList<Choice> fromJSONArray(JSONArray jsonArray){
        ArrayList<Choice> choices = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject choiceJson = jsonArray.getJSONObject(i);
                Choice choice = Choice.fromJSON(choiceJson);
                if (choice!= null){
                    choices.add(choice);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return choices;

    }**/

    public static ArrayList<Choice> fromDataSnapshot(DataSnapshot snapshot) {
        ArrayList<Choice> choices = new ArrayList<>();

//        Log.d("lalala", snapshot.getChildrenCount() + "");
//        for (int i = (int) snapshot.getChildrenCount(); i > 0; i--) {
        for (DataSnapshot item : snapshot.getChildren()) {
            Choice choice = Choice.fromSnapshot(item);
            choices.add(choice);
        }
//        }

        return choices;
    }


    public static Choice fromSnapshot(DataSnapshot snapshot) {
        final Choice choice = new Choice();

        choice.setCid(snapshot.child("cid").getValue().toString());
//      choice.setImgSrc(snapshot.child("imgSrc").getValue().toString());
        choice.setText(snapshot.child("text").getValue().toString());
        choice.setVotes((long) snapshot.child("votes").getValue());

        return choice;
    }

}
