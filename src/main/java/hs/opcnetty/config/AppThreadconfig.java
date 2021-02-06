package hs.opcnetty.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/10/8 11:28
 */
@Configuration
public class AppThreadconfig {
    @Bean
    public ExecutorService threadpool(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("opc-netty-pool-%d").setDaemon(true).build();

        ExecutorService pool = new ThreadPoolExecutor(10, 100,
                60L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1), namedThreadFactory,  new ThreadPoolExecutor.CallerRunsPolicy());

        return pool;
    }
}
