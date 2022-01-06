package com.kegel.pocketdance.ui.figures;

import java.io.Serializable;

public class FigureContent implements Serializable {
    private String videoFileName;
    private String figureName;
    private String thumbnailFileName;
    private int plays;
    private long start;
    private long end;

    public FigureContent(String fileName, String danceName, String thumbnailFileName, int plays, long start, long end) {
        this.videoFileName = fileName;
        this.figureName = danceName;
        this.thumbnailFileName = thumbnailFileName;
        this.plays = plays;
        this.start = start;
        this.end = end;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public String getFigureName() {
        return figureName;
    }

    public int getPlays() {
        return plays;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getThumbnailFileName() {
        return thumbnailFileName;
    }
}
