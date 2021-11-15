package com.github.zitnik.heosnotifscrobbler;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	private static final String PREF_NAME = "HEOScrobblerPreferences";
	
	private static Settings instance;
	public static Settings getInstance(Context context){
		if(instance==null)
			instance = new Settings(context);
		return instance;
	}
	
	private SharedPreferences prefs;
	
	private Settings(Context context) {
		prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}
	
	private static final String scrobble = "SETTING_SCROBBLE";
	public void setScrobbleEnabled(boolean a){
		prefs.edit().putBoolean(scrobble, a).apply();
	}
	
	public boolean getScrobbleEnabled(){
		return prefs.getBoolean(scrobble, true);
	}
	
	private static final String scrobbleDab = "SETTING_DAB";
	public void setScrobbleDAB(boolean a){
		prefs.edit().putBoolean(scrobbleDab, a).apply();
	}
	
	public boolean getScrobbleDAB(){
		return prefs.getBoolean(scrobbleDab, false);
	}
}
