package com.haozhang.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.haozhang.sliding.SlidingRefreshLayout;

import java.util.Arrays;
import java.util.List;

public class SlidingRefreshActivity extends AppCompatActivity {
    private static final String TAG = "SlidingRefreshActivity";
    int[] mChildsRes = {R.layout.layout_nested_scrollview, R.layout.layout_recycler, R.layout.layout_listview, R.layout.layout_normal};
    View[] mChilds = new View[4];
    SlidingRefreshLayout mSlidingLayout;
    View mChild;
    List<String> mListData;
    int mChildType = 0;
    Handler mHandler = new Handler();
    ProgressBar mProgressBar;
    TextView mRefreshText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_refresh);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSlidingLayout = (SlidingRefreshLayout) findViewById(R.id.parent);
        String[] stringArray = getResources().getStringArray(R.array.list_value);
        mListData = Arrays.asList(stringArray);
        initChilds();
        mRefreshText = (TextView) mSlidingLayout.findViewById(R.id.text);
        mProgressBar = (ProgressBar) mSlidingLayout.findViewById(R.id.bar);

        mSlidingLayout.setOnRefreshListener(new SlidingRefreshLayout.onRefreshListener() {
            @Override
            public void onRefreshing() {
                Log.d(TAG, "onRefreshing() called");
                mRefreshText.setText("正在刷新");
                mProgressBar.setVisibility(View.VISIBLE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSlidingLayout.stopRefreshing();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }, 2000);
            }

            @Override
            public void onRefreshingReady() {
                Log.d(TAG, "onRefreshingReady() called");
                mRefreshText.setText("释放刷新");
            }

            @Override
            public void onRefreshProcess() {
                Log.d(TAG, "onRefreshProcess() called");
                mRefreshText.setText("下拉刷新");
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onRefreshDone() {
                Log.d(TAG, "onRefreshDone() called");
                mRefreshText.setText("刷新完成");
            }
        });


    }

    public void initChilds() {
        for (int i = 0; i < 4; i++) {
            mChilds[i] = getLayoutInflater().inflate(mChildsRes[i], null);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mListData);
        ListView lv = (ListView) mChilds[2];
        lv.setAdapter(adapter);

        String[] stringArray = getResources().getStringArray(R.array.list_value);
        mListData = Arrays.asList(stringArray);
        RecyclerView rv = (RecyclerView) mChilds[1];
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv.setAdapter(new SlidingRefreshActivity.MyAdapter());

        mChildType = 0;
        mChild = mChilds[0];
        mSlidingLayout.addView(mChild);
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
            case R.id.sliding_nested_scrollView:
                type = 0;
                break;
            case R.id.sliding_nested_recyclerView:
                type = 1;
                break;
            case R.id.sliding_nested_listView:
                type = 2;
                break;
            case R.id.sliding_nested_normal:
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
