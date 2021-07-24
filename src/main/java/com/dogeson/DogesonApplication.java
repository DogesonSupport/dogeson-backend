package com.dogeson;

import com.dogeson.okhttp.HttpClientExecutor;
import com.dogeson.okhttp.OkHttpHelper;
import com.dogeson.okhttp.OkRequest;
import com.dogeson.okhttp.OkResponse;
import org.apache.http.HttpStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class DogesonApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        SpringApplication.run(DogesonApplication.class, args);
    }
}
