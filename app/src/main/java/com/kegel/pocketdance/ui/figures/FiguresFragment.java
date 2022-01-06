package com.kegel.pocketdance.ui.figures;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.kegel.pocketdance.AppDirectory;
import com.kegel.pocketdance.Assets;
import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.DanceData;
import com.kegel.pocketdance.R;
import com.kegel.pocketdance.video.VideoPlayerRecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class FiguresFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private FigureRecyclerAdapter adapter;
    private CardView organizeCard;
    private RecyclerView recyclerView;
    private int selectedPosition;
    private FigureContent selectedContent;
    private DanceData.StyleData selectedStyle;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FiguresFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FiguresFragment newInstance(int columnCount) {
        FiguresFragment fragment = new FiguresFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.figure_fragment_item_list, container, false);

        Bundle style = getArguments();

        if (style == null) {
            //this shouldn't happen
            Navigation.findNavController(view).popBackStack();
            return view;
        }

        selectedStyle = (DanceData.StyleData) style.getSerializable(getContext().getString(R.string._style));
        organizeCard = view.findViewById(R.id.create_thumbnail);

        VideoPlayerRecyclerView rView = view.findViewById(R.id.figure_list);
        // Set the adapter
        if (rView instanceof RecyclerView) {
            recyclerView = (RecyclerView) rView;
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            registerForContextMenu(recyclerView);
            if (adapter != null && adapter.getStyleName().equals(selectedStyle.getName())) {
                recyclerView.setAdapter(adapter);
            }
            else {
                if (countThumbsNeeded(selectedStyle) > 3) {
                    organizeCard.setVisibility(View.VISIBLE);
                }
                createThumbnails(selectedStyle, () -> {
                    createAdapter();
                    recyclerView.setAdapter(adapter);
                    organizeCard.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            organizeCard.setVisibility(View.INVISIBLE);
                            organizeCard.setAlpha(1f);
                        }
                    });
                });
            }
        }
        return view;
    }

    private void createAdapter() {
        adapter = new FigureRecyclerAdapter(selectedStyle, getActivity(), initGlide(), new OnFigureClickListener() {
            @Override
            public void onFigureClickListener(FigureContent content) {
                Bundle b = new Bundle();
                DanceData data = Assets.getInstance(getActivity()).getDanceData();
                DanceData.StyleData.FigureData figureData = data.getFigureData(selectedStyle.getName(), content.getVideoFileName());

                if (figureData != null) {
                    figureData.incrementPlays();
                    Assets.getInstance(getActivity()).updateDanceData();
                }
                b.putSerializable(getString(R.string.video_data), content);
                Navigation.findNavController(getView()).navigate(R.id.nav_dance_video, b);
            }

            @Override
            public void onFigureLongClick(FigureContent content, int index) {
                RecyclerView.ViewHolder view = recyclerView.findViewHolderForAdapterPosition(index);
                if (view instanceof FigureViewHolder) {
                    recyclerView.showContextMenuForChild(((FigureViewHolder) view).getRoot());
                    selectedPosition = index;
                    selectedContent = content;
                }
            }
        });
    }


    private RequestManager initGlide(){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                askForRemoval(selectedContent, selectedPosition);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);

    }

    private void askForRemoval(FigureContent content, int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(getString(R.string.remove_figure_title))
                .setMessage(R.string.remove_figure_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeContent(content, index);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void removeContent(FigureContent content, int index) {
        DanceData data = Assets.getInstance(getActivity()).getDanceData();
        if (!data.removeFigureData(selectedStyle.getName(), content.getVideoFileName())) {
            return;
        }
        Assets.getInstance(getActivity()).updateDanceData();
        adapter.remove(index);
        File thumb = new File(content.getThumbnailFileName());
        thumb.delete();

        File src = new File(content.getVideoFileName());
        String fname = AppDirectory.dateFile(String.format("%X", Objects.hash(src.getName(), selectedStyle.getName())), "mp4", src.getName());

        File dest = new File(AppDirectory.loadExternalFile(AppDirectory.getRecordingsPath()), fname);

        try {
            FileUtils.moveFile(src, dest);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, String.format("Cannot move file: %s", e.getMessage()));
        }
        if (adapter.getItemCount() == 0) {
            Navigation.findNavController(recyclerView).popBackStack();
        }
    }

    private int countThumbsNeeded(DanceData.StyleData styleContent) {
        File out = new File(getActivity().getFilesDir(), AppDirectory.getThumbPath());
        int count = 0;
        for (DanceData.StyleData.FigureData info : styleContent.getFigures()) {
            String fname = String.format(Locale.US, "%x.jpg", info.hashCode());
            File thumbFile = new File(out, fname);
            if (!thumbFile.exists()) {
                count++;
            }
        }
        return count;
    }


    private void createThumbnails(DanceData.StyleData styleContent, Runnable after) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            String dir = String.format("%s%s/", AppDirectory.getPocketPath(), styleContent.getDirectory());
            File out = new File(getActivity().getFilesDir(), AppDirectory.getThumbPath());
            out.mkdirs();
            for (DanceData.StyleData.FigureData info : styleContent.getFigures()) {
                String fname = String.format(Locale.US, "%x.jpg", info.hashCode());
                File thumbFile = new File(out, fname);
                if (thumbFile.exists()) {
                    continue;
                }
                File videoFile = new File(Environment.getExternalStorageDirectory()+dir, info.getMedia());
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoFile.getAbsolutePath(), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                try (FileOutputStream s = new FileOutputStream(thumbFile)) {
                    stream.writeTo(s);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mainHandler.post(() -> {
                after.run();
            });
        });
    }

}