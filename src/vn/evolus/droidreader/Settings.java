package vn.evolus.droidreader;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
	public static final int MAX_ITEMS = 15;
	private static final String PREFS_NAME = "vn.evolus.droidreader_preferences";
	
	public static boolean getFirstTime(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(context.getString(R.string.first_time_key), true);
	}
	
	public static void saveFirstTime(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(context.getString(R.string.first_time_key), false);
		
		editor.putString(context.getString(R.string.update_interval_key), "5");
		editor.putString(context.getString(R.string.max_items_per_channel_key), "20");
		editor.putBoolean(context.getString(R.string.auto_update_key), true);
		editor.putBoolean(context.getString(R.string.show_updated_channels_key), false);
		editor.putString(context.getString(R.string.language_key), "en");
		
		editor.commit();
	}
	
	public static int getUpdateInterval(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return Integer.parseInt(prefs.getString(context.getString(R.string.update_interval_key), "5"));	
	}
	
	public static int getMaxItemsPerChannel(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return Integer.parseInt(prefs.getString(context.getString(R.string.max_items_per_channel_key), "20"));	
	}
	
	public static boolean getAutoUpdate(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);		
		return prefs.getBoolean(context.getString(R.string.auto_update_key), true);
	}
	
	public static boolean getShowUpdatedChannels(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(context.getString(R.string.show_updated_channels_key), false);
	}

	public static String getLocale(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString(context.getString(R.string.language_key), "en");
	}
	
	public static String getGoogleReaderAccessToken(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("google_reader_access_token", null);
	}
	public static String getGoogleReaderTokenSecret(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getString("google_reader_token_secret", null);
	}
	public static void saveGoogleReaderAccessTokenAndSecret(Context context, 
			String accessToken, String tokenSecret) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString("google_reader_access_token", accessToken);
		editor.putString("google_reader_token_secret", tokenSecret);
		
		editor.commit();
	}

	public static void clearGoogleReaderAccessTokenAndSecret(Context context) {
		saveGoogleReaderAccessTokenAndSecret(context, null, null);
	}

	public static boolean isAuthenticated(Context context) {
		return getGoogleReaderAccessToken(context) != null;
	}	
}