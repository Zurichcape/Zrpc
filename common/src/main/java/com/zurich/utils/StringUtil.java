package com.zurich.utils;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/22 11:26
 * @description
 */
public class StringUtil {
    public static boolean isBlank(String s){
        if (s == null || s.length() == 0){
            return true;
        }
        for(int i=0;i<s.length();i++){
            if(!Character.isWhitespace(s.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
