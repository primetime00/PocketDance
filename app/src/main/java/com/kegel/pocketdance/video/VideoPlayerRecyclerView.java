package com.kegel.pocketdance.video;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.R;
import com.kegel.pocketdance.ui.figures.FigureDancePlayer;
import com.kegel.pocketdance.ui.figures.FigureRecyclerAdapter;
import com.kegel.pocketdance.ui.figures.FigureViewHolder;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerRecyclerView extends RecyclerView {
    private static final String TAG = "PocketDance";

    // ui
    private ImageView thumbnail;
    private PlayerView thumbnailVideo;
    private View viewHolderParent;
    private FigureDancePlayer videoPlayer;
    private boolean firstPlay = false;

    // vars
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private final Handler displayHandler = new Handler();

    public VideoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releasePlayer();
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        Log.e(Constants.LOG_TAG, "Everything Invisible");
        thumbnailVideo.setVisibility(INVISIBLE);
        thumbnail.setVisibility(VISIBLE);
        isVideoViewAdded = false;
        firstPlay = false;
    }

    private void init(Context context){
        this.context = context.getApplicationContext();

        videoPlayer = new FigureDancePlayer(context);

        addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (!firstPlay) {
                firstPlay = playVideo(VideoPlayerRecyclerView.this);
            }
        });

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.");
                    playVideo(recyclerView);
                }
                else {
                    if (thumbnailVideo != null) {
                        thumbnailVideo.setVisibility(INVISIBLE);
                        playPosition = -1;
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        videoPlayer.setListener(new DanceVideoPlayer.DanceVideoListener() {
            @Override
            public void onLoaded() {
                if(!isVideoViewAdded){
                    addVideoView();
                }
            }

            @Override
            public void onVideoEnded() {

            }

            @Override
            public void onPlayerError(String error) {

            }
        });

    }

    public boolean isOnScreen(float percentClipped, View view) {
        if (view == null || !view.isShown())
            return false;
        Rect clippingRect = new Rect();
        view.getGlobalVisibleRect(clippingRect);
        int clippedHeight = clippingRect.bottom - clippingRect.top;
        int viewHeight = view.getBottom() - view.getTop();
        int offset = viewHeight - (int)(viewHeight * percentClipped);
        return clippedHeight >= offset;
    }

    public boolean playVideo(RecyclerView recyclerView) {

        int targetPosition;
        View thumbView;
        List<ScreenViews> inScreenViews = new ArrayList<>();

        int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

        if (startPosition == -1 && endPosition == -1) {
            return false;
        }

        if (recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()) {
            targetPosition = getAdapter().getItemCount() - 1;
        }
        else if (recyclerView.computeVerticalScrollOffset() == 0) {
            targetPosition = 0;
        }
        else {
            int pos = 0;
            for (int index = startPosition; index <= endPosition; ++index, ++pos) {
                View v = getLayoutManager().getChildAt(pos);
                if (v == null) {
                    continue;
                }
                thumbView = v.findViewById(R.id.figure_thumb);
                if (thumbView == null) {
                    continue;
                }
                if (isOnScreen(0.9f, thumbView)) {
                    inScreenViews.add(new ScreenViews(index, thumbView));
                }
            }
            //find the one closest to center
            Rect lvRect = new Rect();
            this.getGlobalVisibleRect(lvRect);
            int center = (lvRect.top - lvRect.bottom) / 2 + lvRect.bottom;
            int distance = Integer.MAX_VALUE;
            targetPosition = -1;
            int px[] = new int[2];
            for (ScreenViews s : inScreenViews) {
                int height = s.view.getBottom() - s.view.getTop();
                s.view.getLocationInWindow(px);
                int viewCenter = px[1] + (height / 2);
                int calcPos = Math.abs(viewCenter - center);
                if (calcPos < distance) {
                    distance = calcPos;
                    targetPosition = s.position;
                }
            }
        }

        Log.d(TAG, "playVideo: target position: " + targetPosition);


        playPosition = targetPosition;

        int currentPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null) {
            return false;
        }

        FigureViewHolder holder = (FigureViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return false;
        }


        if (thumbnailVideo != null) {
            thumbnailVideo.setVisibility(INVISIBLE);
            thumbnailVideo.setPlayer(null);
            isVideoViewAdded = false;
        }

        thumbnail = holder.getThumbnail();
        thumbnailVideo = holder.getThumbnailVideo();
        viewHolderParent = holder.itemView;

        thumbnailVideo.setPlayer(videoPlayer.getVideoPlayer());


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                context, Util.getUserAgent(context, "RecyclerView VideoPlayer"));

        FigureRecyclerAdapter adapter = (FigureRecyclerAdapter) getAdapter();
        if (targetPosition < adapter.getItemCount()) {
            String mediaUrl = adapter.getFigures().get(targetPosition).getVideoFileName();
            videoPlayer.setStartTime(adapter.getFigures().get(targetPosition).getStart());
            videoPlayer.setEndTime(adapter.getFigures().get(targetPosition).getEnd());
            videoPlayer.loadContent(mediaUrl);
        }
        return true;
    }

    private void addVideoView(){
        if (thumbnailVideo == null || thumbnail == null) {
            Log.e(TAG, "Could not add video!");
            return;
        }
        isVideoViewAdded = true;

        thumbnailVideo.requestFocus();
        thumbnailVideo.setAlpha(1);
        //thumbnailVideo.getLayoutParams().width = thumbnail.getLayoutParams().width;
        //thumbnailVideo.getLayoutParams().height = (int)(thumbnail.getLayoutParams().width * ratio);
        //thumbnailVideo.requestLayout();
        //thumbnailVideo.setVisibility(VISIBLE);

        displayHandler.postDelayed(() -> thumbnailVideo.setVisibility(VISIBLE), 200);
    }

    public void releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer.close();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    static private class ScreenViews {
        public int position;
        public View view;

        public ScreenViews(int pos, View thumbView) {
            this.position = pos;
            this.view = thumbView;
        }
    }
}
