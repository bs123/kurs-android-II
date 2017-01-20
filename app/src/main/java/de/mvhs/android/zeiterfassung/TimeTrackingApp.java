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
 * Created by Kurs on 18.01.2017.
 */

@ReportsCrashes(
        mailTo = "crash@mvhs.de",
        mode = ReportingInteractionMode.DIALOG,
        resDialogTitle = R.string.CrashDialogTitle,
        resDialogText = R.string.CrashDialogText,
        resDialogIcon = R.mipmap.ic_launcher,
        resDialogTheme = R.style.AppTheme
)
public final class TimeTrackingApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Initialisierung über Code
//        try {
//            final ACRAConfiguration config =
//                    new ConfigurationBuilder(this)
//                            .setMailTo("crash@mvhs.de")
//                            .setReportingInteractionMode(ReportingInteractionMode.DIALOG)
//                            .setResDialogText(R.string.CrashDialogText)
//                            .setResDialogTitle(R.string.CrashDialogTitle)
//                            .setResDialogTheme(R.style.AppTheme)
//                            .setResDialogIcon(R.mipmap.ic_launcher)
//                            .build();
//
//            ACRA.init(this, config);
//        } catch (ACRAConfigurationException e) {
//            e.printStackTrace();
//        }

        ACRA.init(this);
    }
}
