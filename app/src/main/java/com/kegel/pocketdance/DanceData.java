package com.kegel.pocketdance;

import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DanceData implements Serializable {
    public static int CURRENT_VERSION = 1;
    private int version;
    private Set<StyleData> styles;

    public DanceData() {
        this.version = CURRENT_VERSION;
        this.styles = new HashSet<>();
    }

    public void add(String style, String figure, String path) {
        add(style, figure, path, 0, 0);
    }


    public void add(String style, String figure, String mediaFile, long start, long end) {
        if (styles == null) {
            styles = new HashSet<>();
        }
        StyleData newStyle = null;
        for (StyleData storedStyle: styles) {
            if (storedStyle.name.toLowerCase().equals(style.toLowerCase())) {
                newStyle = storedStyle;
                break;
            }
        }
        if (newStyle == null) {
            newStyle = new StyleData();
            newStyle.name = style;
            newStyle.directory = mediaFile == null ? "" : FilenameUtils.getBaseName(new File(mediaFile).getParent());
            styles.add(newStyle);
        }
        else if ((newStyle.directory == null || newStyle.directory.isEmpty()) && mediaFile != null) {
            newStyle.directory = FilenameUtils.getBaseName(new File(mediaFile).getParent());
        }

        if (newStyle.figures == null) {
            newStyle.figures = new ArrayList<>();
        }
        if (figure != null && mediaFile != null) {
            StyleData.FigureData figureData = new StyleData.FigureData();
            figureData.name = figure;
            figureData.media = FilenameUtils.getName(mediaFile);
            figureData.plays = 0;
            figureData.startTime = start > 0 ? start : 0;
            figureData.endTime = end > figureData.startTime ? end : 0;
            newStyle.figures.add(figureData);
        }
    }

    public void add(String style) {
        add(style, null, null);
    }

    public Set<StyleData> getStyles() {
        return styles;
    }

    public StyleData.FigureData getFigureData(String styleName, String filename) {
        String figureFile = FilenameUtils.getName(filename);
        //let's search for the style name first
        for (StyleData styleData : styles) {
            if (styleData.getName().equals(styleName)) {
                for (StyleData.FigureData figureData : styleData.getFigures()) {
                    if (figureData.getMedia().equals(figureFile)) {
                        return figureData;
                    }
                }
            }
        }

        //worst case, lets just find the file
        for (StyleData styleData : styles) {
            for (StyleData.FigureData figureData : styleData.getFigures()) {
                if (figureData.getMedia().equals(figureFile)) {
                    return figureData;
                }
            }
        }
        return null;
    }

    public boolean verifyVideoFile() {
        boolean modified = false;
        for (Iterator<StyleData> sit = styles.iterator();sit.hasNext();) {
            StyleData styleData = sit.next();
            File dir = null;
            try {
                dir = AppDirectory.getStyleDirectory(styleData.getDirectory());
            } catch (IOException e) {
                sit.remove();
                modified = true;
                continue;
            }
            for (Iterator<StyleData.FigureData> fit = styleData.getFigures().iterator(); fit.hasNext();) {
                StyleData.FigureData figureData = fit.next();
                File nf = new File(dir, figureData.getMedia());
                if (!nf.exists() || !nf.isFile()) {
                    fit.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    public boolean cleanUpStrayVideos() {
        boolean modified = false;
        List<String> medias;
        for (StyleData styleData : styles) {
            if (styleData.getDirectory().isEmpty()) {
                continue;
            }
            medias = styleData.getFigureMedias();
            try {
                File dir = AppDirectory.getStyleDirectory(styleData.getDirectory());
                for (File media : dir.listFiles()) {
                    if (!FilenameUtils.getExtension(media.getName()).toLowerCase().equals("mp4")) {
                        continue;
                    }
                    if (!medias.contains(media.getName())) {
                        modified = true;
                        StyleData.FigureData fg = new StyleData.FigureData(media.getName(), "Unknown Figure");
                        styleData.getFigures().add(fg);
                    }
                }
            } catch (IOException ignored) {

            }
        }
        return modified;
    }

    public boolean removeFigureData(String name, String videoFileName) {
        StyleData.FigureData data = getFigureData(name, videoFileName);
        if (data == null) {
            //Log.e(Constants.LOG_TAG, "Could not find figure content to remove.");
            return false;
        }

        for (StyleData style : styles) {
            if (style.getFigures().remove(data)) {
                break;
            }
        }
        return true;
    }


    public static class StyleData implements Serializable {
        private String name;
        private String directory;
        private List<FigureData> figures;

        public static StyleData create(String name, String directory) {
            StyleData data = new StyleData();
            data.name = name;
            data.directory = directory;
            data.figures = new ArrayList<>();
            return data;
        }

        public String getName() {
            return name;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public List<FigureData> getFigures() {
            return figures;
        }

        public List<String> getFigureMedias() {
            List<String> medias = new ArrayList<>();
            for (FigureData data : figures) {
                medias.add(data.getMedia());
            }
            return medias;
        }

        public static class FigureData implements Serializable {
            private String name;
            private String media;
            private long startTime;
            private long endTime;
            private int plays;

            public FigureData() {
            }

            public FigureData(String media, String name) {
                this.media = media;
                this.name = name;
                this.startTime = 0;
                this.endTime = 0;
                this.plays = 0;
            }

            public static FigureData create(String name, String video) {
                FigureData data = new FigureData();
                data.media = video;
                data.name = name;
                return data;
            }

            public String getName() {
                return name;
            }

            public String getMedia() {
                return media;
            }

            public long getStartTime() {
                return startTime;
            }

            public long getEndTime() {
                return endTime;
            }

            public int getPlays() {
                return plays;
            }

            public void incrementPlays() {
                plays = plays+1;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                FigureData that = (FigureData) o;
                return startTime == that.startTime &&
                        endTime == that.endTime &&
                        plays == that.plays &&
                        Objects.equals(name, that.name) &&
                        Objects.equals(media, that.media);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, media);
            }
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StyleData styleData = (StyleData) o;
            return Objects.equals(name, styleData.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
