package com.buddycloud.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.content.BuddyCloud.Roster;

public class RosterActivity extends BCActivity
    implements OnItemClickListener {

    protected static final String TAG = "RosterActivity";

    protected ListView rosterList;
    protected RosterAdapter listAdapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.roster);

        rosterList = (ListView) findViewById(R.id.roster_list);

        Cursor buddies = managedQuery(
                Roster.VIEW_CONTENT_URI,
                Roster.PROJECTION_MAP,
                null,
                null,
                "self DESC, last_updated DESC, cache_update_timestamp DESC");

        listAdapter = new RosterAdapter(this, buddies);
        rosterList.setAdapter(listAdapter);
        rosterList.setOnItemClickListener(this);
    }

    public void onItemClick(
        AdapterView<?> parent,
        View view,
        int position,
        long id
    ) {
        Log.d(TAG, "position: " + position);
        Log.d(TAG, "id: " + listAdapter.getItemId(position));
        Cursor cursor = (Cursor)listAdapter.getItem(position);
        Log.d(TAG, "item: " + cursor.getString(cursor.getColumnIndex(Roster.JID)));

        String channel = cursor.getString(cursor.getColumnIndex(Roster.JID));
        Intent openChannel = new Intent();
        openChannel.setClassName("com.buddycloud",
                ChannelMessageActivity.class.getCanonicalName());
        openChannel.setData(Uri.parse("channel:" + channel));

        startActivity(openChannel);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {

        getMenuInflater().inflate(R.menu.roster_context, menu);

        Intent openChannel = new Intent();
        openChannel.setClassName("com.buddycloud.android.buddydroid",
                ChannelMessageActivity.class.getCanonicalName());

        Cursor buddy = (Cursor) listAdapter.getItem(
                ((AdapterContextMenuInfo) menuInfo).position);
        String jid = buddy.getString(buddy.getColumnIndex(Roster.JID));

        openChannel.setData(Uri.parse("channel:" + jid));

        menu.findItem(R.id.open_channel).setIntent(openChannel);

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private class RosterAdapter extends CursorAdapter {

        public RosterAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.roster_row, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            String jid = cursor.getString(cursor.getColumnIndex(Roster.JID));
            int unread = cursor.getInt(
                    cursor.getColumnIndex(Roster.UNREAD_MESSAGES));

            boolean isChannel = false;

            if (jid.startsWith("/user/")) {
                jid = jid.substring(6);
                if (jid.endsWith("/channel")) {
                    jid = jid.substring(0, jid.length() - 8);
                }
            } else if (jid.startsWith("/channel/")) {
                jid = jid.substring(9);
                isChannel = true;
            }

            TextView tv = (TextView) view.findViewById(R.id.channel);
            tv.setText("#" + jid);

            tv = (TextView) view.findViewById(R.id.status);
            tv.setText(cursor.getString(cursor
                    .getColumnIndex(Roster.GEOLOC)));

            view.findViewById(R.id.geo_prev).setVisibility(View.GONE);
            view.findViewById(R.id.geo).setVisibility(View.GONE);
            view.findViewById(R.id.geo_next).setVisibility(View.GONE);

            if (isChannel) {
                ((ImageView) view.findViewById(R.id.icon))
                        .setImageResource(R.drawable.channel);
            } else {
                ((ImageView) view.findViewById(R.id.icon))
                        .setImageResource(R.drawable.contact);
            }

            if (unread == 0) {
                view.findViewById(R.id.badge).setVisibility(View.GONE);
            } else {
                TextView unreadView = (TextView) view.findViewById(R.id.badge);
                unreadView.setText(Integer.toString(unread));
                unreadView.setVisibility(View.VISIBLE);
            }
            // view.setTag(jid);
            return;
        }
    }

}