package com.springgears.cloud.eurekaclient;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupListener
        implements ApplicationListener<ContextRefreshedEvent> {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final Random RANDOM = new Random();

    private final DiscoveryClient discoveryClient;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        IntStream.iterate(0, i -> i + 1)
                .forEach(id -> {
                    sleep();
                    getSongById(id);
                });
    }

    private void getSongById(int id) {
        List<ServiceInstance> instances = discoveryClient.getInstances("song-library");
        ServiceInstance serviceInstance = instances.get(RANDOM.nextInt(instances.size()));
        try {
            Song song = REST_TEMPLATE.getForEntity(
                    serviceInstance.getUri() + "/songs/" + id, Song.class).getBody();
            log.info("Retrieved song with discovery client: " + song);
        } catch (Exception e) {
            log.error("Failed to retrieve song.", e);
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("Failed to create delay.", e);
        }
    }
}
