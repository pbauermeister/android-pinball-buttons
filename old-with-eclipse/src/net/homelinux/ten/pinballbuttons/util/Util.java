package net.homelinux.ten.pinballbuttons.util;

import java.util.ArrayList;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.widget.Toast;

/**
 * A small collection of useful features.
 * 
 * @author pascal
 * 
 */
public class Util {

	public static void delay(int ms) {
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
		}
	}

	private static ArrayList<Toast> toasts = new ArrayList<Toast>();

	public static void toast(Context context, String text) {
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		toasts.add(toast);
	}

	public static synchronized void cancelToasts() {
		for (Toast t : toasts) {
			t.cancel();
		}
		toasts.clear();
	}

	public static void beep() {
		ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
		tg.startTone(ToneGenerator.TONE_PROP_BEEP, 300);
	}
}
