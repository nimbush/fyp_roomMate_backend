package com.apu.roommate_filter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class RoomMateFilterApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kathmandu"));
        System.setProperty("user.timezone", "Asia/Kathmandu");
        SpringApplication.run(RoomMateFilterApplication.class, args);
    }

}
