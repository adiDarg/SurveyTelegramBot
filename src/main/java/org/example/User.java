package org.example;

public class User {
    private int currentQuestion;
    private Poll currentDraft;
    public User() {
        currentDraft = null;
        currentQuestion = 0;
    }
    public int getCurrentQuestion() {
        return currentQuestion + 1;
    }

    public void answerQuestion(Poll poll,int answer) {
        poll.getQuestion(currentQuestion).submitAnswer(answer);
        this.currentQuestion++;
        if (this.currentQuestion >= poll.questionCount()) {
            currentQuestion = 0;
        }
    }

    public Poll getCurrentDraft() {
        return currentDraft;
    }

    public void setCurrentDraft(Poll currentDraft) {
        this.currentDraft = currentDraft;
    }
    public boolean isDraftValid(){
        if (currentDraft.questionCount() < 1){
            return false;
        }
        for (int i = 1; i <= currentDraft.questionCount(); i++) {
            if (currentDraft.getQuestion(i).optionCount() < 2) {
                return false;
            }
        }
        return true;
    }
}
