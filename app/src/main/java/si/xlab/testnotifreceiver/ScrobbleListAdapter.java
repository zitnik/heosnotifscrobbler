package si.xlab.testnotifreceiver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ScrobbleListAdapter extends ArrayAdapter<Database.Scrobble> {
	private Context context;
	private int resourceLayout;
	
	public ScrobbleListAdapter(Context context, int resource, List<Database.Scrobble> items) {
		super(context, resource, items);
		this.context = context;
		this.resourceLayout = resource;
	}
	
	void refresh() {
		clear();
		addAll(Database.getInstance(context).getAll());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Database.Scrobble scrobble = getItem(position);
		
		TextView textSong;
		TextView textTimestamp;
		TextView textScrobbleStatus;
		
		if(convertView==null){
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(resourceLayout, null);
		}
		textSong = convertView.findViewById(R.id.textSong);
		textTimestamp = convertView.findViewById(R.id.textTimestamp);
		textScrobbleStatus = convertView.findViewById(R.id.textScrobbleStatus);
		
		textSong.setText(scrobble.getSong().toString());
		textTimestamp.setText(DateFormat.getInstance().format(new Date(scrobble.getTime())));
		textScrobbleStatus.setText(scrobble.isScrobbled() ? "[Scrobbled]" : "[Pending]");
		
		return convertView;
	}
}
