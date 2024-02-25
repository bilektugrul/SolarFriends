package io.github.bilektugrul.solarfriendsbungee;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarfriendsbungee.command.FriendCommand;
import io.github.bilektugrul.solarfriendsbungee.listener.ProxyPlayerListener;
import io.github.bilektugrul.solarfriendsbungee.user.UserManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class SolarFriends extends Plugin {

    public static SolarFriends INSTANCE;

    private Logger logger;

    private ProxyServer server;
    private Path dataDirectory;
    private UserManager userManager;
    private Configuration config;

    @Override
    public void onEnable() {
        INSTANCE = this;
        HCore.initialize(this);

        this.server = this.getProxy();
        this.logger = this.getProxy().getLogger();
        this.userManager = new UserManager(this);
        try {
            this.makeConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.getPluginManager().registerListener(this, new ProxyPlayerListener(this));
        server.getPluginManager().registerCommand(this, new FriendCommand(this));

        this.logger.info("SolarFriends is working now!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void makeConfig() throws IOException {
        // Create plugin config folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
            InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
            in.transferTo(outputStream); // Throws IOException
        }

        this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
    }

    public void reloadConfig() {
        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public Logger getLogger() {
        return logger;
    }

}
