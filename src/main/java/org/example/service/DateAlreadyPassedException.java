package org.example.service;

public class DateAlreadyPassedException extends Exception {

    public DateAlreadyPassedException(String message) {
        super(message);
    }

}