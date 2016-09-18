package com.facebook.quire.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.quire.R;
import com.facebook.quire.models.Choice;

import java.util.ArrayList;

/**
 * Created by jenniferdu on 7/25/16.
 */
public class ComposeChoicesFragment extends Fragment {


    TextView tvQuestion;
    LinearLayout llChoices;
    FrameLayout flChoices;
    ImageView ivAddChoice;
    FrameLayout flEditChoice;
    ScrollView scrollView;

    ArrayList<TextView> tvChoices;
    ArrayList<String> choicesText;
    ArrayList<Choice> choices;
    String questionText;
    EditText etChoice;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Typeface bold = Typeface.createFromAsset(getContext().getAssets(),"fonts/Dosis-Bold.ttf");
        
        Typeface regular = Typeface.createFromAsset(getContext().getAssets(),"fonts/Dosis-Regular.ttf");

        View v = inflater.inflate(R.layout.fragment_compose_choices, container, false);

        //scrollView = (ScrollView) v.findViewById(R.id.scrollView);
        tvQuestion = (TextView) v.findViewById(R.id.tvQuestion);
        llChoices = (LinearLayout) v.findViewById(R.id.llChoices);
        flChoices = (FrameLayout) v.findViewById(R.id.flChoices);
        ivAddChoice = (ImageView) v.findViewById(R.id.ivAddChoice);
        flEditChoice = (FrameLayout) v.findViewById(R.id.flEditChoice);
        etChoice = (EditText) v.findViewById(R.id.etChoice);

        //scrollView.scrollTo(0,0);

        choices = new ArrayList<>();
        tvChoices = new ArrayList<>();
        choicesText = new ArrayList<>();

// display question
        questionText = getArguments().get("question").toString();
        tvQuestion.setText(questionText);
        tvQuestion.setTextColor(getResources().getColor(R.color.black));



// adding more options
        ivAddChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LinearLayout llNew = (LinearLayout) inflater.inflate(R.layout.compose_choice, null);
                final TextView tvNew = (TextView) llNew.findViewById(R.id.tvNew);
                ImageView ivRemove = (ImageView) llNew.findViewById(R.id.ivRemove);

                tvNew.setText(etChoice.getText().toString());

                tvChoices.add(etChoice);

                final String theText = etChoice.getText().toString();

                if (theText.trim().length() > 0) {
                    choicesText.add(etChoice.getText().toString());
                }

                ivRemove.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        llNew.setVisibility(View.GONE);
                        tvNew.setVisibility(View.GONE);
                        tvChoices.remove(tvNew);
                        choicesText.remove(theText);
                    }
                });

                llChoices.addView(llNew);

                // hide keyboard
                View v = getActivity().getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }


                etChoice.setText(null);
                etChoice.setHint("Add another choice...");

                choicesText = getChoicesText();

            }
        });

        return v;
    }

    public String getQuestionText(){
        return questionText;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public ComposeOptionsFragment goNext(ArrayList<String> choices){
        ComposeOptionsFragment composeOptionsFrag = new ComposeOptionsFragment();
        Bundle args = new Bundle();
        args.putString("question", questionText);
        ArrayList<String> finalChoiceArray = new ArrayList<>();
        for (String c : choicesText){
            if (c.trim().length() > 0){
                finalChoiceArray.add(c);
            }
        }
        args.putStringArrayList("choiceArrayList", finalChoiceArray);
        composeOptionsFrag.setArguments(args);
        return composeOptionsFrag;
    }


    public ArrayList<String> getChoicesText(){

        for (TextView tv : tvChoices){
            String text = tv.getText().toString();
            Choice c = new Choice(text);
            choices.add(c);
        }
        return choicesText;
    }

    public ComposeQuestionFragment goBack(){
        ComposeQuestionFragment composeQuestionFragment = new ComposeQuestionFragment();
        Bundle args = new Bundle();
        args.putString("question", questionText);
        composeQuestionFragment.setArguments(args);
        return composeQuestionFragment;
    }

}
