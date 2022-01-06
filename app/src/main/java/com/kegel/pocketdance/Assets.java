package com.kegel.pocketdance;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Assets {
    private static Assets instance;

    Context ctx;
    private DanceData danceData;

    public Assets(Context ctx) {
        this.ctx = ctx;
    }

    public static Assets getInstance(Context ctx) {
        if (instance == null) {
            instance = new Assets(ctx);
        }
        return instance;
    }

    public DanceData getDanceData() {
        if (danceData == null) {
            AppDirectory.createPocketDirectory();
            Gson s = new Gson();
            File f = AppDirectory.loadExternalFile(AppDirectory.getDataFile());
            if (f.exists()) {
                try (FileReader reader = new FileReader(f)) {
                    danceData = s.fromJson(reader, DanceData.class);
                    if (danceData.verifyVideoFile()) {
                        updateDanceData();
                    }
                    if (danceData.cleanUpStrayVideos()) {
                        updateDanceData();
                    }
                } catch (Exception e) {
                    danceData = new DanceData();
                    Log.e("PocketDance", "Failed to open or read data.");
                }
            }
            else {
                danceData = new DanceData();
                addDefaultStypes(danceData);
                updateDanceData();
            }
        }
        return danceData;
    }

    private void addDefaultStypes(DanceData danceData) {
        TypedArray danceArray = ctx.getResources().obtainTypedArray(R.array.dance_styles);
        for (int i=0; i<danceArray.length(); ++i) {
            danceData.add(danceArray.getString(i));
        }
        danceArray.recycle();
    }

    public void updateDanceData() {
        if (danceData == null) {
            danceData = new DanceData();
        }
        Gson s = new GsonBuilder().setPrettyPrinting().create();
        File f = AppDirectory.loadExternalFile(AppDirectory.getDataFile());
        try (FileWriter w = new FileWriter(f)) {
            s.toJson(danceData, DanceData.class, s.newJsonWriter(w));
            w.flush();
        } catch (Exception e) {
            Log.e("PocketDance", "Failed to open or write data.");
        }
    }

    public List<String> getStyles() {
        DanceData data = getDanceData();
        List<String> ar = new ArrayList<>();
        Set<DanceData.StyleData> sd = data.getStyles();
        for (DanceData.StyleData styleItem : sd) {
            ar.add(styleItem.getName());
        }
        return ar;
    }

/*
    public void updateStyles(Context ctx) {
        styles = null;
        getStyles(ctx);
    }

    public List<String> getStyles(Context ctx) {
        if (styles != null) {
            return styles;
        }
        File styleJsonFile = AppDirectory.loadLocalFile(ctx, "styles.json");
        if (!styleJsonFile.exists()) {
            try {
                FileWriter st = new FileWriter(styleJsonFile);
                BufferedWriter writer = new BufferedWriter(st);
                JSONArray array = new JSONArray();
                array.put("Salsa");
                array.put("East Coast Swing");
                array.put("Rumba");
                array.put("Waltz");
                array.put("Foxtrot");
                array.put("Samba");
                array.put("Cha Cha");
                array.put("Bachata");
                array.put("Hustle");
                array.put("Merengue");
                array.put("Night Club Two Step");
                array.put("Quick Step");
                array.put("Tango");
                array.put("West Coast Swing");
                array.put("Line Dance");
                writer.write(array.toString());
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> s = new ArrayList<>();
        try {
            FileInputStream inputStream = new FileInputStream(styleJsonFile);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            JSONArray array = new JSONArray(new String(buffer));
            for (int i = 0; i < array.length(); ++i) {
                s.add(array.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            s.clear();
        }
        styles = s;
        return s;
    }
*/
}
