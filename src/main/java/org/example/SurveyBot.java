package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SurveyBot extends TelegramLongPollingBot {
    private final Map<Long,User> chatIds;
    private Poll poll;
    private final Map<Poll,LocalDateTime> timesReserved;
    public SurveyBot() {
        chatIds = new HashMap<>();
        poll = null;
        timesReserved = new HashMap<>();
    }
    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = new SendMessage();
        Long currentId = update.getMessage().getChatId();
        message.setChatId(currentId);
        if ((update.getMessage().getText().equals("/start") || update.getMessage().getText().equals("hi") ||
        update.getMessage().getText().equals("היי")) && !chatIds.containsKey(currentId)) {
            chatIds.put(currentId, new User());
            SendMessage newMemberMessage = new SendMessage();
            newMemberMessage.setText("New member has joined! current community size: " + chatIds.size());
            for (Long chatId : chatIds.keySet()) {
                if (chatId.equals(currentId)) {
                    continue;
                }
                newMemberMessage.setChatId(chatId);
                try {
                    execute(newMemberMessage);
                } catch (TelegramApiException ignored) {
                    //ignore
                }
            }
            message.setText("Type /help to see this message again.\n" + helpMessage());
        }
        else if (update.getMessage().getText().equals("/help")){
            message.setText(helpMessage());
        }
        else if (update.getMessage().getText().startsWith("/create")) {
            if (chatIds.size() < 3) {
                message.setText("Community is too small to add a poll!");
            }
            else if (chatIds.get(currentId).getCurrentDraft() != null){
                message.setText("In order to create a new draft, you must first finish/delete your previous one!");
            }
            else {
                if (isCommandUsageValid(currentId,message)){
                    message.setText("Creating a new poll draft!");
                    String title = message.getText().substring(8);
                    chatIds.get(currentId).setCurrentDraft(new Poll(title));
                }
            }
        }
        else if (update.getMessage().getText().equals("/delete")) {
            if (isCommandUsageValid(currentId,message)){
                message.setText("Deleting poll!");
                chatIds.get(currentId).setCurrentDraft(null);
            }
        }
        else if (update.getMessage().getText().startsWith("/send")) {
            if (isCommandUsageValid(currentId,message)){
                if (!chatIds.get(currentId).isDraftValid()){
                    message.setText("Current draft is invalid!");
                    return;
                }
                int waitTime;
                try {
                    waitTime = Integer.parseInt(message.getText().split(" ")[1]);
                } catch (NumberFormatException e) {
                    message.setText("Please enter a valid number!");
                    return;
                }
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime reserve = now.plusMinutes(waitTime);
                for (Poll key: timesReserved.keySet()){
                    if (Duration.between(timesReserved.get(key),reserve).toMinutes() <= 5){
                        message.setText("Can't reserve poll for this time, too close to a different poll!");
                        return;
                    }
                }
                timesReserved.put(chatIds.get(currentId).getCurrentDraft(),reserve);
                int finalWaitTime = waitTime;
                try {
                    Thread.sleep(finalWaitTime * 60000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                message.setText("Sending poll!");
                poll = chatIds.get(currentId).getCurrentDraft();
                for (Long chatId : chatIds.keySet()) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("New poll! " + poll.getTitle());
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException ignored) {
                        //ignore
                    }
                }
                chatIds.get(currentId).setCurrentDraft(null);
                int seconds = 0;
                try {
                    while (seconds < 300) {
                        Thread.sleep(1000);
                        seconds++;
                        if (poll.getQuestion(poll.questionCount()).answerCount() == chatIds.size()){
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    //ignore
                }
                timesReserved.remove(poll);
                SendMessage statistics = new SendMessage();
                statistics.setChatId(currentId);
                statistics.setText(poll.statistics());
                this.poll = null;
                try {
                    execute(statistics);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else if (update.getMessage().getText().startsWith("/addQuestion")){
            if (isCommandUsageValid(currentId,message)){
                String text = message.getText().substring(13);
                chatIds.get(currentId).getCurrentDraft().addQuestion(new Question(text));
            }
        }
        else if (update.getMessage().getText().startsWith("/removeQuestion")){
            if (isCommandUsageValid(currentId,message)){
                String text = message.getText().substring(13);
                try {
                    int questionNum = Integer.parseInt(text);
                    chatIds.get(currentId).getCurrentDraft().removeQuestion(questionNum);
                }catch (Exception e){
                    message.setText("Question not found!");
                }
            }
        }
        else if (update.getMessage().getText().startsWith("/addOption")){
            if (isCommandUsageValid(currentId,message)){
                String[] text = message.getText().split(" ");
                if (text.length < 3){
                    message.setText("Invalid command usage!");
                    return;
                }
                try {
                    int questionNum = Integer.parseInt(text[1]);
                    String option = text[2];
                    chatIds.get(currentId).getCurrentDraft().getQuestion(questionNum).addOption(option);
                } catch (Exception e){
                    message.setText("Question not found!");
                }
            }
        }
        else if (update.getMessage().getText().startsWith("/removeOption")){
            if (isCommandUsageValid(currentId,message)){
                String[] text = message.getText().split(" ");
                if (text.length < 3){
                    message.setText("Invalid command usage!");
                    return;
                }
                try {
                    int questionNum = Integer.parseInt(text[1]);
                    int option = Integer.parseInt(text[2]);
                    chatIds.get(currentId).getCurrentDraft().getQuestion(questionNum).removeOption(option);
                } catch (Exception e){
                    message.setText("Question/Option not found!");
                }
            }
        }
        else if (update.getMessage().getText().equals("/show")){
            if (isCommandUsageValid(currentId,message)){
                if (poll == null){
                    message.setText("There is no active poll!");
                }
                message.setText(poll + "\nYou need to answer question number " + (chatIds.get(currentId).getCurrentQuestion() + 1));
            }
        }
        else if (update.getMessage().getText().contains("/answer")) {
            if (isCommandUsageValid(currentId,message)){
                try {
                    String answer = update.getMessage().getText().split(" ")[1];
                    int number = Integer.parseInt(answer);
                    if (number > poll.getQuestion(chatIds.get(currentId).getCurrentQuestion()).optionCount() ||
                        number <= 0) {
                        message.setText("Invalid answer number");
                    }
                    else {
                        chatIds.get(currentId).answerQuestion(poll,number);
                    }
                }
                catch (Exception e) {
                    message.setText("Invalid command format!");
                }
            }
        }

        try {
            execute(message);
        } catch (TelegramApiException ignored) {
            //ignore
        }
    }
    public String helpMessage(){
        return """
                    If there is an active poll, type /answer i, with i being the answer you wish to choose for the question you're currently answering.
                    The poll will be answered from first question to last.
                    Type /show for the bot to re-send the current poll.
                    
                    In order to create a draft for a poll, type /create (pollTitle). Note that you cannot create a new draft if you already have an unsent draft.
                    In order to add a question, type /addQuestion (question text).
                    In order to remove a question, type /removeQuestion (questionNumber).
                    In order to add options to a question, type /addOption (questionNumber) (optionText).
                    In order to remove an option from a question, type /removeOption (questionNumber) (optionNumber).
                    In order to send your latest poll draft, type /send (Number of minutes to wait before sending poll). A poll will only be sent if there is no active poll.
                    In order to delete your poll draft, type /delete.
                    Note that /send will only work if you have between 1-3 questions in your draft and between 2-4 options for each question.
                    """;
    }
    public boolean isCommandUsageValid(Long currentId, SendMessage message) {
        if (!chatIds.containsKey(currentId)) {
            message.setText("You are not part of the community!");
            return false;
        }
        else if (poll == null) {
            message.setText("There is no poll!");
            return false;
        }
        return true;
    }
    public String getBotToken(){
        return "6725312308:AAFRgNt5nmbElOX9AjnmNQwfgkMxs5Paamw";
    }
    @Override
    public String getBotUsername() {
        return "survey144_bot";
    }
}
