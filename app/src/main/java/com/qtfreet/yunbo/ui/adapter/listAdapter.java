package com.qtfreet.yunbo.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qtfreet.yunbo.R;
import com.qtfreet.yunbo.bean.SearchBean;

import java.util.List;

/**
 * Created by qtfreet on 2016/4/7.
 */
public class listAdapter extends BaseAdapter {
    private List<SearchBean> mSearchResult;
    private Context context;


    public listAdapter(Context mContext, List<SearchBean> searchResult) {
        mSearchResult = searchResult;
        this.context = mContext;
    }


    @Override
    public int getCount() {
        return mSearchResult.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchResult.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(mSearchResult.get(position).getName());
        return convertView;
    }

    static class ViewHolder {
        public TextView title;
    }
}
