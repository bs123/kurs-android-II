package de.mvhs.android.zeiterfassung;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // Nur für Google SpreadSheets notwendig
mode = ReportingInteractionMode.TOAST, mailTo = "android@webducer.de",

resNotifTitle = R.string.acra_notif_title, resNotifTickerText = R.string.acra_notif_ticker_text, resNotifText = R.string.acra_notif_ticker_text, resNotifIcon = android.R.drawable.stat_notify_error,

resDialogText = R.string.acra_notif_ticker_text, resDialogIcon = android.R.drawable.ic_dialog_info, resDialogTitle = R.string.acra_notif_title, resDialogCommentPrompt = R.string.acra_dialog_comment, resDialogOkToast = R.string.cmd_yes,

resToastText = R.string.acra_toast_text)
public class ZeiterfassungApplication extends Application {
	@Override
	public void onCreate() {
		// Initialisierung des Reporters
		ACRA.init(this);
		super.onCreate();
	}
}
