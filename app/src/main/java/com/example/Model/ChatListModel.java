package com.example.Model;

public class ChatListModel {

    // Chat list POJO includes the data of a chat (Chat's id, last message and its date and to whom).
    String chatListID, date, lastMessage, member;

    public ChatListModel() { //TODO: delete (?)
    }

    public ChatListModel(String chatListID, String date, String lastMessage, String member) {
        this.chatListID = chatListID;
        this.date = date;
        this.lastMessage = lastMessage;
        this.member = member;
    }
    //TODO: delete (?)
    public String getChatListID() {
        return chatListID;
    }

    public void setChatListID(String chatListID) {
        this.chatListID = chatListID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String data) {
        this.date = data;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

}