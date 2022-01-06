package com.kegel.pocketdance.conversion;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.kegel.pocketdance.AppDirectory;
import com.kegel.pocketdance.Assets;
import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.DanceData;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Convert {
    public static void convert(Context c) {
        File f = new File(Environment.getExternalStorageDirectory(), "PocketDance2");
        File updateXml = new File(f, "update.xml");
        String currentStyle = "";
        Map<String, DanceData.StyleData> styles = new HashMap<>();

        try (FileInputStream sr = new FileInputStream(updateXml)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(sr, null);
            parser.nextTag();

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("dance")) {
                    String styleName = parser.getAttributeValue(null, "name");
                    String styleDirectory = parser.getAttributeValue(null, "directory");
                    currentStyle = styleName;
                    if (!styles.containsKey(styleName)) {
                        styles.put(styleName, DanceData.StyleData.create(styleName, styleDirectory));
                    }
                }
                else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("figure")) {
                    String video = parser.getAttributeValue(null, "video");
                    String name = parser.nextText();
                    DanceData.StyleData.FigureData figData = DanceData.StyleData.FigureData.create(name, video);
                    if (!currentStyle.isEmpty()) {
                        styles.get(currentStyle).getFigures().add(figData);
                    }
                }
            }
        } catch (Exception e) {

        }
        Log.e(Constants.LOG_TAG, "All XML data converted");
        copyContent(c, styles);

    }

    private static void copyContent(Context c, Map<String, DanceData.StyleData> styles) {
        File srcRoot = new File(Environment.getExternalStorageDirectory(), "PocketDance2");
        DanceData danceData = Assets.getInstance(c).getDanceData();
        for (Map.Entry<String, DanceData.StyleData> style : styles.entrySet()) {
            String directory = style.getValue().getDirectory();
            File styleDirectory = AppDirectory.loadExternalFile(directory);
            if (!styleDirectory.exists()) {
                styleDirectory.mkdirs();
            }
            for (DanceData.StyleData.FigureData figure : style.getValue().getFigures()) {
                File dest = new File(styleDirectory, figure.getMedia());
                File srcDir = new File(srcRoot, directory);
                File src = new File(srcDir, figure.getMedia());
                if (!src.exists()) {
                    continue;
                }
                try {
                    if (!dest.exists()) {
                        FileUtils.copyFile(src, dest);
                    }
                } catch (IOException e) {
                    Log.e(Constants.LOG_TAG, String.format("Could not copy %s! %s", src, e.getMessage()));
                    continue;
                }
                danceData.add(style.getValue().getName(), figure.getName(), dest.getAbsolutePath());
            }
        }
        Assets.getInstance(c).updateDanceData();
    }
}
