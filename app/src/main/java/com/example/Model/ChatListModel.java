package com.example.Model;

public class ChatListModel {

    //chat POJO includes the data of a chat (sender,receiver,message etc).
    String chatListID, date, lastMessage, member;

    public ChatListModel() {
    }


    public ChatListModel(String chatListID, String data, String lastMessage, String member) {
        this.chatListID = chatListID;
        this.date = data;
        this.lastMessage = lastMessage;
        this.member = member;
    }


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