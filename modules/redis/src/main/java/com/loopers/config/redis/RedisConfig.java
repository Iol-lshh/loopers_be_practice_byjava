package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private static final String CONNECTION_MASTER = "redisConnectionMaster";
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Primary
    @Bean
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();

        return lettuceConnectionFactory(
                database, master, replicas,
                b -> b.readFrom(ReadFrom.REPLICA_PREFERRED)
        );
    }

    @Bean
    @Qualifier(CONNECTION_MASTER)
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();

        return lettuceConnectionFactory(
                database, master, replicas,
                b -> b.readFrom(ReadFrom.MASTER)
        );
    }

    @Primary
    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        return defaultRedisTemplate(template, lettuceConnectionFactory);
    }

    @Bean
    @Qualifier(REDIS_TEMPLATE_MASTER)
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        return defaultRedisTemplate(template, lettuceConnectionFactory);
    }

    private LettuceConnectionFactory lettuceConnectionFactory(
            int database,
            RedisNodeInfo master,
            List<RedisNodeInfo> replicas,
            Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer
    ) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
        if (customizer != null) {
            customizer.accept(builder);
        }
        LettuceClientConfiguration clientConfig = builder.build();

        RedisStaticMasterReplicaConfiguration masterReplica =
                new RedisStaticMasterReplicaConfiguration(master.getHost(), master.getPort());
        masterReplica.setDatabase(database);
        if (replicas != null) {
            for (RedisNodeInfo r : replicas) {
                masterReplica.addNode(r.getHost(), r.getPort());
            }
        }
        return new LettuceConnectionFactory(masterReplica, clientConfig);
    }

    private <K, V> RedisTemplate<K, V> defaultRedisTemplate(
            RedisTemplate<K, V> template,
            LettuceConnectionFactory connectionFactory
    ) {
        StringRedisSerializer string = new StringRedisSerializer();
        template.setKeySerializer(string);
        template.setValueSerializer(string);
        template.setHashKeySerializer(string);
        template.setHashValueSerializer(string);
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
