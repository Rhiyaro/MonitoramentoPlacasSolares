package com.example.monitoramentoplacassolares.activities;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.monitoramentoplacassolares.R;

public class ConfigAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        Toolbar tb = findViewById(R.id.configToolbar);
        tb.setTitle("Configurações");
        setSupportActionBar(tb);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            configuraPreferencias();
        }

        private void configuraPreferencias() {
            EditTextPreference ipPref = findPreference("ip");
            if (ipPref != null) {
                ipPref.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)) {
                        return "Not set";
                    }
                    return text;
                });
            }

            EditTextPreference portPref = findPreference("port");
            if (portPref != null) {
                portPref.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    if (TextUtils.isEmpty(text)) {
                        return "Not set";
                    }
                    return text;
                });
                portPref.setOnBindEditTextListener(
                        editText -> editText.setInputType(InputType.TYPE_CLASS_PHONE));
            }
        }
    }
}