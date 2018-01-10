package com.example.ntlong.fulltextsearch

import android.Manifest
import android.app.Activity
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.ntlong.fulltextsearch.adapter.CursorContactSearchAdapter
import com.example.ntlong.fulltextsearch.virtualtable.DatabaseTable
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("JAVA_CLASS_ON_COMPANION")
/*
 * Created by ntlong on 12/9/17.
 */

class MainActivity : Activity(), android.app.LoaderManager.LoaderCallbacks<Cursor> {


    private val mBaseUri = ContactsContract.Data.CONTENT_URI
    private lateinit var mCursorContactSearchAdapter: CursorContactSearchAdapter
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100

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
        mCursorContactSearchAdapter.swapCursor(data)
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

            /**
             * We can query data by using a normal Cursor as commented code bellow
             */

//            val selection: String = if (constraint.isEmpty()) {
//                ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
//                        ContactsContract.Data.MIMETYPE + "=?"
//            } else {
//                "(" +
//                        ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + constraint + "%'" + " OR " +
//                        ContactsContract.Contacts.DISPLAY_NAME + " LIKE 'N%" + constraint + "%'" + ") AND " +
//                        ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
//                        ContactsContract.Data.MIMETYPE + "=?"
//            }
//
//            val selectArgs = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//
//
//            val projection = arrayOf(ContactsContract.Data.CONTACT_ID,
//                    ContactsContract.Data.DISPLAY_NAME,
//                    ContactsContract.CommonDataKinds.Phone.NUMBER,
//                    ContactsContract.Data.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
//                    ContactsContract.Contacts.SORT_KEY_PRIMARY)
//
//            contentResolver.query(
//                    mBaseUri,
//                    projection,
//                    selection,
//                    selectArgs,
//                    ContactsContract.Contacts.DISPLAY_NAME)


            /**
             * Apply full text search
             */
            DatabaseTable.getInstance().getWordMatches(constraint.toString())
        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCursorContactSearchAdapter = CursorContactSearchAdapter(null)
        contact_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contact_list.adapter = mCursorContactSearchAdapter

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loaderManager.initLoader<Cursor>(0, Bundle(), this)
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loaderManager.initLoader<Cursor>(0, Bundle(), this)

                } else {
                    Toast.makeText(this, "You should allow permission to run this demo", Toast.LENGTH_SHORT).show()

                    val handler = Handler()

                    val runnable = Runnable {

                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.READ_CONTACTS),
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }

                    handler.postDelayed(runnable, 1000)
                }

            }
        }
    }

    companion object {
        val TAG = MainActivity.javaClass.simpleName
    }
}
