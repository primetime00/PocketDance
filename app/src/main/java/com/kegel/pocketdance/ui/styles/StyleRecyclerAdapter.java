package com.kegel.pocketdance.ui.styles;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kegel.pocketdance.Assets;
import com.kegel.pocketdance.DanceData;
import com.kegel.pocketdance.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display.
 * TODO: Replace the implementation with code for your data type.
 */
public class StyleRecyclerAdapter extends RecyclerView.Adapter<StyleRecyclerAdapter.ViewHolder> {

    private final List<DanceData.StyleData> mStyles;
    private final OnStyleClickListener listener;

    public StyleRecyclerAdapter(OnStyleClickListener styleListener, Context context) {
        mStyles = new ArrayList<>();
        listener = styleListener;

        createDances(context);
    }

    private void createDances(Context context) {
        DanceData data = Assets.getInstance(context).getDanceData();

        for (DanceData.StyleData style :  data.getStyles()) {
            if (style.getFigures().isEmpty()) {
                continue;
            }
            mStyles.add(style);
        }

        /*try (InputStream sr =  context.getAssets().open("mockData.xml")) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(sr, null);
            parser.nextTag();

            StyleContent currentContent = null;
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("dance")) {
                    if (currentContent != null) {
                        mStyles.add(currentContent);
                    }
                    currentContent = new StyleContent(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "directory"));
                }
                else if (currentContent != null && parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("figure")) {
                    String video = parser.getAttributeValue(null, "video");
                    String sha = parser.getAttributeValue(null, "sha");
                    String name = parser.nextText();
                    currentContent.addFigure(name, video, sha);
                }
            }
        } catch (Exception e) {
            mStyles.add(new StyleContent(context.getString(R.string.no_dance), ""));
        }*/
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_style, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mStyles.get(position).getName();
        holder.mStyleView.setText(mStyles.get(position).getName());
        holder.mView.setOnClickListener(e -> listener.onStyleClickListener(mStyles.get(position)));
    }




    @Override
    public int getItemCount() {
        return mStyles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mStyleView;
        public String mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mStyleView = (TextView) view.findViewById(R.id.figure_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mStyleView.getText() + "'";
        }
    }
}