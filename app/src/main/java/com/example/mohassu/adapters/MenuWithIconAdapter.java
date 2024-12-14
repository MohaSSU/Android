package com.example.mohassu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mohassu.R;

public class MenuWithIconAdapter extends ArrayAdapter<String> {

    private final int[] icons;
    private final String[] items;
    private final LayoutInflater inflater;

    public MenuWithIconAdapter(@NonNull Context context, String[] items, int[] icons) {
        super(context, R.layout.dialog_promise_menu, items);
        this.icons = icons;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dialog_promise_menu, parent, false);
        }

        ImageView iconView = convertView.findViewById(R.id.item_icon);
        TextView textView = convertView.findViewById(R.id.item_text);

        iconView.setImageResource(icons[position]);
        textView.setText(items[position]);

        return convertView;
    }
}