package S13P11A708.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@SpringBootApplication
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {

//		// 로컬 개발 시 .env 파일 자동 로드
//		Dotenv dotenv = Dotenv.configure()
//				.filename(".env")
//				.ignoreIfMissing()
//				.load();
//
//		// 시스테 프로퍼티로 설정
//		dotenv.entries().forEach(entry ->
//				System.setProperty(entry.getKey(), entry.getValue()));
//

		SpringApplication.run(BackendApplication.class, args);
	}

}
