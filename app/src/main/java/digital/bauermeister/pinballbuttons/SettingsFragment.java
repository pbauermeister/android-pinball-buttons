package digital.bauermeister.pinballbuttons;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import junit.framework.Assert;

import java.util.ArrayList;

import digital.bauermeister.pinballbuttons.devices.DeviceItem;
import digital.bauermeister.pinballbuttons.devices.DeviceList;

/**
 * This preference fragment is the main constituant of the main screen
 * (SettingsActivity).
 *
 * @author pascal
 */
public class SettingsFragment extends PreferenceFragment {

    public static final String KEY_TS_DEVICE = "ts_device";
    public static final String KEY_KB_DEVICE = "kb_device";
    public static final String KEY_DEVICE_ROTATION = "device_rotation";

    public static final String KEY_MARGIN_H = "margin_h";
    public static final String KEY_MARGIN_V = "margin_v";

    public static final String KEY_ENABLED = "enabled";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getApplicationContext();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key) {
                TheService.restartOrKillService(context);
            }
        });

        addPreferencesFromResource(R.xml.preferences);

        initDevicePreference(KEY_TS_DEVICE);
        initDevicePreference(KEY_KB_DEVICE);
        initRotationPreference();

        EditTextPreference etp;
        etp = (EditTextPreference) findPreference(KEY_MARGIN_H);
        etp.setOnPreferenceChangeListener(onPreferenceChangeListener);
        etp.setSummary(prefs.getString(KEY_MARGIN_H, null));

        etp = (EditTextPreference) findPreference(KEY_MARGIN_V);
        etp.setOnPreferenceChangeListener(onPreferenceChangeListener);
        etp.setSummary(prefs.getString(KEY_MARGIN_V, null));
    }

    private SharedPreferences prefs;
    private Context context;

    /**
     * Inits the ListPreference for device rotation.
     */
    private void initRotationPreference() {
        ListPreference lp = (ListPreference) findPreference(KEY_DEVICE_ROTATION);
        String dialogTitle = getResources().getString(
                R.string.pref_screen_rotation);
        dialogTitle += " - ";
        dialogTitle += String.format(
                getResources().getString(R.string.pref_screen_rotation_now),
                getScreenRotation());
        lp.setDialogTitle(dialogTitle);
        lp.setOnPreferenceChangeListener(onPreferenceChangeListener);
        String defVal = prefs.getString(KEY_DEVICE_ROTATION, null);
        // lp.setDefaultValue(defVal);
        lp.setSummary(defVal);
    }

    /**
     * Inits a ListPreference for device choice (options and listeners).
     *
     * @param key
     */
    private void initDevicePreference(String key) {
        ListPreference lp = (ListPreference) findPreference(key);
        lp.setOnPreferenceChangeListener(onPreferenceChangeListener);
        lp.setOnPreferenceClickListener(new MyPreferenceClickListener(key));
        setListPreference(key);
    }

    /**
     * Populate a devices list.
     *
     * @param key
     */
    private void setListPreference(String key) {
        ListPreference lp = (ListPreference) findPreference(key);
        String defVal = prefs.getString(key, null);
        DeviceList devs = new DeviceList();

        // add items
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        boolean found = false;
        for (DeviceItem item : devs) {
            entries.add(item.getDisplay());
            entryValues.add(item.getValue());
            if (item.getValue().equals(defVal))
                found = true;
        }

        // add default value if missing from list
        if (!found && defVal != null) {
            DeviceItem item = DeviceItem.unpack(defVal);
            if (item != null) {
                entries.add(item.getDisplay());
                entryValues.add(item.getValue());
            }
        }

        // populate menu
        lp.setEntries(entries.toArray(new CharSequence[0]));
        lp.setEntryValues(entryValues.toArray(new CharSequence[0]));
        lp.setDefaultValue(defVal);
        lp.setSummary(defVal);
    }

    /**
     * Obtain screen rotation, in degrees.
     *
     * @return
     */
    private int getScreenRotation() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rot = display.getRotation();
        switch (rot) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_270:
                return 270;
            default:
                Assert.fail();
                return 0;
        }
    }

    /**
     * When selection has changed, update the summary
     */
    private OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference pref, Object val) {
            pref.setSummary((CharSequence) val);
            return true;
        }
    };

    /**
     * When selection has changed, update the summary
     */
    private class MyPreferenceClickListener implements
            OnPreferenceClickListener {

        public MyPreferenceClickListener(String key) {
            this.key = key;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            setListPreference(key);
            return true;
        }

        private String key;
    }

    ;
}