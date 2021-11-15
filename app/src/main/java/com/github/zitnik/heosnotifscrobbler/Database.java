package com.github.zitnik.heosnotifscrobbler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Database {
	private static Database instance;
	public static Database getInstance(Context context){
		if(instance==null)
			instance = new Database(context);
		return instance;
	}
	
	private DbHelper dbHelper;
	private SQLiteDatabase db;
	private Database(Context context) {
		dbHelper = new DbHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	public void insertScrobble(Song song, long time){
		ContentValues values = new ContentValues();
		values.put(COL_TIMESTAMP, time);
		values.put(COL_TITLE, song.getTitle());
		values.put(COL_ARTIST, song.getArtist());
		values.put(COL_RADIO, song.isFromRadio());
		db.insert(SCROBBLES_TABLE_NAME, null, values);
	}
	
	public Scrobble getOldestUnscrobbled(){
		Cursor cursor = db.query(SCROBBLES_TABLE_NAME,
				null,
				COL_SCROBBLED + " = 0",
				null,
				null,
				null,
				COL_TIMESTAMP + " ASC",
				"1");
		if(!cursor.moveToFirst())
			return null;
		return Scrobble.fromCursor(cursor);
	}
	
	public List<Scrobble> getAll(){
		Cursor cursor = db.query(SCROBBLES_TABLE_NAME,
				null,
				null,
				null,
				null,
				null,
				COL_TIMESTAMP + " DESC");
		ArrayList<Scrobble> r = new ArrayList<>(cursor.getCount());
		while(cursor.moveToNext()){
			r.add(Scrobble.fromCursor(cursor));
		}
		return r;
	}
	
	public void setScrobbled(Scrobble scrobble){
		ContentValues values = new ContentValues();
		values.put(COL_SCROBBLED, 1);
		db.update(SCROBBLES_TABLE_NAME,
				values,
				COL_ID + " = " + scrobble.id,
				null);
		scrobble.scrobbled = true;
	}
	
	public int deleteScrobbledEntries(){
		return db.delete(SCROBBLES_TABLE_NAME,
				COL_SCROBBLED + " = 1",
				null);
	}
	
	public long countAll(){
		return DatabaseUtils.queryNumEntries(db, SCROBBLES_TABLE_NAME);
	}
	
	public long countPending(){
		return DatabaseUtils.queryNumEntries(db, SCROBBLES_TABLE_NAME,
				COL_SCROBBLED + " = 0");
	}
	
	private static final String SCROBBLES_TABLE_NAME = "scrobbles";
	public static final String COL_ID = "id";
	public static final String COL_SCROBBLED = "scrobbled";
	public static final String COL_TIMESTAMP = "timestamp";
	public static final String COL_ARTIST = "artist";
	public static final String COL_TITLE = "title";
	public static final String COL_RADIO = "is_radio";
	private static class DbHelper extends SQLiteOpenHelper{
		private static final String SQL_CREATE = "CREATE TABLE "+SCROBBLES_TABLE_NAME+" (" +
				COL_ID+" INTEGER PRIMARY KEY," +
				COL_SCROBBLED+" INTEGER DEFAULT 0," +
				COL_TIMESTAMP+" INTEGER NOT NULL," +
				COL_ARTIST+" TEXT NOT NULL," +
				COL_TITLE+" TEXT NOT NULL," +
				COL_RADIO+" INTEGER DEFAULT 0" +
				")";
		private static final String DB_NAME = "HEOScrobbler.db";
		public static final int DB_VERSION = 1;
		
		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		}
	}
	
	static class Scrobble {
		private long id;
		private boolean scrobbled;
		private long time;
		private Song song;
		
		public Scrobble(long id, boolean scrobbled, long time, Song song) {
			this.id = id;
			this.scrobbled = scrobbled;
			this.time = time;
			this.song = song;
		}
		
		public long getId() {
			return id;
		}
		
		public boolean isScrobbled() {
			return scrobbled;
		}
		
		public long getTime() {
			return time;
		}
		
		public Song getSong() {
			return song;
		}
		
		private static Scrobble fromCursor(Cursor cursor){
			return new Scrobble(
					cursor.getLong(0),
					cursor.getInt(1)==1,
					cursor.getLong(2),
					new Song(
							cursor.getString(3),
							cursor.getString(4),
					cursor.getInt(5)==1)
					);
		}
	}
}
