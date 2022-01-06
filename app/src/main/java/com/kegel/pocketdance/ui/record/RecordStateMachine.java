package com.kegel.pocketdance.ui.record;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.kegel.pocketdance.AppDirectory;
import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kegel.pocketdance.ui.record.RecordState.IDLE;
import static com.kegel.pocketdance.ui.record.RecordState.RECORDING;

public class RecordStateMachine {
    private boolean deleteVideo;
    private Activity activity;
    private ProcessCameraProvider cameraProvider;
    private PreviewView cameraView;
    private VideoCapture videoCapture;
    private RecordState recordState;
    private ImageButton recordButton;
    private ImageButton stopAndRecordButton;
    private TextView checkmark;
    private TextView xmark;
    private TextView recTime;
    private LinearLayout statusLayout;
    private TextView statusText;
    private long captureTime;
    private OnRecordingStopped onRecordingStopped = null;
    private Preview preview;
    private CameraSelector cameraSelector;
    private Handler timerHandler = new Handler();


    public RecordStateMachine(Activity activity, View view) {

        cameraView = view.findViewById(R.id.camera_preview);
        recordButton = view.findViewById(R.id.record_button);
        stopAndRecordButton = view.findViewById(R.id.record_next_button);
        checkmark = view.findViewById(R.id.checkmark);
        xmark = view.findViewById(R.id.x);
        statusLayout = view.findViewById(R.id.record_status_layout);
        statusText = view.findViewById(R.id.record_status_text);
        recTime = view.findViewById(R.id.record_time);
        this.recordState = RecordState.IDLE;
        this.activity = activity;
    }

    private Runnable onTimer = new Runnable() {
        @Override
        public void run() {
            long tm = System.currentTimeMillis() - captureTime;
            long minutes = tm / 60000;
            long seconds = (tm % 60000) / 1000;
            long ms = (tm % 1000) / 100;
            recTime.setText(String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, ms));
            timerHandler.postDelayed(onTimer, 300);
        }
    };

    public void prepare(ProcessCameraProvider provider) {
        cameraProvider = provider;
        setupCamera();
    }
    public void cleanup() {
    }

    public void setState(RecordState state) {
        RecordState currentState = recordState;
        switch (state) {
            case IDLE: //we want to get to idle state
                if (currentState == RECORDING) {
                    stopRecording(new OnRecordingStopped() {
                        @Override
                        public void onRecordingStopped(boolean error) {

                        }

                        @Override
                        public void whileRecording() {
                            animateRecordToStopped();
                        }
                    });
                }
                else {
                    return;
                }
                break;
            case RECORDING:
                if (currentState == IDLE) {
                    animateStoppedToRecord();
                    startRecording();
                }
                break;
            case RESTART_RECORD:
                if (currentState == RECORDING) {
                    stopRecording(new OnRecordingStopped() {
                        @Override
                        public void onRecordingStopped(boolean error) {
                            startRecording();
                        }

                        @Override
                        public void whileRecording() {
                            animateReRecord();
                        }
                    });
                }
                break;
        }
    }

    private File setupRecordFile() {
        File movieDir = AppDirectory.loadExternalFile(AppDirectory.getRecordingsPath());

        if(!movieDir.exists())
            movieDir.mkdirs();

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String timestamp = df.format(date);
        String vidFilePath = movieDir.getAbsolutePath() + "/" + timestamp + ".mp4";
        File vidFile = new File(vidFilePath);
        return vidFile;
    }

    @SuppressLint("RestrictedApi")
    private void setupCamera() {
        cameraProvider.unbindAll();
        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        cameraView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(cameraView.getDisplay().getRotation())
                //.setMaxResolution(new Size(1280, 720))
                .build();
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .setMaxResolution(new Size(1280, 720))
                .setTargetResolution(new Size(1280, 720))
                .setBitRate(2000000)
                ///.setAudioBitRate(64000)
                ///.setAudioSampleRate(48000)
                .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, preview, videoCapture);
    }

    @SuppressLint("RestrictedApi")
    public boolean stopRecording(OnRecordingStopped recInf) {
        timerHandler.removeCallbacks(onTimer);
        if (videoCapture != null) {
            onRecordingStopped = recInf;
            captureTime = System.currentTimeMillis() - captureTime;
            if (captureTime < Constants.MIN_RECORD_LENGTH) { //if the capture length was less than 3.5 seconds, let's not keep it.  Might have been a mistake
                deleteVideo = true;
                showVideoCancelled();
            }
            else {
                deleteVideo = false;
                showVideoSaved();
            }
            videoCapture.stopRecording();
            onRecordingStopped.whileRecording();
        }
        return true;
    }

    @SuppressLint("RestrictedApi")
    private boolean startCapture() {
        if (videoCapture == null) {
            return false;
        }
        File captureFile = setupRecordFile();
        captureTime = System.currentTimeMillis();
        deleteVideo = false;
        recordState = RECORDING;
        videoCapture.startRecording(
                new VideoCapture.OutputFileOptions.Builder(captureFile)
                        .setMetadata(new VideoCapture.Metadata())
                        .build(),
                ContextCompat.getMainExecutor(activity),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        if (deleteVideo) {
                            deleteVideo = false;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    File fp = new File(outputFileResults.getSavedUri().getEncodedPath());
                                    if (fp.exists()) {
                                        fp.delete();
                                    }
                                }
                            });
                        }
                        recordState = IDLE;
                        if (onRecordingStopped != null) {
                            onRecordingStopped.onRecordingStopped(false);
                        }
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        deleteVideo = false;
                        recordState = IDLE;
                        if (onRecordingStopped != null) {
                            onRecordingStopped.onRecordingStopped(true);
                        }
                    }
                }
        );
        return true;
    }

    public boolean startRecording() {
        if (recordState == IDLE && videoCapture != null) {
            timerHandler.postDelayed(onTimer, 300);
            recTime.setAlpha(0f);
            recTime.animate().alpha(1).setDuration(600).start();
            startCapture();
            return true;
        }
        return false;
    }

    private void animateReRecord() {
        recordButton.setSelected(false);
        recordButton.postDelayed((Runnable) () -> recordButton.setSelected(true), 200);
    }

    private void animateRecordToStopped() {
        recTime.animate().alpha(0).setDuration(600).start();
        recordButton.setSelected(false);
        stopAndRecordButton.setAlpha(1f);
        stopAndRecordButton.setVisibility(View.VISIBLE);
        stopAndRecordButton.animate().alpha(0f).setDuration(200).start();
    }

    private void animateStoppedToRecord() {
        recordButton.setSelected(true);
        stopAndRecordButton.setAlpha(0f);
        stopAndRecordButton.setVisibility(View.VISIBLE);
        stopAndRecordButton.animate().alpha(1f).setDuration(200).start();
    }

    private void showVideoCancelled() {
        statusLayout.setAlpha(0f);
        statusText.setText(R.string.dance_not_captured);
        checkmark.setVisibility(View.GONE);
        xmark.setVisibility(View.VISIBLE);
        AnimatorSet as = new AnimatorSet();
        as.playTogether(ObjectAnimator.ofFloat(statusLayout, "translationX", 300f, -100f).setDuration(400),
                            ObjectAnimator.ofFloat(statusLayout, "alpha", 1f).setDuration(100));
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(statusLayout, "alpha", 0f).setDuration(500);
                anim.setStartDelay(500);
                anim.start();
            }
        });
        as.start();
    }

    private void showVideoSaved() {

        statusLayout.setAlpha(0f);
        xmark.setVisibility(View.GONE);
        statusText.setText(R.string.dance_captured);
        checkmark.setVisibility(View.VISIBLE);
        AnimatorSet as = new AnimatorSet();
        as.playTogether(ObjectAnimator.ofFloat(statusLayout, "translationX", 300f, -100f).setDuration(400),
                ObjectAnimator.ofFloat(statusLayout, "alpha", 1f).setDuration(100));
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(statusLayout, "alpha", 0f).setDuration(500);
                anim.setStartDelay(500);
                anim.start();
            }
        });
        as.start();
/*
        checkmark.setAlpha(0f);
        checkmark.setVisibility(View.VISIBLE);
        //xmark.setVisibility(View.INVISIBLE);
        checkmark.animate().alpha(1.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                checkmark.postDelayed(() -> {
                    checkmark.setVisibility(View.INVISIBLE);
                    animationDone = true;
                    if (!recording) {
                        if (onRecordingStopped != null) {
                            onRecordingStopped.onRecordingStopped(false);
                        }
                    }
                }, 500);
            }
        }).start();*/
    }
}
