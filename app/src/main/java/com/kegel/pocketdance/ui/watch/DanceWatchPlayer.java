package com.kegel.pocketdance.ui.watch;

import android.content.Context;

import com.kegel.pocketdance.video.DanceVideoPlayer;

public class DanceWatchPlayer extends DanceVideoPlayer {
    public DanceWatchPlayer(Context context) {
        super(context);
        getVideoPlayer().setVolume(1f);
    }
}
