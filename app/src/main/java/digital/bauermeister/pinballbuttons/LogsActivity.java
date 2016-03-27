package digital.bauermeister.pinballbuttons;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import digital.bauermeister.pinballbuttons.R;

/**
 * This activity displays the logs.
 * 
 * @author pascal
 * 
 */
public class LogsActivity extends Activity {
//	private static final String TAG = "LogsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.logs_view);
		setTitle(R.string.logs_view_title);

		TextView tv = (TextView) findViewById(R.id.text);
		tv.setText(Logs.get());

		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		tv.setClickable(false);
		tv.setLongClickable(false);

		Button okButton = (Button) findViewById(R.id.ok_button);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
