package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Poll {
    private String title;
    private final List<Question> questions;
    public Poll(String title) {
        this.title = title;
        questions = new ArrayList<Question>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Question getQuestion(int index) {
        return questions.get(index - 1);
    }
    public void addQuestion(Question question) {
        if (questions.size() < 3) {
            questions.add(question);
        }
    }
    public void removeQuestion(int questionIndex) {
        questions.remove(questionIndex - 1);
    }
    public int questionCount() {
        return questions.size();
    }
    public String toString(){
        StringBuilder result = new StringBuilder(title + "\n");
        for (int i = 0; i < questions.size(); i++) {
            result.append("\t").append(i + 1).append(")").append(questions.get(i).toString()).append("\n");
        }
        return result.toString();
    }
    public String statistics(){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            result.append("Question ").append(i + 1).append(":\n");
            int finalI = i;
            List<Integer> orderedPercent = questions.get(i).getAnswers().keySet().stream().
                    sorted((k1,k2) -> -1 * questions.get(finalI).getAnswers().get(k1).compareTo(questions.get(finalI).getAnswers().get(k2)))
                    .toList();
            for (int j = 0; j < orderedPercent.size(); j++) {
                result.append("\t").append(orderedPercent.get(j)).append(": ").append((double)questions.get(finalI).getAnswers().get(orderedPercent.get(j)) * 100/questions.get(finalI).answerCount()).append("%\n");
            }
        }
        return result.toString();
    }
}
