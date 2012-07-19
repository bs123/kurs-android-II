package de.mvhs.android.zeiterfassung;

import java.util.List;

import android.preference.PreferenceActivity;

public class AppPreferenceActivity extends PreferenceActivity {
	@Override
	public void onBuildHeaders(List<Header> target) {
		// Laden der Kopfdaten aus XML
		loadHeadersFromResource(R.xml.pref_activity, target);
	}
}
