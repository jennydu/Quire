package com.facebook.quire.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.activities.LoginActivity;
import com.facebook.quire.models.Choice;
import com.facebook.quire.models.Quire;
import com.facebook.quire.models.User;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * Created by jenniferdu on 7/25/16.
 */
public class ComposeOptionsFragment extends Fragment{

    LinearLayout llQuire;
    TextView tvQuestion;
    LinearLayout llChoices;
    LinearLayout llOptionsActions;
    LinearLayout llBtnAddDescription;
    ImageView ivAddDescription;
    TextView tvAddDescription;
    LinearLayout llBtnShare;
    ImageView ivShare;
    TextView tvShare;
            TextView tvSubmit;
            EditText etDescription;
    FrameLayout flEdit;
    ImageView ivAdd;
    TextView tvDescription;


    String questionText;
    ArrayList<String> choicesText;
    ArrayList<String> choices2;
    Quire newQuire;

    private String mUserId;
    private String mQuireId;

    private String userUrl; // = Constants.FIREBASE_URL + "/users/" + mUserId;
    private String userQuiresUrl;// = Constants.FIREBASE_URL + "/users/" + mUserId + "/quires/";
    private String quiresUrl; // = Constants.FIREBASE_URL + "/quires/";
    private String choicesUrl;

    private ArrayList<EditText> allChoiceETs = new ArrayList<EditText>();
    private ArrayList<ImageView> allChoiceIVs = new ArrayList<ImageView>();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_compose_options, container, false);

        ButterKnife.bind(getActivity());


//check Authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        try {
            mUserId = mAuth.getCurrentUser().getUid(); // gets the user id
        } catch (Exception e) {
            loadLoginView();
        }

        tvDescription = (TextView) v.findViewById(R.id.tvDescription);
        llChoices = (LinearLayout) v.findViewById(R.id.llChoices);
        llQuire = (LinearLayout) v.findViewById(R.id.llQuire);
        tvQuestion = (TextView) v.findViewById(R.id.tvQuestion);
        llOptionsActions = (LinearLayout) v.findViewById(R.id.llOptionsActions);
        llBtnAddDescription = (LinearLayout) v.findViewById(R.id.llBtnAddDescription);
        ivAddDescription = (ImageView) v.findViewById(R.id.ivAddDescription);
        tvAddDescription = (TextView) v.findViewById(R.id.tvAddDescription);
        etDescription = (EditText) v.findViewById(R.id.etDescription);

        flEdit = (FrameLayout) v.findViewById(R.id.flEdit);
        ivAdd = (ImageView) v.findViewById(R.id.ivAdd);

        quiresUrl = Constants.FIREBASE_URL + "/quires";
        userQuiresUrl = Constants.FIREBASE_URL + "/users/" + mUserId + "/quires/";
        choicesUrl = Constants.FIREBASE_URL + "/choices";


// display question
        questionText = getArguments().get("question").toString();
        tvQuestion.setText(questionText);

// load the choices
        choicesText = (ArrayList<String>) getArguments().get("choiceArrayList");
        choices2 = new ArrayList<>();

        for (String s : choicesText){
            if (s.trim().length()>0) {
                choices2.add(s);
                final LinearLayout llNew = (LinearLayout) inflater.inflate(R.layout.compose_choice, null);
                TextView choice = (TextView) llNew.findViewById(R.id.tvNew);
                ImageView ivRemove = (ImageView) llNew.findViewById(R.id.ivRemove);
                final String theText = s;

                choice.setText(s);
                llChoices.addView(llNew);

                ivRemove.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        llNew.setVisibility(View.GONE);
                        choices2.remove(theText);
                    }
                });
            }
        }



// set on click listener for adding a description
        llBtnAddDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flEdit.setVisibility(View.VISIBLE);
                ivAddDescription.setVisibility(View.GONE);
                tvAddDescription.setVisibility(View.GONE);
            }
        });

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide keyboard
                View v = getActivity().getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                flEdit.setVisibility(View.GONE);

                tvAddDescription.setVisibility(View.VISIBLE);
                ivAddDescription.setVisibility(View.VISIBLE);

                tvDescription.setText(etDescription.getText().toString());
                tvDescription.setVisibility(View.VISIBLE);
            }
        });

        return v;
    }






    public void submit(String questionText, ArrayList<String> choicesText){


                // create new quireText with the user id and the question text
                newQuire = new Quire(questionText);


                String key = new Firebase(quiresUrl).push().getKey();


                newQuire.setQid(key);
                newQuire.setCommentCount(0);
                newQuire.setUid(mUserId);
                newQuire.setStatus("open");
                newQuire.setDescriptionText(etDescription.getText().toString());
                newQuire.setTimestamp(System.currentTimeMillis());
                newQuire.setInvertedTimestamp(0-System.currentTimeMillis());
                newQuire.setTotalVotes(0);


                newQuire.setChoicesCount(choices2.size());

                ArrayList<Choice> finalChoices = new ArrayList<Choice>();
                for (String et: choices2){//choicesText){
                    String text = et;
                    Choice newChoice = new Choice(text);

                    String choiceKey = new Firebase(quiresUrl)
                            .child(key)
                            .child("choices")
                            .push().getKey();

                    newChoice.setCid(choiceKey);
                    newChoice.setVotes(0);
                    newChoice.setUsers(new ArrayList<User>());
                    finalChoices.add(newChoice);

                    //adds the new choice
                    new Firebase(choicesUrl)
                            .child(choiceKey)
                            .setValue(newChoice);
                }

                newQuire.setChoices(finalChoices);

                // this part saves the quire into the database
                new Firebase(quiresUrl)
                        .child(key)
                        .setValue(newQuire);

                // Update user's list of created quires
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(key, "hi");

                new Firebase(userQuiresUrl)
                        .updateChildren(map);

    }


    private void loadLoginView() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public ComposeChoicesFragment goBack(){
        ComposeChoicesFragment composeChoicesFragment = new ComposeChoicesFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("choices", choicesText);
        args.putString("question", questionText);
        composeChoicesFragment.setArguments(args);
        return composeChoicesFragment;
    }


}
