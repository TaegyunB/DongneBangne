package S13P11A708.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // 스프링 실행 시 설정파일 읽어드리기 위한 어노테이션
public class SwaggerConfig {

    private final String jwtSchemeName = "bearerAuth";

    /**
     * Swagger UI의 메인 설정을 담당하는 메서드
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(createApiInfo())  // API 기본 정보 설정
                .addSecurityItem(createSecurityRequirement())  // JWT 보안 요구사항 추가
                .components(createComponents());  // 보안 스키마 컴포넌트 등록
    }

    /**
     * API 전체에 적용될 보안 요구사항을 정의
     */
    public SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement()
                .addList(jwtSchemeName); // bearerAuth라는 이름의 보안 스키마를 요구사항 목록에 추가
                                         // 이를 통해 모든 API 엔드포인트에 JWT 인증이 필요하다고 표시됨
    }

    /**
     * Swagger UI 상단에 표시될 API 문서의 기본 정보를 설정
     */
    public Info createApiInfo() {
        return new Info()
                .title("DongneBangne API")
                .description("DongneBangne API 문서입니다.")
                .version("1.0.0");
    }

    /**
     * JWT 인증 방식에 대한 구체적인 보안 스키마를 정의
     */
    public Components createComponents() {
        return new Components().addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                .name(jwtSchemeName)  // "bearerAuth"라는 이름으로 실제 JWT Bearer 토큰 인증 방식을 정의
                .type(SecurityScheme.Type.HTTP)  // HTTP 인증 방식
                .scheme("bearer")  // Bearer 토큰 사용
                .bearerFormat("JWT")  // JWT 형식
                .in(SecurityScheme.In.HEADER)  // HEADER에 포함
                .name("Authorization"));  // Authorization 헤더 사용
    }
}
