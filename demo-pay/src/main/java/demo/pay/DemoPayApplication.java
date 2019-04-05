package demo.pay;

import demo.lib.user.starter.autoconfig.EnableUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableUser
public class DemoPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoPayApplication.class, args);
    }

}
