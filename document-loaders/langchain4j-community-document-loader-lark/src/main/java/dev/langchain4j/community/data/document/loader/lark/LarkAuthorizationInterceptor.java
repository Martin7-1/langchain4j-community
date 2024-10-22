package dev.langchain4j.community.data.document.loader.lark;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

class LarkAuthorizationInterceptor implements Interceptor {

    private final String accessToken;

    LarkAuthorizationInterceptor(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        return chain.proceed(request);
    }
}
