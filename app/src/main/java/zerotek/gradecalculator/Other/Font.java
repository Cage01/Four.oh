package zerotek.gradecalculator.Other;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;

/**
 * Created by Mason on 7/16/2016.
 */
public class Font {

public static void setFont(Context context, Button button){
    Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
    button.setTypeface(type);
}

}
