package ru.tele2.govorova.otus.java.pro.student_management.util;

import java.util.HashMap;
import java.util.Map;

public class TextUtil {

    public static String transliterate(String input) {
        Map<Character, String> transliterationMap = new HashMap<>();
        transliterationMap.put('А', "A");
        transliterationMap.put('Б', "B");
        transliterationMap.put('В', "V");
        transliterationMap.put('Г', "G");
        transliterationMap.put('Д', "D");
        transliterationMap.put('Е', "E");
        transliterationMap.put('Ё', "E");
        transliterationMap.put('Ж', "Zh");
        transliterationMap.put('З', "Z");
        transliterationMap.put('И', "I");
        transliterationMap.put('Й', "Y");
        transliterationMap.put('К', "K");
        transliterationMap.put('Л', "L");
        transliterationMap.put('М', "M");
        transliterationMap.put('Н', "N");
        transliterationMap.put('О', "O");
        transliterationMap.put('П', "P");
        transliterationMap.put('Р', "R");
        transliterationMap.put('С', "S");
        transliterationMap.put('Т', "T");
        transliterationMap.put('У', "U");
        transliterationMap.put('Ф', "F");
        transliterationMap.put('Х', "H");
        transliterationMap.put('Ц', "Ts");
        transliterationMap.put('Ч', "Ch");
        transliterationMap.put('Ш', "Sh");
        transliterationMap.put('Щ', "Sch");
        transliterationMap.put('Ъ', "");
        transliterationMap.put('Ы', "Y");
        transliterationMap.put('Ь', "");
        transliterationMap.put('Э', "E");
        transliterationMap.put('Ю', "Yu");
        transliterationMap.put('Я', "Ya");

        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            char upperCaseChar = Character.toUpperCase(c);
            if (transliterationMap.containsKey(upperCaseChar)) {
                String transliterated = transliterationMap.get(upperCaseChar);
                result.append(transliterated);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
