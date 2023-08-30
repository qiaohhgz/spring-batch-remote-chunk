package com.example.springbatchremotechunk.manager;

import com.example.springbatchremotechunk.worker.Customer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("manager")
public class RemoteChunkManagerConfiguration
{
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private RemoteChunkingManagerStepBuilderFactory stepBuilderFactory;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Bean
    public Job chunkJob(JobRepository jobRepository)
    {
        return new JobBuilder("chunkJob")
                .start(stepBuilderFactory.<Integer, Customer>get("step1")
                        // 每个消息存放的数据量
                        .chunk(20)
                        // 数据读取
                        .reader(itemReader())
                        // 输出到的管道（消息队列）
                        .outputChannel(requests())
                        // 接收处理完成的回复（消息队列）
                        .inputChannel(replies())
                        .build())
                .repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public ItemReader<Integer> itemReader()
    {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 10000; i++)
        {
            data.add(i);
        }
        return new ListItemReader<>(data);
    }

    @Bean
    public DirectChannel requests()
    {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow outboundFlow(ActiveMQConnectionFactory connectionFactory)
    {
        return IntegrationFlows
                .from(requests())
                .log()
                .handle(Jms.outboundAdapter(connectionFactory).destination("requests"))
                .get();
    }

    /*
     * Configure inbound flow (replies coming from workers)
     */
    @Bean
    public QueueChannel replies()
    {
        return new QueueChannel();
    }

    @Bean
    public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory)
    {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("replies"))
                .channel(replies())
                .get();
    }
}
