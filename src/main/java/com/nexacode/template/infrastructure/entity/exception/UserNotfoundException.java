package com.nexacode.template.infrastructure.entity.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserNotfoundException extends EntityNotFoundException {
    public UserNotfoundException() {
        super("존재하지 않는 회원입니다.");
    }
}