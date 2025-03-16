package com.Jarvis.JarvisX.Controllers;

import com.Jarvis.JarvisX.Services.TranslationService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class TranslationChatGUI {
    private JFrame frame;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JComboBox<String> languageComboBox;
    private TranslationService translationService;

    public TranslationChatGUI() {
        translationService = new TranslationService();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Translation Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        inputTextArea = new JTextArea();
        inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                translateText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                translateText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                translateText();
            }
        });

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);

        String[] languages = {"es", "fr", "de", "it"}; // Add more languages as needed
        languageComboBox = new JComboBox<>(languages);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(inputTextArea), BorderLayout.NORTH);
        panel.add(languageComboBox, BorderLayout.CENTER);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void translateText() {
        String text = inputTextArea.getText();
        String targetLanguage = (String) languageComboBox.getSelectedItem();
        if (!text.isEmpty()) {
            String translatedText = translationService.translateText(text, targetLanguage);
            outputTextArea.setText(translatedText);
        } else {
            outputTextArea.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TranslationChatGUI());
    }
}
