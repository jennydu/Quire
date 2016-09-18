package com.facebook.quire;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.quire.activities.CommentActivity;
import com.facebook.quire.activities.ProfileActivity;
import com.facebook.quire.activities.SetResultActivity;
import com.facebook.quire.activities.VotersActivity;
import com.facebook.quire.models.Quire;
import com.facebook.quire.models.User;
import com.facebook.quire.views.ChoiceView;
import com.facebook.quire.views.CommentsIcon;
import com.facebook.quire.views.VotersIcon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;
import org.parceler.Parcels;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class QuiresAdapter extends RecyclerView.Adapter<QuiresAdapter.ViewHolder>{
    private static final String TAG = "QuiresAdapter";
    private List<Quire> mQuires = new ArrayList<>();
    private Context mContext;
    private DatabaseReference mDatabase;
    private DatabaseReference mChoices;
    Typeface bold;
    Typeface regular;
    ArrayList<Drawable> array = new ArrayList<>();
    ColorStateList finishedColorList;
    ColorStateList activeColorList;

    public QuiresAdapter(Context context) {
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
        @BindView(R.id.tvUsername) TextView tvUsername;
        @BindView(R.id.tvTime) TextView tvTime;
//        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvDescription) TextView tvDescription;
        @BindView(R.id.tvQuestion) TextView tvQuestion;
        @BindView(R.id.llPoll) LinearLayout llPoll;
        @BindView(R.id.btnDone) Button btnDone;
//        @BindView(R.id.btnClosed) Button btnClosed;
//        @BindView(R.id.btnOpen) Button btnOpen;
        @BindView(R.id.ivVoters) ImageView ivVoters;
        @BindView(R.id.cvVoters) VotersIcon cvVoters;
        @BindView(R.id.cvComments) CommentsIcon cvComments;
        @BindView(R.id.rlQuire) RelativeLayout rlQuire;
//        @BindView(R.id.flTotal) FrameLayout flTotal;
        @BindView(R.id.rlTotal) RelativeLayout rlTotal;

        public ViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public QuiresAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_quire, parent, false);
        Log.d("inflate", "inflated");

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);

        return viewHolder;
    }

    private List<Quire> cachedQuires = new ArrayList<>();

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final QuiresAdapter.ViewHolder viewHolder, final int position) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mChoices = mDatabase.child("choices");

        bold = Typeface.createFromAsset(getContext().getAssets(),"fonts/Dosis-Bold.ttf");
        regular = Typeface.createFromAsset(getContext().getAssets(),"fonts/Dosis-Regular.ttf");

        finishedColorList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        getContext().getResources().getColor(R.color.percentage),
                        getContext().getResources().getColor(R.color.percentage)
                }
        );

        activeColorList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        getContext().getResources().getColor(R.color.textColorPrimary),
                        getContext().getResources().getColor(R.color.textColorPrimary)
                }
        );

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get the data model based on position
        final Quire quire = mQuires.get(position);
        final String quireID = quire.getQid();

//        final long totalVotes;
//
//        Quire cachedQuire = cachedQuires.get(position);
//        if (cachedQuire == null) {
//            cachedQuire = new Quire();
        mDatabase.child("quires").child(quireID).child("totalVotes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                cachedQuire.setTotalVotes((long) dataSnapshot.getValue());
                viewHolder.cvVoters.setValue((long) dataSnapshot.getValue() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        array.add(getContext().getResources().getDrawable(R.drawable.progressbar_green));
        array.add(getContext().getResources().getDrawable(R.drawable.progressbar));
        array.add(getContext().getResources().getDrawable(R.drawable.progressbar_red));
        array.add(getContext().getResources().getDrawable(R.drawable.progressbar_yellow));

        // Set item views based on your views and data model
        Date date = new Date(quire.getTimestamp());
        //Log.d("this is date", date.toString());
        PrettyTime p = new PrettyTime();
        viewHolder.tvTime.setText(p.format(date));

        //only display description if it is present
        if (!quire.getDescriptionText().isEmpty()) {
            viewHolder.tvDescription.setText(quire.getDescriptionText());
        } else {
            viewHolder.tvDescription.setVisibility(View.GONE);
        }

        //viewHolder.btnDone.setVisibility(View.VISIBLE);

        // Displays "Mark As Done" button only if quire's user id matches current user's id
        // If quire's user id matches, then button is visible and onClick handler will happen
        // Else, button is gone and nothing appears on item_quire
        if (quire.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                && quire.getStatus().equals("open")) {
            if (quire.getTotalVotes() == 0) {
                viewHolder.btnDone.setEnabled(false);
            }
            viewHolder.btnDone.setEnabled(true);
            viewHolder.btnDone.setVisibility(View.VISIBLE);
            // Creating an onClick Handler for "Mark As Done" Button
            viewHolder.btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getContext(),SetResultActivity.class);
                    i.putExtra("quire", Parcels.wrap(quire));
                    getContext().startActivity(i);
                }
            });
        } else {
            viewHolder.btnDone.setVisibility(View.GONE);
        }


        // Load ProfileActivity when you click on a user's profile image and user's name
        viewHolder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(),ProfileActivity.class);
                i.putExtra("uid", quire.getUid());
                getContext().startActivity(i);
            }
        });

        //somehow: check if the current user has commented.
        mDatabase.child("users").child(currentUserId).child("commentedQuires").child(quireID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    //then user has commented, so change the image
                    viewHolder.cvComments.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_comments_filled, null));
                } else {
                    viewHolder.cvComments.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_comments_unfilled, null));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Load all the comments for an adapter
        viewHolder.cvComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CommentActivity.class);
                i.putExtra("qid",quire.getQid());
                i.putExtra("uid",quire.getUid());
                getContext().startActivity(i);
            }
        });

        viewHolder.tvQuestion.setText(quire.getQuestionText());
        //viewHolder.tvComments.setText(quire.getCommentCount() + " comments");

        mDatabase.child("quires").child(quireID).child("commentCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.cvComments.setValue(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set on click listener to launch Voter fragment
        viewHolder.cvVoters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), VotersActivity.class);
                i.putExtra("qid", quireID);
                getContext().startActivity(i);
            }
        });

        viewHolder.llPoll.removeAllViews();

//        //if this is your quire
//        if (quire.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//            //then display a finished poll
//            loadYourPoll(viewHolder, quire, currentUserId, quireID);
//        } else {
            //check if the poll is finished....
            mDatabase.child("quires").child(quireID).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String status = (String) dataSnapshot.getValue();
                    //if open, then do the check about whether user has voted, etc. old code.
                    if (status.equals("open")) {

                        //temporary: check if user has voted before or not.
                        mDatabase.child("quires").child(quireID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount() != 0) {
                                    //then "USERS" exists, someone has voted on this poll before
                                    boolean userHasVoted = false;
                                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                                        //currently iterating through all users who have voted
                                        if (s.getKey().equals(currentUserId)) {
                                            userHasVoted = true;
                                            //and if user has voted, then fill in the icon
                                            viewHolder.cvVoters.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_vote_filled, null));
                                            loadFinishedPoll(viewHolder, quire, currentUserId, quireID);
                                        }
                                    }
                                    if (!userHasVoted) {
                                        viewHolder.cvVoters.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_vote_unfilled, null));
                                        loadPoll(viewHolder, quire, currentUserId, quireID);
                                    }
                                } else {
                                    //then no one has ever voted on this poll before
                                    loadPoll(viewHolder, quire, currentUserId, quireID);
                                    viewHolder.cvVoters.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_vote_unfilled, null));

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }

                    //if closed, then change the background color. and then load finished poll
                    else {
                        loadClosedPoll(viewHolder, quire, currentUserId, quireID);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
//        }

        String uid = quire.getUid();
        mDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                viewHolder.tvUsername.setText(user.getScreen_name());
                if (user.getProfile_image_url() != null) {
                    String imageUri = user.getProfile_image_url();
                    Picasso.with(getContext()).load(imageUri).transform(new CircleTransform()).fit().into(viewHolder.ivProfileImage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void loadFinishedPoll(final QuiresAdapter.ViewHolder viewHolder, final Quire quire, final String currentUserId, final String quireID) {
        viewHolder.rlQuire.setBackgroundColor(getContext().getResources().getColor(R.color.radiogroupBackground));

        //user has voted, fill vote icon
        viewHolder.cvVoters.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_vote_filled, null));

        viewHolder.llPoll.removeAllViews();
        final RadioGroup rgPoll = new RadioGroup(getContext());
        rgPoll.setOnCheckedChangeListener(null);
        rgPoll.clearCheck();

        //get the user's vote
        mDatabase.child("users").child(currentUserId).child("answeredQuires").child(quireID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("old vote id is ", (String) dataSnapshot.getValue());
                String userChoiceId = (String) dataSnapshot.getValue();

                //then populate radiogroup while checking if it is matching
                int choicesCount = (int) quire.getChoicesCount();
                for (int i = 0; i < choicesCount; i++) {
                    final int j = i;
                    String currentChoiceId = quire.getChoices().get(i).getCid();

                    final ChoiceView cvChoice = new ChoiceView(getContext());
                    cvChoice.setButtonId(i);
                    cvChoice.setChoiceText(quire.getChoices().get(i).getText());

                    //if this choice is the voted on one, then make it checked
                    if (currentChoiceId.equals(userChoiceId)) {
                        cvChoice.setOptionChecked(true);
                    }

                    cvChoice.setPollEnabled(false);

                    cvChoice.displayProgress(true);

                    //get number of votes for this choice
                    mChoices.child(currentChoiceId).child("votes").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long votes = (long) dataSnapshot.getValue();

                            //set progress bar
                            cvChoice.setMax((int) quire.getTotalVotes());
                            cvChoice.setVotes((int) votes);

                            //current color
                            Drawable d = array.get(j % 4);
                            cvChoice.setProgressDrawable(d);

                            //set percentage
                            String percentage = MessageFormat.format("{0, number, #%}", (double) votes/ (double) quire.getTotalVotes());
                            cvChoice.setPercentage(percentage);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    rgPoll.addView(cvChoice);
                }

                mDatabase.child("quires").child(quireID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            viewHolder.cvVoters.setValue(dataSnapshot.getChildrenCount() + "");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        viewHolder.llPoll.addView(rgPoll);
    }

    private void loadPoll(final QuiresAdapter.ViewHolder viewHolder, final Quire quire, final String currentUserId, final String quireID) {

        viewHolder.rlQuire.setBackgroundColor(getContext().getResources().getColor(R.color.radiogroupBackground));

        viewHolder.llPoll.removeAllViews();
//        viewHolder.llPoll.setBackgroundColor(getContext().getResources().getColor(R.color.radiogroupBackground));
        RadioGroup rgPoll = new RadioGroup(getContext());
        rgPoll.setOnCheckedChangeListener(null);
        rgPoll.clearCheck();
//        rgPoll.setPadding(100, 30, 10, 10);

        int choicesCount = (int) quire.getChoicesCount();
        for (int i = 0; i < choicesCount; i++) {
            final int j = i;
//            add a checkbox for every option present
//            final RadioButton option = new RadioButton(getContext());
//            option.setId(i);
//            option.setText(quire.getChoices().get(i).getText());
//            option.setTypeface(bold);
//            option.setTextSize(17);
//            option.setTextColor(ContextCompat.getColor(mContext, R.color.textColorPrimary));
//            option.setPadding(50, 0, 0, 10);

            final ChoiceView cvChoice = new ChoiceView(getContext());
            cvChoice.setButtonId(i);
            cvChoice.setChoiceText(quire.getChoices().get(i).getText());
            cvChoice.setButtonColor(activeColorList);

            //don't display progress bar
            cvChoice.displayProgress(false);

            //add the view...
//            viewHolder.llPoll.addView(cvChoice);
            //add the button to the group for listening...
            rgPoll.addView(cvChoice);

            //make a on click listener for this choice

            cvChoice.getButton().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    //1. put user in list of users for this choice
                    final String choiceId = quire.getChoices().get(j).getCid();
                    Map<String, Object> users = new HashMap<String, Object>();
                    users.put(currentUserId, "hi");
                    mChoices.child(choiceId).child("users").updateChildren(users);
                    //and... remove user from the other choice
                    //TODO: check if user has voted on this poll. if so, then remove when user changes their vote

                    //2. update total # of votes for choice object
                    //get current number of votes, and add one
                    //TODO: and take one away from the one they changed from, if that's the case

                    mChoices.child(choiceId).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //votes is updated
                            //this works
                            mChoices.child(choiceId).child("votes").setValue((long) dataSnapshot.getValue() + (long) 1);

                            quire.setTotalVotes(quire.getTotalVotes() + 1);

                            mDatabase.child("quires").child(quireID).child("totalVotes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //WHY DOESNT THIS UPDATE?
                                    //ohhh need to push it back.
                                    mDatabase.child("quires").child(quireID).child("totalVotes").setValue((long) dataSnapshot.getValue() + 1);
//                                quire.setTotalVotes((long) dataSnapshot.getValue() + 1);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            //3. add user to list of users who have voted in this quire
                            Map<String, Object> votedUsers = new HashMap<String, Object>();
                            votedUsers.put(currentUserId, "hi");
                            mDatabase.child("quires").child(quireID).child("users").updateChildren(votedUsers);

                            //4. add qid, cid under user.
                            Map<String, Object> answeredQuires = new HashMap<String, Object>();
                            answeredQuires.put(quireID, choiceId);
                            mDatabase.child("users").child(currentUserId).child("answeredQuires").updateChildren(answeredQuires);

                            //5. then update the ui

                            loadFinishedPoll(viewHolder, quire, currentUserId, quireID);
                            //update votes
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });


        }
        viewHolder.llPoll.addView(rgPoll);
    }

    private void loadYourPoll(final QuiresAdapter.ViewHolder viewHolder, final Quire quire, final String currentUserId, final String quireID) {
        viewHolder.rlQuire.setBackgroundColor(getContext().getResources().getColor(R.color.radiogroupBackground));

        viewHolder.llPoll.removeAllViews();
//        viewHolder.llPoll.setBackgroundColor(getContext().getResources().getColor(R.color.radiogroupBackground));
        RadioGroup rgPoll = new RadioGroup(getContext());
        rgPoll.setOnCheckedChangeListener(null);
        rgPoll.clearCheck();
//        rgPoll.setPadding(100, 30, 10, 10);

        int choicesCount = (int) quire.getChoicesCount();
        for (int i = 0; i < choicesCount; i++) {
            final int j = i;

            final ChoiceView cvChoice = new ChoiceView(getContext());
            cvChoice.setButtonId(i);
            cvChoice.setChoiceText(quire.getChoices().get(i).getText());
            cvChoice.setButtonColor(activeColorList);

            cvChoice.displayProgress(true);

            String currentChoiceId = quire.getChoices().get(i).getCid();

            //display the votes
            //get number of votes for this choice
            mChoices.child(currentChoiceId).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long votes = (long) dataSnapshot.getValue();

                    //set progress bar
                    cvChoice.setMax((int) quire.getTotalVotes());
                    cvChoice.setVotes((int) votes);

                    //current color
                    Drawable d = array.get(j % 4);
                    cvChoice.setProgressDrawable(d);

                    //set percentage
                    String percentage = MessageFormat.format("{0, number, #%}", (double) votes/ (double) quire.getTotalVotes());
                    cvChoice.setPercentage(percentage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //add the view...
//            viewHolder.llPoll.addView(cvChoice);
            //add the button to the group for listening...
            rgPoll.addView(cvChoice);

            //make a on click listener for this choice

            cvChoice.getButton().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    //1. put user in list of users for this choice
                    final String choiceId = quire.getChoices().get(j).getCid();
                    Map<String, Object> users = new HashMap<String, Object>();
                    users.put(currentUserId, "hi");
                    mChoices.child(choiceId).child("users").updateChildren(users);
                    //and... remove user from the other choice
                    //TODO: check if user has voted on this poll. if so, then remove when user changes their vote

                    //2. update total # of votes for choice object
                    //get current number of votes, and add one
                    //TODO: and take one away from the one they changed from, if that's the case

                    mChoices.child(choiceId).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //votes is updated
                            //this works
                            mChoices.child(choiceId).child("votes").setValue((long) dataSnapshot.getValue() + (long) 1);

                            quire.setTotalVotes(quire.getTotalVotes() + 1);

                            mDatabase.child("quires").child(quireID).child("totalVotes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //WHY DOESNT THIS UPDATE?
                                    //ohhh need to push it back.
                                    mDatabase.child("quires").child(quireID).child("totalVotes").setValue((long) dataSnapshot.getValue() + 1);
//                                quire.setTotalVotes((long) dataSnapshot.getValue() + 1);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            //3. add user to list of users who have voted in this quire
                            Map<String, Object> votedUsers = new HashMap<String, Object>();
                            votedUsers.put(currentUserId, "hi");
                            mDatabase.child("quires").child(quireID).child("users").updateChildren(votedUsers);

                            //4. add qid, cid under user.
                            Map<String, Object> answeredQuires = new HashMap<String, Object>();
                            answeredQuires.put(quireID, choiceId);
                            mDatabase.child("users").child(currentUserId).child("answeredQuires").updateChildren(answeredQuires);

                            //5. then update the ui

                            loadFinishedPoll(viewHolder, quire, currentUserId, quireID);
                            //update votes
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });


        }
        viewHolder.llPoll.addView(rgPoll);
    }

    private void loadClosedPoll(final QuiresAdapter.ViewHolder viewHolder, final Quire quire, final String currentUserId, final String quireID) {
        //check if user has voted
        mDatabase.child("quires").child(quireID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    //user has voted, fill vote icon
                    viewHolder.cvVoters.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_vote_filled_gray, null));
                } else {
                    //user has not voted, fill vote icon
                    viewHolder.cvVoters.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_vote_unfilled_gray, null));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //check if user has commented
        mDatabase.child("users").child(currentUserId).child("commentedQuires").child(quireID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    //then user has commented, so change the image
                    viewHolder.cvComments.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_comment_filled_gray, null));
                } else {
                    viewHolder.cvComments.setDrawable(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_comment_unfilled_gray, null));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set darker color
//        viewHolder.rlTotal.setBackgroundColor(getContext().getResources().getColor(R.color.finishedHeader));
        viewHolder.rlQuire.setBackgroundColor(getContext().getResources().getColor(R.color.finishedPoll));

        viewHolder.llPoll.removeAllViews();
        final RadioGroup rgPoll = new RadioGroup(getContext());
        rgPoll.setOnCheckedChangeListener(null);
        rgPoll.clearCheck();

        //get the user's vote
        mDatabase.child("users").child(currentUserId).child("answeredQuires").child(quireID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("old vote id is ", (String) dataSnapshot.getValue());
                String userChoiceId = (String) dataSnapshot.getValue();

                //then populate radiogroup while checking if it is matching
                int choicesCount = (int) quire.getChoicesCount();
                for (int i = 0; i < choicesCount; i++) {
                    final int j = i;
                    String currentChoiceId = quire.getChoices().get(i).getCid();

                    final ChoiceView cvChoice = new ChoiceView(getContext());
                    cvChoice.setButtonColor(activeColorList);
                    cvChoice.setButtonId(i);
                    cvChoice.setChoiceText(quire.getChoices().get(i).getText());

                    //if this choice is the voted on one, then make it checked
                    if (currentChoiceId.equals(userChoiceId)) {
                        cvChoice.setOptionChecked(true);
                    }

                    cvChoice.setBackgroundColor(getContext().getResources().getColor(R.color.finishedPoll));

                    cvChoice.setButtonColor(finishedColorList);

                    cvChoice.setPollEnabled(false);

                    cvChoice.displayProgress(true);

                    //get number of votes for this choice
                    mChoices.child(currentChoiceId).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long votes = (long) dataSnapshot.getValue();

                            //set progress bar
                            cvChoice.setMax((int) quire.getTotalVotes());
                            cvChoice.setVotes((int) votes);

                            if (votes == 0) {
                                Drawable d = getContext().getResources().getDrawable(R.drawable.progressbar_zerovotes_finished);
                                cvChoice.setProgressDrawable(d);
                            } else {
                                //gray them out
                                Drawable d = getContext().getResources().getDrawable(R.drawable.progressbar_gray);
                                cvChoice.setProgressDrawable(d);
                            }

                            //set percentage
                            if (quire.getTotalVotes() == 0) {
                                cvChoice.setPercentage(MessageFormat.format("{0, number, #%}", 0));
                            } else {
                                String percentage = MessageFormat.format("{0, number, #%}", (double) votes/ (double) quire.getTotalVotes());
                                cvChoice.setPercentage(percentage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    rgPoll.addView(cvChoice);
                }

                mDatabase.child("quires").child(quireID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.cvVoters.setValue(dataSnapshot.getChildrenCount() + "");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        viewHolder.llPoll.addView(rgPoll);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mQuires.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        mQuires.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Quire> list) {
        mQuires.addAll(list);
        notifyDataSetChanged();
    }

    public void addToFront(List<Quire> list) {
        for (Quire q : list) {
            mQuires.add(0, q);
        }
        notifyDataSetChanged();
    }

    public void setQuires(List<Quire> quires) {
        mQuires = quires;
        notifyDataSetChanged();
    }

    public Quire getQuireAt(int index) {
        return mQuires.get(index);
    }
}
