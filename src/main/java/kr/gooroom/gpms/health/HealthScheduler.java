package kr.gooroom.gpms.health;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.gooroom.gpms.health.service.HealthService;

@Component
public class HealthScheduler {
    private static final Logger logger = LoggerFactory.getLogger(HealthScheduler.class);
    @Resource(name = "healthService")
    private HealthService healthService;

    @Scheduled(cron = "0/5 * * * * *")
    void updateLastActivatedTime() {
        try {
            healthService.updateHealth();
        } catch (Exception ex) {
            logger.error("error at healthService", ex.toString());
        }
    }
}
