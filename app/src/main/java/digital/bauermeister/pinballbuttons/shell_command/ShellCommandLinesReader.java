package digital.bauermeister.pinballbuttons.shell_command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digital.bauermeister.pinballbuttons.Logs;


/**
 * Executes a shell command, and read its stdout by lines. When one output line
 * starts in a given way, stops storing stdout, and kills the command.
 * 
 * @author pascal
 * 
 */
public class ShellCommandLinesReader extends ShellCommand {

	public boolean executeUntil(String stopAt, String... cmd) {
		if (super.execute(cmd)) {
			while (true) {
				String line;
				try {
					line = stdinReader.readLine();
					if (line == null) {
						break;
					} else if (line.startsWith(stopAt)) {
						destroy();
						break;
					}

				} catch (IOException e) {
					Logs.e(TAG, toString() + " ERROR executeUntil() readLine: "
							+ e);
					e.printStackTrace();
					break;
				}
				lines.add(line);
			}
			return finish() == 0;
		} else {
			return false;
		}
	}

	public List<String> getLines() {
		return lines;
	}

	private static final String TAG = "ShellCommandLinesReader";
	private List<String> lines = new ArrayList<String>();
}
