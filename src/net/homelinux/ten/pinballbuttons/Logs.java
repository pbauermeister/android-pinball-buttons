package net.homelinux.ten.pinballbuttons;

import java.util.Queue;

import net.homelinux.ten.pinballbuttons.util.EvictingQueue;
import android.util.Log;

import com.google.common.base.Joiner;

/**
 * Our own logging facility stores to a buffer (an EvictingQueue, to limit the
 * history size) and to the standard Log facility.
 * 
 * @author pascal
 * 
 */
public class Logs {

	public static void d(String tag, String message) {
		add("D", tag, message);
		Log.d(tag, message);
	}

	public static void e(String tag, String message) {
		add("E", tag, message);
		Log.e(tag, message);
	}

	public static String get() {
		return Joiner.on("\n").join(items);
	}

	private static final int MAX_ITEMS = 500;
	private static Queue<String> items = makeList();
	private static int count = 0;

	private static Queue<String> makeList() {
		return EvictingQueue.create(MAX_ITEMS);
	}

	private static void add(String level, String tag, String message) {
		++count;
		items.add(String.format("%4d %s %s %s", count, level, tag, message));
	}

}
