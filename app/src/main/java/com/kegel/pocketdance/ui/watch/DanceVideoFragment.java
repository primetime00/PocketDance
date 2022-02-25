package com.kegel.pocketdance.ui.watch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.ui.PlayerView;
import com.kegel.pocketdance.Assets;
import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.DanceData;
import com.kegel.pocketdance.R;
import com.kegel.pocketdance.ui.figures.FigureContent;
import com.kegel.pocketdance.video.DanceVideoPlayer;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DanceVideoFragment extends Fragment {

    private PlayerView videoView;
    private DanceWatchPlayer player;


    //private MediaPlayer mediaPlayer;
    private TextView speedText;
    private GestureDetector gestureDetector;
    enum PlaybackSpeed {
        NORMAL,
        SLOW,
        SLOWEST
    }
    private PlaybackSpeed playbackSpeed = PlaybackSpeed.NORMAL;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            Activity activity = getActivity();
            if (activity != null
                    && activity.getWindow() != null) {
                activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            }
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dance_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        player = new DanceWatchPlayer(getActivity());


        mVisible = true;
        gestureDetector = new GestureDetector(getActivity(), new DanceVideoGestureListener() {
            @Override
            public boolean onDoublePress(MotionEvent e) {
                //Log.d("PocketDance", "Double Press");
                if (player != null) {
                    player.restart();
                    if (player.isPaused()) {
                        player.play();
                    }
                }
                return false;
            }

            @Override
            public boolean onFlingBack() {
                //Log.d("PocketDance", "Fling Back");
                if (playbackSpeed == PlaybackSpeed.NORMAL) {
                    speedText.setText(R.string.half_speed);
                    speedText.animate().alpha(1).setStartDelay(0).setDuration(200).start();
                    setPlaybackSpeed(PlaybackSpeed.SLOW);
                } else if (playbackSpeed == PlaybackSpeed.SLOW) {
                    setPlaybackSpeed(PlaybackSpeed.SLOWEST);
                    speedText.setText(R.string.quarter_speed);
                }
                return false;
            }

            @Override
            public boolean onFlingForward() {
                //Log.d("PocketDance", "Fling Forward");
                if (playbackSpeed == PlaybackSpeed.SLOW) {
                    setPlaybackSpeed(PlaybackSpeed.NORMAL);
                    speedText.setText(R.string.normal_speed);
                    speedText.animate().alpha(0).setStartDelay(1000).setDuration(200).start();
                } else if (playbackSpeed == PlaybackSpeed.SLOWEST) {
                    setPlaybackSpeed(PlaybackSpeed.SLOW);
                    speedText.setText(R.string.half_speed);
                }
                return false;
            }

            @Override
            public boolean onTap() {
                //Log.d("PocketDance", "Tap");
                if (player != null) {
                    if (!player.isPaused()) {
                        player.pause();
                    } else {
                        player.play();
                    }
                }
                return false;
            }
        });

        speedText = view.findViewById(R.id.speed_text);


        videoView = view.findViewById(R.id.dance_video_view);
        videoView.setPlayer(player.getVideoPlayer());

        player.setListener(new DanceVideoPlayer.DanceVideoListener() {
            @Override
            public void onLoaded() {
                setPlaybackSpeed(PlaybackSpeed.NORMAL);
            }

            @Override
            public void onVideoEnded() {

            }

            @Override
            public void onPlayerError(String error) {

            }
        });



        videoView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        Bundle b = getArguments();
        FigureContent content = (FigureContent) b.getSerializable(getString(R.string.video_data));
        String uri = content.getVideoFileName();

        player.setStartTime(content.getStart());
        player.setEndTime(content.getEnd());
        player.loadContent(uri);

        videoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Log.e(Constants.LOG_TAG, "Layout happened");
                int []pos = new int[2];
                videoView.getLocationOnScreen(pos);
                speedText.setY(videoView.getHeight() - speedText.getHeight() + pos[1]);
            }
        });

        //videoView.start();
        // Set up the user interaction to manually show or hide the system UI.

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onPause() {
        super.onPause();
        player.pause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }
        show();
    }

    @Override
    public void onDestroy() {
        player.close();
        super.onDestroy();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Nullable
    private ActionBar getSupportActionBar() {
        ActionBar actionBar = null;
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            actionBar = activity.getSupportActionBar();
        }
        return actionBar;
    }

    private void setPlaybackSpeed(PlaybackSpeed speed) {
        if (player != null) {
            float rawSpeed = 1.0f;
            playbackSpeed = speed;
            switch (playbackSpeed) {
                case NORMAL:
                    rawSpeed = 1.0f;
                    break;
                case SLOW:
                    rawSpeed = 0.5f;
                    break;
                case SLOWEST:
                    rawSpeed = 0.25f;
                    break;
            }
            PlaybackParameters pm = new PlaybackParameters(rawSpeed);
            player.getVideoPlayer().setPlaybackParameters(pm);

            //mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(rawSpeed));
        } else {
            //Log.e("PocketDance", "Could not set playback speed.");
        }
    }
}