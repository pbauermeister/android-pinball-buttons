package net.homelinux.ten.pinballbuttons.devices;

/**
 * The DeviceList holds DeviceItems.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import net.homelinux.ten.pinballbuttons.Logs;
import net.homelinux.ten.pinballbuttons.shell_command.ShellCommandLinesReader;

/**
 * This class builds a list of input device, by running the shell command
 * 'getevent -i' as root, and parsing its output.
 * 
 * For touchscreen devices, it can guess its type (A or B).
 * 
 * @author pascal
 * 
 */
public class DeviceList extends ArrayList<DeviceItem> {

	public DeviceList() {
		ShellCommandLinesReader sc = new ShellCommandLinesReader();
		sc.executeUntil("/dev/input/event", "su", "-c", "getevent -i");

		DeviceMap map = new DeviceMap();
		String current = null;
		String name = null;
		String location = null;

		List<String> lines = sc.getLines();
		lines.add("add device ");

		boolean in_events = false;
		boolean in_events_abs = false;
		boolean has_abs_2f = false;
		boolean has_abs_35 = false;

		for (String line : lines) {
			Logs.d(TAG, "dev|" + line);
			// add device 6: /dev/input/event1
			// name: "sun4i-keyboard"
			if (line.startsWith("add device ")) {
				// add last
				String tsT = DeviceItem.TSTYPE_NONE;
				if (has_abs_2f)
					tsT = DeviceItem.TSTYPE_B;
				else if (has_abs_35)
					tsT = DeviceItem.TSTYPE_A;

				if (current != null) {
					DeviceItem di = new DeviceItem(current, name, location, tsT);
					map.put(di.getMapKey(), di);
				}
				current = null;
				name = null;
				location = null;

				String parts[] = line.split(": *");
				if (parts.length == 2) {
					current = parts[1];
				}
			} else if (line.startsWith("  name: ")) {
				name = extractValue(line);
			} else if (line.startsWith("  location: ")) {
				location = extractValue(line);
			} else if (line.startsWith("  events:")) {
				in_events = true;
				in_events_abs = false;
				has_abs_2f = false;
				has_abs_35 = false;
			} else if (in_events) {
				if (line.startsWith("    ")) {
					String s = null;
					if (line.startsWith("      ") && in_events_abs) {
						s = line.split(":")[0].trim();
					} else if (line.startsWith("    ABS ")) {
						in_events_abs = true;
						s = line.split(":")[1].trim();
					}
					if (s != null) {
						Logs.d(TAG, String.format("    ev => <%s>", s));
						int ev = Integer.parseInt(s, 16);
						Logs.d(TAG, String.format("    ev => %d=%04x", ev, ev));
						if (ev == 0x2f)
							has_abs_2f = true;
						else if (ev == 0x35)
							has_abs_35 = true;
					}
				} else {
					in_events = false;
				}
			}
		}

		/* Smooth out */
		Collection<DeviceItem> col = (Collection<DeviceItem>) map.values();
		ArrayList<DeviceItem> al = new ArrayList<DeviceItem>(col);

		for (DeviceItem di : al)
			this.add(di);
	}

	public DeviceItem find(String packed) {
		if (packed == null)
			return null;
		String[] parts = (packed).split("\\:");
		if (parts.length < 2)
			return null;

		DeviceItem searched = new DeviceItem(parts[0].trim(), parts[1].trim(),
				null, null);

		for (DeviceItem item : this) {
			if (item.device.equals(searched.device))
				return item;
		}
		return null;
	}

	private static final long serialVersionUID = 1L;
	private static final String TAG = "DeviceList";

	private class DeviceMap extends TreeMap<String, DeviceItem> {
		private static final long serialVersionUID = 1L;
	}

	private String extractValue(String line) {
		String parts[] = line.split(": *");
		if (parts.length != 2)
			return null;
		String val = parts.length == 2 ? parts[1] : "";
		val = val.startsWith("\"") ? val.substring(1) : val;
		val = val.endsWith("\"") ? val.substring(0, val.length() - 1) : val;
		return val.equals("") ? null : val.trim();
	}
}
