package com.Jarvis.JarvisX.Model;

import java.util.Random;

public class GenericResponses {

    String[] greetings = {"Good day! How can I assist you?",
            "Hey there! What’s on your mind?",
            "Hello! Ready to get things done?",
            "Hi! How can I make your life easier today?",
            "Welcome back! What do you need help with?",
            "Greetings! What can I do for you?",
            "Hey! How’s your day going?",
            "Hello, friend! Let’s get started.",
            "At your service! What’s the task?",
            "Hi there! Need something?"};
    int randNum;

    public GenericResponses(){

    }

    public int getRandNum(){
        Random rand = new Random();
        this.randNum = rand.nextInt(10);
        return randNum;
    }

    public String[] getGreetings() {
        return greetings;
    }

    public String getAGreeting(){
        return greetings[getRandNum()];
    }
}
