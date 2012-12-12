package de.mvhs.android.zeiterfassung;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EinstellungenActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);
	}

	public static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(context.getPackageName()
				+ "_preferences", // Dateiname der Einstellungen
									// "zeiterfassung_preferences"
				PreferenceActivity.MODE_PRIVATE); // Zugriffsmodus
	}
}
