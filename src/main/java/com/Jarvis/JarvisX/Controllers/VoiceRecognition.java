package com.Jarvis.JarvisX.Controllers;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.rpc.*;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/***
 * The boilerplate code for the API was copied from the console.cloud.google.com website and edited
 * for our use case. Our use case being transcribing audio from the device's microphone.
 */
public class VoiceRecognition {
    private static final Logger log = LogManager.getLogger(VoiceRecognition.class);

    /** Performs microphone streaming speech recognition with dynamic end. */
    public static void streamingMicRecognize(String jsonFileName) throws Exception {

        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try {
            InputStream credentialsStream = VoiceRecognition.class.getClassLoader().getResourceAsStream(jsonFileName);
            if (credentialsStream == null) {
                throw new IOException("Credentials file not found: " + jsonFileName);
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();

            try (SpeechClient client = SpeechClient.create(settings)) {

                responseObserver =
                        new ResponseObserver<StreamingRecognizeResponse>() {
                            ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                            public void onStart(StreamController controller) {}

                            public void onResponse(StreamingRecognizeResponse response) {
                                responses.add(response);
                                for (StreamingRecognitionResult result : response.getResultsList()) {
                                    if (result.getAlternativesList().size() > 0) {
                                        System.out.println("Transcript: " + result.getAlternativesList().get(0).getTranscript());
                                    }
                                }
                            }

                            public void onComplete() {
                                System.out.println("Streaming completed.");
                            }

                            public void onError(Throwable t) {
                                System.err.println("Error during streaming: " + t);
                            }
                        };

                ClientStream<StreamingRecognizeRequest> clientStream =
                        client.streamingRecognizeCallable().splitCall(responseObserver);

                RecognitionConfig recognitionConfig =
                        RecognitionConfig.newBuilder()
                                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                .setLanguageCode("en-US")
                                .setSampleRateHertz(16000)
                                .build();
                StreamingRecognitionConfig streamingRecognitionConfig =
                        StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

                StreamingRecognizeRequest request =
                        StreamingRecognizeRequest.newBuilder()
                                .setStreamingConfig(streamingRecognitionConfig)
                                .build();

                clientStream.send(request);

                AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

                if (!AudioSystem.isLineSupported(targetInfo)) {
                    System.out.println("Microphone not supported");
                    System.exit(0);
                }

                TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
                targetDataLine.open(audioFormat);
                targetDataLine.start();

                System.out.println("Start speaking...");

                byte[] buffer = new byte[6400];
                int bytesRead;

                final int SILENCE_THRESHOLD = 100; // Adjust this value
                final long SILENCE_DURATION_MS = 1000; // Adjust this value

                long silenceStart = 0;
                boolean silenceDetected = false;

                while ((bytesRead = targetDataLine.read(buffer, 0, buffer.length)) != -1) {
                    ByteString audioBytes = ByteString.copyFrom(buffer, 0, bytesRead);
                    request = StreamingRecognizeRequest.newBuilder().setAudioContent(audioBytes).build();
                    clientStream.send(request);

                    int maxAmplitude = 0;
                    for (int i = 0; i < bytesRead; i += 2) {
                        int sample = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
                        if (sample > 32767) {
                            sample -= 65536;
                        }
                        maxAmplitude = Math.max(maxAmplitude, Math.abs(sample));
                    }

                    if (maxAmplitude < SILENCE_THRESHOLD) {
                        if (!silenceDetected) {
                            silenceDetected = true;
                            silenceStart = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - silenceStart > SILENCE_DURATION_MS) {
                            System.out.println("Silence detected. Stopping stream.");
                            break;
                        }
                    } else {
                        silenceDetected = false;
                    }
                }

                targetDataLine.stop();
                targetDataLine.close();
                clientStream.closeSend();
            }
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    public static void main(String[] args) {
        try {
            streamingMicRecognize("jarvisx-453820-7c0b5e5a52e5.json"); // Replace with your file name
        } catch (Exception e) {
            log.error("Error in main: ", e);
        }
    }
}