package com.example.airqualityapp.ui.info;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.airqualityapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * App- Info
 */
public class InfoFragment extends Fragment {

    private InfoViewModel infoViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        infoViewModel =
                new ViewModelProvider(this).get(InfoViewModel.class);

        View root = inflater.inflate(R.layout.fragment_info, container, false);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView versionNameTagView;
        final WebView webView;
        final ProgressBar progress;
        final StringBuilder htmlSite=new StringBuilder();
        StringBuilder htSite;

        final Handler handler = new Handler();
        Context context= getActivity().getApplicationContext();

        versionNameTagView=view.findViewById(R.id.version_name_tag_display);
        webView = (WebView) view.findViewById(R.id.browser);
        progress = (ProgressBar) view.findViewById(R.id.html_load_progress);

        // @rem:Get current locale (determine language from Androids settings@@
        //final Locale current=getResources().getConfiguration().locale;
        final String current = getResources().getConfiguration().locale.getLanguage();

        // @rem: Shows how to retrieve the version- name tag from the 'build.gradle'- file@@
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            versionNameTagView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionNameTagView.setText("-");
        }
        //@@

        // Load html...
        htSite =infoViewModel.getHtmlSite();

        if (htSite.toString().isEmpty()) {
            progress.setVisibility(View.VISIBLE);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        BufferedReader bufferedReader = null;

                        // @rem:Shows how to load data from androids 'assests'- folder@@
                        if (current.equals("de") || current.equals("en")) {
                            if (current.equals("de"))
                                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("InfoPage-de.html")));
                            if (current.equals("en"))
                                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("InfoPage-en.html")));
                        } else
                            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("InfoPage-en.html")));

                        String line;
                        int lines = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            htmlSite.append(line);
                            lines++;
                        }

                    } catch (IOException io) {
                        Log.v("Info", io.toString());
                    }

                    // Wait a vew millisec's to enable the main UI thread
                    // to react.
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }

                    // Show
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.GONE);
                            webView.loadData(htmlSite.toString(), "text/html", null);
                            infoViewModel.setHtmlSite(htmlSite);
                        }
                    });
                }
            });
            t.start();
        } else {
            progress.setVisibility(View.GONE);
            webView.loadData(htSite.toString(), "text/html", null);
        }
    }
}