package com.example.ProSudoku;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.example.ProSudoku.R;

/**
 * Created by Vanya on 26.04.2015
 */
public class AboutTwitterFragment extends Fragment {

    private static final String baseURl = "https://twitter.com";
    private static final String widgetInfo = " <a class=\"twitter-timeline\" href=\"https://twitter.com/Bazger\" data-widget-id=\"592304691645534209\">Òâèòû îò @Bazger97</a> " +
            "<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id))" +
            "{js=d.createElement(s);js.id=id;js.src=p+\"://platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script> ";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.about_twitter_fragment, container, false);

        load_background_color(rootView);
        WebView webView = (WebView) rootView.findViewById(R.id.webViewTwitter);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(baseURl, widgetInfo, "text/html", "UTF-8", null);

        return rootView;
    }


    private void load_background_color(View rootView) {
        WebView webView = (WebView) rootView.findViewById(R.id.webViewTwitter);
        //webView.setBackgroundColor(getResources().getColor(R.color.twitter_dark));
        webView.setBackgroundColor(0); // transparent
    }
}
