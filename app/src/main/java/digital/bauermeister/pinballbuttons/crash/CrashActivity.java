package digital.bauermeister.pinballbuttons.crash;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import digital.bauermeister.pinballbuttons.R;

/**
 * This creates a simple activity displaying the stack trace upon a crash.
 * 
 * @author pascal
 * 
 */
public class CrashActivity extends Activity {
	static final String STACKTRACE = "stacktrace";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crash_view);
		final String stackTrace = getIntent().getStringExtra(STACKTRACE);
		final TextView reportTextView = (TextView) findViewById(R.id.text);
		reportTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		reportTextView.setClickable(false);
		reportTextView.setLongClickable(false);

		reportTextView.append("Application has crashed, sorry. \n\n");
		reportTextView.append(stackTrace);
	}
}
