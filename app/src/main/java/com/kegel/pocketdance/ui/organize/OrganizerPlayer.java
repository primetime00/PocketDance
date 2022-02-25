package com.kegel.pocketdance.ui.organize;

import android.content.Context;
import android.util.Log;

import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.video.DanceVideoPlayer;

import java.util.List;

public class OrganizerPlayer extends DanceVideoPlayer implements DanceVideoPlayer.DanceVideoListener {
    private List<OrganizeContent> content;
    private int currentIndex = 0;
    private ErrorListener errorListener;

    public OrganizerPlayer(Context context, List<OrganizeContent> content) {
        super(context);
        this.content = content;
        setReplay(true);
        this.getVideoPlayer().setVolume(1f);
    }


    public void setContentIndex(int index) {
        if (index != currentIndex) {
            currentIndex = index;
        }
    }

    public void loadContent() {
        if (currentIndex >= content.size()) {
            return;
        }
        if (currentIndex < 0) {
            return;
        }
        String mediaUrl = content.get(currentIndex).getMediaFile().getAbsolutePath();
        setStartTime(content.get(currentIndex).getStartTime());
        setEndTime(content.get(currentIndex).getEndTime());
        loadContent(mediaUrl);
    }

    public boolean markStartTime() {
        if (isClosed()) {
            return false;
        }
        long pos = getVideoPlayer().getContentPosition();
        if (pos < 200) {
            return false;
        }
        long endTime = getEndTime() > 0 ? getEndTime() : getVideoPlayer().getDuration();
        if (endTime - pos < Constants.MIN_RECORD_LENGTH) {
            //Log.w(Constants.LOG_TAG, String.format("You've tried to clip the video to %d ms, but that is too short.", endTime - pos));
            return false;
        }
        content.get(currentIndex).setStartTime(pos);
        setStartTime(pos);
        return true;
    }

    public boolean markEndTime() {
        if (isClosed()) {
            return false;
        }
        long pos = getVideoPlayer().getContentPosition();
        if (pos >= getVideoPlayer().getDuration()-200) {
            return false;
        }
        long startTime = content.get(currentIndex).getStartTime() > 0 ? content.get(currentIndex).getStartTime() : 0;
        if (pos - startTime < Constants.MIN_RECORD_LENGTH) {
            //Log.w(Constants.LOG_TAG, String.format("You've tried to clip the video to %d ms, but that is too short.", pos - startTime));
            return false;
        }
        content.get(currentIndex).setEndTime(pos);
        setEndTime(pos);
        restart();
        return true;
    }

    public void clearStartTime() {
        content.get(currentIndex).setStartTime(0);
        setStartTime(0);
    }

    public OrganizeContent getCurrentContent() {
        if (isClosed()) {
            return null;
        }
        return content.get(currentIndex);
    }

    public boolean hasNext() {
        return currentIndex+1 < content.size();
    }

    public boolean hasPrevious() {
        return currentIndex-1 >= 0;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void incrementContentIndex() {
        if (hasNext()) {
            setContentIndex(currentIndex + 1);
        }
    }

    public void decrementContentIndex() {
        if (hasPrevious()) {
            setContentIndex(currentIndex - 1);
        }
    }

    public int countContent() {
        return content.size();
    }

    public int removeContent() {
        int lastSize = content.size();
        content.remove(currentIndex);
        if (content.isEmpty()) { //the content list is now empty
            setContentIndex(0);
            return 0;
        }
        else if (currentIndex == lastSize-1) { //we removed the very last item
            setContentIndex(currentIndex-1);
            return 1;
        }
        else { //we've removed an item that was not he last
            return -1;
        }
    }

    public void clearEndTime() {
        content.get(currentIndex).setEndTime(0);
        setEndTime(0);
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onVideoEnded() {

    }

    @Override
    public void onPlayerError(String message) {
        //Log.e(Constants.LOG_TAG, "There was an error loading the content.");
        if (errorListener != null) {
            errorListener.onError(message);
        }
    }

    public interface ErrorListener {
        public void onError(String msg);
    }
}
