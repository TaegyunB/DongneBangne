package S13P11A708.backend.config;

import S13P11A708.backend.domain.game.GameStatusRedis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "gameStatusRedisTemplate")
    public RedisTemplate<String, GameStatusRedis> gameStatusRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, GameStatusRedis> tpl = new RedisTemplate<>();
        var keySer = new StringRedisSerializer();
        var valSer = new GenericJackson2JsonRedisSerializer(); // 저장/조회 동일 직렬화기
        tpl.setConnectionFactory(cf);
        tpl.setKeySerializer(keySer);
        tpl.setValueSerializer(valSer);
        tpl.setHashKeySerializer(keySer);
        tpl.setHashValueSerializer(valSer);
        tpl.afterPropertiesSet();
        return tpl;
    }

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//
//        //직렬화
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // JSON 직렬화
//
//        return template;
//    }
}
