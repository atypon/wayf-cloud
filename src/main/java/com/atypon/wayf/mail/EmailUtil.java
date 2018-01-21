package com.atypon.wayf.mail;


import java.util.regex.Pattern;

public class EmailUtil {

    private final static String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$";
    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);


    public static boolean IsValidEmailAddress(String mailAddress){
        return EMAIL_ADDRESS_PATTERN.matcher(mailAddress).find();
    }


}
