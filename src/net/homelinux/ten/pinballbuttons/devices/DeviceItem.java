package net.homelinux.ten.pinballbuttons.devices;

import java.util.Locale;

/**
 * A DeviceItem represents a /dev/input/event* entry.
 * 
 * @author pascal
 * 
 */
public class DeviceItem {

	public static final String TSTYPE_NONE = "";
	public static final String TSTYPE_A = "A";
	public static final String TSTYPE_B = "B";

	/*
	 * Data members are public. Berk. It should, instead, follow the Javabean
	 * pattern
	 */
	public String device;
	public String name;
	public String location;
	public String tsType;

	public DeviceItem(String device, String name, String location, String tsType) {
		this.device = clean(device);
		this.name = clean(name);
		this.location = clean(location);
		this.tsType = tsType;
	}

	public DeviceItem(DeviceItem from) {
		this.device = from.device;
		this.name = from.name;
		this.location = from.location;
		this.tsType = from.tsType;
	}

	public String getDisplay() {
		Locale locale = null;
		return String.format(locale, "\n%s\n(%s)\n", name, device);
	}

	public String getValue() {
		Locale locale = null;
		return String.format(locale, "%s %s %s", device, tsType, name);
	}

	public String getMapKey() {
		return getValue();
	}

	public static DeviceItem unpack(String packed) {
		if (packed == null)
			return null;
		String[] parts = (packed).split(" ", 3);
		return parts.length == 3 //
		? new DeviceItem(parts[0], parts[2], null, parts[1])
				: null;
	}

//	private static final String TAG = "DeviceItem";

	private String clean(String s) {
		return s == null || s.equals("-") ? "" : s.replace("|", " ").trim();
	}
}
