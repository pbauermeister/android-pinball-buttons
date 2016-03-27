package net.homelinux.ten.pinballbuttons;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.homelinux.ten.crash.UncaughtExceptionHandler;
import net.homelinux.ten.pinballbuttons.devices.DeviceItem;
import net.homelinux.ten.pinballbuttons.files.CompactSettings;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is the app's main activity. It displays the settings.
 * 
 * It can be activated either as a standard application (icon, apps list, apps
 * history, etc.) or by an entry in the notification area, sustained by our
 * Service.
 * 
 * This activity restarts the Sevice upon onResume().
 * 
 * @author pascal
 * 
 */
public class SettingsActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_about:
			showAboutDialog();
			return true;
		case R.id.menu_logs:
			showLogs();
			return true;
		case R.id.menu_restart_service:
			TheService.restartOrKillService(context);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// install crash handler
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(
				this));

		// init
		context = getApplicationContext();
		super.onCreate(savedInstanceState);

		// display settings fragment as the main content.
		SettingsFragment frag = new SettingsFragment();
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, frag).commit();

		savePrefsToCompactFile();
		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(
						onSharedPreferenceChangeListener);

		Logs.d(TAG, "Version: " + getSoftwareVersion());
		TheService.restartOrKillService(context);
	}

	@Override
	protected void onResume() {
		// TheService.startService(context);
		super.onResume();
	}

	private static final String TAG = "SettingsActivity";
	private static Context context;

	/**
	 * Apply changes: rewrite compact settings file, and restart service.
	 */
	private void savePrefsToCompactFile() {
		String s;
		HashMap<String, String> extraMap = new HashMap<String, String>();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// screen size
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point screenSize = new Point();
		display.getSize(screenSize);

		// add screen size to extra info
		extraMap.put("screen_width", String.format("%d", screenSize.x));
		extraMap.put("screen_height", String.format("%d", screenSize.y));

		// add ts type to extra info
		s = prefs.getString(SettingsFragment.KEY_TS_DEVICE, null);
		if (s != null) {
			DeviceItem item = DeviceItem.unpack(s);
			extraMap.put("ts_type", item.tsType);
		}

		// save file: shared prefs, and extra info
		Map<String, ?> prefsMap = prefs.getAll();
		CompactSettings.save(context, prefsMap, extraMap);
		Logs.d(TAG, "File saved");
	}

	/**
	 * Listen to settings global changes, because listening to fragment changes
	 * would give us only partially up-to-date prefs.
	 */
	private OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			savePrefsToCompactFile();
		}
	};

	/**
	 * Compute program version.
	 * 
	 * @return
	 */
	private String getSoftwareVersion() {
		String revision = "???";
		try {
			InputStream is = getApplicationContext().getAssets().open(
					"config/Version.config");
			Properties prop = new Properties();
			prop.load(is);
			is.close();
			revision = prop.get("Revision").toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String version = "???";
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}

		return version + ".r" + revision;
	}

	private void showAboutDialog() {
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.about_dialog);
		dialog.setTitle(R.string.about_dialog_title);

		// set the custom dialog components - text, image and button
		TextView textView;
		textView = (TextView) dialog.findViewById(R.id.version_text);

		String fmt = context.getString(R.string.version_fmt);
		String appName = context
				.getString(context.getApplicationInfo().labelRes);
		String appVersion = getSoftwareVersion();

		String info = String.format(fmt, appName, appVersion);
		textView.setText(info);

		textView = (TextView) dialog.findViewById(R.id.about_text);
		textView.setText(R.string.about_dialog_text);

		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.ic_launcher);

		Button okButton = (Button) dialog.findViewById(R.id.ok_button);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void showLogs() {
		Intent intent = new Intent(this, LogsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
