package com.example.workload.exception;

public class TrainerNotFoundException extends RuntimeException {

    public TrainerNotFoundException(String username) {
        super("Trainer not found with username: " + username);
    }
}

