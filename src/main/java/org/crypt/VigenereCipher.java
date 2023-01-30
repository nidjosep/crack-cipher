package org.crypt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VigenereCipher {
    private static final int KEY_LENGTH = 5;
    private static final Map<Character, Float> FREQ_MAP = new LinkedHashMap<>();

    static {
        FREQ_MAP.put('A', 8.2F);
        FREQ_MAP.put('B', 1.5F);
        FREQ_MAP.put('C', 2.8F);
        FREQ_MAP.put('D', 4.2F);
        FREQ_MAP.put('E', 12.7F);
        FREQ_MAP.put('F', 2.2F);
        FREQ_MAP.put('G', 2.0F);
        FREQ_MAP.put('H', 6.01F);
        FREQ_MAP.put('I', 7.0F);
        FREQ_MAP.put('J', 0.1F);
        FREQ_MAP.put('K', 0.1F);
        FREQ_MAP.put('L', 4.0F);
        FREQ_MAP.put('M', 2.4F);
        FREQ_MAP.put('N', 6.7F);
        FREQ_MAP.put('O', 7.5F);
        FREQ_MAP.put('P', 1.9F);
        FREQ_MAP.put('Q', 0.1F);
        FREQ_MAP.put('R', 6.0F);
        FREQ_MAP.put('S', 6.3F);
        FREQ_MAP.put('T', 9.0F);
        FREQ_MAP.put('U', 2.8F);
        FREQ_MAP.put('V', 1.0F);
        FREQ_MAP.put('W', 2.4F);
        FREQ_MAP.put('X', 0.1F);
        FREQ_MAP.put('Y', 2.0F);
        FREQ_MAP.put('Z', 0.1F);
    }

    private static final List<Character> CHARACTER_SET = FREQ_MAP.keySet().stream().toList();

    public static void main(String[] args) {
        String cipherText = "QAVHYFOVEDFMPBQKXNOCRGJJTZDZFDFMLNEBWFAEEXKELABKVZKTCGPOKZGZORFSEEXNBWXLKBBBRGRZMEVGSBKZIPOMYNECEFJDYXJVOBMYRFKBMRCPBKLTPPFYLPMFDLIHETHEBTUWFOVGSBPFYLPMFDTVBBJZITJGZNKVALJXUFEGHYACFOVEMVXREWVVFYZKBRYLRMYBCFMZRDTTJNGFMRYAXKKBQIBWRQLKKUPTHCNDQHHVJFDGEZSBUVYDYFBOXGUZPABTVYBTEQNLGERNQBETEEXNBWXLKBBBRMVWITXRDXEFARPBURTQMYRWXGUSPAUPGSBKZIPOPRFFPXUSZOALAEFGXULOOVFEFGXZPABTVYBLRAOLUKNTKBETMRBCQTKZDNEBKZNWPWRVWVEZSPXGUPFIMLEPTXIRCFVYNYAORETBWTRCBFFATBLNRCBIVEQLKDROXGUXYLPCRODXNNDMTJFPAWFJYQAIBFDAXRYBKRGTLGJGSBNEVGBKJVEVHWAPTUIHYPPZPVFLKUPCBIFEBGXYTPACNYDNRTPRGZIPOLZGJFGTNYXWROFQMYRQFKJGWXGXHLDXJCZHXEBYQAZFWXGUVDTHCNDQHHRJITKHHBPRXZKMYRQFKJGEBTTUTKZJBYQAVYLKWNRCBMYREBTTUTKZJBQQAVJZITJGZNBPVVX";
        System.out.printf("Splitting the cipher text into %d groups.\n", KEY_LENGTH);
        List<String> wordList = getCharacterGroups(cipherText, KEY_LENGTH);
        StringBuilder keyBuilder = new StringBuilder();
        int[] keyCodes = new int[KEY_LENGTH];
        for (int groupIndex = 0; groupIndex < KEY_LENGTH; groupIndex++) {
            List<Character> characterGroup = getCharacterGroupByIndex(wordList, groupIndex);
            Map<Character, Float> cipherFrequencyMap = getCipherTextFrequencyMap(characterGroup);
            keyCodes[groupIndex] = getBestDeltaDistance(cipherFrequencyMap);
            System.out.printf("The delta for group %d has been identified as %d (%s).\n", groupIndex, keyCodes[groupIndex], CHARACTER_SET.get(keyCodes[groupIndex]));
            keyBuilder.append(CHARACTER_SET.get(keyCodes[groupIndex]));
        }
        System.out.println("\nThe encryption key has been identified as " + keyBuilder);

        StringBuilder plainText = new StringBuilder();
        for (int index = 0; index < cipherText.length(); index++) {
            int y = CHARACTER_SET.indexOf(cipherText.charAt(index));
            int k = keyCodes[index % KEY_LENGTH];
            int reverseShift = y - k > 0 ? y - k : y - k + 26;
            plainText.append(CHARACTER_SET.get(reverseShift % 26));
        }
        System.out.println("Decrypted plain text is: " + plainText);
    }

    private static List<String> getCharacterGroups(String cipherText, int groupSize) {
        List<String> characterGroup = new ArrayList<>();
        int index = 0;
        while (index < cipherText.length()) {
            String word = cipherText.substring(index, Math.min(index + groupSize, cipherText.length()));
            characterGroup.add(word);
            index += groupSize;
        }
        return characterGroup;
    }

    private static List<Character> getCharacterGroupByIndex(List<String> wordList, int groupIndex) {
        return wordList.stream()
                .filter(word -> groupIndex < word.length())
                .map(word -> word.charAt(groupIndex))
                .collect(Collectors.toList());
    }

    private static Map<Character, Float> getCipherTextFrequencyMap(List<Character> characterList) {
        Map<Character, Float> characterOccurrences = new HashMap<>();
        for (Character character : characterList) {
            if (!characterOccurrences.containsKey(character)) {
                characterOccurrences.put(character, 0F);
            }
            characterOccurrences.put(character, characterOccurrences.get(character) + 1);
        }
        characterOccurrences.replaceAll((k, v) -> v / characterList.size() * 100);
        return characterOccurrences;
    }

    private static int getBestDeltaDistance(Map<Character, Float> cipherFrequencyMap) {
        int minimumDelta = Integer.MAX_VALUE;
        double minimumDiff = Double.MAX_VALUE;
        List<Float> standardFrequencies = FREQ_MAP.values().stream().toList();
        List<Float> cipherFrequencies = new ArrayList<>();
        for (Character character : CHARACTER_SET) {
            cipherFrequencies.add(cipherFrequencyMap.getOrDefault(character, 0F));
        }
        for (int delta = 0; delta < 26; delta++) {
            double xSquare = IntStream.range(0, standardFrequencies.size())
                    .mapToDouble(position -> getXSquareByPosition(standardFrequencies, cipherFrequencies, position)).sum();
            if (xSquare < minimumDiff) {
                minimumDiff = xSquare;
                minimumDelta = delta;
            }
            Collections.rotate(cipherFrequencies, -1);
        }
        return minimumDelta;
    }

    private static double getXSquareByPosition(List<Float> standardFrequencies, List<Float> cipherFrequencies, int i) {
        return (Math.pow(cipherFrequencies.get(i) - standardFrequencies.get(i), 2)) / standardFrequencies.get(i);
    }
}
