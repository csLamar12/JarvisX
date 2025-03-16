package com.Jarvis.JarvisX.Model;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TTS {

    public static void convertTTS(String text) throws Exception {

        try {
            InputStream credentialsStream = TTS.class.getClassLoader().getResourceAsStream("jarvisx-453820-7c0b5e5a52e5.json");
            if (credentialsStream == null) {
                throw new IOException("Credentials file not found.");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();

            // Create the TextToSpeechClient using the settings with credentials
            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
                // Set the text input to be synthesized
                SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

                // Build the voice request
                VoiceSelectionParams voice =
                        VoiceSelectionParams.newBuilder()
                                .setLanguageCode("en-US")
                                .setSsmlGender(SsmlVoiceGender.MALE)
                                .build();

                // Select the audio file type
                AudioConfig audioConfig =
                        AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

                // Perform the text-to-speech request
                SynthesizeSpeechResponse response =
                        textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

                // Get the audio contents
                ByteString audioContents = response.getAudioContent();

                // Write the response to the output file
                try (OutputStream out = new FileOutputStream("src/main/resources/Output/output.mp3")) {
                    out.write(audioContents.toByteArray());
                    System.out.println("Audio content written to file \"output.mp3\"");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}