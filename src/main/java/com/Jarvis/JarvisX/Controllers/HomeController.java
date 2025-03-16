package com.Jarvis.JarvisX.Controllers;

import com.Jarvis.JarvisX.Model.GenericResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import static com.Jarvis.JarvisX.Model.TTS.convertTTS;
import static com.Jarvis.JarvisX.Model.VoiceRecognition.streamingMicRecognize;

@Controller
public class HomeController implements CommandLineRunner {
    private static final Logger log = LogManager.getLogger(HomeController.class);
    private GenericResponses gr;
    private String transcription = "";

    @Override
    public void run(String... args) throws Exception {
        gr = new GenericResponses();
        startListeningThread();
        speak(gr.getAGreeting());
    }

    private void startListeningThread() {
        Thread listeningThread = new Thread(() -> {
            while (true) { // Infinite loop to keep listening
                try {
                    transcription = streamingMicRecognize("jarvisx-453820-7c0b5e5a52e5.json");

                    if (transcription != null && !transcription.isEmpty()) {
                        System.out.println("Home Controller: " + transcription);
                        if (transcription.toLowerCase().contains("jarvis shutdown")){
                            log.info("Application exited with code 9 - Shutdown");
                            System.exit(9);
                        }
                        // Process the transcription (e.g., send to LLM, execute commands)
                        // ... your processing logic here ...
                        //reset transcription
                        transcription = "";
                    }


                } catch (Exception e) {
                    log.error("Error in listening thread: ", e);
                    // Optionally add a delay here to prevent rapid retries after an error
                    try {
                        Thread.sleep(5000); // Wait for 5 seconds before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        listeningThread.start();
    }

    private void speak(String text){
        try {
            convertTTS(text);
            AudioPlayer ap = new AudioPlayer();
            ap.playSound();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void displayWelcomeScreen(String greeting){
        System.out.println(greeting);
    }
}
