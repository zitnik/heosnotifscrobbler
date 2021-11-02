package si.xlab.testnotifreceiver;

import android.content.Context;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;

public class LastFm {
//	Application name	HEOScrobbler
	private static final String API_KEY = "d01110908f5f8a09b1cfe7d8ab104210";
	private static final String API_SECRET = "c2422b18fb849ad8137577f8a804b65b";
	
	private static final String API_ROOT = "https://ws.audioscrobbler.com/2.0";
	
	private Session session;
	private Context context;
	private Executor executor;
	LastFm(Context context){
		this.context = context;
		executor = new ThreadPoolExecutor(0, 1, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	private void init() throws LoginException {
		if(session==null)
			login();
		if(session==null)
			throw new LoginException();
	}
	
	private void login(){
		Caller.getInstance().setApiRootUrl(API_ROOT);
		
		session = Authenticator.getMobileSession("grain_z",
				"be93e3c393a837cc193564cdb0777f6b", // md5sum of pass
				API_KEY, API_SECRET);
	}
	
	private boolean scrobble(Database.Scrobble scrobble){
//		ScrobbleResult r = Track.scrobble(scrobble.getSong().getArtist(),
//				scrobble.getSong().getTitle(), (int) (scrobble.getTime()/1000), session);
		ScrobbleData data = new ScrobbleData(scrobble.getSong().getArtist(), scrobble.getSong().getTitle(),
				(int) (scrobble.getTime()/1000));
		data.setChosenByUser(!scrobble.getSong().isFromRadio());
		ScrobbleResult r = Track.scrobble(data, session);
		return r.isSuccessful();
	}
	
	public void scrobbleAll(){
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Database db = Database.getInstance(context);
				try {
					init();
					Database.Scrobble i;
					while ((i = db.getOldestUnscrobbled()) != null) {
						if (scrobble(i))
							db.setScrobbled(i);
						else
							break;
					}
				} catch (LoginException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
