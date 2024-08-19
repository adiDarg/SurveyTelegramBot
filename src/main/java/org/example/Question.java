package org.example;

import java.util.*;

public class Question {
    private final String question;
    private final List<String> options;
    private final Map<Integer,Integer> answers;
    public Question(String question) {
        this.question = question;
        options = new ArrayList<>();
        answers = new HashMap<>();
    }
    public Map<Integer, Integer> getAnswers() {
        return answers;
    }
    public int optionCount() {
        return options.size();
    }
    public int answerCount(){
        return answers.size();
    }
    public void addOption(String option) {
        if (options.size() < 4) {
            options.add(option);
            answers.put(options.size(),0);
        }
    }
    public void removeOption(int optionIndex) {
        options.remove(optionIndex - 1);
    }
    public void submitAnswer(int option){
        answers.put(option,answers.get(option) + 1);
    }
    public String toString(){
        StringBuilder result = new StringBuilder(question + "\n");
        for (int i = 0; i < options.size(); i++) {
            result.append("\t\t").append(i + 1).append(".").append(options.get(i)).append("\n");
        }
        return result.toString();
    }
}
