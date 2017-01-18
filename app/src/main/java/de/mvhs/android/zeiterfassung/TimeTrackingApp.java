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
 * Created by javadev on 18.01.17.
 */

@ReportsCrashes(mailTo = "big.sexy@o2online.de",
mode = ReportingInteractionMode.DIALOG,
resDialogText = R.string.CrashDialogText,
resDialogTitle = R.string.CrashDialogTitle,
resDialogTheme = R.style.AppTheme)
public final class TimeTrackingApp extends Application {
    //Acra.ch

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    // init über code
/*
        try {
            final ACRAConfiguration config = new ConfigurationBuilder(this)
                        .setMailTo("big.sexy@o2online.de")
                        .setReportingInteractionMode(ReportingInteractionMode.DIALOG)
                        .setResDialogText(R.string.CrashDialogText)  // Res = ressource
                        .setResDialogTitle(R.string.CrashDialogTitle)
                        .setResDialogTheme(R.style.AppTheme)
                   // .setReportDialogClass() // komplett eigener dialog
                        .build();

            ACRA.init(this, config);
        } catch (ACRAConfigurationException e) {
            e.printStackTrace();
        }

*/

    ACRA.init(this ); // rest über die annotation

    }
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
