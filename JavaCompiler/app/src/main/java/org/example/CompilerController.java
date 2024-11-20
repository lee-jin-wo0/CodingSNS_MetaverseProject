package org.example;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api") // URL Prefix
public class CompilerController {
    private static final String RAPIDAPI_HOST = "judge0-ce.p.rapidapi.com";
    private static final String API_KEY = "2060b58485mshe42d21440c14d9ap153565jsn2ccd2e28e064"; // 실제 API 키로 바꿔주세요
    private static final String SUBMISSION_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=false";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/compile")
    public Map<String, String> compileCode(@RequestBody Map<String, String> requestBody) throws Exception {
        // 요청 페이로드 구성
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("language_id", "62"); // Java 언어 ID
        requestPayload.put("source_code", Base64.getEncoder().encodeToString(requestBody.get("sourceCode").getBytes()));
        requestPayload.put("stdin", "");

        // JSON으로 변환
        String requestBodyJson = objectMapper.writeValueAsString(requestPayload);

        // API 요청 빌드 및 실행
        try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
            RequestBuilder requestBuilder = new RequestBuilder("POST")
                .setUrl(SUBMISSION_URL)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", RAPIDAPI_HOST)
                .addHeader("Content-Type", "application/json")
                .setBody(requestBodyJson);

            // 요청 실행 및 결과 처리
            CompletableFuture<Response> futureResponse = client.executeRequest(requestBuilder.build()).toCompletableFuture();
            Response response = futureResponse.join();

            // JSON 응답에서 토큰 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getResponseBody(), Map.class);
            String token = responseBody.get("token").toString(); // 주의: String으로 바로 캐스팅 가능

            // 결과를 가져오는 메서드 호출
            Map<String, String> result = new HashMap<>();
            result.put("token", token);
            return result; // 토큰 반환
        }
    }

    @GetMapping("/result/{token}") // 결과를 가져오는 엔드포인트
    public Map<String, Object> getExecutionResult(@PathVariable String token) throws Exception {
        String resultUrl = "https://judge0-ce.p.rapidapi.com/submissions/" + token + "?base64_encoded=true";

        try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
            RequestBuilder requestBuilder = new RequestBuilder("GET")
                .setUrl(resultUrl)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", RAPIDAPI_HOST);

            CompletableFuture<Response> futureResponse = client.executeRequest(requestBuilder.build()).toCompletableFuture();
            Response response = futureResponse.join();

            // JSON 파싱하여 stdout 가져오기
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.getResponseBody(), Map.class);
            String base64Output = (String) responseBody.get("stdout");

            // Base64 디코딩
            String output = new String(Base64.getDecoder().decode(base64Output), "UTF-8");

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("stdout", output); // 표준 출력 결과 저장
            return resultMap; // 결과 반환
        }
    }
}
