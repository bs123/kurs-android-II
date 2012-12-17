package de.mvhs.android.zeiterfassung;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EinstellungenActivity extends PreferenceActivity {
	private final static String _EXPORT_PATH_KEY = "eport_path";
	private final static String _SEND_TO_ADDRESS = "send_to";

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

	/**
	 * Vom Benutzer eingestellter Export-Verzeichnis
	 * 
	 * @param context
	 *            App Context
	 * @return Pfad zum Export-Verzeichnis
	 */
	public static String getExportPath(Context context) {
		return getPrefs(context).getString(_EXPORT_PATH_KEY,
				"mnt/sdcard/export");
	}

	/**
	 * Vom Benutzer eingestellte E-Mail-Adresse für den Export
	 * 
	 * @param context
	 *            App Context
	 * @return E-Mail-Adresse für den Export
	 */
	public static String getSendToAddress(Context context) {
		return getPrefs(context).getString(_SEND_TO_ADDRESS, "");
	}
}
