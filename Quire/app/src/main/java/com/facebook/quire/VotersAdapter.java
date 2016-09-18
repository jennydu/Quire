package com.facebook.quire;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.quire.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by amao on 7/25/16.
 */
public class VotersAdapter extends RecyclerView.Adapter<VotersAdapter.ViewHolder> {

    private List<User> mVoters = new ArrayList<>();
    private Context mContext;

    public VotersAdapter(Context context) {
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFullName) TextView tvName;
        @BindView(R.id.tvUsername) TextView tvUsername;
        @BindView(R.id.ivProfileImage) ImageView ivProfileImage;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate layout
        View voterView = inflater.inflate(R.layout.item_voter, parent, false);
        ViewHolder viewHolder = new ViewHolder(voterView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final VotersAdapter.ViewHolder viewHolder, int position) {
        User voter = mVoters.get(position);

        viewHolder.tvName.setText(voter.getFull_name());
        viewHolder.tvUsername.setText(voter.getScreen_name());
        Picasso.with(getContext()).load(voter.getProfile_image_url()).transform(new CircleTransform()).fit().into(viewHolder.ivProfileImage);
    }

    @Override
    public int getItemCount() {
        return mVoters.size();
    }

    public void setVoters(List<User> voters) {
        mVoters = voters;
        notifyDataSetChanged();
        Log.d("surprise", voters.toString());
        //so voters is updated, but why isnt the view?

    }
}
