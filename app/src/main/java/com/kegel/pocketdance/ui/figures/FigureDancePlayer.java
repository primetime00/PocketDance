package com.kegel.pocketdance.ui.figures;

import android.content.Context;

import com.kegel.pocketdance.video.DanceVideoPlayer;

public class FigureDancePlayer extends DanceVideoPlayer {
    public FigureDancePlayer(Context context) {
        super(context);
        setReplay(true);
    }
}
