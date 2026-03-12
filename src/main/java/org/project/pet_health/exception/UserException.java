package org.project.pet_health.exception;


import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

    private Integer code;
    public UserException(final Integer code, final String message) {
        super(message);
        this.code = code;
    }

    public UserException(final String message) {
        super(message);
        this.code = 500;
    }

}
