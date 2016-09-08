package zerotek.gradecalculator.Other;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import zerotek.gradecalculator.R;

/**
 * Created by Mason on 7/11/2016.
 */
public class Advertisements {

    public static void bannerAd(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        AdView adView = (AdView) ((Activity) context).getWindow().getDecorView().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
              //  .addTestDevice(android_id)
                .build();
        adView.loadAd(adRequest);
    }
}
