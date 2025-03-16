package com.Jarvis.JarvisX.Controllers;

import com.Jarvis.JarvisX.Services.TranslationService;
import com.Jarvis.JarvisX.Exceptions.TranslationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@RestController
@RequestMapping("/api/translate")
public class TranslationController {
    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public String translate(@RequestParam String text, @RequestParam String targetLanguage) {
        return translationService.translateText(text, targetLanguage);
    }
}*/

@RestController
@RequestMapping("/api/translate")
public class TranslationController {
    private static final Logger logger = LoggerFactory.getLogger(TranslationController.class);
    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<String> translate(@RequestParam String text, @RequestParam String targetLanguage) {
        try {
            if (text == null || text.isEmpty() || targetLanguage == null || targetLanguage.isEmpty()) {
                logger.error("Text and target language must be provided");
                return ResponseEntity.badRequest().body("Text and target language must be provided");
            }
            String translatedText = translationService.translateText(text, targetLanguage);
            return ResponseEntity.ok(translatedText);
        } catch (TranslationException e) {
            logger.error("Translation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Translation failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}