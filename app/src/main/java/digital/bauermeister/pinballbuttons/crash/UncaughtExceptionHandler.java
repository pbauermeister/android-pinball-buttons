package digital.bauermeister.pinballbuttons.crash;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.Intent;
import android.os.Process;

/**
 * This catches all yet uncaught exception, and alls the CrashActivity to
 * display the stack trace.
 * 
 * @author pascal
 * 
 */
public class UncaughtExceptionHandler implements
		java.lang.Thread.UncaughtExceptionHandler {
	private final Context context;

	public UncaughtExceptionHandler(Context context) {
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		System.err.println(writer);

		Intent intent = new Intent(context, CrashActivity.class);
		intent.putExtra(CrashActivity.STACKTRACE, writer.toString());
		context.startActivity(intent);

		Process.killProcess(Process.myPid());
		System.exit(10);
	}
}
