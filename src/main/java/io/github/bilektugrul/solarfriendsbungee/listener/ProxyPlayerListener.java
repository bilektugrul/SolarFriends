package io.github.bilektugrul.solarfriendsbungee.listener;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarfriendsbungee.SolarFriends;
import io.github.bilektugrul.solarfriendsbungee.user.User;
import io.github.bilektugrul.solarfriendsbungee.user.UserManager;
import io.github.bilektugrul.solarfriendsbungee.util.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProxyPlayerListener implements Listener {

    private final UserManager userManager;

    public ProxyPlayerListener(SolarFriends plugin) {
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onProxyJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        User user = userManager.loadUser(player);

        String onlineMsg = Utils.getMessage("friend-join", player);
        user.getOnlineFriends().forEach(f -> f.sendMessage(onlineMsg));

        HCore.asyncScheduler()
                .after(4, TimeUnit.SECONDS)
                .run(() -> {
                    Map<String, List<String>> allMails = user.getMails();
                    if (allMails.isEmpty()) return;

                    for (String name : allMails.keySet()) {
                        String senderMessage = Utils.getMessage("msg-format.mail-received", player).replace("%user%", name);
                        List<String> mails = allMails.get(name);
                        player.sendMessage(senderMessage.replace("%messages%", Utils.listToString(mails)));
                    }

                    user.clearMails();
                });
    }

    @EventHandler
    public void onProxyQuit(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        User user = userManager.getUser(player);
        try {
            user.save();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        String offlineMsg = Utils.getMessage("friend-leave", player);
        user.getOnlineFriends().forEach(f -> f.sendMessage(offlineMsg));

        userManager.removeUser(user);
    }

}