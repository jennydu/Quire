package com.facebook.quire;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.quire.models.Comment;
import com.facebook.quire.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by claudiawu on 7/20/16.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private DatabaseReference mDatabase;
    private List<Comment> mComments = new ArrayList<>();
    private Context mContext;

    public CommentsAdapter(Context context) {
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
//        @BindView(R.id.tvUsername) TextView tvUsername;
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvTime) TextView tvTime;
        @BindView(R.id.tvComment) TextView tvComment;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate layout
        View contactView = inflater.inflate(R.layout.item_comment,parent,false);

        ViewHolder viewHolder = new ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.ViewHolder viewHolder, int position) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final Comment comment = mComments.get(position);

        //is this not the right user id?
        mDatabase.child("users").child(comment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                viewHolder.tvName.setText(user.getScreen_name());
                Picasso.with(getContext()).load(user.getProfile_image_url()).transform(new CircleTransform()).fit().into(viewHolder.ivProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.tvComment.setText(comment.getText());

        // Modifying timestamp
        Date date = new Date(comment.getTimestamp());
        PrettyTime time = new PrettyTime();
        viewHolder.tvTime.setText(time.format(date));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void clear() {
        mComments.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Comment> list) {
        mComments.addAll(list);
        notifyDataSetChanged();
    }

    public void setComments(List<Comment> comments) {
        mComments = comments;
        notifyDataSetChanged();
    }
}