package digital.bauermeister.pinballbuttons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import digital.bauermeister.pinballbuttons.util.Util;

/**
 * This Service is here to provide an permanent item in the notification area,
 * as well as to install (if needed) and restart the Mapper.
 *
 * @author pascal
 */
public class TheService extends Service {

    /**
     * Factory
     *
     * @param context
     */
    public static synchronized void restartOrKillService(Context context) {
        Util.cancelToasts();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (prefs.getBoolean(SettingsFragment.KEY_ENABLED, true)) {
            Intent serviceIntent = new Intent(context, TheService.class);
            context.startService(serviceIntent);
        } else {
            Intent serviceIntent = new Intent(context, TheService.class);
            context.stopService(serviceIntent);

            NotificationManager notifyManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this.getApplicationContext();
        startForeground(NOTIFICATION_ID, makeNotification());
        new Mapper(context).startMapperAsDaemon();
        return START_STICKY;
    }

    // private static final String TAG = "TheService";
    private static int NOTIFICATION_ID = 1;
    private Context context;

    private Notification makeNotification() {
        // the intent to launch when the user clicks the expanded notification
        Intent in = new Intent(this, SettingsActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // notification info
        String title = getResources().getString(R.string.app_name);
        String hint = getResources().getString(R.string.notification_hint);
        PendingIntent pi = PendingIntent.getActivity(this, 0, in, 0);

        // notification
        Notification notification = new Notification.Builder(this)
                .setContentIntent(pi).setContentTitle(title)
                .setContentText(hint).setSmallIcon(R.drawable.ic_notification)
                .getNotification();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        return notification;
    }
}