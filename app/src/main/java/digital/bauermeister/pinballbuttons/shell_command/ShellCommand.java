package digital.bauermeister.pinballbuttons.shell_command;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import digital.bauermeister.pinballbuttons.Logs;
import digital.bauermeister.pinballbuttons.util.Util;


/**
 * Executes a shell command.
 * 
 * @author pascal
 * 
 */
public class ShellCommand {

	public static boolean isRootable() {
		ShellCommand cmd = new ShellCommand();
		boolean ret = cmd.execute("su", "-c", "true");
		cmd.finish();
		return ret;
	}

	public String toString() {
		return "cmd=[" + command + "]";
	}

	public boolean execute(String... cmd) {
		// for debug
		for (String e : cmd) {
			if (command.length() > 0)
				command += " ";
			command += e;
		}
		Logs.d(TAG, "Launching shell command: [" + command + "]");

		// run
		try {
			process = new ProcessBuilder().command(cmd)
					.redirectErrorStream(true).start();

			stdinWriteStream = new DataOutputStream(process.getOutputStream());
			stdoutReadStream = new DataInputStream(process.getInputStream());
			stderrReadStream = new DataInputStream(process.getErrorStream());

			stdinReader = new BufferedReader(new InputStreamReader(
					stdoutReadStream));
			stderrReader = new BufferedReader(new InputStreamReader(
					stderrReadStream));

		} catch (IOException e) {
			Logs.e(TAG, toString() + " ERROR execute(): " + e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void destroy() {
		try {
			process.destroy();
		} catch (Exception e) {
			Logs.e(TAG, toString() + " ERROR destroy(): " + e);
			e.printStackTrace();
		}
	}

	public int finish() {
		if (process == null) {
			return -100;
		}
		// wait dead
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			Logs.e(TAG, toString() + " ERROR finish() waitFor: " + e);
			e.printStackTrace();
			return -101;
		}

		// return code
		for (int i = 10; i > 0; --i) {
			try {
				returnCode = process.exitValue();
				break;
			} catch (IllegalThreadStateException e) {
				if (i == 1) {
					return -102;
				}
				Util.delay(100);
			}
		}

		Logs.d(TAG, toString() + " --> " + returnCode);

		// error stream
		error = "";
		try {
			while (stderrReader.ready()) {
				error += stderrReader.readLine() + "\n";
			}
		} catch (IOException e) {
			Logs.e(TAG, toString() + " ERROR finish() readLine: " + e);
			e.printStackTrace();
		}
		Logs.e(TAG, toString() + " ==> " + error);

		// close all
		tryCloseOutput(stdinWriteStream);
		tryCloseInput(stdoutReadStream);
		tryCloseInput(stderrReadStream);

		return returnCode;
	}

	protected String command = "";
	protected DataOutputStream stdinWriteStream = null;
	protected DataInputStream stderrReadStream = null;
	protected DataInputStream stdoutReadStream = null;
	protected Process process = null;

	protected BufferedReader stdinReader;
	protected BufferedReader stderrReader;
	protected String error = null;
	protected Integer returnCode = null;

	private static final String TAG = "ShellCommand";

	private void tryCloseInput(InputStream s) {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
		}
	}

	private void tryCloseOutput(OutputStream s) {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
		}
	}
}
