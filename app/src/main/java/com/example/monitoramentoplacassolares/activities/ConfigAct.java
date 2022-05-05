package com.example.monitoramentoplacassolares.activities;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.monitoramentoplacassolares.R;
import com.google.firebase.messaging.FirebaseMessaging;

public class ConfigAct extends AppCompatActivity {

    public static final String TOPICO_NOTIFICACOES = "avisos";

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

            final SwitchPreference recebePref = findPreference("recebe_avisos");
            if (recebePref != null){
                recebePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (!recebePref.isChecked()) { // Método é chamado com valor anterior, então a checagem é invertida
                        FirebaseMessaging.getInstance().subscribeToTopic(TOPICO_NOTIFICACOES)
                                .addOnCompleteListener(task -> {
                                    String msg;
                                    if (task.isSuccessful()) {
                                        msg = "Você receberá notificações agora";//getString(R.string.msg_subscribed);
                                    } else {
                                        msg = "Erro ao se inscrever no tópico"; //getString(R.string.msg_subscribe_failed);
                                    }
                                    Toast.makeText(preference.getContext(), msg, Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPICO_NOTIFICACOES)
                                .addOnCompleteListener(task -> {
                                    String msg;
                                    if (task.isSuccessful()) {
                                        msg = "Você NÃO receberá mais notificações";//getString(R.string.msg_unsubscribed);
                                    } else {
                                        msg = "Erro ao cancelar inscrição no tópico"; //getString(R.string.msg_unsubscribe_failed);
                                    }
                                    Toast.makeText(preference.getContext(), msg, Toast.LENGTH_SHORT).show();
                                });
                    }

                    return true;
                });
            }
        }
    }
}