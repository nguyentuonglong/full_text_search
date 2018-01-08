package com.example.ntlong.fulltextsearch.adapter

import android.database.Cursor
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.ntlong.fulltextsearch.R

/*
 * Created by ntlong on 11/29/17.
 */

class CursorContactSearchAdapter(cursor: Cursor?) : CursorRecyclerViewAdapter<CursorContactSearchAdapter.ViewHolder>(cursor) {

    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor, position: Int) {

        viewHolder.renderData(cursor, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact_list, parent, false)
        return ViewHolder(itemView)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var nameView: TextView = itemView.findViewById(R.id.name) as TextView


        private lateinit var name: String


        internal fun renderData(cursor: Cursor, position: Int) {

            cursor.moveToPosition(position)

            //get data from cursor
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

            nameView.text = name
        }

    }
}
