package com.example.monitoramentoplacassolares.httpcomm;

import android.content.Context;

import com.example.monitoramentoplacassolares.excecoes.HttpLoginException;
import com.example.monitoramentoplacassolares.excecoes.HttpRequestException;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MpsHttpClient {
    public static final String TAG = "MpsHttpClient";

    public static final String PREFIXO_GERAL = "mps";

    public static final int HTTP_OK_RESPONSE = 200;
    public static final int HTTP_FORBIDDEN_RESPONSE = 403;
    public static final int HTTP_NOT_FOUND_RESPONSE = 404;
    public static final int HTTP_NOT_IMPLEMENTED_RESPONSE = 501;

    public static final int REQUEST_TIMEOUT_SECONDS = 3;

    private static MpsHttpClient Instancia;

    private static OkHttpClient okHttpCliente;
    private final String scheme = "http";
    private String hostname;
    private int port = 8080;

    private static List<String> strCookies;
    private static List<Cookie> cookies;
    private static ClearableCookieJar cookieJar;

    public MpsHttpClient(Context context) {
        cookieJar = new PersistentCookieJar(new SetCookieCache(),
                new SharedPrefsCookiePersistor(context.getApplicationContext()));


        okHttpCliente = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        strCookies = Collections.synchronizedList(new ArrayList<>());
        cookies = Collections.synchronizedList(new ArrayList<>());
    }

    public MpsHttpClient(String hostname, int port, Context context) {
        this(context);
        this.hostname = hostname;
        this.port = port;
    }

    public static void criaInstancia(MpsHttpClient cliente) {
        Instancia = cliente;
    }

    public static MpsHttpClient instacia() {
        return Instancia;
    }

    public Request newGetRequest(String path) {

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme(scheme)
                .host(hostname)
                .port(port)
                .addPathSegment(PREFIXO_GERAL)
                .addPathSegment(path);

        HttpUrl url = urlBuilder.build();

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.get();

        for (String cookie : strCookies) {
            requestBuilder.addHeader("Cookie", cookie);
        }

        // FIXME: Added for compatibility
        requestBuilder.addHeader("BearerToken", MpsHttpServerInfo.bearerToken);

        return requestBuilder.build();
    }

    public Request newGetRequest(String path, Map<String, String> queryParams) {

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme(scheme)
                .host(hostname)
                .port(port)
                .addPathSegment(PREFIXO_GERAL)
                .addPathSegment(path);

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        HttpUrl url = urlBuilder.build();

        cookieJar.loadForRequest(url);
        cookieJar.saveFromResponse(url, cookies);

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.get();

        for (String cookie : strCookies) {
            requestBuilder.addHeader("Cookie", cookie);
        }

        // FIXME: Added for compatibility
        requestBuilder.addHeader("BearerToken", MpsHttpServerInfo.bearerToken);

        return requestBuilder.build();
    }

    public Response sendGetRequest(Request request) throws InterruptedException, ExecutionException, TimeoutException {
        CallbackFuture callbackFuture = new CallbackFuture();
        okHttpCliente.newCall(request).enqueue(callbackFuture);
        return callbackFuture.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public Response sendGetRequest(String path) throws InterruptedException, ExecutionException, TimeoutException {
        return sendGetRequest(newGetRequest(path));
    }

    public Response sendGetRequest(String path, Map<String, String> queryParams) throws InterruptedException, ExecutionException, TimeoutException {
        return sendGetRequest(newGetRequest(path, queryParams));
    }

    public String logarCliente(String login, String senha) throws HttpLoginException {
        Map<String, String> queryParams = new HashMap<>(2);
        queryParams.put("login", login);
        queryParams.put("senha", senha);

        try (Response loginResponse = sendGetRequest(newGetRequest(MpsHttpServerInfo.PATH_LOGIN, queryParams))) {
            return Objects.requireNonNull(loginResponse.body()).string();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new HttpLoginException("Erro de Login: Erro de Execução", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new HttpLoginException("Erro de Login: Tarefa Interrompida", e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new HttpLoginException("Erro de Login: Tempo Esgotado", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new HttpLoginException("Erro de Login: Resposta Inesperada", e);
        }
    }

    public Response doGet(Request request) throws HttpRequestException {
        try {
            return sendGetRequest(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new HttpRequestException("Falha de Requisição: Interrupção", e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new HttpRequestException("Falha de Requisição: Erro de Execução", e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new HttpRequestException("Falha de Requisição: Tempo Esgotado", e);
        }
    }

    public Response doGet(String path) throws HttpRequestException {
        return doGet(instacia().newGetRequest(path));
    }

    public Response doGet(String path, Map<String, String> queryParams) throws HttpRequestException {
        return doGet(instacia().newGetRequest(path, queryParams));
    }

    private static class CallbackFuture extends CompletableFuture<Response> implements Callback {
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            super.complete(response);
        }

        public void onFailure(@NotNull Call call,@NotNull IOException e) {
            super.completeExceptionally(e);
        }
    }
}
