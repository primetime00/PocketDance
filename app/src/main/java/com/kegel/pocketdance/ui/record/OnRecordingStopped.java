package com.kegel.pocketdance.ui.record;

public interface OnRecordingStopped {
    void onRecordingStopped(boolean error);
    void whileRecording();
}
