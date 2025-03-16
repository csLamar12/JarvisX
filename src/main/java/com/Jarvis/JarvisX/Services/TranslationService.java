package com.Jarvis.JarvisX.Services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.cloud.translate.Translation;
import org.springframework.stereotype.Service;


@Service    // This annotation tells Spring that this class is a service class
public class TranslationService {
    private Translate translate;

    public TranslationService() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/jarvisx-453820-7c0b5e5a52e5.json"));
            TranslateOptions options = TranslateOptions.newBuilder().setCredentials(credentials).build();
            this.translate = options.getService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load credentials", e);
        }
    }

    public String translateText(String text, String targetLanguage) {
        Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage(targetLanguage));
        return translation.getTranslatedText();
    }
/*
    private final Translate translate;

    public TranslationService() {
        this.translate = TranslateOptions.getDefaultInstance().getService();
    }

    public String translateText(String text, String targetLanguage) {
        return translation.getTranslatedText();
    }
*/

}
