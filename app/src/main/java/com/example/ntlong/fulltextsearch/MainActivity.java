package com.example.ntlong.fulltextsearch;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

/**
 * Created by ntlong on 12/9/17.
 */

public class MainActivity extends Activity {

    private EditText mEditText;
    private RecyclerView mContactList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.search);
        mContactList = (RecyclerView) findViewById(R.id.contact_list);
    }
}
