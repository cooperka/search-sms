package me.cooperka.searchsms;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabClass extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tabclass);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, Search.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("search").setIndicator(res.getString(R.string.tab_search), res.getDrawable(R.drawable.search)).setContent(intent);
        tabHost.addTab(spec);
        /*
        intent = new Intent().setClass(this, Backup.class);
        spec = tabHost.newTabSpec("backup").setIndicator(res.getString(R.string.tab_backup), res.getDrawable(R.drawable.search)).setContent(intent);
        tabHost.addTab(spec);
         */
        intent = new Intent().setClass(this, Analyze.class);
        spec = tabHost.newTabSpec("analyze").setIndicator(res.getString(R.string.tab_analyze), res.getDrawable(R.drawable.search)).setContent(intent);
        tabHost.addTab(spec);
        /*
        intent = new Intent().setClass(this, Saved.class);
        spec = tabHost.newTabSpec("view").setIndicator(res.getString(R.string.tab_saved), res.getDrawable(R.drawable.search)).setContent(intent);
        tabHost.addTab(spec);
        */
        //NOTE: these drawables are from http://www.axialis.com/free/icons/ or www.softicons.com
        //"icons are licensed under the Creative Commons Attribution License (http://creativecommons.org/licenses/by/2.5/). It means that you can use them in any project or website, commercially or not."
        //"The only restrictions are: (a) you must keep the credits of the authors: "Axialis Team", even if you modify them; (b) link to us if you use them on your website"

        tabHost.setCurrentTab(0); // default to Search
    }

}
