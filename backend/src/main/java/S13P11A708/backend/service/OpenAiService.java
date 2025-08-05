package S13P11A708.backend.service;

import S13P11A708.backend.domain.Challenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper =  new ObjectMapper();
    private final RestTemplate restTemplate;


    /**
     * 개별 챌린지 기반 신문 기사 생성
     */
    public String generateChallengeArticle(Challenge challenge) {

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("다음 경로당 어르신의 활동을 바탕으로 신문 기사를 작성해주세요.\n\n");

        promptBuilder.append("활동 정보:\n");
        promptBuilder.append("- 활동명: ").append(challenge.getChallengeTitle()).append("\n");
        promptBuilder.append("- 장소: ").append(challenge.getChallengePlace()).append("\n");
        promptBuilder.append("- 설명: ").append(challenge.getDescription()).append("\n");
        promptBuilder.append("- 미션 진행 사진: ").append(challenge.getChallengeImage()).append("\n");
        promptBuilder.append("- 미션 진행 사진 설명: ").append(challenge.getImageDescription()).append("\n");

        promptBuilder.append("\n신문 작성 요구사항:\n");
        promptBuilder.append("1. 어르신들이 읽기 쉬운 큰 글씨체를 고려한 적절한 길이\n");
        promptBuilder.append("2. 따뜻하고 긍정적인 톤앤매너\n");
        promptBuilder.append("3. 각 미션의 의미와 가치를 부각\n");
        promptBuilder.append("4. 신문 기사 형식이긴 하지만 본문으로만 구성\n");
        promptBuilder.append("5. HTML 태그는 사용하지 말고 순수 텍스트로 작성\n");

        return callOpenAiApi(promptBuilder.toString());
    }

    /**
     * 신문 제목 생성
     */
    public String generateNewsTitle(String seniorCenterName, Integer year, Integer month) {
        String prompt = String.format(
                "%s에서 %d년 %d월에 진행한 활동들을 담은 신문의 제목을 생성해주세요. " +
                        "어르신들이 좋아할만한 따뜻하고 친근한 제목으로 만들어주세요. " +
                        "제목만 생성해주시고, 다른 설명은 필요하지 않습니다.",
                seniorCenterName, year, month
        );

        return callOpenAiApi(prompt);
    }

    /**
     * OpenAI API 호출
     */
    private String callOpenAiApi(String prompt) {
        try {
            String fullUrl = apiUrl + "/chat/completions";
            log.info("OpenAI API 요청 URL: {}", fullUrl);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);  // 창의성 수준 (0-1)

            // 대화 형식으로 메시지 구성
            List<Map<String, String>> messages = Arrays.asList(
                    Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            // JSON 바디 수동 직렬화
            String json = objectMapper.writeValueAsString(requestBody);
            log.info("전송할 JSON: {}", json);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            // API 호출 및 응답 파싱
            ResponseEntity<Map> response = restTemplate.postForEntity(fullUrl, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("AI 콘텐츠 생성에 실패했습니다.");
        }
    }
}