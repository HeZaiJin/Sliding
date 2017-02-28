package com.haozhang.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.haozhang.sliding.SlidingLayout;

import java.util.Arrays;
import java.util.List;

public class SlidingActivity extends AppCompatActivity {
    int mChildType = 0;
    SlidingLayout mSlidingLayout;
    View[] mChilds = new View[4];
    int[] mChildsRes = {R.layout.layout_scrollview, R.layout.layout_listview, R.layout.layout_recycler, R.layout.layout_normal};
    View mChild;
    List<String> mListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSlidingLayout = (SlidingLayout) findViewById(R.id.content_sliding);
        initChilds();
        mChild = mChilds[0];
        mSlidingLayout.addView(mChild);
    }

    public void initChilds() {
        for (int i = 0; i < 4; i++) {
            mChilds[i] = getLayoutInflater().inflate(mChildsRes[i], null);
        }
        String[] stringArray = getResources().getStringArray(R.array.list_value);
        mListData = Arrays.asList(stringArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mListData);
        ListView lv = (ListView) mChilds[1];
        lv.setAdapter(adapter);
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
