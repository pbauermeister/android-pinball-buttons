package net.homelinux.ten.pinballbuttons.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import net.homelinux.ten.pinballbuttons.Logs;
import android.content.Context;

/**
 * This class generates a file containing the apps' SharedPreferences, in a way
 * that is very compact and easier to parse for the Mapper (written in C) than
 * would be XML.
 * 
 * @author pascal
 * 
 */
public class CompactSettings {

	public static void save(Context context, Map<String, ?>... maps) {
		try {
			FileOutputStream fos = context.openFileOutput(
					COMPACT_SETTINGS_FNAME, Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos);

			for (Map<String, ?> map : maps) {
				for (String key : new ArrayList<String>(new TreeSet<String>(
						map.keySet()))) {

					String value = map.get(key).toString();
					value = value.split(" ")[0];
					Logs.d(TAG, "[" + key + "] = " + value);

					String s = key + " " + value + "\n";
					osw.write(s);
				}
			}
			osw.close();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot write to file "
					+ COMPACT_SETTINGS_FNAME + ": " + ex);
		}

		Logs.d(TAG, "Saved " + getFilePath(context));
	}

	public static String getFilePath(Context context) {
		File f = context.getFileStreamPath(COMPACT_SETTINGS_FNAME);
		return f.exists() ? f.getAbsolutePath() : null;
	}

	private static final String COMPACT_SETTINGS_FNAME = "apps_settings.cnf";
	private static final String TAG = "CompactSettings";
}
