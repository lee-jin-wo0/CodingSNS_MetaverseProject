package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Judge0Client {
    private static final String RAPIDAPI_HOST = "judge0-ce.p.rapidapi.com";
    private static final String API_KEY = "2060b58485mshe42d21440c14d9ap153565jsn2ccd2e28e064"; // RapidAPI에서 받은 API 키
    private static final String SUBMISSION_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=false";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String compile(String sourceCode) throws Exception {
        // 요청 페이로드 구성
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("language_id", "62"); // Java 언어 ID
        requestPayload.put("source_code", Base64.getEncoder().encodeToString(sourceCode.getBytes()));
        requestPayload.put("stdin", "");

        // JSON으로 변환
        String requestBody = objectMapper.writeValueAsString(requestPayload);

        // API 요청 빌드 및 실행
        try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
            RequestBuilder requestBuilder = new RequestBuilder("POST")
                .setUrl(SUBMISSION_URL)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", RAPIDAPI_HOST)
                .addHeader("Content-Type", "application/json")
                .setBody(requestBody);

            // 요청 실행 및 결과 처리
            CompletableFuture<Response> futureResponse = client.executeRequest(requestBuilder.build()).toCompletableFuture();
            Response response = futureResponse.join();
            return response.getResponseBody(); // 결과 반환
        }
    }
}
