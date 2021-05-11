package com.example.vocabbuilder;

public class WordDetails {
    private String word;
    private String defination;
    private String examples;
    private String displayDate;

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefination() {
        return defination;
    }

    public void setDefination(String defination) {
        this.defination = defination;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public WordDetails() {}

    public WordDetails(String word, String defination, String examples, String displayDate) {
        this.word = word;
        this.defination = defination;
        this.examples = examples;
        this.displayDate = displayDate;
    }
}
