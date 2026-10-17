package com.example.music_streamer.logger;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;

public class Logger {

    private final Context context;
    public Logger(Context context) {
        this.context = context;
    }
    public void log(String log) {
        FileWriter fileWriter;
        try {
            File file = new File(context.getFilesDir(), "log.txt");

            fileWriter = new FileWriter(file, true);
            fileWriter.write(log);
            fileWriter.close();
        } catch (Exception exception) {
            System.out.println("Error logging details : " + exception.getMessage());
        }
    }
}
