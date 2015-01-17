package me.cooperka.searchsms;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Search extends ListActivity implements OnClickListener {

    private ArrayList<Message> m_msgs = null; // List of message items
    private MessageAdapter m_adapter;
    private String search;
    Button b_search;
    CheckBox c_inbox, c_sent, c_other;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_search);

        m_msgs = new ArrayList<Message>();
        this.m_adapter = new MessageAdapter(this, R.layout.row_message, m_msgs);
        setListAdapter(this.m_adapter);

        // Set listeners for button and checkboxes
        b_search = (Button)findViewById(R.id.search);
        c_inbox = (CheckBox)findViewById(R.id.c_inbox);
        c_sent = (CheckBox)findViewById(R.id.c_sent);
        c_other = (CheckBox)findViewById(R.id.c_other);
        b_search.setOnClickListener(this);
        c_inbox.setOnClickListener(this);
        c_sent.setOnClickListener(this);
        c_other.setOnClickListener(this);

        loadPrefs(); // Load stored preferences about checkboxes
    }

    private void loadPrefs() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        c_inbox.setChecked(sp.getBoolean("inbox", true));
        c_sent.setChecked(sp.getBoolean("sent", true));
        c_other.setChecked(sp.getBoolean("other", true));
    }

    private boolean isChecked(String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(key, true);
    }

    private void savePrefs(String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public void onClick(View v) {
        if (v.getTag().equals("b_search")) {
            m_msgs.clear();
            m_adapter.clear();
            search = ((EditText)findViewById(R.id.query)).getText().toString();
            search = search.toUpperCase();

            ((Button) findViewById(R.id.search)).setVisibility(View.GONE);
            ((ProgressBar) findViewById(R.id.sPBar)).setVisibility(View.VISIBLE);

            Thread thread = new Thread(null, doSearch);
            thread.start();
        }
        else if (v.getTag().equals("c_inbox")) {
            savePrefs("inbox", c_inbox.isChecked());
        }
        else if (v.getTag().equals("c_sent")) {
            savePrefs("sent", c_sent.isChecked());
        }
        else if (v.getTag().equals("c_other")) {
            savePrefs("other", c_other.isChecked());
        }
        else if (v.getTag().equals("b_saved")) {
            // TODO go to saved screen, also remove tab
        }
    }

    private Runnable doSearch = new Runnable() {
        public void run() {
            // Fill global m_msgs with messages matching search term
            searchFolders(isChecked("inbox"), isChecked("sent"), isChecked("other"));

            runOnUiThread(returnRes);
        }
    };

    private String getCol(Cursor c, String name) {
        String str;

        try { str = c.getString(c.getColumnIndexOrThrow(name)).toString(); }
        catch (Exception e) { Log.v("Tag", "no such column: " + name); str = ""; }

        return str;
    }

    void searchFolders(boolean I, boolean S, boolean O) {
        Uri folderUri = Uri.parse("content://sms/"); // "All" messages
        Cursor c = getContentResolver().query(folderUri, null, null, null, null);

        // Cursor over the messages within the folder
        // No idea why this line is here
        //if (c == null) return;

        if (c.moveToFirst()) {
            // HashMap of contact <number, name> pairs
            Map<String, String> contactsHash = new HashMap<String, String>();

            for (int i = 0; i < c.getCount(); i++) {
                //for (int k = 0; k < c.getColumnCount(); k++)
                //	Log.v("Tag", c.getColumnNames()[k]);

                Message m = new Message();

                // Get info about the message
                m.setText(getCol(c, "body"));
                m.setNumber(getCol(c, "address"));
                m.setDate(Long.parseLong(getCol(c, "date")));
                m.setType(getCol(c, "type"));

                if (m.getNumber().equals(""))
                    m.setName("[no number]");
                else {
                    // Find the contact name from the HashMap
                    m.setName(contactsHash.get(m.getNumber()));

                    // Or look it up if not found
                    if (m.getName() == null) {
                        Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(m.getNumber()));
                        Cursor ctemp = getContentResolver().query(contactUri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
                        if (ctemp.moveToFirst()) {
                            m.setName(getCol(ctemp, PhoneLookup.DISPLAY_NAME));
                            contactsHash.put(m.getNumber(), m.getName());
                        }
                        else {
                            m.setName("[unknown]");
                            contactsHash.put(m.getNumber(), m.getName());
                        }
                    }
                }

                // Check if this message is worth displaying
                if (m.isBeingSearched(I, S, O) && m.matches(search))
                {
                    // Add the message to the global array
                    m_msgs.add(m);
                }

                c.moveToNext();
            }
        }
        c.close();
    }

    // Add the found messages to the list
    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            // Update the displayed list of messages
            m_adapter.notifyDataSetChanged();

            //if (m_ProgressDialog != null) m_ProgressDialog.dismiss();
            ((Button) findViewById(R.id.search)).setVisibility(View.VISIBLE);
            ((ProgressBar) findViewById(R.id.sPBar)).setVisibility(View.GONE);
        }
    };

    // Adapter for list of messages
    public class MessageAdapter extends ArrayAdapter<Message> {
        private ArrayList<Message> items;

        public MessageAdapter(Context context, int textViewResourceId, ArrayList<Message> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row_message, null);

            Message m = items.get(position);
            //Log.v("Tag", "Loading message with " + m.getNumber());

            if (m != null) {
                TextView text = (TextView) v.findViewById(R.id.text);
                TextView name = (TextView) v.findViewById(R.id.name);
                TextView date = (TextView) v.findViewById(R.id.date);

                if (text != null)
                    // Body of message
                    text.setText(m.getText());
                if (name != null)
                    // e.g. "To: Fred (555-1212)"
                    name.setText(m.getTypeText() + ": " + m.getName() + " (" + m.getNumber() + ")");
                if (date != null)
                    // Date of message
                    date.setText(m.getDateString());
            }
            return v;
        }
    }

}
