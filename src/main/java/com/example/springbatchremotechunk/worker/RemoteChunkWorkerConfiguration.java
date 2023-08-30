package com.example.springbatchremotechunk.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile("worker")
public class RemoteChunkWorkerConfiguration
{
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private RemoteChunkingManagerStepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Value("${worker.time:0}")
    private Long time;

    /*
     * Configure inbound flow (requests coming from the manager)
     */
    @Bean
    public DirectChannel requests()
    {
        return new DirectChannel();
    }

    /**
     * 创建从MQ接收数据放入管道
     */
    @Bean
    public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory)
    {
        return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("requests"))
                .channel(requests())
                .get();
    }

    /*
     * Configure outbound flow (replies going to the manager)
     */
    @Bean
    public DirectChannel replies()
    {
        return new DirectChannel();
    }

    /**
     * 创建从管道接收数据后发送回复消息
     */
    @Bean
    public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory)
    {
        return IntegrationFlows.from(replies()).handle(Jms.outboundAdapter(connectionFactory).destination("replies")).get();
    }

    /*
     * Configure the ChunkProcessorChunkHandler
     * 创建数据流中间的一个处理器，接收requests管道数据，结束后通知replies管道
     */
    @Bean
    @ServiceActivator(inputChannel = "requests", outputChannel = "replies")
    public ChunkProcessorChunkHandler<Integer> chunkProcessorChunkHandler()
    {
        ChunkProcessor<Integer> chunkProcessor = new SimpleChunkProcessor<>(itemProcessor(), itemWriter());
        ChunkProcessorChunkHandler<Integer> chunkProcessorChunkHandler = new ChunkProcessorChunkHandler<>();
        chunkProcessorChunkHandler.setChunkProcessor(chunkProcessor);
        return chunkProcessorChunkHandler;
    }

    private ItemProcessor<Integer, Customer> itemProcessor()
    {
        return id ->
        {
            Thread.sleep(time);
            log.info("process data id:{} sleep:{}ms", id, time);
            return new Customer(id);
        };
    }

    private ItemWriter<Customer> itemWriter()
    {
        return items ->
        {
            log.info("http -> {}", items.stream().findFirst());
        };
    }
}
