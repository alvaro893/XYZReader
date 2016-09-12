/**
 * Copyright (C) 2016 Alvaro Bolanos Rodriguez
 */
package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/*
 * TODO: Create JavaDoc
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {
    private static final String TAG = ArticleListAdapter.class.getSimpleName();
    private static final long DURATION_ANIMATION = 1000L;
    private static final float OFFSET_INCREASE = 1.4f;
    private static final float INITIAL_ALPHA = 0.85f;
    private static final float FINAL_ALPHA = 1f;
    private Cursor mCursor;
    private int mLastPosition = -1;
    private float offset;
    private Activity mContext;

    public ArticleListAdapter(Cursor cursor, Activity mContext) {
        mCursor = cursor;
        this.mContext = mContext;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mContext.getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
        final ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        holder.subtitleView.setText(
                DateUtils.getRelativeTimeSpanString(
                        mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR));
        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(mContext).getImageLoader());
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setAnimation(holder.articleRoot, position);
        }

        holder.articleRoot.setOnClickListener(itemClick(holder));
    }

    private View.OnClickListener itemClick(final ViewHolder holder){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle =  ActivityOptions.makeSceneTransitionAnimation(mContext,
                        holder.thumbnailView, holder.thumbnailView.getTransitionName()).toBundle();
                Uri uri = ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent, bundle);
            }
        };
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    /** this will animate the items in the recycler view once for each*/
    private void setAnimation(View viewToAnimate, int position){
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > mLastPosition){
            //The view has to be invisible for the animation
            viewToAnimate.setVisibility(View.INVISIBLE);

            offset = mContext.getResources().getDimensionPixelSize(R.dimen.offset_y);
            Interpolator interpolator =
                    AnimationUtils.loadInterpolator(mContext, android.R.interpolator.linear_out_slow_in);


            Log.d(TAG, "position: " + position);
            viewToAnimate.setVisibility(View.VISIBLE);
            viewToAnimate.setTranslationY(offset);
            viewToAnimate.setAlpha(INITIAL_ALPHA);
            // then animate back to natural position
            viewToAnimate.animate()
                    .translationY(0f)
                    .alpha(FINAL_ALPHA)
                    .setInterpolator(interpolator)
                    .setDuration(DURATION_ANIMATION)
                    .start();
            // increase the offset distance for the next view
            offset *= OFFSET_INCREASE;

            mLastPosition = position;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;
        public CardView articleRoot;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            articleRoot = (CardView) view.findViewById(R.id.article_root);
        }
    }
}