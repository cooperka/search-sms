package me.cooperka.searchsms;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Analyze extends Activity implements OnClickListener {

    private static String stats;
    private Button b_analyze;
    private TableRow row;
    private TextView tv;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_analyze);
        context = this;

        // Set listeners for button
        b_analyze = (Button)findViewById(R.id.anaButton);
        b_analyze.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getTag().equals("anaButton")) {
            ((TextView)findViewById(R.id.stats)).setText("Analyzing...");
            ((Button)findViewById(R.id.anaButton)).setVisibility(View.GONE);
            ((ProgressBar)findViewById(R.id.anaProg)).setVisibility(View.VISIBLE);

            Thread thread = new Thread(null, doAnalyze);
            thread.start();
        }
    }

    private String getCol(Cursor c, String name) {
        String str;

        try { str = c.getString(c.getColumnIndexOrThrow(name)).toString(); }
        catch (Exception e) { Log.v("Tag", "no such column: " + name); str = ""; }

        return str;
    }

    private Runnable doAnalyze = new Runnable() {
        // Return the number of messages in a map
        public int getNum(Map<String, Integer> map) {
            int ret = 0;
            ArrayList<Integer> countList = new ArrayList<Integer>();

            countList.addAll(map.values());
            for (int i = 0; i < countList.size(); i++)
                ret += countList.get(i);

            return ret;
        }

        public void run() {
            String body = "", num = "", toFrom = "";
            int msgSent = 0, msgRec = 0, wSent = 0, wRec = 0, uwSent = 0, uwRec = 0, uwTot = 0, unSent = 0, unRec = 0;
            long T1, T2, T3, T4;

            // HashMaps of unique words/numbers sent/received along with the counts associated with them
            Map<String, Integer> words = new HashMap<String, Integer>();
            Map<String, Integer> wordsSent = new HashMap<String, Integer>();
            Map<String, Integer> wordsRec = new HashMap<String, Integer>();
            Map<String, Integer> numsSent = new HashMap<String, Integer>();
            Map<String, Integer> numsRec = new HashMap<String, Integer>();

            Uri uri = Uri.parse("content://sms"); // "All" messages
            Cursor c = getContentResolver().query(uri, null, null, null, null);

            T1 = System.currentTimeMillis();

            if (c.moveToFirst())
            {
                String tempStr;
                int next, last;
                Integer tempInt;

                for (int i = 0; i < c.getCount(); i++)
                {
                    body = getCol(c, "body");
                    num = getCol(c, "address");

                    if (getCol(c, "type").equals("1"))
                        toFrom = "From";
                    else if (getCol(c, "type").equals("2"))
                        toFrom = "To";
                    else
                        toFrom = "[other]"; // TODO figure out types

                    // Remove all non-word characters EXCEPT if it's part of a smiley face
                    //body = body.replaceAll("&apos;", "\'").replaceAll("&#64;", "@").replaceAll("&amp;", "&").replaceAll("&#10;", " ");
                    body = (body.replaceAll("(?![a-zA-Z0-9 ]|[:=;][\\(\\)\\[\\]o0OpPxXdDcC]).(?<![:=;][\\(\\)\\[\\]o0OpPxXdDcC])","").trim() + " ").replaceAll("[ ]+"," ").toUpperCase();
                    //name = name.replaceAll("&apos;", "\'").replaceAll("&#64;", "@").replaceAll("&amp;", "&").replaceAll("&#10;", " ");

                    last = 0;
                    while ((next = body.indexOf(" ", last)) >= 0)
                    {
                        tempStr = body.substring(last, next);
                        if (tempStr.length() >= 1) // Don't count messages that have an empty body
                        {
                            // Insert new words into HashMaps, or increase count if they already exist

                            tempInt = words.get(tempStr);
                            words.put(tempStr, tempInt == null ? 1 : tempInt + 1);

                            if (toFrom.equals("To")) {
                                tempInt = wordsSent.get(tempStr);
                                wordsSent.put(tempStr, tempInt == null ? 1 : tempInt + 1);
                            }
                            else if (toFrom.equals("From")) {
                                tempInt = wordsRec.get(tempStr);
                                wordsRec.put(tempStr, tempInt == null ? 1 : tempInt + 1);
                            }
                        }

                        last = next + 1;
                    }

                    // Insert new numbers into HashMaps, or increase count if they already exist
                    if (toFrom.equals("To")) {
                        tempInt = numsSent.get(num);
                        numsSent.put(num, tempInt == null ? 1 : tempInt + 1);
                    }
                    else if (toFrom.equals("From")) {
                        tempInt = numsRec.get(num);
                        numsRec.put(num, tempInt == null ? 1 : tempInt + 1);
                    }

                    c.moveToNext();
                }
            }

            c.close();

            T2 = System.currentTimeMillis();

            // Calculate total unique vars
            uwTot = words.size();
            uwSent = wordsSent.size();
            uwRec = wordsRec.size();
            unSent = numsSent.size();
            unRec = numsRec.size();

            // Calculate other vars by summing
            wSent = getNum(wordsSent);
            wRec = getNum(wordsRec);
            msgSent = getNum(numsSent);
            msgRec = getNum(numsRec);

            T3 = System.currentTimeMillis();

            // Sort the list of words by frequency
            Map<String, Integer> wordsSorted = sortByValues(wordsSent);

            int i = 1;
            for (Map.Entry<String, Integer> entry : wordsSorted.entrySet()) {
                String word = entry.getKey();
                Integer reps = entry.getValue();

                addRow(i, word, reps);

                if (i == 2) // TODO this doesn't work for more than i=2?
                    break;
                i++;
            }

            T4 = System.currentTimeMillis();

            stats = "Messages sent: " + msgSent + "<br />"
                    + "Messages received: " + msgRec + "<br />"
                    + "Total messages: " + (msgSent + msgRec) + "<br />" + "<br />"

                    + "Total words sent: " + wSent + "<br />"
                    + "Total words received: " + wRec + "<br />"
                    + "Total words: " + (wSent + wRec) + "<br />" + "<br />"

                    + "Unique words sent: " + uwSent + "<br />"
                    + "Unique words received: " + uwRec + "<br />"
                    + "Total unique words: " + uwTot + "<br />" + "<br />"

                    + "Unique contacts sent to: " + unSent + "<br />"
                    + "Unique contacts received from: " + unRec + "<br />" + "<br />"

                    + "Avg messages sent per contact: " + (unSent == 0 ? 0 : (msgSent / unSent)) + "<br />"
                    + "Avg messages received per contact: " + (unRec == 0 ? 0 : (msgRec / unRec)) + "<br />" + "<br />"

                    + "Avg words sent per contact: " + (unSent == 0 ? 0 : (wSent / unSent)) + "<br />"
                    + "Avg words received per contact: " + (unRec == 0 ? 0 : (wRec / unRec)) + "<br />" + "<br />"

                    + "10 most common words:" + "<br />";

            Log.v("Tag", "T2 - T1 = " + (T2 - T1));
            Log.v("Tag", "T4 - T3 = " + (T4 - T3));
            Log.v("Tag", "Total   = " + (T4 - T1));

            runOnUiThread(showStats);
        }

        public void addRow(int i, String word, int reps) {
            // Create a new row
            row = new TableRow(context);

            // Add textviews to the row
            tv = new TextView(context);
            tv.setText("" + i);
            tv.setGravity(Gravity.RIGHT);
            row.addView(tv);
            tv = new TextView(context);
            tv.setText(word);
            tv.setGravity(Gravity.RIGHT);
            row.addView(tv);
            tv = new TextView(context);
            tv.setText("" + reps);
            tv.setGravity(Gravity.RIGHT);
            row.addView(tv);

            runOnUiThread(addRow);
        }

        public <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
            Comparator<K> valueComparator =  new Comparator<K>() {
                public int compare(K k1, K k2) {
                    int compare = map.get(k2).compareTo(map.get(k1));
                    if (compare == 0) return 1;
                    else return compare;
                }
            };
            Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
            sortedByValues.putAll(map);
            return sortedByValues;
        }
    };

    private Runnable addRow = new Runnable() {
        public void run() {
            TableLayout table = (TableLayout)findViewById(R.id.anaTable);
            table.addView(row);
        }
    };

    private Runnable showStats = new Runnable() {
        public void run() {
            ((TextView) findViewById(R.id.stats)).setText(Html.fromHtml(stats));
            ((Button) findViewById(R.id.anaButton)).setVisibility(View.VISIBLE);
            ((ProgressBar) findViewById(R.id.anaProg)).setVisibility(View.GONE);
        }
    };

}
