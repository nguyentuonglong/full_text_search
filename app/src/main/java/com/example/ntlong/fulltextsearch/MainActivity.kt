package com.example.ntlong.fulltextsearch

import android.app.Activity
import android.content.Loader
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.ntlong.fulltextsearch.adapter.CursorContactSearchAdapter
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("JAVA_CLASS_ON_COMPANION")
/*
 * Created by ntlong on 12/9/17.
 */

class MainActivity : Activity(), android.app.LoaderManager.LoaderCallbacks<Cursor> {


    private val mBaseUri = ContactsContract.Data.CONTENT_URI
    private lateinit var mCursorContactSearchAdapter: CursorContactSearchAdapter

    override fun onCreateLoader(id: Int, args: Bundle): android.content.Loader<Cursor> {
        val selection = ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
                ContactsContract.Data.MIMETYPE + "=?"

        val selectArgs = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

        Log.d(TAG, ".Inside onCreateLoader, selection = " + selection)

        val projection = arrayOf(ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Data.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.SORT_KEY_PRIMARY)


        return android.content.CursorLoader(
                this,
                mBaseUri,
                projection,
                selection,
                selectArgs,
                ContactsContract.Contacts.DISPLAY_NAME)
    }


    override fun onLoadFinished(loader: android.content.Loader<Cursor>?, data: Cursor?) {
        mCursorContactSearchAdapter = CursorContactSearchAdapter(data)
        contact_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contact_list.adapter = mCursorContactSearchAdapter
        mCursorContactSearchAdapter.notifyDataSetChanged()


        //Add text change listener
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mCursorContactSearchAdapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })


        //Query data
        mCursorContactSearchAdapter.setFilterQueryProvider { constraint ->
            val selection: String

            if (constraint.isEmpty()) {
                selection = ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
                        ContactsContract.Data.MIMETYPE + "=?"
            } else {
                selection = "(" +
                        ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + constraint + "%'" + " OR " +
                        ContactsContract.Contacts.DISPLAY_NAME + " LIKE 'N%" + constraint + "%'" + ") AND " +
                        ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
                        ContactsContract.Data.MIMETYPE + "=?"
            }

            val selectArgs = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)


            val projection = arrayOf(ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Data.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY)

            contentResolver.query(
                    mBaseUri,
                    projection,
                    selection,
                    selectArgs,
                    ContactsContract.Contacts.DISPLAY_NAME)
        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        loaderManager.initLoader<Cursor>(0, Bundle(), this)
    }

    companion object {
        val TAG = MainActivity.javaClass.simpleName
    }
}
