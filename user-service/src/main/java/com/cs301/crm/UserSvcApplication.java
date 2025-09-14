package com.cs301.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class UserSvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSvcApplication.class, args);
    }

}
