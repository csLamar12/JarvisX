package com.Jarvis.JarvisX.Model;

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

public class VoiceRecognition {
    private static final Logger log = LogManager.getLogger(VoiceRecognition.class);
    private static String transcription;
    private static boolean listening = false;

    public static String streamingMicRecognize(String jsonKey) throws Exception {

        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try {
            InputStream speechCredentialsStream = VoiceRecognition.class.getClassLoader().getResourceAsStream(jsonKey);
            if (speechCredentialsStream == null) {
                throw new IOException("Speech Credentials file not found: " + jsonKey);
            }
            GoogleCredentials speechCredentials = GoogleCredentials.fromStream(speechCredentialsStream);
            FixedCredentialsProvider speechCredentialsProvider = FixedCredentialsProvider.create(speechCredentials);
            SpeechSettings speechSettings = SpeechSettings.newBuilder().setCredentialsProvider(speechCredentialsProvider).build();

            try (SpeechClient speechClient = SpeechClient.create(speechSettings)) {

                responseObserver =
                        new ResponseObserver<StreamingRecognizeResponse>() {
                            ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                            public void onStart(StreamController controller) {}

                            public void onResponse(StreamingRecognizeResponse response) {
                                responses.add(response);
                                for (StreamingRecognitionResult result : response.getResultsList()) {
                                    if (result.getAlternativesList().size() > 0) {
                                        transcription = result.getAlternativesList().get(0).getTranscript();

                                        if (!listening && transcription.toLowerCase().contains("hey jarvis")) {
                                            listening = true;
                                            System.out.println("Listening...");
                                        }

                                        if (listening) {
                                            System.out.println("Transcript: " + transcription); // Moved inside listening check.
                                        }
                                    }
                                }
                            }

                            public void onComplete() {
                                System.out.println("Streaming completed.");
                                listening = false;
                            }

                            public void onError(Throwable t) {
                                System.err.println("Error during streaming: " + t);
                                listening = false;
                            }
                        };

                ClientStream<StreamingRecognizeRequest> clientStream =
                        speechClient.streamingRecognizeCallable().splitCall(responseObserver);

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

                final int SILENCE_THRESHOLD = 1000;
                final long SILENCE_DURATION_MS = 1000;

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

//                    System.out.println(maxAmplitude);
                    if (listening && transcription.toLowerCase().contains("hey jarvis")){
                        continue;
                    }
                    if (maxAmplitude < SILENCE_THRESHOLD) {
                        if (!silenceDetected) {
                            silenceDetected = true;
                            silenceStart = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - silenceStart > SILENCE_DURATION_MS && listening) {
                            System.out.println("Silence detected. Stopping stream.");
                            listening = false;
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
            listening = false;
        }
        return transcription;
    }
}