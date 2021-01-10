package com.example.airqualityapp.ui.info;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InfoViewModel extends ViewModel {

    StringBuilder htmlSite=new StringBuilder();

    public InfoViewModel() {

    }

    public void setHtmlSite(StringBuilder htmlsite){
        this.htmlSite=htmlsite;
    }

    public StringBuilder getHtmlSite(){
        return htmlSite;
    }
}