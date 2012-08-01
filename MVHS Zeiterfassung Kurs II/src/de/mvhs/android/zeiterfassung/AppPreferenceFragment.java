package de.mvhs.android.zeiterfassung;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class AppPreferenceFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Laden der Einstellungsdefinition aus XML
		addPreferencesFromResource(R.xml.pref_fragment);
	}
	
	/**
	 * Zugriff auf die App Einstellungen
	 * @param context
	 * App Context
	 * @return
	 * SharedPreferences Objekt auf die App Einstellungen
	 */
	public static SharedPreferences getPreferences(Context context){
		return context.getSharedPreferences(
				context.getPackageName() + "_preferences", // Dateiname (zeiterfassung_preferences)
				PreferenceActivity.MODE_PRIVATE); // Zugriffsmodus
	}
}
