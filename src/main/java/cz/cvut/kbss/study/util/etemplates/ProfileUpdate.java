package cz.cvut.kbss.study.util.etemplates;

import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.service.ConfigReader;
import cz.cvut.kbss.study.util.ConfigParam;

import java.util.HashMap;
import java.util.Map;

public class ProfileUpdate extends BaseEmailTemplate {
    public ProfileUpdate(ConfigReader config, User user) {
        this.config = config;
        this.username = user.getUsername();
    }

    private ConfigReader config;
    private String username;

    @Override
    public String getSubject() {
        return config.getConfig(ConfigParam.E_PROFILE_UPDATE_SUBJECT);
    }

    @Override
    public String getHTMLContent() {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("appContext", config.getConfig(ConfigParam.APP_CONTEXT));
        return config.getConfigWithParams(ConfigParam.E_PROFILE_UPDATE_CONTENT, params);
    }
}
