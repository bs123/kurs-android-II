package de.mvhs.android.zeiterfassung;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import de.mvhs.android.zeiterfassung.web.DefaultReader;
import de.mvhs.android.zeiterfassung.web.GithubApiReader;

/**
 * Created by eugen on 23.01.17.
 */

public class IssueActivity extends AppCompatActivity {
    private WebView _content;
    private final static String _URL = "https://api.github.com/repos/WebDucer-MVHS/kurs-android-II/issues";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);

        _content = (WebView) findViewById(R.id.IssueView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_issues, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.MenuItemLoad:
                if(checkConnection()){
                    // Default
                    //DefaultReader reader = new DefaultReader(_content);
                    //reader.execute(_URL);

                    // API
                    GithubApiReader reader = new GithubApiReader(_content);
                    reader.execute(_URL);
                } else {
                    _content.loadData("Keine Verbindung!", "text/plain", "UTF-8");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkConnection() {
        // Service für Verbindungen
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        // Informationen zu Netzwerken
        NetworkInfo defaultInfo = manager.getActiveNetworkInfo();
        if (defaultInfo.isAvailable()){ // Ein Adapter ist vorhanden
            if (defaultInfo.isConnected()){ // Verbindung aufgebaut
                return true;
            }
        }
        return false; // Nicht verbunden oder nicht aktiv

//        // Spezialisierungen auf WLAN
//        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        if(wifiMgr.isWifiEnabled()){
//            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
//            if (wifiInfo.getNetworkId() == -1){
//                return false; // Mit keinen Access Point verbunden
//            }
//            return true; // Verbunden
//        } else {
//            return false; // WLAN nicht aktiviert
//        }
    }
}
