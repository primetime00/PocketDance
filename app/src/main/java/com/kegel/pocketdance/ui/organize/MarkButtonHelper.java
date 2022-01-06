package com.kegel.pocketdance.ui.organize;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

public class MarkButtonHelper {
    private Activity activity;
    private OrganizerPlayer player;
    private Button markButton;
    private CardView markView;
    private Button setButton;
    private Button clearButton;
    private int[] positioning = new int[2];

    private ButtonListener buttonSuccess;
    private ButtonListener buttonPopup;
    private ButtonListener buttonSet;
    private ButtonListener buttonFailed;
    private ButtonListener buttonCleared;

    public MarkButtonHelper(Activity activity, OrganizerPlayer player, Button markButton, CardView markView, Button setButton, Button clearButton) {
        this.activity = activity;
        this.markButton = markButton;
        this.markView = markView;
        this.setButton = setButton;
        this.clearButton = clearButton;
        this.player = player;

        markButton.setOnClickListener(v -> {
            if (markButton.isSelected()) { //we already have a set time
                if (markView.getVisibility() == View.VISIBLE) { //we hit the button twice for no reason do nothing but make menu disappear
                    markView.setVisibility(View.INVISIBLE);
                    if (buttonFailed != null) {
                        buttonFailed.onButton();
                    }
                    return;
                }
                player.pause();
                markView.setVisibility(View.VISIBLE);
                if (buttonPopup != null) {
                    buttonPopup.onButton();
                }
                return;
            }
            if (buttonSuccess != null && buttonSuccess.onButton()) {
                markButton.setSelected(true);
            }
        });

        clearButton.setOnClickListener(v -> {
            if (buttonCleared != null) {
                buttonCleared.onButton();
            }
            player.play();
            markButton.setSelected(false);
            markView.setVisibility(View.INVISIBLE);
        });

        setButton.setOnClickListener(v -> {
            if (buttonSet != null && buttonSet.onButton()) {
                markButton.setSelected(true);
                player.play();
                markView.setVisibility(View.INVISIBLE);
            }
        });

        markButton.setSelected(player.getCurrentContent().getStartTime() > 0);


        ViewTreeObserver vt = markButton.getViewTreeObserver();
        vt.addOnDrawListener(() -> {
            int oldPosX = positioning[0];
            int oldPosY = positioning[1];
            markButton.getLocationOnScreen(positioning);
            if (oldPosX != positioning[0] || oldPosY != positioning[1]) {
                ((ConstraintLayout.LayoutParams)markView.getLayoutParams()).leftMargin = positioning[0];
                ((ConstraintLayout.LayoutParams)markView.getLayoutParams()).rightMargin = markView.getWidth() - positioning[0]-markButton.getWidth();
            }
        });
    }

    public void setButtonSuccess(ButtonListener buttonSuccess) {
        this.buttonSuccess = buttonSuccess;
    }

    public void setButtonPopup(ButtonListener buttonPopup) {
        this.buttonPopup = buttonPopup;
    }

    public void setButtonSet(ButtonListener buttonSet) {
        this.buttonSet = buttonSet;
    }

    public void setButtonFailed(ButtonListener buttonFailed) {
        this.buttonFailed = buttonFailed;
    }

    public void setButtonCleared(ButtonListener buttonCleared) {
        this.buttonCleared = buttonCleared;
    }
}
