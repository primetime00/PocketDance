package com.kegel.pocketdance.ui.organize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.textfield.TextInputLayout;
import com.kegel.pocketdance.AppDirectory;
import com.kegel.pocketdance.Assets;
import com.kegel.pocketdance.Constants;
import com.kegel.pocketdance.DanceData;
import com.kegel.pocketdance.R;
import com.kegel.pocketdance.ui.watch.DanceVideoGestureListener;
import com.kegel.pocketdance.utils.FileNameCleaner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_ENTER;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrganizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizeFragment extends Fragment {

    private PlayerView video_view;
    private OrganizerPlayer player;
    private GestureDetector gestureDetector;
    private DataSource.Factory dataSourceFactory;
    private List<OrganizeContent> mediaData;
    private LinearLayout button_layout;
    private ConstraintLayout style_name_layout;
    private ConstraintLayout figure_name_layout;
    private Button nameButton;
    private Button deleteButton;
    private Button markStartButton;
    private Button markEndButton;
    private ImageButton styleNameSubmitButton;
    private ImageButton styleNameCancelButton;

    private ImageButton figureNameSubmitButton;
    private ImageButton figureNameCancelButton;

    private MarkButtonHelper markStartHelper;
    private MarkButtonHelper markEndHelper;

    private CardView markStartMenuView;
    private ConstraintLayout.LayoutParams markStartMenuLayout;


    private AppCompatAutoCompleteTextView autoText;

    private TextInputLayout styleName;
    private TextInputLayout figureName;

    private View noDancesView;
    private View toolBar;
    private View numRecordingsLayout;
    private TextView numRecordingsText;

    private final Handler focusChecker = new Handler();
    private boolean itemSelected = false;
    private Runnable focusChecherRunnable = new Runnable() {
        @Override
        public void run() {
            if (autoText != null && autoText.hasFocus()) {
                if (itemSelected) {
                    itemSelected = false;
                    ((AutoAdapter)autoText.getAdapter()).getFilter().filter(autoText.getEditableText());
                }
                autoText.showDropDown();
            }
            focusChecker.postDelayed(this, 500);
        }
    };



    public OrganizeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrganizeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizeFragment newInstance(String param1, String param2) {
        OrganizeFragment fragment = new OrganizeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_organize, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        video_view = view.findViewById(R.id.video_view);
        nameButton = view.findViewById(R.id.name_button);
        deleteButton = view.findViewById(R.id.delete_button);
        markStartButton = view.findViewById(R.id.start_button);
        markEndButton = view.findViewById(R.id.end_button);
        button_layout = view.findViewById(R.id.organize_button_layout);
        style_name_layout = view.findViewById(R.id.organize_style_name_layout);
        figure_name_layout = view.findViewById(R.id.organize_figure_name_layout);

        noDancesView = view.findViewById(R.id.no_recordings);
        toolBar = view.findViewById(R.id.tool_bar);
        numRecordingsLayout = view.findViewById(R.id.num_recordings_layout);
        numRecordingsText = view.findViewById(R.id.num_recordings);

        styleNameSubmitButton = view.findViewById(R.id.submit_style_name);
        styleNameCancelButton = view.findViewById(R.id.cancel_style_name);

        figureNameSubmitButton = view.findViewById(R.id.submit_figure_name);
        figureNameCancelButton = view.findViewById(R.id.cancel_figure_name);

        markStartMenuView = view.findViewById(R.id.mark_start_menu);
        markStartMenuLayout = (ConstraintLayout.LayoutParams) markStartMenuView.getLayoutParams();

        styleName = view.findViewById(R.id.organize_style_name);
        autoText = view.findViewById(R.id.organize_style_name_edit);
        autoText.setThreshold(1);
        AutoAdapter la = new AutoAdapter(getActivity(), Assets.getInstance(getActivity()).getStyles());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        autoText.setDropDownBackgroundResource(R.color.autocomplete_background);
        autoText.setAdapter(la);

        focusChecker.postDelayed(focusChecherRunnable, 500);

        autoText.addTextChangedListener(new OnOrganizeTextListener() {
            @Override
            public void textChanged(String text) {
                player.getCurrentContent().setStyle(text.trim());;
            }
        });

        autoText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KEYCODE_ENTER) {
                styleNameSubmitButton.callOnClick();
            }
            return false;
        });


        autoText.setOnItemClickListener((parent, view1, position, id) -> itemSelected = true);

        autoText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoText.showDropDown();
            }
        });


        figureName = view.findViewById(R.id.organize_figure_name);
        figureName.getEditText().addTextChangedListener(new OnOrganizeTextListener() {
            @Override
            public void textChanged(String text) {
                player.getCurrentContent().setFigure(text.trim());
            }
        });

        gestureDetector = new GestureDetector(getActivity(), new DanceVideoGestureListener() {
            @Override
            public boolean onDoublePress(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onFlingBack() {
                int w = video_view.getWidth();
                if (player.hasNext()) {
                    flingView(video_view, 0, 0, w, video_view.getWidth()*4, (animation, canceled, value, velocity) -> {
                        player.incrementContentIndex();
                        animateLoad(-w, -w, 0, video_view.getWidth()*2);
                    });
                }
                return false;
            }

            @Override
            public boolean onFlingForward() {
                int w = video_view.getWidth();
                if (player.hasPrevious()) {
                    flingView(video_view, 0, -w, 0, video_view.getWidth()*-4, (animation, canceled, value, velocity) -> {
                        player.decrementContentIndex();
                        animateLoad(w, 0, w, video_view.getWidth()*-2);
                    });
                }
                return false;
            }

            @Override
            public boolean onTap() {
                if (!player.isPaused()) {
                    player.pause();
                } else {
                    player.play();
                }
                return false;
            }
        });

        video_view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        mediaData = getRecordingList();

        if (!mediaData.isEmpty()) {

            setupVideo();
            setupButtons(view);


            dataSourceFactory = new DefaultDataSourceFactory(
                    getActivity(), Util.getUserAgent(getActivity(), "PocketDance Video Organizer"));

            player.setContentIndex(0);
            numRecordingsText.setText(String.format(Locale.US, "%d/%d", player.getCurrentIndex()+1, player.countContent()));
            showDanceScreen(true, false);
            player.loadContent();

        }
        else {
            showDanceScreen(false, false);
        }
    }

    private void animateLoad(int start, int min, int max, int velo) {
        numRecordingsText.setText(String.format(Locale.US, "%d/%d", player.getCurrentIndex()+1, player.countContent()));
        player.loadContent();
        video_view.setAlpha(0);
        video_view.animate().alpha(1.0f).setDuration(1000).start();
        flingView(video_view, start, min, max, velo, (animation, canceled, value, velocity) -> {
            //video_view.setAlpha(0);
            video_view.setScrollX(0);
            //video_view.animate().alpha(1.0f).setDuration(500).start();
            styleName.getEditText().setText(player.getCurrentContent().getStyle());
            figureName.getEditText().setText(player.getCurrentContent().getFigure());
            markStartButton.setSelected(player.getCurrentContent().getStartTime() > 0);
            markEndButton.setSelected(player.getCurrentContent().getEndTime() > 0);

        });

    }

    private void showDanceScreen(boolean visible, boolean animate) {
        if (visible) {
            video_view.setVisibility(View.VISIBLE);
            toolBar.setVisibility(View.VISIBLE);
            numRecordingsLayout.setVisibility(View.VISIBLE);
            noDancesView.setVisibility(View.INVISIBLE);
        } else {
            video_view.setVisibility(View.INVISIBLE);
            toolBar.setVisibility(View.INVISIBLE);
            numRecordingsLayout.setVisibility(View.INVISIBLE);
            noDancesView.setVisibility(View.VISIBLE);

        }
    }

    private void flingView(View view, int startX, int min, int max, int velo, DynamicAnimation.OnAnimationEndListener onEnd) {
        FlingAnimation fling = new FlingAnimation(view, DynamicAnimation.SCROLL_X);
        view.setScrollX(startX);

        fling.setStartVelocity(velo)
                .setMinValue(min)
                .setMaxValue(max)
                .setFriction(0.2f)
                .addEndListener(onEnd)
                //.addEndListener((animation, canceled, value, velocity) -> {
                    //videoPlayer.stop();
                    //currentMediaIndex++;
                    //loadMedia(currentMediaIndex);
                //})
                .start();
    }

    private void setupButtons(View view) {
        styleNameSubmitButton.setOnClickListener(v -> {
            if (styleName.getEditText().getText().length() > 0) {
                crossFade(figure_name_layout, style_name_layout);
            }
        });
        styleNameCancelButton.setOnClickListener(v -> crossFade(button_layout, style_name_layout));

        figureNameSubmitButton.setOnClickListener(v -> {
            String entry = figureName.getEditText().getText().toString().trim();
            if (entry.length() > 0) {
                confirmOrganize(() -> {
                    organizeItem();
                    crossFade(button_layout, figure_name_layout);
                }, null);
            }
        });
        figureNameCancelButton.setOnClickListener(v -> crossFade(style_name_layout, figure_name_layout));


        nameButton.setOnClickListener(v -> crossFade(style_name_layout, button_layout));

        deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setTitle("Delete Dance?")
                    .setMessage("Are you sure you want to delete this dance?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteItem(player.getCurrentContent());
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        });

        markStartHelper = new MarkButtonHelper(getActivity(), player, markStartButton, view.findViewById(R.id.mark_start_menu), view.findViewById(R.id.mark_start_set_button), view.findViewById(R.id.mark_start_clear_button));
        markEndHelper = new MarkButtonHelper(getActivity(), player, markEndButton, view.findViewById(R.id.mark_end_menu), view.findViewById(R.id.mark_end_set_button), view.findViewById(R.id.mark_end_clear_button));

        markStartHelper.setButtonSuccess(() -> player.markStartTime());
        markStartHelper.setButtonCleared(() -> {
            player.clearStartTime();
            return true;
        });
        markStartHelper.setButtonSet(() -> player.markStartTime());

        markEndHelper.setButtonSuccess(() -> player.markEndTime());
        markEndHelper.setButtonCleared(() -> {
            player.clearEndTime();
            return true;
        });
        markEndHelper.setButtonSet(() -> player.markEndTime());



    }

    private void nextVideo() {
        int w = video_view.getWidth();
        switch (player.removeContent()) {
            default:
            case 0:
                showDanceScreen(false, false);
                break;
            case 1:
                animateLoad(w, 0, w, video_view.getWidth() * -2);
                break;
            case -1:
                animateLoad(-w, -w, 0, video_view.getWidth() * 2);
                break;
        }
    }

    private void deleteItem(OrganizeContent organizeContent) {
        nextVideo();
        try {
            FileUtils.forceDelete(organizeContent.getMediaFile());
        } catch (IOException e) {
            //Log.e(Constants.LOG_TAG, String.format("Could not delete dance file! %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    private void organizeItem() {
        String figureFileName = "";
        OrganizeContent organizeContent = player.getCurrentContent();
        DanceData data = Assets.getInstance(getActivity()).getDanceData();
        String styleSanitize = StringUtils.trim(organizeContent.getStyle());
        String figureSanitize = StringUtils.trim(organizeContent.getFigure());
        styleSanitize = StringUtils.capitalize(styleSanitize);

        long start = player.getCurrentContent().getStartTime();
        long end = player.getCurrentContent().getEndTime();

        hideKeyboard();

        //create the new style directory if it exists
        AppDirectory.createStyleDirectory(styleSanitize);


        try {
            File f = AppDirectory.getStyleDirectory(styleSanitize);
            figureFileName = AppDirectory.dateFile(figureSanitize, FilenameUtils.getExtension(organizeContent.getMediaFile().getName()).toLowerCase(), organizeContent.getMediaFile().getName());
            figureFileName = FileNameCleaner.cleanFileName(figureFileName);
            File file = new File(f, figureFileName);
            if (file.exists()) { //this figure name already exists for some reason
                figureFileName = AppDirectory.incrementExistingExternalFile(f, figureFileName);
            }
        } catch (IOException e) {
            //Log.e(Constants.LOG_TAG, "Could not crate style directory");
            return;
        }
        nextVideo();
        organizeContent.setFigure(figureSanitize);
        organizeContent.setStyle(styleSanitize);

        //move the file to the new location
        try {
            File f = AppDirectory.getStyleDirectory(styleSanitize);
            File dest = new File(f, figureFileName);
            FileUtils.moveFile(organizeContent.getMediaFile(), dest);
            data.add(styleSanitize, figureSanitize, dest.getAbsolutePath(), start, end);
        } catch (IOException e) {
            //Log.e(Constants.LOG_TAG, String.format("Could not move dance file! %s", e.getMessage()));
            e.printStackTrace();
        }
        Assets.getInstance(getActivity()).updateDanceData();
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void confirmOrganize(Runnable yes, Runnable no) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Organize Dance?")
                .setMessage(String.format("Add this figure %s to %s?", player.getCurrentContent().getFigure(), player.getCurrentContent().getStyle()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yes != null) {
                            yes.run();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (no != null) {
                            no.run();
                        }
                    }
                })
                .show();
    }

    private void crossFade(ViewGroup in, ViewGroup out) {
        out.setAlpha(0f);
        in.setAlpha(0f);
        out.setVisibility(View.VISIBLE);
        in.setVisibility(View.VISIBLE);


        AnimatorSet as = new AnimatorSet();
        as.playTogether(
                ObjectAnimator.ofFloat(in, "alpha", 0f, 1f).setDuration(300),
                ObjectAnimator.ofFloat(out, "alpha", 1f, 0f).setDuration(300));
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                out.setVisibility(View.GONE);
                in.setVisibility(View.VISIBLE);
                in.requestFocus();
            }
        });
        as.start();
    }

    @Override
    public void onDetach() {
        if (player != null) {
            player.close();
        }
        super.onDetach();
    }

    @Override
    public void onPause() {
        if (player != null) {
            player.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (player != null) {
            player.play();
        }
        super.onResume();
    }

    private void setupVideo() {
        player = new OrganizerPlayer(getActivity(), mediaData);
        player.setErrorListener(msg -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setTitle(getString(R.string.recording_error_title))
                    .setMessage(String.format(getString(R.string.recording_error_description), player.getCurrentContent().getMediaFile().getName()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> deleteItem(player.getCurrentContent()))
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        nextVideo();
                    })
                    .show();
        });

        video_view.setPlayer(player.getVideoPlayer());
    }

    private List<OrganizeContent> getRecordingList() {
        File movieDir = AppDirectory.loadExternalFile(AppDirectory.getRecordingsPath());
        if (!movieDir.exists())
            movieDir.mkdirs();
        File[] fList = movieDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));
        if (fList == null) {
            return new ArrayList<>();
        }
        ArrayList<OrganizeContent> content = new ArrayList<>();
        for (File f: fList) {
            if (f.getAbsolutePath().contains("\n")) {
                f = new File(f.getAbsolutePath().replace("\n", ""));
            }
            if (f.exists()) {
                content.add(new OrganizeContent(f));
            }
        }
        return content;
    }

    public int getScreenOrientation()
    {
        Display getOrient = getActivity().getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_PORTRAIT;
        Point sz = new Point();
        getOrient.getSize(sz);
        if (sz.x >= sz.y) {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

}