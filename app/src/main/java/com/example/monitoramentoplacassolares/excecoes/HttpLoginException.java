package com.example.monitoramentoplacassolares.excecoes;

public class HttpLoginException extends Exception{

    public HttpLoginException(String message) {
        super(message);
    }
    public HttpLoginException(String message, Throwable cause) {
        super(message, cause);
    }

}
