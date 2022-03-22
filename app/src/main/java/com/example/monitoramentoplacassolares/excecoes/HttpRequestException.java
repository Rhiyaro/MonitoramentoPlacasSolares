package com.example.monitoramentoplacassolares.excecoes;

public class HttpRequestException extends Exception {

    public HttpRequestException(String message) {
        super(message);
    }

    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
