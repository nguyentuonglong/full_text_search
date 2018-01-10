package com.example.ntlong.fulltextsearch.virtualtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.ntlong.fulltextsearch.App;

import java.text.Normalizer;

/**
 * Created by ntlong on 12/8/17.
 */

public class DatabaseTable {

    private static final String TAG = DatabaseTable.class.getSimpleName();

    //The columns we'll include in the table
    static final String COL_CONTACT_ID = ContactsContract.Data.CONTACT_ID;
    static final String COL_DISPLAY_NAME = ContactsContract.Data.DISPLAY_NAME;
    static final String COL_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    static final String COL_HAS_PHONE_NUMBER = ContactsContract.Data.HAS_PHONE_NUMBER;
    static final String COL_PHOTO_THUMBNAIL_URI = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI;
    static final String COL_SORT_KEY_PRIMARY = ContactsContract.Contacts.SORT_KEY_PRIMARY;
    static final String COL_MIME_TYPE = ContactsContract.Data.MIMETYPE;
    static final String COL_NORMALIZED = "COL_NORMALIZED";


    private static final String DATABASE_NAME = "FTS_CONTACT_DB";
    private static final String FTS_VIRTUAL_TABLE = "FTS_CONTACT";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseOpenHelper mDatabaseOpenHelper = null;

    private static DatabaseTable INSTANCE;

    public static DatabaseTable getInstance(){
        if (INSTANCE == null){
            INSTANCE = new DatabaseTable();
            mDatabaseOpenHelper = new DatabaseOpenHelper(App.Companion.getInstance());
        }

        return INSTANCE;
    }

    private DatabaseTable() {}

    public static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_CONTACT_ID + ", " +
                        COL_DISPLAY_NAME + ", " +
                        COL_NUMBER + ", " +
                        COL_HAS_PHONE_NUMBER + ", " +
                        COL_PHOTO_THUMBNAIL_URI + ", " +
                        COL_SORT_KEY_PRIMARY + ", " +
                        COL_NORMALIZED + ", " +
                        COL_MIME_TYPE +
                        ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);

            String selection = ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
                    ContactsContract.Data.MIMETYPE + "=?";

            String[] selectArgs = new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

            Log.d(TAG, ".Inside onCreateLoader, selection = " + selection);

            String[] projection = new String[]{
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Data.HAS_PHONE_NUMBER,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY,
                    ContactsContract.Data.MIMETYPE
            };


            Cursor c = App.instance.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    projection,
                    selection,
                    selectArgs,
                    ContactsContract.Contacts.DISPLAY_NAME);

            processContactData(c);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        void processContactData(Cursor cursor) {
            Log.d(TAG, ".Inside processContactData");

            if (cursor != null) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String photoThumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String sortKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.SORT_KEY_PRIMARY));
                    String mineType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                    long id = addContact(contactId, displayName, phoneNumber, hasPhoneNumber, photoThumbnailUri, sortKey, mineType);
                    Log.d(TAG, ".Inside processContactData, add contact: contactId=" + contactId +
                            " ,displayName=" + displayName);

                    if (id < 0) {
                        Log.d(TAG, ".Inside processContactData, unable to add contact: contactId=" + contactId +
                                " ,displayName=" + displayName);
                    }

                    cursor.moveToNext();
                }

                cursor.close();
            }


        }

        long addContact(int contactId, String displayName, String phoneNumber, int hasPhoneNumber, String thumbUri, String sortKey, String mineType) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_CONTACT_ID, contactId);
            initialValues.put(COL_DISPLAY_NAME, displayName);
            initialValues.put(COL_NUMBER, phoneNumber);
            initialValues.put(COL_HAS_PHONE_NUMBER, hasPhoneNumber);
            initialValues.put(COL_PHOTO_THUMBNAIL_URI, thumbUri);
            initialValues.put(COL_SORT_KEY_PRIMARY, sortKey);
            initialValues.put(COL_NORMALIZED, processNormalized(displayName));
            initialValues.put(COL_MIME_TYPE, mineType);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        String processNormalized(String string) {

            return Normalizer
                    .normalize(string, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");
        }
    }

    public Cursor getWordMatches(String query) {

        String[] projection = new String[]{
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Data.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.SORT_KEY_PRIMARY,
                ContactsContract.Data.MIMETYPE,
                COL_NORMALIZED
        };

        Log.d(TAG, ".Inside getWordMatches, before convert query=" + query);

        String convertedString =
                Normalizer
                        .normalize(query, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "");

        Log.d(TAG, ".Inside getWordMatches, after convert query=" + convertedString);

        String selection =
                COL_NORMALIZED + " LIKE '%" + convertedString + "%'" + " AND " +
                ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND " +
                ContactsContract.Data.MIMETYPE + "=?";

        Log.d(TAG, ".Inside getWordMatches, selection=" + selection);

        String[] selectArgs = new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

        return query(selection, selectArgs, projection);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        }

        return cursor;
    }

    public void dropTable(){
        Log.d(TAG,".Inside dropTable, DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);

        Log.d(TAG,".Inside dropTable, create table " + FTS_VIRTUAL_TABLE);
        mDatabaseOpenHelper.onCreate(db);
    }


}

