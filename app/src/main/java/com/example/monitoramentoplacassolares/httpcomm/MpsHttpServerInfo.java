package com.example.monitoramentoplacassolares.httpcomm;

public class MpsHttpServerInfo {
    public static final String PREFIXO_GERAL = "/mps/";
    public static final String PATH_SAUDACAO = "hello";
    public static final String PATH_ECHO = "echo";

    public static final String PATH_LOGIN = "api/auth/v0/login";
    public static final String PATH_LOCAIS = "api/park/v0/get-parks";
    public static final String PATH_ULT_DADOS = "api/measure/v0/last-data";
    public static final String PATH_DADOS_DATA = "api/measure/v0/date-data";
    public static final String PATH_NOTIFICACOES = "notificacoes";

    // FIXME: Added for compatibility
    public static String bearerToken = "";
}
