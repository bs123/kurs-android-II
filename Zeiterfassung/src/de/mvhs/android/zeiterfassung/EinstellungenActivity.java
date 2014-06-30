package de.mvhs.android.zeiterfassung;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EinstellungenActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Laden der Einsellungs-XML
		addPreferencesFromResource(R.xml.preferences);
	}
}
