package kr.gooroom.gpms.health.service.impl;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import kr.gooroom.gpms.common.service.dao.SqlSessionMetaDAO;

@Repository("healthDAO")
public class HealthDAO extends SqlSessionMetaDAO{
    private static final Logger logger = LoggerFactory.getLogger(HealthDAO.class);

    public long updateHealth() throws SQLException{
        try{
            return (long) sqlSessionMeta.update("updateHealth");
        }
        catch(Exception ex){
            logger.error("error in updateHealth : {}, {}, {}","failed at updateHealth glm","fail",ex.toString());
        }
        return 0;
    }
}