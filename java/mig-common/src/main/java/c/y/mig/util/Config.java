package c.y.mig.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Config {

    private static char seqValue = 0;
    private static Config instance = new Config();
    private static Map<String, String> properties = new HashMap<>();

    public static Config getConfig() {
        return instance;
    }

    public String getString(String key) {
        return properties.getOrDefault(key, "");
    }

    public static String getOrdNoSequence() {
        return Long.toString(System.currentTimeMillis(), 36);
    }

    public static String getOrdNoSequence(String type) {
        return type + "-" + getOrdNoSequence();
    }

    public synchronized static String getUUID() {
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0: temp.append((char) ((int) (rnd.nextInt(26)) + 97)); break;
                case 1: temp.append((char) ((int) (rnd.nextInt(26)) + 65)); break;
                case 2: temp.append((rnd.nextInt(10))); break;
            }
        }
        return temp.toString() + String.format("%04d", (++seqValue % 10000));
    }
}
