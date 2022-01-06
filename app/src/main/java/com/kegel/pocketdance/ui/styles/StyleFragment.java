package com.kegel.pocketdance.ui.styles;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kegel.pocketdance.DanceData;
import com.kegel.pocketdance.R;

/**
 * A fragment representing a list of Items.
 */
public class StyleFragment extends Fragment implements OnStyleClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private RecyclerView styleListView;
    private View noStylesView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StyleFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static StyleFragment newInstance(int columnCount) {
        StyleFragment fragment = new StyleFragment();
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
        View view = inflater.inflate(R.layout.fragment_style_list, container, false);
        styleListView = view.findViewById(R.id.figure_list);
        noStylesView = view.findViewById(R.id.no_styles_view);

    // Set the adapter
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            styleListView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            styleListView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        styleListView.setAdapter(new StyleRecyclerAdapter(this, getActivity()));

        if (styleListView.getAdapter().getItemCount() == 0) { //we don't have any styles to show.  Let user know to record something
            styleListView.setVisibility(View.INVISIBLE);
            noStylesView.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onStyleClickListener(DanceData.StyleData style) {
        //Toast.makeText(getContext(), String.format("Pressed %s", style), Toast.LENGTH_SHORT).show();
        Bundle b = new Bundle();
        b.putSerializable(getString(R.string._style), style);
        b.putString("figure", style.getName());
        Navigation.findNavController(getView()).navigate(R.id.nav_figures, b);
    }
}