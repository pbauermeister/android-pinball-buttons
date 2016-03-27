package digital.bauermeister.pinballbuttons;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

import digital.bauermeister.pinballbuttons.R;
import digital.bauermeister.pinballbuttons.shell_command.ShellCommand;
import digital.bauermeister.pinballbuttons.util.Util;

/**
 * This handles all aspects of the Mapper. The Mapper is a program written in C,
 * named 'pinball_buttons_mapper', which is running in background as a daemon,
 * and reading keyboard events to generate wanted touchscreen events.
 * 
 * The Mapper is compiled separately, and is placed into the Java project as a
 * res/raw resource file.
 * 
 * This class can install the Mapper, kill any possibly running instance, and
 * start it. It is meant to be calld by the Service.
 * 
 * The Mapper monitors the config file, so we do not need to signal any changes
 * explicitly.
 * 
 * The Mapper accesses the keyboard and touchscreen as system devices
 * (/dev/input/eventN) and hence needs to run as root. Its killing/launching is
 * executed in the present class by a shell command involving 'su -c'. This is
 * one of the parts of the app needing the device to be rooted.
 * 
 * @author pascal
 * 
 */
public class Mapper {

	public Mapper(Context context) {
		this.context = context;
		installBin();
	}

	public void startMapperAsDaemon() {
		killMapper();

		Logs.d(TAG, "*** Starting mapper");
		ShellCommand cmd = new ShellCommand();
		boolean ret = cmd.execute("su", "-c", bin_full_path);
		// TODO: check if bin_full_path exists

		int fin = cmd.finish();
		Logs.d(TAG, String.format("%s --> %s, finish --> %d ", cmd.toString(),
				ret, fin));

		if (fin != 0) {
			Util.toast(context, cmd.toString() + " returned " + fin);
		}
	}

	private static final String TAG = "Mapper";

	private static final int RESOURCE_ID = R.raw.pinball_buttons_mapper_armeabi;
	private static final String BIN_NAME = "pinball_buttons_mapper_armeabi";
	private String bin_full_path = null;
	private final Context context;

	private void killMapper() {
		Logs.d(TAG, "*** Killing mapper");
		ShellCommand cmd;
		cmd = new ShellCommand();
		boolean ret = cmd.execute("su", "-c", "killall " + BIN_NAME);
		int fin = cmd.finish();
		Logs.d(TAG, String.format("%s --> %s, finish --> %d ", cmd.toString(),
				ret, fin));
		if (!ret || fin != 127) {
			cmd = new ShellCommand();
			ret = cmd.execute("su", "-c", "busybox killall " + BIN_NAME);
			fin = cmd.finish();

			if (fin != 0) {
				Util.toast(context, "Killing mapper: " + cmd.toString()
						+ " returned " + fin);
			}
		}
	}

	private void installBin() {
		Logs.d(TAG, "*** Installing bin");

		/* Make full path */
		File folder = context.getFilesDir();
		String folderPath = null;
		try {
			folderPath = folder.getCanonicalPath();
		} catch (IOException e) {
			Logs.d(TAG, e.toString());
			Logs.d(TAG, "### Installing bin: cannot locate files dir");
			return;
		}
		if (!folderPath.endsWith("/"))
			folderPath += "/";
		bin_full_path = folderPath + BIN_NAME;
		Logs.d(TAG, "Bin path: " + bin_full_path);

		/* Check if exists */
		File file = new File(bin_full_path);
		if (file.exists() && false) {
			Logs.d(TAG, "File exists");
		} else {
			Logs.d(TAG, "Copying file");
			/* Locate binary resource and copy it as file */
			InputStream input = null;
			OutputStream output = null;

			try {
				input = context.getResources().openRawResource(RESOURCE_ID);
				output = context.openFileOutput(BIN_NAME, Context.MODE_PRIVATE);

				byte[] buffer = new byte[1024 * 4];
				int n;
				while ((n = input.read(buffer)) > 0)
					output.write(buffer, 0, n);
			} catch (IOException e) {
				Logs.d(TAG, e.toString());
				Logs.d(TAG, "### Installing bin: cannot open or copy file");
				return;
			} finally {
				tryClose(input);
				tryClose(output);
			}
		}

		/* make it executable */
		Logs.d(TAG, "Making file executable");
		try {
			Runtime.getRuntime().exec("chmod 770 " + bin_full_path).waitFor();
		} catch (IOException e) {
			Logs.d(TAG, "### " + e.toString());
		} catch (InterruptedException e) {
			Logs.d(TAG, "### " + e.toString());
		}

		Logs.d(TAG, "==> Installing bin: done");
	}

	private static void tryClose(Closeable stream) {
		try {
			if (stream != null)
				stream.close();
		} catch (IOException e) {
		}
	}

}
