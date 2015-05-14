package com.soboapps.ohfark;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.util.Log;

public class PlayerSetup extends PreferenceActivity {

        private int numOfPlayers = 1;
        private boolean playersPrefChanged = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.local_players_prefs);

                Preference p1 = findPreference("onePlayPref");
                Preference p2 = findPreference("twoPlayPref");
                
                ((PreferenceGroup) findPreference("playerPrefs")).removePreference(p1);

                p1.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                                //startActivity(new Intent(PlayerSetup.this, AiOhFarkActivity.class));
                                PlayerSetup.this.finish();
                                return true;
                        }

                });
                
                p2.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                            startActivity(new Intent(PlayerSetup.this, OhFarkActivity.class));
                            PlayerSetup.this.finish();
                            return true;
                    }

            });

        }

        protected void enablePrefs() {

                Preference p1 = null;
                Preference p2 = null;
                for (int i = 1; i <= 2; i++) {

                        Log.d(getClass().getName(), "player" + i + "Pref");
                        p1 = (Preference) findPreference("onePlayPref");
                        p2 = (Preference) findPreference("twoPlayPref");

                        if (i <= numOfPlayers) {

                                p1.setEnabled(true);
                        } else {
                                p2.setEnabled(false);
                        }
                }
                
                p2 = findPreference("twoPlayPref");
                p2.setEnabled(true);
        }

        protected void setUpListeners(boolean setListeners) {
                CheckBoxPreference c = null;

                for (int i = 2; i <= numOfPlayers; i++) {
                        c = (CheckBoxPreference) findPreference("player" + i + "onePlayPref");

                        EditTextPreference e = (EditTextPreference) findPreference("player" + i + "NamePref");
                        ListPreference l = (ListPreference) findPreference("player" + i + "DiffPref");
                    

                        if (c.isChecked() == true) {
                                e.setEnabled(true);
                                l.setEnabled(false);
                        } else {
                                e.setEnabled(false);
                                l.setEnabled(true);
                        }

                        if (setListeners) {

                                c.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                                        @Override
                                        public boolean onPreferenceChange(Preference preference,
                                                        Object newValue) {

                                                int i = Integer.valueOf(""
                                                                + preference.getKey().charAt(6));

                                                EditTextPreference e = (EditTextPreference) findPreference("player" + i + "NamePref");
                                                ListPreference l = (ListPreference) findPreference("player" + i + "DiffPref");

                                                if (((Boolean) newValue).booleanValue() == true) {
                                                        e.setEnabled(true);
                                                        l.setEnabled(false);
                                                } else {
                                                        e.setEnabled(false);
                                                        l.setEnabled(true);
                                                }

                                                return true;
                                        }

                                });
                        }

                }

        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
                
                Log.d(getClass().getName(),"playersPrefChanged= " + playersPrefChanged);
                outState.putBoolean("playersPrefChanged", playersPrefChanged);
                super.onSaveInstanceState(outState);
        }
}