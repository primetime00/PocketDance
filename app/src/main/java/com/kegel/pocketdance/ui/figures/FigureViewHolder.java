package com.kegel.pocketdance.ui.figures;

import android.content.Context;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.kegel.pocketdance.R;

public class FigureViewHolder extends RecyclerView.ViewHolder implements ViewTreeObserver.OnGlobalLayoutListener, View.OnCreateContextMenuListener {

    LinearLayout media_container;
    TextView title;
    ImageView thumbnail;
    PlayerView thumbnailVideo;
    View parent;
    RequestManager requestManager;

    public FigureViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        parent.getViewTreeObserver().addOnGlobalLayoutListener(this);
        media_container = itemView.findViewById(R.id.figure_layout);
        thumbnail = itemView.findViewById(R.id.figure_thumb);
        thumbnailVideo = itemView.findViewById(R.id.thumbnail_video);
        title = itemView.findViewById(R.id.figure_name);
        parent.setOnCreateContextMenuListener(this);
    }

    public View getRoot() {
        return parent;
    }

    private int convertToDp(int input, Context c) {
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (input * scale + 0.5f);
    }

    public TextView getTitle() {
        return title;
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public PlayerView getThumbnailVideo() {
        return thumbnailVideo;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public void onBind(FigureContent content, RequestManager requestManager) {
        this.requestManager = requestManager;
        parent.setTag(this);
        this.requestManager
                .load(content.getThumbnailFileName())
                .into(thumbnail);
        title.setText(content.getFigureName());
    }

    @Override
    public void onGlobalLayout() {
        float asp = (float)parent.getHeight() / parent.getWidth();
        if (asp > 0.9f) {
            parent.getLayoutParams().height = (int)(parent.getWidth() * 0.9f);
        }
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //menuInfo is null
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.remove_figure);
            //menu.add(Menu.NONE, 1, Menu.NONE, R.string.nothing);
    }
}
