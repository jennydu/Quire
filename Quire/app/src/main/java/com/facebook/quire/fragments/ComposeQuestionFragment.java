package com.facebook.quire.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.facebook.quire.R;

import java.util.ArrayList;

/**
 * Created by jenniferdu on 7/25/16.
 */
public class ComposeQuestionFragment extends Fragment{


    public static EditText etQuestion;


    Toolbar tb;

    String questionText;

    Menu menu;
    View v;

    View.OnClickListener clickListener;
    ActionBar actionBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_compose_question, container, false);

        etQuestion = (EditText) v.findViewById(R.id.etQuestion);


        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);


        return v;
    }

    public ComposeChoicesFragment goNext(ArrayList<String> choices){

        String question = etQuestion.getText().toString();

        ComposeChoicesFragment composeChoicesFrag = new ComposeChoicesFragment();
        Bundle args = new Bundle();
        args.putString("question", question);
        args.putStringArrayList("choices", choices);
        composeChoicesFrag.setArguments(args);
        return composeChoicesFrag;
    }

    public String getQuestion (){
        String question = etQuestion.getText().toString();
        return question;
    }

    public ComposeQuestionFragment newInstance(String questionText){
        ComposeQuestionFragment composeQuestionFrag = new ComposeQuestionFragment();
        Bundle args = new Bundle();
        args.putString("question", questionText);
        //Toast.makeText(getContext(), "made it here", Toast.LENGTH_SHORT).show();
        etQuestion.setText(questionText);
        composeQuestionFrag.setArguments(args);
        return composeQuestionFrag;
    }

    public void populateQuestion(EditText et, String question) {
        //et.setText(question, TextView.BufferType.EDITABLE);
        et.append(question);
    }

    public EditText getEt(){
        return etQuestion;
    }


}
