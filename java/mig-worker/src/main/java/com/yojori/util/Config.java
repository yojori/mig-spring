package com.yojori.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Config {

    private static char seqValue = 0;

    public synchronized static String getOrdNoSequence() {
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    // a-z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    // A-Z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    // 0-9
                    temp.append((rnd.nextInt(10)));
                    break;
            }
        }

        SimpleDateFormat vans = new SimpleDateFormat("yyyyMM");
        return vans.format(new Date()) + "-" + temp.toString();
    }

    public synchronized static String getOrdNoSequence(String type) {
        return type + "-" + getOrdNoSequence();
    }

    public synchronized static String getUUID() {
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    // a-z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    // A-Z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    // 0-9
                    temp.append((rnd.nextInt(10)));
                    break;
            }
        }

        return temp.toString() + String.format("%04d", (++seqValue % 10000));
    }
}
