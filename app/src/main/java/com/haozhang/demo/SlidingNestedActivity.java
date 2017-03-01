package com.haozhang.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class SlidingNestedActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    List<String> mListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_sliding_nested);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.child);
        String[] stringArray = getResources().getStringArray(R.array.list_value);

        mListData = Arrays.asList(stringArray);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(new SlidingNestedActivity.MyAdapter());
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tv;

        public Holder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(android.R.id.text1);
            tv.setTextColor(Color.BLACK);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<SlidingActivity.Holder> {

        @Override
        public SlidingActivity.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null);
            view.setBackgroundColor(Color.BLUE);
            return new SlidingActivity.Holder(view);
        }

        @Override
        public void onBindViewHolder(SlidingActivity.Holder holder, int position) {
            String s = mListData.get(position);
            holder.tv.setText(s);
        }

        @Override
        public int getItemCount() {
            return mListData.size();
        }
    }
}
