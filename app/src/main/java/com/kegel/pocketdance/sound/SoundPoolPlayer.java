package com.kegel.pocketdance.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.kegel.pocketdance.R;

import java.util.HashMap;
import java.util.Map;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer= null;
    private final Map<Integer, Integer> mSounds = new HashMap();

    static private SoundPoolPlayer instance;

    static public SoundPoolPlayer getInstance(Context ctx) {
        if (instance == null) {
            instance = new SoundPoolPlayer(ctx);
        }
        return instance;
    }

    public SoundPoolPlayer(Context pContext)
    {
        // setup Soundpool
        this.mShortPlayer = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        //this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);


        mSounds.put(R.raw.rec_start, this.mShortPlayer.load(pContext, R.raw.rec_start, 1));
        mSounds.put(R.raw.rec_stop, this.mShortPlayer.load(pContext, R.raw.rec_stop, 1));
        mSounds.put(R.raw.re_rec, this.mShortPlayer.load(pContext, R.raw.re_rec, 1));
    }

    public void playSound(int piResource) {
        Integer iSoundId = mSounds.get(piResource);
        if (iSoundId != null) {
            this.mShortPlayer.play(iSoundId, 0.025f, 0.025f, 0, 0, 1);
        }
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
        instance = null;
    }
}
