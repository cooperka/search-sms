package me.cooperka.searchsms;

import android.app.ListActivity;
import android.os.Bundle;

public class Saved extends ListActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_saved);
		
		// TODO load saved messages from internal database?
	}
}