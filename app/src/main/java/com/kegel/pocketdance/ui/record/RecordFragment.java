package com.kegel.pocketdance.ui.record;

import android.annotation.SuppressLint;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.kegel.pocketdance.R;
import com.kegel.pocketdance.sound.SoundPoolPlayer;

import java.util.concurrent.ExecutionException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecordFragment extends Fragment {

    private PreviewView mCameraView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageButton recordButton;
    private ImageButton stopAndRecordButton;
    private RecordStateMachine recordMachine;

    private RecordState recordState = RecordState.IDLE;

    private VideoCapture.OnVideoSavedCallback currentCallback = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCameraView = view.findViewById(R.id.camera_preview);
        recordButton = view.findViewById(R.id.record_button);
        stopAndRecordButton = view.findViewById(R.id.record_next_button);
        recordMachine = new RecordStateMachine(getActivity(), view);
        hide();

        recordButton.setOnClickListener(v -> {
            if (v.isSelected()) {
                recordMachine.setState(RecordState.IDLE);
                SoundPoolPlayer.getInstance(getActivity()).playSound(R.raw.rec_stop);
            }
            else {
                recordMachine.setState(RecordState.RECORDING);
                SoundPoolPlayer.getInstance(getActivity()).playSound(R.raw.rec_start);
            }
        });

        stopAndRecordButton.setOnClickListener(v -> {
            recordMachine.setState(RecordState.RESTART_RECORD);
            SoundPoolPlayer.getInstance(getActivity()).playSound(R.raw.re_rec);
        });

        cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try {
                recordMachine.prepare(cameraProviderFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getActivity()));

    }

    @Override
    public void onDetach() {
        show();
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        hide();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }
        //show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void toggle() {
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
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

}