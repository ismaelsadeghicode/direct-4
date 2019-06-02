package com.dml.topup.service;

import com.dml.topup.config.Constants;
import com.dml.topup.dto.response.topup.Response;
import com.dml.topup.exception.ErrorCode;
import com.dml.topup.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * @author i.sadeghi
 */
@PropertySource("classpath:topup.properties")
public abstract class Service<T> {

    @Autowired
    private WebClient webClient;

    @Autowired
    private Environment env;
    private Class<T> result;

    public Service(final Class<T> result) {
        this.result = result;
    }

    Environment getEnv() {
        return env;
    }

    public Mono<T> callWebService(HttpMethod httpMethod, String url, Object body) {
        Mono<T> response = null;

        if (HttpMethod.GET.equals(httpMethod)) {
            response = webClient.get()
                    .uri(url)
                    .exchange()
                    .flatMap(clientResponse -> clientResponse.bodyToMono(result));
        } else if (HttpMethod.POST.equals(httpMethod)) {
            response = webClient.post()
                    .uri(url)
                    .body(Mono.just(body), Object.class)
                    .header(Constants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(result);
        }
        return response;
    }

    // Todo implementation spring AOP
    protected Response cheackAuthentication(String authorization) {
        Response response = new Response();
        if (!String.format(Constants.AUTHENTICATION_BASIC, SecurityUtil.userAuthorization).equals(String.format(Constants.DATA_HEADER_VALUE, authorization))) {
            response.setErrorCode(ErrorCode.AUTHORIZATION_EXCEPTION.getCode());
            response.setErrorDescription(ErrorCode.AUTHORIZATION_EXCEPTION.getDescription());
        }
        return response;
    }

    public String createUrl(String key) {
        return String.format("%s%s", getEnv().getProperty(Constants.SETAREYEK_URL), getEnv().getProperty(key));
    }

    // Todo delete when applied from the client side
    public int generateResNo() {
        int m = (int) Math.pow(10, 6 - 1);
        return m + new Random().nextInt(9 * m);
    }

    public int firstDigit(int n) {
        while (n >= 10)
            n /= 10;
        return n;
    }
}