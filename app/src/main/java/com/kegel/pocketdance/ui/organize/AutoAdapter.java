package com.kegel.pocketdance.ui.organize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.kegel.pocketdance.R;

import java.util.ArrayList;
import java.util.List;

public class AutoAdapter extends BaseAdapter implements Filterable {

    private Context context;

    private List<String> originalData;
    private List<String> filteredData;

    private ItemFilter mFilter = new ItemFilter();

    public AutoAdapter(Context ctx, List<String> itemList) {
        originalData = new ArrayList<String>();
        filteredData = new ArrayList<String>();

        filteredData = itemList;
        originalData = itemList;

        context = ctx;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.autocomplete, null);
        }

        final TextView text = (TextView) convertView.findViewById(R.id.text1);

        text.setText(filteredData.get(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            if (constraint == null) {
                Filter.FilterResults results = new FilterResults();
                results.count = originalData.size();
                results.values = originalData;
                return results;
            }
            String filterString = constraint.toString().toLowerCase();

            Filter.FilterResults results = new FilterResults();

            final List<String> list = originalData;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {

                filterableString = list.get(i);

                if (filterableString.toLowerCase().startsWith(filterString)) {
                    nlist.add(filterableString);
                }
            }
            if (nlist.size() == 1 && nlist.get(0).toLowerCase().equals(constraint.toString().toLowerCase())) {
                results.values = new ArrayList<>();
                results.count = 0;
                return results;
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }
}
