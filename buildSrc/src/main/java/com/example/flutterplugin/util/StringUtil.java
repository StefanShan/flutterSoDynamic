package com.example.flutterplugin.util;

public class StringUtil {

    public static String capitalize(CharSequence self){
        return  self.length() == 0 ? "" : "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }
}
