package io.github.bilektugrul.solarfriendsbungee.user;

import io.github.bilektugrul.solarfriendsbungee.SolarFriends;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private final SolarFriends plugin;
    private final Set<User> userList = new HashSet<>();

    public UserManager(SolarFriends plugin) {
        this.plugin = plugin;
    }

    public User loadUser(ProxiedPlayer p)    {
        return loadUser(p.getName(), true);
    }

    public User loadUser(String name, boolean keep) {
        Configuration dataFile;
        try {
            File file = new File(plugin.getDataFolder(), "/players/" + name + ".yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            dataFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        User user = new User(dataFile, name);
        if (keep) {
            userList.add(user);
        }

        return user;
    }

    public User getUser(ProxiedPlayer p) {
        return getUser(p.getName());
    }

    public User getUser(String name) {
        for (User user : userList) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }

        return null;
    }

    public User getOfflineUser(String name) {
        return loadUser(name, false);
    }

    public boolean isLoaded(String name) {
        return getUser(name) != null;
    }

    public void removeUser(User user) {
        userList.remove(user);
    }

    public Set<User> getUserList() {
        return new HashSet<>(userList);
    }

    public void saveUsers() throws IOException {
        for (User user : userList) {
            user.save();
        }
    }

}