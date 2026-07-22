package com.mgt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AsyncConfig — @Async এর জন্য proper thread pool।
 * Default SimpleAsyncTaskExecutor প্রতিবার নতুন thread তৈরি করে।
 * এটা ThreadPoolTaskExecutor ব্যবহার করে যা thread reuse করে।
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);         // সবসময় ৪টা thread active
        executor.setMaxPoolSize(10);         // সর্বোচ্চ ১০টা thread
        executor.setQueueCapacity(100);      // ১০০টা email queue হতে পারবে
        executor.setThreadNamePrefix("EmailAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
