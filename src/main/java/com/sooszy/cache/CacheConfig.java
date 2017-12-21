package com.sooszy.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooszy.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;

/**
 *  缓存模块相关bean注册类
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
    private static JedisPool pool;//jedis连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20")); //最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","20"));//在jedispool中最大的idle状态(空闲的)的jedis实例的个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","20"));//在jedispool中最小的idle状态(空闲的)的jedis实例的个数
    private static Long maxWaitMillis = Long.parseLong(PropertiesUtil.getProperty("redis.maxWaitMillis","-1"));//最大建立连接等待时间
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));//在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true。则得到的jedis实例肯定是可以用的。
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","true"));//在return一个jedis实例的时候，是否要进行验证操作，如果赋值true。则放回jedispool的jedis实例肯定是可以用的。
    private static Boolean testWhileIdle = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.whileIdle","false"));
    private static String hostName = PropertiesUtil.getProperty("redis.ip");
    private static Integer port = Integer.parseInt(PropertiesUtil.getProperty("redis.port","6379"));
    private static String password = PropertiesUtil.getProperty("redis.password");
    private static Integer timeout = Integer.parseInt(PropertiesUtil.getProperty("redis.timeout","2000"));
    private static Long defaultExpiration = Long.parseLong(PropertiesUtil.getProperty("redis.defaultExpiration","3600"));


   /* @Value("${redis.pool.maxTotal:10}")
    private int maxTotal; // 最大连接数
    @Value("${redis.pool.maxIdle:10}")
    private int maxIdle; // 最大空闲连接数
    @Value("${redis.pool.minIdle:0}")
    private int minIdle; // 最小空闲连接数
    @Value("${redis.pool.maxWaitMillis:-1}")
    private long maxWaitMillis; // 最大建立连接等待时间
    @Value("${redis.pool.testOnBorrow:false}")
    private boolean testOnBorrow; // 获取连接时检查有效性
    @Value("${redis.pool.testWhileIdle:false}")
    private boolean testWhileIdle; // 空闲时检查有效性

    @Value("${redis.hostName:127.0.0.1}")
    private String hostName; // 主机名
    @Value("${redis.port:6379}")
    private int port; // 监听端口
    @Value("${redis.password:}")
    private String password; // 密码
    @Value("${redis.timeout:2000}")
    private int timeout; // 客户端连接时的超时时间（单位为秒）

    @Value("${redis.cache.defaultExpiration:3600}")
    private long defaultExpiration; // 缓存时间，单位为秒（默认为0，表示永不过期）*/


    /**
     * 构造jedis连接池配置对象
     *
     * @return
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestWhileIdle(testWhileIdle);
        return config;
    }

    @Bean
    public JedisPool jedisPool() {
        JedisPool pool = new JedisPool(jedisPoolConfig(), hostName, port, timeout);
        return pool;
    }

    /**
     * 构造jedis连接工厂
     *
     * @return
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory(jedisPoolConfig());
        factory.setHostName(hostName);
        factory.setPort(port);
        //factory.setPassword(password);
        factory.setTimeout(timeout);
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * 注入redis template
     *
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new JdkSerializationRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 注入redis cache manager
     *
     * @return
     */
    @Bean
    @Primary
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
        cacheManager.setDefaultExpiration(defaultExpiration);
        return cacheManager;
    }

    @Bean
    public RedisCacheTemplate redisCacheTemplate() {
        return new RedisCacheTemplate();
    }



    //第二种 继承了 CachingConfigurerSupport 的 cache策略

    /**
     * 配置缓存键值的生成器 (因为Redis是键值对存储的数据库, 所以要配置一个键值缓存的生成器)
     *
     * @return
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            // 配置生成器
            @Override
            public Object generate(Object target, Method method, Object... objects) {
                StringBuilder sb = new StringBuilder();
                // 规则1: 拼接class名称
                sb.append(target.getClass().getName());
                // 规则2: 拼接方法名称
                sb.append(method.getName());
                for (Object obj : objects) {
                    // 规则3: 拼接参数值
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

    /**
     * 配置缓存时间, 单位秒
     * @param redisTemplate
     * @return
     */
    @Bean
    public CacheManager cacheManager(@Autowired @Qualifier("stringRedisTemplate") StringRedisTemplate redisTemplate) {
        RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
        // 设定缓存过期时间, 单位秒
        rcm.setDefaultExpiration(60);
        return rcm;
    }

    /**
     * @param factory
     * @return
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(@Autowired @Qualifier("jedisConnectionFactory") RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        /**
         * 配置一个序列器, 将对象序列化为字符串存储, 和将对象反序列化为对象
         */
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}