package com.sas.saveandsound.cashe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    public SoundCache soundCache() {
        return new SoundCache(100);
    }
}

