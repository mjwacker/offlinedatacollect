package gov.mt.mdt.data;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(PreferencesActivity.this, R.xml.preferences, false);
	}
}