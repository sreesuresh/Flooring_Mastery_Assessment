package org.example.service;

public class NoSuchItemException extends Exception {

    public NoSuchItemException(String message) {
        super(message);
    }

}