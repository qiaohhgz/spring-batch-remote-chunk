package com.example.springbatchremotechunk;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AmqpConfiguration
{
    @Bean
    public ActiveMQConnectionFactory connectionFactory()
    {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setTrustAllPackages(true);
        factory.setBrokerURL("tcp://localhost:61616");
        Properties props = new Properties();
        // 设置相对较高的预取值会导致更高的性能。
        // 因此，主题的默认值通常大于 1000 且要高得多，对于非持久性消息，默认值则更高。
        // 预取大小决定了客户端上的 RAM 中将保留多少条消息，因此如果您的 RAM 有限，您可能需要设置一个较低的值，例如 1 或 10 等。
        props.put("prefetchPolicy.queuePrefetch", "1");
        factory.setProperties(props);
        return factory;
    }
}
