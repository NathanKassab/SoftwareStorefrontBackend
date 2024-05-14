package me.bannock.capstone.backend.loader.prot.service.donutguard.plugin;

import me.bannock.donutguard.obf.config.ConfigurationGroup;
import me.bannock.donutguard.obf.config.impl.ConfigKeyString;

public class WatermarkerConfigGroup extends ConfigurationGroup {

    // Default values don't matter because our service sets them to the correct values
    // for the specific job it wants to run
    public final static ConfigKeyString API_KEY = new ConfigKeyString("API key", "");
    public final static ConfigKeyString UID = new ConfigKeyString("uid", "");
    public final static ConfigKeyString SERVER_IP = new ConfigKeyString("Auth server ip", "");
    public final static ConfigKeyString REQUEST_PROTOCOL = new ConfigKeyString("Protocol", "http");

}
