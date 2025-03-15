package com.Jarvis.JarvisX.Controllers;

import com.Jarvis.JarvisX.Model.GenericResponses;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController implements CommandLineRunner {
    GenericResponses gr;

    @Override
    public void run(String... args) throws Exception {
        gr = new GenericResponses();
        displayWelcomeScreen(gr.getAGreeting());
    }

    public void displayWelcomeScreen(String greeting){
        System.out.println(greeting);
    }
}
