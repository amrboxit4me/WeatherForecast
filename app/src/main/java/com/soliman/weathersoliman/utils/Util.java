package com.soliman.weathersoliman.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.soliman.weathersoliman.Constants;
import com.soliman.weathersoliman.R;

import java.util.Locale;

/**
 * Created by islam on 3/30/2016.
 */
public class Util {

    private static Util instance;

    public static Util getInstance() {
        if (instance == null) {
            instance = new Util();
        }
        return instance;
    }

    /**
     * check internet availability
     *
     * @param context use getApplicationContext()
     * @return
     */
    public boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivityManager != null) {
                //noinspection deprecation
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.d("Network", "NETWORKNAME: " + anInfo.getTypeName());
                            return true;
                        }
                    }
                }
            }
        }
        Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     *
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public String getUserCountryNameEnglish(Context context) {
        String countryName = "";
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String countrySimIso = tm.getSimCountryIso();
            if (countrySimIso != null && countrySimIso.length() == 2) { // SIM country code is available
                countryName = new Locale("", countrySimIso).getDisplayCountry(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String countryNetworkIso = tm.getNetworkCountryIso();
                if (countryNetworkIso != null && countryNetworkIso.length() == 2) { // network country code is available
                    countryName = new Locale("", countrySimIso).getDisplayCountry(Locale.US);
                }
            }
        } catch (Exception e) {
        }
        return countryName;
    }

    /**
     * to get device country name in arabic language
     * @param context
     * @return
     */
    public String getUserCountryNameArabic(Context context) {
        String countryName = "";
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String countrySimIso = tm.getSimCountryIso();
            if (countrySimIso != null && countrySimIso.length() == 2) { // SIM country code is available
                countryName = new Locale("", countrySimIso).getDisplayCountry();
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String countryNetworkIso = tm.getNetworkCountryIso();
                if (countryNetworkIso != null && countryNetworkIso.length() == 2) { // network country code is available
                    countryName = new Locale("", countrySimIso).getDisplayCountry();
                }
            }
        } catch (Exception e) {
        }
        return countryName;
    }

    /**
     * use this to change app language from english to arabic and vice versa
     * in runtime
     * @param context
     */
    public void changeLanguage(Context context) {
        Locale locale;
        Resources resources = context.getResources();
        if (Shared.getInstance().isEnglish()) {
            locale = new Locale(Constants.ArabicLanguageCode);
            Shared.getInstance().saveLanguage(Constants.ArabicLanguageCode);
        } else {
            locale = new Locale(Constants.EnglishLanguageCode);
            Shared.getInstance().saveLanguage(Constants.EnglishLanguageCode);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        ((Activity) context).recreate();
    }

    /**
     * this to change app language to the saved language in user preferences
     * @param context
     * @return
     */
    public boolean setDefaultLocale(Context context) {
        Resources resources = context.getResources();
        String localLanguage = resources.getConfiguration().locale.getLanguage();
        boolean isLanguageChanged = !Shared.getInstance().getLanguage().equalsIgnoreCase(localLanguage);
        if (isLanguageChanged) {
            Locale locale = new Locale(Shared.getInstance().getLanguage());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        return isLanguageChanged;
    }

    /**
     * to return basic authentication to fill in web service request header
     * @return
     */
    public String getAuth() {
        return String.format("Basic %s", Base64.encodeToString(
                String.format("%s:%s", "username", "password").getBytes(), Base64.DEFAULT));
    }

    /**
     * load image from url and fill it in image view
     *
     * @param context
     * @param imageView   the view which will be filled by the bitmap result
     * @param imageUrl    the image link
     * @param imageHolder this drawable for place holder image
     */
    public void loadImage(Context context, ImageView imageView, String imageUrl, @DrawableRes int imageHolder) {
        GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                //.addHeader("Authorization", Util.getInstance().getAuth())
                .build());
        if (imageHolder == 0)
            imageHolder = R.drawable.error_image;
        Glide.with(context)
                .load(glideUrl)
                .placeholder(imageHolder).error(imageHolder)
                .into(imageView);
    }

    /**
     * convert english number to Arabic number
     * @param number the english number text
     * @return
     */
    public String getArabicNumber(String number) {
        if (Shared.getInstance().isEnglish())
            return number;
        char[] arabicChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            if (Character.isDigit(number.charAt(i))) {
                builder.append(arabicChars[(int) (number.charAt(i)) - 48]);
            } else {
                builder.append(number.charAt(i));
            }
        }
        return builder.toString();
    }

    /**
     * convert english number to Arabic number
     *
     * @param numberText the english number text
     * @return
     */
    public String getArabicNumber(int numberText) {
        String number = String.valueOf(numberText);
        if (Shared.getInstance().isEnglish())
            return number;
        char[] arabicChars = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            if (Character.isDigit(number.charAt(i))) {
                builder.append(arabicChars[(int) (number.charAt(i)) - 48]);
            } else {
                builder.append(number.charAt(i));
            }
        }
        return builder.toString();
    }
}
