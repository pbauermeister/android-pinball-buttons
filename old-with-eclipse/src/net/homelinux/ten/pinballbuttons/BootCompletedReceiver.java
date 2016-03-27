package net.homelinux.ten.pinballbuttons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * On System startup, launch our service.
 * 
 * @author pascal
 * 
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		TheService.restartOrKillService(context);
	}
}
