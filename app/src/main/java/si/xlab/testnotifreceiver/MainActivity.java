package si.xlab.testnotifreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
	private TextView textStatusLastFm;
	private Button buttonLastFm; //TODO input&save last.fm credentials
	private TextView textEnabled;
	private Button buttonEnabled;
	private TextView textScrobbleDAB;
	private Button buttonScrobbleDAB;
	private TextView textServiceStatus;
	private Button buttonPermission;
	private TextView textAllScrobbles;
	private TextView textPendingScrobbles;
	private Button buttonClearDb;
	private ListView listViewScrobbles;
	private ScrobbleListAdapter scrobbleListAdapter;
	
	private Database db;
	private Settings settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textStatusLastFm = findViewById(R.id.textStatusLastFm);
		buttonLastFm = findViewById(R.id.buttonLastFm);
		textEnabled = findViewById(R.id.textEnabled);
		buttonEnabled = findViewById(R.id.buttonEnabled);
		textScrobbleDAB = findViewById(R.id.textScrobbleDAB);
		buttonScrobbleDAB = findViewById(R.id.buttonScrobbleDAB);
		textServiceStatus = findViewById(R.id.textServiceStatus);
		buttonPermission = findViewById(R.id.buttonPermission);
		textAllScrobbles = findViewById(R.id.textAll);
		textPendingScrobbles = findViewById(R.id.textPending);
		buttonClearDb = findViewById(R.id.buttonClearDb);
		listViewScrobbles = findViewById(R.id.listViewScrobbles);
		
		db = Database.getInstance(this);
		settings = Settings.getInstance(this);
		
		scrobbleListAdapter = new ScrobbleListAdapter(this, R.layout.scrobble_item, db.getAll());
		
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				textServiceStatus.setText(intent.getBooleanExtra(MyService.EXTRA_SERVICE_RUNNING, false) ? "OK" : "Not running!");
			}
		}, new IntentFilter(MyService.ACTION_CHECK_SERVICE_RESPONSE));
		
		buttonEnabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean enabled = !settings.getScrobbleEnabled();
				settings.setScrobbleEnabled(enabled);
				textEnabled.setText(enabled ? "Enabled" : "Disabled");
			}
		});
		
		buttonScrobbleDAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean enabled = !settings.getScrobbleDAB();
				settings.setScrobbleDAB(enabled);
				textScrobbleDAB.setText(enabled ? "Enabled" : "Disabled");
			}
		});
		
		buttonPermission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				startActivity(intent);
			}
		});
		
		buttonClearDb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				db.deleteScrobbledEntries();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshView();
	}
	
	private void refreshView(){
		final Database db = Database.getInstance(this);
		scrobbleListAdapter.refresh();// TODO this is not exactly optimal...
		
		Intent intent = new Intent(MyService.ACTION_CHECK_SERVICE_QUERY);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
		textEnabled.setText(settings.getScrobbleEnabled() ? "Enabled" : "Disabled");
		textScrobbleDAB.setText(settings.getScrobbleDAB() ? "Enabled" : "Disabled");
		
		textAllScrobbles.setText(String.valueOf(db.countAll()));
		textPendingScrobbles.setText(String.valueOf(db.countPending()));
		listViewScrobbles.setAdapter(scrobbleListAdapter);
	}
}
