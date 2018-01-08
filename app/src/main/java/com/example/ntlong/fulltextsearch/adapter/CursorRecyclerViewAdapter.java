package com.example.ntlong.fulltextsearch.adapter;

/*
 * Created by ntlong on 11/29/17.
 */

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;


public abstract class CursorRecyclerViewAdapter<VH
        extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
        implements Filterable, CursorFilter.CursorFilterClient {
    private static final String TAG = CursorRecyclerViewAdapter.class.getSimpleName();
    private boolean isDataValid;
    private Cursor mCursor;
    private ChangeObserver mChangeObserver;
    private DataSetObserver mDataSetObserver;
    private CursorFilter mCursorFilter;
    private FilterQueryProvider mFilterQueryProvider;

    CursorRecyclerViewAdapter(Cursor cursor) {
        init(cursor);
    }

    private void init(Cursor c) {
        boolean cursorPresent = c != null;
        mCursor = c;
        isDataValid = cursorPresent;

        mChangeObserver = new ChangeObserver();
        mDataSetObserver = new CursorDataSetObserver();

        if (cursorPresent) {
            if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int i) {
        if (!isDataValid) {
            Log.d(TAG, ".Inside onBindViewHolder the cursor is valid");
        }
        onBindViewHolder(holder, mCursor, i);
    }


    public abstract void onBindViewHolder(VH holder, Cursor cursor, int position);

    @Override
    public int getItemCount() {
        if (isDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }


    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }


    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            isDataValid = true;
            notifyDataSetChanged();
        } else {
            isDataValid = false;
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }


    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }

        return mCursor;
    }

    public Filter getFilter() {
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
    }


    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }

    private void onContentChanged() {
        Log.d(TAG, ".Inside onContentChanged");
    }

    private class ChangeObserver extends ContentObserver {
        ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class CursorDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            isDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            isDataValid = false;
            notifyItemRangeRemoved(0, getItemCount());
        }
    }

}
