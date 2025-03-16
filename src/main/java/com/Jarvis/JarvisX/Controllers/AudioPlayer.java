package com.Jarvis.JarvisX.Controllers;

import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class AudioPlayer {

    private String filePath;

    public AudioPlayer() {
        filePath = "src/main/resources/Output/output.mp3";
    }

    public void playSound() {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            Player player = new Player(bis);
            player.play();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}