package org.crypt;

import java.util.*;
import java.util.stream.Collectors;

public class SubstitutionCipher {
    private static final List<String> CHARSET = List.of("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));

    public static void main(String[] args) {
        String cipherText = "KDNSWSQQKBEKQBZSCYBZSFYDKNXSNBKYNBZSNJSFZAJQAHXSFQKBEKQBZSCYBZSFYDWFEJBANAREQKQ";
        /* As we only have a cipher text, we can use letter frequency to identify main characters */
        List<String> characterList = List.of(cipherText.split(""));
        Map<String, Float> cipherTextFrequency = getCipherTextFrequency(characterList);

        System.out.println("As we only have the cipher text, initiating frequency analysis to identify frequently used letters.");
        for (String character : cipherTextFrequency.keySet()) {
            System.out.println(character + " : " + cipherTextFrequency.get(character));
        }

        String[] estimator = cipherTextFrequency.keySet().toArray(new String[0]);

        System.out.printf("Based on the frequency analysis, assuming E as %s, and T as %s in the cipher text\n", estimator[0], estimator[1]);

        int x1 = getMappedValue("E");
        int y1 = getMappedValue(estimator[0]);
        int x2 = getMappedValue("T");
        int y2 = getMappedValue(estimator[1]);

        System.out.printf("Building equations based on (x1, y1), (x2, y2) pairs => (%d, %d), (%d, %d)\n", x1, y1, x2, y2);
        System.out.println("We can assume the relationship between x and y as a simple equation like y = x + k mod 26, assuming it is a shifted cipher. However, we can build equations in the generic affine form y = a * x + b mod 26 because when solving if it is a simple substitution like a shifted cipher, we will get \"a\" as 1.");

        /* As we only have a cipher text, we can use letter frequency to identify main characters */
        System.out.printf("a * %d + b = %d mod 26\n", x1, y1);
        System.out.printf("a * %d + b = %d mod 26\n", x2, y2);

        int x, y;
        if (x2 > x1) {
            x = x2 - x1;
            y = y2 - y1;
        } else {
            x = x1 - x2;
            y = y1 - y2;
        }
        y = y > 0 ? y : y + 26;
        System.out.printf("\nCombining equations: a * %d = %d mod 26\n", x, y);

        int a = findA(x, y);
        int b = findB(x1, y1, a);

        System.out.printf("Evaluated a as %d\n", a);
        System.out.printf("Evaluated b as %d\n", b);

        System.out.println("\nCracked Text:");
        for (String character : characterList) {
            int yDash = getMappedValue(character.toUpperCase());
            int xDash = getX(yDash % 26, a, b);
            System.out.print(CHARSET.get(xDash));
        }
        System.out.println(".");
    }

    private static Map<String, Float> getCipherTextFrequency(List<String> characterList) {
        Map<String, Float> cipherTextFrequency = new HashMap<>();
        for (String character : characterList) {
            if (!cipherTextFrequency.containsKey(character)) {
                cipherTextFrequency.put(character, 0.0F);
            }
            cipherTextFrequency.put(character, cipherTextFrequency.get(character) + 1);
        }
        cipherTextFrequency = sortMapByValue(cipherTextFrequency);
        cipherTextFrequency.replaceAll((k, v) -> v / characterList.size() * 100);
        return cipherTextFrequency;
    }

    private static int getX(int y, int a, int b) {
        int x = 0;
        while ((a * x + b) % 26 != y) {
            x++;
        }
        return x % 26;
    }

    private static int findB(int x1, int y1, int a) {
        int b = 1;
        while ((a * x1 + b) % 26 != y1) {
            b++;
        }
        return b % 26;
    }

    private static int findA(int x, int y) {
        int a = 1;
        while (a * x % 26 != y) {
            a++;
        }
        return a % 26;
    }

    private static int getMappedValue(String character) {
        return CHARSET.indexOf(character);
    }

    private static Map<String, Float> sortMapByValue(Map<String, Float> unsortedMap) {
        List<Map.Entry<String, Float>> list = new LinkedList<>(unsortedMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }
}
