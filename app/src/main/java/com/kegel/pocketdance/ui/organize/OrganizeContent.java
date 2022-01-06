package com.kegel.pocketdance.ui.organize;

import android.text.Editable;

import java.io.File;
import java.io.Serializable;

public class OrganizeContent implements Serializable {
    private File mediaFile;
    private String style;
    private String figure;
    private long startTime;
    private long endTime;

    public OrganizeContent(File mediaFile, String style, String figure) {
        this.mediaFile = mediaFile;
        this.style = style;
        this.figure = figure;
        this.startTime = -1;
        this.endTime = -1;
    }

    public OrganizeContent(File mediaFile) {
        this(mediaFile, "", "");
    }

    public File getMediaFile() {
        return mediaFile;
    }

    public String getStyle() {
        return style;
    }

    public String getFigure() {
        return figure;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setFigure(String figure) {
        this.figure = figure;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
