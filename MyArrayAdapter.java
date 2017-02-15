package com.hand.my.myfacebookapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import static android.content.Context.MODE_PRIVATE;


public class MyArrayAdapter extends ArrayAdapter {
    private final Activity context;
    private final String[] names;
    private SharedPreferences mupOfFiles;

    public MyArrayAdapter(Activity context, String[] names) {
        super(context, R.layout.rowlayout, names);
        this.context = context;
        this.names = names;
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.rowlayout, null, true);
            holder = new ViewHolder();
            holder.textView = (TextView) rowView.findViewById(R.id.label);
            holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
            holder.textView.setText(getShortFileName(names[position]));
            if (getBoolFacebook(names[position])) {
                holder.imageView.setImageResource(android.R.drawable.checkbox_on_background);
            } else {
                holder.imageView.setImageResource(android.R.drawable.checkbox_off_background);
            }
        return rowView;
    }

    boolean getBoolFacebook(String nameFile) {
        mupOfFiles = context.getSharedPreferences("ChekBoxPref", MODE_PRIVATE);
        Boolean answer = mupOfFiles.getBoolean(nameFile, false);
        return answer;
    }
    private String getShortFileName(String fullNameFile){
        mupOfFiles = context.getSharedPreferences("FileNamePref", MODE_PRIVATE);
        return mupOfFiles.getString(fullNameFile, "errorName");
    }


}

