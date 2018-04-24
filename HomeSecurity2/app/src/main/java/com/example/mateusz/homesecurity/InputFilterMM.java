package com.example.mateusz.homesecurity;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

//https://stackoverflow.com/questions/14212518/is-there-a-way-to-define-a-min-and-max-value-for-edittext-in-android

public class InputFilterMM implements InputFilter {

    private int min, max;
    private final String TAG = "Input Filter Min Max";

    public InputFilterMM(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMM(String min, String max){
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try{
            int input = Integer.parseInt(dest.toString() + source.toString());
            if(isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe){
            Log.e(TAG, nfe.getMessage());
        }
        return null;
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}






