package com.kegel.pocketdance.ui.figures;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.kegel.pocketdance.AppDirectory;
import com.kegel.pocketdance.DanceData;
import com.kegel.pocketdance.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FigureRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<FigureContent> figures;
    private String style;
    private RequestManager requestManager;
    private Context appContext;
    private GestureDetector detector;
    private final OnFigureClickListener listener;



    public FigureRecyclerAdapter(DanceData.StyleData content, Context appContext, RequestManager requestManager, OnFigureClickListener listener) {
        this.requestManager = requestManager;
        this.appContext = appContext;
        this.style = content.getName();
        this.figures = createFigures(content);
        this.listener = listener;
        this.detector = new GestureDetector(appContext, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                listener.onFigureClickListener(figures.get(e.getSource()));
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                listener.onFigureLongClick(figures.get(e.getSource()), e.getSource());
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    private List<FigureContent> createFigures(DanceData.StyleData styleContent) {
        String dir = String.format("%s%s/", AppDirectory.getPocketPath(), styleContent.getDirectory());
        List<FigureContent> figureList = new ArrayList<>();
        for (DanceData.StyleData.FigureData info : styleContent.getFigures()) {
            String videoFilePath = Environment.getExternalStorageDirectory()+dir+info.getMedia();
            String thumbFilePath = appContext.getFilesDir() + "/" + AppDirectory.getThumbPath()+ "/" + String.format(Locale.US, "%x.jpg", info.hashCode());
            FigureContent content = new FigureContent(videoFilePath, info.getName(), thumbFilePath, info.getPlays(), info.getStartTime(), info.getEndTime());
            figureList.add(content);
        }
        Collections.sort(figureList, (o1, o2) -> o2.getPlays() - o1.getPlays());
        return figureList;
    }

    public List<FigureContent> getFigures() {
        return figures;
    }

    public void remove(int index) {
        figures.remove(index);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FigureViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.figure_fragment_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        ((FigureViewHolder)holder).onBind(figures.get(i), requestManager);
        ((FigureViewHolder) holder).media_container.setOnTouchListener((v, event) -> {
            event.setSource(i);
            detector.onTouchEvent(event);
            return true;
        });

    }

    public String getStyleName() {
        return style;
    }

    @Override
    public int getItemCount() {
        if (figures != null)
            return figures.size();
        return 0;
    }
}
