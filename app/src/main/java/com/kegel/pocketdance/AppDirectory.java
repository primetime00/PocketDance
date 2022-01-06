package com.kegel.pocketdance;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppDirectory {
    private static final String pocketPath = "/PocketDance/";
    private static final String recordPath = ".Recordings";
    private static final String thumbPath = "thumbs";
    private static final String dataPath = "danceData.json";

    public static File loadLocalFile(Context ctx, String path) {
        return new File(ctx.getFilesDir(), path);
    }

    public static String getThumbPath() {
        return thumbPath;
    }

    public static String getPocketPath() {
        return pocketPath;
    }

    public static String getRecordingsPath() {
        return recordPath;
    }

    public static File loadExternalFile(String file) {
        File f = new File(Environment.getExternalStorageDirectory(), getPocketPath());
        File ld = new File(f, file);
        return ld;
    }

    public static String getDataFile() {
        return dataPath;
    }

    public static String incrementExistingExternalFile(File dir, String file) {
        Pattern PATTERN = Pattern.compile("(.*?)-(\\d+)\\.([^.]*)");
        String base = FilenameUtils.getBaseName(file);
        String ext = FilenameUtils.getExtension(file);
        File newFile = null;
        Matcher m = PATTERN.matcher(file);
        if (m.matches()) {
            String prefix = m.group(1);
            String last = m.group(2);
            String suffix = m.group(3);
            if (suffix == null) suffix = "";

            int count = last != null ? Integer.parseInt(last) : 0;
            do {
                count++;
                newFile = new File(dir, prefix + "-" + String.format(Locale.US, "%03d", count) + "." + suffix);
            } while (newFile.exists());
        }
        else {
            int count = 0;
            do {
                count++;
                newFile = new File(dir, base + "-" + String.format(Locale.US, "%03d", count) + "." + ext);
            } while (newFile.exists());
        }
        return newFile.getName();
    }

    public static String dateFile(String base, String ext, String originalName) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String timestamp = df.format(date);
        if (originalName == null) {
            return base + "-" + timestamp + "." + ext;
        }
        //check original file for date
        Pattern PATTERN = Pattern.compile(".*?-(\\d\\d-\\d\\d-\\d\\d\\d\\d_\\d\\d-\\d\\d-\\d\\d).*");
        Matcher m = PATTERN.matcher(originalName);
        if (m.matches()) {
            timestamp = m.group(1);
            return base + "-" + timestamp + "." + ext;
        }
        return base + "-" + timestamp + "." + ext;
    }

    public static void createStyleDirectory(String styleDir) {
        File f = AppDirectory.loadExternalFile(styleDir);
        if (!f.exists()) {
            boolean res = f.mkdirs();
            if (!res) {
                Log.e(Constants.LOG_TAG, String.format("Could not create style directory %s", styleDir));
                return;
            }
        }
    }

    public static File getStyleDirectory(String styleDir) throws IOException {
        File f = AppDirectory.loadExternalFile(styleDir);
        if (f.exists()) {
            return f;
        }
        throw new IOException("The style directory doesn't exist.");
    }

    public static void createPocketDirectory() {
        File f = new File(Environment.getExternalStorageDirectory(), getPocketPath());
        if (!f.exists()) {
            boolean res = f.mkdirs();
            if (!res) {
                Log.e(Constants.LOG_TAG, "Could not create main Pocket Dance directory!");
            }
        }
        File recFile = new File(f, recordPath);
        if (!recFile.exists()) {
            boolean res = recFile.mkdirs();
            if (!res) {
                Log.e(Constants.LOG_TAG, "Could not create main Pocket Dance Recording directory!");
            }
        }
    }
}
