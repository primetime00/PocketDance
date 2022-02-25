package com.kegel.pocketdance.video;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.kegel.pocketdance.Constants;

public class DanceVideoPlayer implements Player.EventListener {
    private SimpleExoPlayer videoPlayer;
    private DataSource.Factory dataSourceFactory;
    protected long startTime;
    protected long endTime;
    private Handler handler;
    private boolean closed = false;
    private Context context;
    private DanceVideoListener listener;
    private boolean replay;

    public DanceVideoPlayer(Context context) {
        this.context = context;
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        this.videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        this.videoPlayer.addListener(this);
        dataSourceFactory = new DefaultDataSourceFactory(
                context, Util.getUserAgent(context, "PocketDance Video Organizer"));
        this.videoPlayer.setVolume(0f);
        this.videoPlayer.addListener(this);
        handler = new Handler();
        this.replay = false;
    }

    public void setListener(DanceVideoListener listener) {
        this.listener = listener;
    }

    public void setReplay(boolean replay) {
        this.replay = replay;
    }

    public void loadContent(String url) {
        if (closed) {
            return;
        }
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url));
        videoPlayer.prepare(videoSource);
        videoPlayer.setPlayWhenReady(false);
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    private Runnable clipRunnable = new Runnable() {
        @Override
        public void run() {
            if (closed) {
                return;
            }
            long et = endTime;
            long ct = videoPlayer.getCurrentPosition();
            if (ct < et-100) {
                //Log.e(Constants.LOG_TAG, String.format("New delay for %d ms", et - ct));
                handler.postDelayed(clipRunnable, et - ct);
                return;
            }
            onPlayerStateChanged(videoPlayer.getPlayWhenReady(), Player.STATE_ENDED);
        }
    };

    private void setEndHandler() {
        if (closed) {
            return;
        }
        if (endTime > 0) {
            handler.removeCallbacks(clipRunnable);
            //Log.e(Constants.LOG_TAG, String.format("New initial delay for %d ms", endTime - startTime));
            handler.postDelayed(clipRunnable , endTime - startTime);
        }
    }

    public boolean isPaused() {
        if (closed) {
            return false;
        }
        return videoPlayer.getPlaybackState() == Player.STATE_READY && !videoPlayer.getPlayWhenReady();
    }

    public void pause() {
        videoPlayer.setPlayWhenReady(false);
    }

    public void play() {
        videoPlayer.setPlayWhenReady(true);
    }

    public void close() {
        closed = true;
        handler.removeCallbacks(clipRunnable);
        videoPlayer.stop();
        videoPlayer.release();
    }

    public boolean isClosed() {
        return closed;
    }

    public void restart() {
        setEndHandler();
        videoPlayer.seekTo(startTime);
    }



    public interface DanceVideoListener {
        void onLoaded();
        void onVideoEnded();
        void onPlayerError(String error);
    }


    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
        if (reason != Player.TIMELINE_CHANGE_REASON_DYNAMIC) {
            return;
        }
        if (closed) {
            return;
        }
        setEndHandler();
        videoPlayer.seekTo(startTime);
        videoPlayer.setPlayWhenReady(true);

        if (listener != null) {
            listener.onLoaded();
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (closed) {
            return;
        }
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                break;
            case Player.STATE_ENDED:
                    if (replay) {
                        restart();
                    }
                    if (listener != null) {
                        listener.onVideoEnded();
                    }
                break;
            case Player.STATE_READY:
                break;
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (listener != null) {
            listener.onPlayerError(error.getMessage());
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public SimpleExoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
