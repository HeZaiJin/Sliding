package com.haozhang.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haozhang.sliding.SlidingNestedLayout;

import java.util.Arrays;
import java.util.List;

public class SlidingNestedActivity extends AppCompatActivity {
    int[] mChildsRes = {R.layout.layout_nested_scrollview, R.layout.layout_recycler};
    View[] mChilds = new View[2];
    SlidingNestedLayout mSlidingLayout;
    View mChild;
    List<String> mListData;
    int mChildType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_nested);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSlidingLayout = (SlidingNestedLayout) findViewById(R.id.parent);

        String[] stringArray = getResources().getStringArray(R.array.list_value);
        mListData = Arrays.asList(stringArray);
        initChilds();
        mChildType = 0;
        mChild = mChilds[0];
        mSlidingLayout.addView(mChild);
    }

    public void initChilds() {
        for (int i = 0; i < 2; i++) {
            mChilds[i] = getLayoutInflater().inflate(mChildsRes[i], null);
        }

        String[] stringArray = getResources().getStringArray(R.array.list_value);
        mListData = Arrays.asList(stringArray);
        RecyclerView rv = (RecyclerView) mChilds[1];
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.setAdapter(new SlidingNestedActivity.MyAdapter());
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sliding_nested, menu);
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
            case R.id.sliding_recyclerView:
                type = 1;
                break;
        }
        if (type >= 0) {
            changeChildView(type);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
