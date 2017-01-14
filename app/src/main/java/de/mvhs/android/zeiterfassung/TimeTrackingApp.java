package de.mvhs.android.zeiterfassung;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;

/**
 * Created by eugen on 14.01.17.
 */

@ReportsCrashes(
    mailTo = "android@webducer.de",
    mode = ReportingInteractionMode.DIALOG,
    resDialogText = R.string.CrashDialogText,
    resDialogTitle = R.string.CrashDialogTitle,
    resDialogTheme = R.style.AppTheme,
    resDialogIcon = R.drawable.ic_cancel
)
public class TimeTrackingApp  extends Application {
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);

    // Init Crash Reporting for production
    //if (!BuildConfig.DEBUG) {
      // Config on init
//      try {
//        final ACRAConfiguration config = new ConfigurationBuilder(this)
//            .setMailTo("android@webducer.de")
//            .setReportingInteractionMode(ReportingInteractionMode.DIALOG)
//            .setResDialogText(R.string.CrashDialogText)
//            .setResDialogTitle(R.string.CrashDialogTitle)
//            .setResDialogIcon(R.drawable.ic_cancel)
//            .setResDialogTheme(R.style.AppTheme)
//            .build();

        ACRA.init(this);
//      } catch (ACRAConfigurationException e) {
//        e.printStackTrace();
//      }
    //}
  }
}
