package hs.opcnetty;

import com.runlion.iot.driver.sdk.IotDriverClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({IotDriverClientAutoConfiguration.class})
public class OpcNettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpcNettyApplication.class, args);
    }
}
