package S13P11A708.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class BackendApplicationTests {

	@Container
	static final GenericContainer<?> REDIS =
			new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry r) {
		r.add("spring.data.redis.host", REDIS::getHost);
		r.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
	}

	@Autowired
	RedisTemplate<String, ?> redisTemplate;

	@Test
	void redis_set_get_ok() {
		var ops = redisTemplate.opsForValue();
		String key = "test:string";
		String expected = "helloWorld!";

//		ops.set(key, expected);
		Object raw = ops.get(key);

		// 역직렬화된 타입은 Object라 캐스팅 필요
		String actual = (raw instanceof String) ? (String) raw : String.valueOf(raw);

		Assertions.assertEquals(expected, actual);
		redisTemplate.delete(key);
	}

}
