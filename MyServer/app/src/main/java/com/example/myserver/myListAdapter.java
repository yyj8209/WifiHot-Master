package com.example.myserver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class myListAdapter extends BaseAdapter {

    private List<ConcurrentHashMap<String, Socket>> Clients;
    private LayoutInflater mInflater;


    public myListAdapter(List<ConcurrentHashMap<String, Socket>> clients, Context context){


        Clients = clients;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return Clients.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return Clients.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.client_item, null);
            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.v = (TextView) convertView.findViewById(R.id.client);
            convertView.setTag(holder);

        }else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }
        // Bind the data with the holder.

        holder.v.setText((String) Clients.get(position).keySet().toString());

        return convertView;
    }

    class ViewHolder {
        TextView v;
    }

}
//
//————————————————
//        版权声明：本文为CSDN博主「iteye_15461」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/iteye_15461/article/details/82043154