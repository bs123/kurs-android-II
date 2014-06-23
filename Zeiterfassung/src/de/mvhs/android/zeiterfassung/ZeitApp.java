package de.mvhs.android.zeiterfassung;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // Kompabilität
mailTo = "bugs@meineapp.de", mode = ReportingInteractionMode.TOAST, resToastText = R.string.acra_toast_crash)
public class ZeitApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
	}
}
