package com.haozhang.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.haozhang.sliding.SlidingLayout;

import java.util.Arrays;
import java.util.List;

public class SlidingActivity extends AppCompatActivity {
    int[] mChildsRes = {R.layout.layout_scrollview, R.layout.layout_listview, R.layout.layout_recycler, R.layout.layout_normal};
    View[] mChilds = new View[4];
    SlidingLayout mSlidingLayout;
    View mChild;

    List<String> mListData;
    int mChildType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding);
        mSlidingLayout = (SlidingLayout) findViewById(R.id.content_sliding);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initChilds();
        mChildType = 0;
        mChild = mChilds[0];
        mSlidingLayout.addView(mChild);
    }

    public void initChilds() {
        for (int i = 0; i < 4; i++) {
            mChilds[i] = getLayoutInflater().inflate(mChildsRes[i], null);
        }
        mChilds[3].findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SlidingLayout", "child 3 on click");
            }
        });

        String[] stringArray = getResources().getStringArray(R.array.list_value);
        mListData = Arrays.asList(stringArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mListData);
        ListView lv = (ListView) mChilds[1];
        lv.setAdapter(adapter);

        RecyclerView rv = (RecyclerView) mChilds[2];
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.setAdapter(new MyAdapter());
    }


    static class Holder extends RecyclerView.ViewHolder {
        TextView tv;

        public Holder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(android.R.id.text1);
            tv.setTextColor(Color.BLACK);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            String s = mListData.get(position);
            holder.tv.setText(s);
        }

        @Override
        public int getItemCount() {
            return mListData.size();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sliding, menu);
        return true;
    }

    public void changeChildView(int type) {
        if (type != mChildType) {
            mSlidingLayout.removeView(mChild);
            mChild = mChilds[type];
            mSlidingLayout.addView(mChild);
            mChildType = type;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int type = -1;
        switch (item.getItemId()) {
            case R.id.sliding_scrollView:
                type = 0;
                break;
            case R.id.sliding_listView:
                type = 1;
                break;
            case R.id.sliding_recyclerView:
                type = 2;
                break;
            case R.id.sliding_normalView:
                type = 3;
                break;
        }
        if (type >= 0) {
            changeChildView(type);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
