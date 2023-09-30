package org.example;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
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
}
