package kr.gooroom.gpms.health.service.impl;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import kr.gooroom.gpms.health.service.HealthService;

@Service("healthService")
public class HealthServiceImpl implements HealthService {
    @Resource(name="healthDAO")
    HealthDAO healthDAO;

    public long updateHealth(){
        try{
            return healthDAO.updateHealth();
        }
        catch (Exception e){
            return 0;
        }
    }
}