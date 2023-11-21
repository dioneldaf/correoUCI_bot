package org.example;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class Helper {
    public static void LAF() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    public static String getEnv(String key) {
        Dotenv dotenv = Dotenv.load();
        return dotenv.get(key);
    }

    public static void saveBin(ArrayList<?> list) {
        try {
            FileOutputStream file = new FileOutputStream(Const.PATH);
            ObjectOutputStream pipeline = new ObjectOutputStream(file);
            pipeline.writeObject(list);
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<BotUser> loadBin() throws Exception {
        Object object;
        FileInputStream file = new FileInputStream(Const.PATH);
        ObjectInputStream pipeline = new ObjectInputStream(file);
        object = pipeline.readObject();
        file.close();
        //noinspection unchecked
        return (ArrayList<BotUser>) object;
    }

    public static LocalDateTime parseDate(String stringDate) {
        System.out.println(stringDate);
        int year;
        int month;
        int day;
        LocalTime hour;
        if (stringDate.contains("/")) {
            String[] texts = stringDate.split("/");
            year = Integer.parseInt(texts[Const.THIRD_ELEMENT]) + 2000;
            month = Integer.parseInt(texts[Const.FIRST_ELEMENT]);
            day = Integer.parseInt(texts[Const.SECOND_ELEMENT]);
            hour = LocalTime.MIDNIGHT;
        } else if (stringDate.contains("de")) {
            year = LocalDateTime.now().getYear();
            String[] texts = stringDate.split(" de ");
            month = monthToInt(texts[Const.SECOND_ELEMENT]);
            day = Integer.parseInt(texts[Const.FIRST_ELEMENT]);
            hour = LocalTime.MIDNIGHT;
        } else if (stringDate.split(" ").length == 2 && !stringDate.contains("AM") && !stringDate.contains("PM")) {
            stringDate = stringDate.toLowerCase();
            year = LocalDateTime.now().getYear();
            String[] texts = stringDate.split(" ");
            month = monthToInt(texts[Const.FIRST_ELEMENT]);
            day = Integer.parseInt(texts[Const.SECOND_ELEMENT]);
            hour = LocalTime.MIDNIGHT;
        } else {
            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
            month = now.getMonthValue();
            day = now.getDayOfMonth();
            String[] texts = stringDate.split(" ");
            if (texts[Const.FIRST_ELEMENT].split(":")[0].length() == 1)
                texts[Const.FIRST_ELEMENT] = "0".concat(texts[Const.FIRST_ELEMENT]);
            hour = LocalTime.parse((texts[Const.FIRST_ELEMENT]));
            if (texts[Const.SECOND_ELEMENT].equalsIgnoreCase("PM")) hour = hour.plusHours(12);
        }
        return LocalDateTime.of(LocalDate.of(year, month, day), hour);
    }

    private static int monthToInt(String stringMonth) {
        switch (stringMonth) {
            case "ene" -> {
                return 1;
            }
            case "feb" -> {
                return 2;
            }
            case "mar" -> {
                return 3;
            }
            case "abr" -> {
                return 4;
            }
            case "may" -> {
                return 5;
            }
            case "jun" -> {
                return 6;
            }
            case "jul" -> {
                return 7;
            }
            case "ago" -> {
                return 8;
            }
            case "sep" -> {
                return 9;
            }
            case "oct" -> {
                return 10;
            }
            case "nov" -> {
                return 11;
            }
            case "dic" -> {
                return 12;
            }
            default -> {
                return -1;
            }
        }
    }
}
