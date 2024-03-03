package io.github.bilektugrul.solarfriendsbungee.user;

import io.github.bilektugrul.solarfriendsbungee.SolarFriends;
import io.github.bilektugrul.solarfriendsbungee.util.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private static final SolarFriends plugin = SolarFriends.INSTANCE;

    private final Configuration data;
    private final String name;

    private final List<String> friends = new ArrayList<>();
    private final List<String> friendRequests = new ArrayList<>();
    private final List<String> blocked = new ArrayList<>();
    private final Map<String, List<String>> mails = new HashMap<>();

    public User(Configuration data, String name) {
        this.data = data;
        this.name = name;

        friends.addAll(data.getStringList("friends"));
        blocked.addAll(data.getStringList("blocked"));

        for (String sender : data.getSection("mails").getKeys()) {
            mails.put(sender, data.getStringList("mails." + sender));
        }
    }

    public List<String> getFriends() {
        return friends;
    }

    public List<String> getFriendRequests() {
        return friendRequests;
    }

    public List<String> getBlocked() {
        return blocked;
    }

    public boolean isFriendWith(User other) {
        return friends.contains(other.getName());
    }

    public boolean isFriendWith(String other) {
        return friends.contains(other);
    }

    public boolean hasRequestFrom(User other) {
        return friendRequests.contains(other.getName());
    }

    public void addFriend(User other) {
        friends.add(other.getName());
    }

    public void removeFriend(User other) {
        friends.remove(other.getName());
    }

    public void addRequest(User other) {
        friendRequests.add(other.getName());
    }

    public void removeRequest(User other) {
        friendRequests.remove(other.getName());
    }

    public void block(String name) {
        blocked.add(name);
    }

    public boolean isBlocked(String name) {
        return blocked.contains(name);
    }

    public void unblock(String name) {
        blocked.remove(name);
    }

    public Map<String, List<String>> getMails() {
        return mails;
    }

    public int addMailBy(String name, String message) {
        if (mails.size() == Utils.getInt("general-mail-limit")) {
            return 0;
        }

        List<String> allMails = new ArrayList<>();
        if (mails.containsKey(name)) allMails.addAll(mails.get(name));

        if (allMails.size() == Utils.getInt("per-player-mail-limit")) {
            return -1;
        }

        allMails.add(message);
        mails.put(name, allMails);
        return 1;
    }

    public void clearMails() {
        mails.clear();
    }

    public String getName() {
        return name;
    }

    public ProxiedPlayer getPlayer() {
        return plugin.getServer().getPlayer(name);
    }

    public void sendMessage(String message) {
        Utils.sendMessage(message, getPlayer());
    }

    public void sendRawMessage(String message) {
        getPlayer().sendMessage(Utils.colored(message));
    }

    public List<ProxiedPlayer> getOnlineFriends() {
        List<ProxiedPlayer> onlineFriends = new ArrayList<>();

        for (ProxiedPlayer loop : plugin.getServer().getPlayers()) {
            for (String friend : friends) {
                if (loop.getName().equalsIgnoreCase(friend)) {
                    onlineFriends.add(loop);
                }
            }
        }

        return onlineFriends;
    }

    public void save() throws IOException {
        data.set("friends", friends);
        data.set("blocked", blocked);
        if (mails.isEmpty()) {
            data.set("mails", null);
        } else {
            mails.keySet().forEach(key -> data.set("mails." + key, mails.get(key)));
        }

        ConfigurationProvider.getProvider(YamlConfiguration.class).save(data, new File(plugin.getDataFolder(),"/players/" + name + ".yml"));
    }

}