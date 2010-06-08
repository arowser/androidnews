package vn.evolus.news;

import vn.evolus.news.rss.Channel;
import vn.evolus.news.rss.Item;
import vn.evolus.news.util.ImageLoader;
import vn.evolus.news.widget.ItemListView;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;

import com.github.droidfu.concurrent.BetterAsyncTask;
import com.github.droidfu.concurrent.BetterAsyncTaskCallable;

public class ChannelActivity extends Activity {		
	private final int MENU_BACK = 0;
	private final int MENU_REFRESH = 1;
	private final int MENU_MARK_ALL_AS_READ = 2;
	
	private ContentResolver cr;
	private Channel channel;
	
	TextView channelName;	
	ItemListView itemListView;	
	ViewSwitcher refreshOrProgress;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ImageLoader.initialize(this);
		this.cr = getContentResolver();
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.channel_view);
		
		Button back = (Button)findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		refreshOrProgress = (ViewSwitcher)findViewById(R.id.refreshOrProgress);
		ImageButton refresh = (ImageButton)findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refresh();
			}			
		});
		
		channelName = (TextView)findViewById(R.id.channelName);
        itemListView = (ItemListView)findViewById(R.id.itemListView);
        itemListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Item item = (Item)adapterView.getItemAtPosition(position);				
				showItem(item);
			}
        });
        
        long channelId = getIntent().getLongExtra("ChannelId", 0);   
        channelName.setText(getIntent().getStringExtra("ChannelTitle"));
        showChannel(channelId);
	}		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_BACK, 0, getString(R.string.back)).setIcon(R.drawable.ic_menu_back);
    	menu.add(0, MENU_REFRESH, 1,  getString(R.string.refresh)).setIcon(R.drawable.ic_menu_refresh);
    	menu.add(0, MENU_MARK_ALL_AS_READ, 1,  getString(R.string.mark_all_as_read)).setIcon(android.R.drawable.ic_menu_view);
		return super.onCreateOptionsMenu(menu);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_BACK) {
    		finish();
    	} else if (item.getItemId() == MENU_REFRESH){
    		refresh();
    	} else if (item.getItemId() == MENU_MARK_ALL_AS_READ){
    		markAllAsRead();
    	}
		
		return true;
	}
	
	private void setBusy() {
		refreshOrProgress.setDisplayedChild(1);
	}
	
	private void setIdle() {
		refreshOrProgress.setDisplayedChild(0);
	}	
	
	private void showChannel(final long channelId) {
		this.setBusy();
		
		BetterAsyncTask<Channel, Void, Channel> task = new BetterAsyncTask<Channel, Void, Channel>(this) {			
			protected void after(Context context, Channel channel) {				
				itemListView.setItems(channel.getItems());
				onChannelUpdated(channel);
			}			
			protected void handleError(Context context, Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Cannot load the feed: " + e.getMessage(), 5).show();
				setIdle();
			}
		};
		task.setCallable(new BetterAsyncTaskCallable<Channel, Void, Channel>() {
			public Channel call(BetterAsyncTask<Channel, Void, Channel> task) throws Exception {
				ContentResolver cr = getContentResolver();
				channel = Channel.load(channelId, cr);
				channel.loadLightweightItems(cr);
				return channel;
			}    			
		});
		task.disableDialog();
		task.execute();		       
	}
	
	private void markAllAsRead() {
		if (this.channel != null) {
			this.channel.markAllAsRead(cr);
			// TRICK: do we need to reload from database?
			for (Item item : this.channel.getItems()) {
				item.setRead(true);
			}
			this.itemListView.refresh();
		}
	}
	
	private void refresh() {
		this.setBusy();
		
		BetterAsyncTask<Void, Void, Void> task = new BetterAsyncTask<Void, Void, Void>(this) {			
			protected void after(Context context, Void args) {				
				onChannelUpdated(channel);				
			}			
			protected void handleError(Context context, Exception e) {
				Toast.makeText(context, "Cannot load the feed: " + e.getMessage(), 5).show();
				setIdle();
			}			
		};
		task.setCallable(new BetterAsyncTaskCallable<Void, Void, Void>() {
			public Void call(BetterAsyncTask<Void, Void, Void> task) throws Exception {
				channel.update(getContentResolver());
				return null;
			}    			
		});
		task.disableDialog();
		task.execute();		       
	}
	
	private void onChannelUpdated(Channel channel) {
		this.channel = channel;
		channelName.setText(channel.getTitle());
		this.setIdle();
	}

	private void showItem(Item item) { 
		Intent intent = new Intent(this, ItemActivity.class);		
		intent.putExtra("ItemId", item.getId());
		intent.putExtra("ChannelId", this.channel.getId());		
		startActivity(intent);		
	}	
}