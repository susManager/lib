package fundur.systems.lib.sec;

import java.util.ArrayList;
import java.util.List;

public class Encoder {
    public static String encode(byte[] arr) {
        StringBuilder fun = new StringBuilder();
        for (byte b : arr) {
            fun.append(String.valueOf(((int) b))).append("<>");
        }
        return fun.toString();
    }

    public static byte[] decode(String encoded) {
        List<Byte> bytes = new ArrayList<>();
        boolean dashBefore = false;
        for (String s : encoded.split("<>")) {
                bytes.add(Byte.valueOf(s));

        }
        return toByteArray(bytes);
    }

    public static byte[] toByteArray(List<Byte> bytes) {
        byte[] arr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            arr[i] = bytes.get(i);
        }
        return arr;
    }
}
