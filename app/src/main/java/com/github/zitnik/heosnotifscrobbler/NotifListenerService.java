package com.github.zitnik.heosnotifscrobbler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

public class NotifListenerService extends NotificationListenerService {
	public static final String EXTRA_SERVICE_RUNNING = "SERVICE_RUNNING_EXTRA";
	public static final String ACTION_CHECK_SERVICE_RESPONSE = "SERVICE_RUNNING_RESPONSE";
	public static final String ACTION_CHECK_SERVICE_QUERY = "SERVICE_RUNNING_QUERY";
//	public static final String ACTION_DO_POST_TO_LASTFM = "SERVICE_SCROBBLE_TO_LASTFM";
	
	private static final long DELAY_MILLIS = 90 * 1000; // will scrobble if song was playing for at least 90s (continuously)
	private static final long SAME_SONG_DELAY_MILLIS = 5 * 60 * 1000; // will not scrobble same song twice in 5 minutes (avoiding duplicates)
	private Song previousScrobbled;
	private long previousScrobbleTime;
	private Song currentSong;
	private long changedTime;
	
	private boolean connected;
	
	public NotifListenerService() {
		connected = false;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent responseIntent = new Intent(ACTION_CHECK_SERVICE_RESPONSE);
				responseIntent.putExtra(EXTRA_SERVICE_RUNNING, connected);
				LocalBroadcastManager.getInstance(NotifListenerService.this).sendBroadcast(responseIntent);
			}
		}, new IntentFilter(ACTION_CHECK_SERVICE_QUERY));
	}
	
	private void scrobble(Song song, long time){
		if(!Settings.getInstance(this).getScrobbleEnabled())
			return;
		
		Log.d("will scrobble", song.toString() + " at time: " + new Date(time).toLocaleString());
		Database.getInstance(this).insertScrobble(song, time);
		new LastFm(this).scrobbleAll();
	}
	
	private void newSong(Song song){
		Log.d("new song:", song.toString());
		long now = new Date().getTime();
		if(changedTime!=0 && now > changedTime + DELAY_MILLIS &&
				!(previousScrobbled!=null && previousScrobbled.equals(currentSong) && now < previousScrobbleTime + SAME_SONG_DELAY_MILLIS)){ // prevents duplicates (play - pause - play)
			scrobble(currentSong, changedTime);
			previousScrobbled = currentSong;
			previousScrobbleTime = now;
		}
		currentSong = song;
		changedTime = now;
	}
	
	private void gotSong(String artist, String title){
		if(!Settings.getInstance(this).getScrobbleEnabled() || artist.isEmpty() || title.isEmpty()) {
			stopped();
			return;
		}
		
		// exceptions
		// FM RADIO:
		// Artist:VAL 202| Title:FM 98.90MHz|
		if(title.matches("FM \\d+\\.\\d+MHz") ||
		// DAB RADIO:
		// Artist:Val 202| Title:Val 202|
		// Artist:01 Val 202| Title:Val 202|
		artist.equals(title) || (artist.substring(0,3).matches("\\d+ ") && artist.endsWith(title))){
			stopped();
			return;
		}
		// optional exception
		// INTERNET RADIO:
		// Artist:MADISON AVENUE| Title:DON'T CALL ME BABY|
		// (assuming capital letters are radio) ¯\_(ツ)_/¯
		boolean fromRadio = false;
		if (!Settings.getInstance(this).getScrobbleDAB() && artist.toUpperCase().equals(artist) && title.toUpperCase().equals(title)){
			stopped();
			return;
		}else{
			// capitalize only first letter of each word
			artist = Utils.capitalizeWords(artist);
			title = Utils.capitalizeWords(title);
			fromRadio = true;
		}
		
		Song song = new Song(artist, title, fromRadio);
		Log.d("gotSong", song.toString());
		if(currentSong==null || !currentSong.equals(song))
			newSong(song);
	}
	
	private void stopped(){
		Log.d("gotSong", "STOPPED");
		if(changedTime!=0 && currentSong!=null && new Date().getTime() > changedTime + DELAY_MILLIS){
			scrobble(currentSong, changedTime);
		}
		changedTime=0;
		currentSong=null;
	}
	
	@Override
	public void onListenerConnected() {
		Log.d("TESTNOTIF", "listener connected");
		connected = true;
	}
	
	@Override
	public void onListenerDisconnected() {
		Log.d("TESTNOTIF", "listener disconnected");
		connected = false;
	}
	
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if(! sbn.getPackageName().contains("com.dnm.heos.phone"))
			return;
		
		String title = null;
		String artist = null;
		
		RemoteViews views = sbn.getNotification().contentView;
		if(views == null)
			return;
		Class viewsClass = views.getClass();
		try {
			Field actionsField = viewsClass.getDeclaredField("mActions");
			actionsField.setAccessible(true);
			ArrayList<Object> actions = (ArrayList<Object>) actionsField.get(views);
			
			for(int j = actions.size()-1; j>=0; j--) {
				Object action = actions.get(j);
				if (!action.getClass().getName().equals("android.widget.RemoteViews$ReflectionAction"))
					continue;
				Field mActionsField = action.getClass().getDeclaredField("methodName");
				mActionsField.setAccessible(true);
				if (!mActionsField.get(action).equals("setText"))
					continue;
				Field valueField = action.getClass().getDeclaredField("value");
				valueField.setAccessible(true);
				String text = (String) valueField.get(action);
				Field viewIdField = action.getClass().getSuperclass().getDeclaredField("viewId");
				viewIdField.setAccessible(true);
				int viewId = viewIdField.getInt(action);
				if (viewId == 2131362711) { // artist textView ID
					if (text.isEmpty())
						stopped();
					artist = text;
				} else if (viewId == 2131362713) { // title textView ID
					if (title != null) { // song title twice -> looks like we're not getting the artist name
						stopped();
						return;
					}
					if (text.isEmpty())
						stopped();
					title = text;
				}
				if (artist != null && title != null)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(artist!=null && title!=null)
			gotSong(artist, title);
	}
}