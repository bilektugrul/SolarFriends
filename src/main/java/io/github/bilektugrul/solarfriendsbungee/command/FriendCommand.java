package io.github.bilektugrul.solarfriendsbungee.command;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarfriendsbungee.SolarFriends;
import io.github.bilektugrul.solarfriendsbungee.user.User;
import io.github.bilektugrul.solarfriendsbungee.user.UserManager;
import io.github.bilektugrul.solarfriendsbungee.util.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public final class FriendCommand extends Command {

    private final SolarFriends plugin;
    private final ProxyServer server;
    private final UserManager userManager;

    public FriendCommand(SolarFriends plugin) {
        super("friend", null, "f", "friends");
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.userManager = plugin.getUserManager();
    }

    @Override
    public void execute(CommandSender source, String[] args) {
        if (args.length != 0 && args[0].equalsIgnoreCase("reload") && source.hasPermission("friends.reload")) {
            plugin.reloadConfig();
            Utils.sendMessage("reloaded", source);
            return;
        }

        if (!(source instanceof ProxiedPlayer sender)) {
            Utils.sendMessage("console", source);
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            Utils.sendMessage("help-command", sender);
        }

        if (args.length == 0) return;

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 1) {
                Utils.sendMessage("type-player", source);
                return;
            }

            ProxiedPlayer player = server.getPlayer(args[1]);
            if (player != null) {
                addCommand(sender, player);
            } else {
                Utils.sendMessage("not-active", source);
            }
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (args.length == 1) {
                Utils.sendMessage("type-player", source);
                return;
            }

            ProxiedPlayer player = server.getPlayer(args[1]);
            if (player != null) {
                acceptCommand(sender, player);
            } else {
                Utils.sendMessage("not-active", source);
            }
        }

        if (args[0].equalsIgnoreCase("deny")) {
            if (args.length == 1) {
                Utils.sendMessage("type-player", source);
                return;
            }

            ProxiedPlayer player = server.getPlayer(args[1]);
            if (player != null) {
                denyCommand(sender, player);
            } else {
                Utils.sendMessage("not-active", source);
            }
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                Utils.sendMessage("type-player", source);
                return;
            }

            ProxiedPlayer player = server.getPlayer(args[1]);
            if (player != null) {
                removeCommand(sender, player);
            } else {
                Utils.sendMessage("not-active", source);
            }
        }

        if (args[0].equalsIgnoreCase("list")) {
            listCommand(sender);
        }

        if (args[0].equalsIgnoreCase("msg")) {
            if (args.length == 1) {
                Utils.sendMessage("type-player", source);
                return;
            }

            if (args.length == 2) {
                // continuous chat
                return;
            }

            ProxiedPlayer player = server.getPlayer(args[1]);
            if (player != null) {
                messageCommand(sender, player, Utils.listToStringNoNl(Arrays.asList(args).subList(2, args.length)));
            } else {
                Utils.sendMessage("not-active", source);
            }
        }

        if (args[0].equalsIgnoreCase("mail")) {
            if (args.length == 1) {
                Utils.sendMessage("type-player", source);
                return;
            }

            ProxiedPlayer player = server.getPlayer(args[1]);
            if (player == null) {
                mailCommand(sender, args[1], Utils.listToStringNoNl(Arrays.asList(args).subList(2, args.length)));
            } else {
                messageCommand(sender, player, Utils.listToStringNoNl(Arrays.asList(args).subList(2, args.length)));
            }
        }

        if (args[0].equalsIgnoreCase("bc") || args[0].equalsIgnoreCase("broadcast")) {
            if (args.length == 1) {
                Utils.sendMessage("type-message", source);
                return;
            }

            broadcastCommand(sender, Utils.listToStringNoNl(Arrays.asList(args).subList(1, args.length)));
        }
    }

    private void addCommand(ProxiedPlayer source, ProxiedPlayer toAdd) {
        if (source == toAdd) {
            source.sendMessage(ChatColor.RED + "Okay then.");
            return;
        }

        User sourceUser = userManager.getUser(source);
        if (sourceUser == null) return;

        if (sourceUser.getFriends().size() == Utils.getInt("friend-limit")) {
            source.sendMessage(Utils.getMessage("max-friends", source));
            return;
        }

        User toAddUser = userManager.getUser(toAdd);
        if (toAddUser == null) {
            sourceUser.sendMessage("not-active");
            return;
        }

        if (sourceUser.isFriendWith(toAddUser)) {
            source.sendMessage(Utils.getMessage("already-friends", source)
                    .replace("%user%", toAdd.getName()));
            return;
        }

        if (sourceUser.hasRequestFrom(toAddUser)) {
            source.sendMessage(Utils.getMessage("have-request-from", source)
                    .replace("%user%", toAdd.getName()));
            acceptCommand(source, toAdd);
            return;
        }

        if (toAddUser.hasRequestFrom(sourceUser)) {
            source.sendMessage(Utils.getMessage("already-sent", source)
                    .replace("%user%", toAdd.getName()));
            return;
        }

        toAddUser.addRequest(sourceUser);
        source.sendMessage(Utils.getMessage("sent-request", source)
                .replace("%user%", toAdd.getName()));
        toAdd.sendMessage(Utils.getMessage("new-request", toAdd)
                .replace("%user%", source.getName()));

        HCore.syncScheduler()
                .after(1, TimeUnit.MINUTES)
                .terminateIf(b -> !toAddUser.hasRequestFrom(sourceUser))
                .run(() -> toAddUser.removeRequest(sourceUser));

    }

    private void acceptCommand(ProxiedPlayer source, ProxiedPlayer toAccept) {
        User sourceUser = userManager.getUser(source);
        if (sourceUser == null) return;

        if (sourceUser.getFriends().size() == Utils.getInt("friend-limit")) {
            source.sendMessage(Utils.getMessage("max-friends", source));
            return;
        }

        User toAcceptUser = userManager.getUser(toAccept);
        if (toAcceptUser == null) {
            sourceUser.sendMessage("not-active");
            return;
        }

        if (!sourceUser.hasRequestFrom(toAcceptUser)) {
            source.sendMessage(Utils.getMessage("no-request", source)
                    .replace("%user%", toAccept.getName()));
            return;
        }

        sourceUser.addFriend(toAcceptUser);
        toAcceptUser.addFriend(sourceUser);
        sourceUser.removeRequest(toAcceptUser);
        source.sendMessage(Utils.getMessage("accepted", source)
                .replace("%user%", toAccept.getName()));
        toAccept.sendMessage(Utils.getMessage("accepted-2", toAccept)
                .replace("%user%", source.getName()));
    }

    private void denyCommand(ProxiedPlayer source, ProxiedPlayer toDeny) {
        User sourceUser = userManager.getUser(source);
        User toDenyUser = userManager.getUser(toDeny);
        if (sourceUser == null) return;
        if (toDenyUser == null) {
            sourceUser.sendMessage("not-active");
            return;
        }

        if (!sourceUser.hasRequestFrom(toDenyUser)) {
            source.sendMessage(Utils.getMessage("no-request", source)
                    .replace("%user%", toDenyUser.getName()));
            return;
        }

        sourceUser.removeRequest(toDenyUser);
        source.sendMessage(Utils.getMessage("denied", source)
                .replace("%user%", toDenyUser.getName()));
        toDenyUser.sendMessage(Utils.getMessage("denied-2", toDeny)
                .replace("%user%", source.getName()));
    }

    private void removeCommand(ProxiedPlayer source, ProxiedPlayer toRemove) {
        User sourceUser = userManager.getUser(source);
        User toRemoveUser = userManager.getUser(toRemove);
        if (sourceUser == null) return;
        if (toRemoveUser == null) {
            sourceUser.sendMessage("not-active");
            return;
        }

        if (!sourceUser.isFriendWith(toRemoveUser)) {
            source.sendMessage(Utils.getMessage("not-friends-with", source)
                    .replace("%user%", toRemoveUser.getName()));
            return;
        }

        sourceUser.removeFriend(toRemoveUser);
        toRemoveUser.removeFriend(sourceUser);
        source.sendMessage(Utils.getMessage("removed", source)
                .replace("%user%", toRemoveUser.getName()));
    }

    private Set<ProxiedPlayer> cooldown = new HashSet<>();

    private void listCommand(ProxiedPlayer source) {
     if (cooldown.contains(source)) {
            source.sendMessage(Utils.getMessage("cooldown", source));
            return;
        }

        User sourceUser = userManager.getUser(source);
        source.sendMessage(Utils.getMessage("list.header", source));
        sourceUser.getFriends().forEach(f -> source.sendMessage(Utils.getMessage("list.format", source)
                .replace("%name%", f)
                .replace("%online%", server.getPlayer(f) == null
                        ? Utils.getColoredString("messages.list.offline")
                        : Utils.getColoredString("messages.list.online")))
        );

        cooldown.add(source);
        HCore.asyncScheduler()
                .after(Utils.getInt("list-cooldown"), TimeUnit.SECONDS)
                .run(() -> cooldown.remove(source));
    }

    private void messageCommand(ProxiedPlayer source, ProxiedPlayer toMessage, String message) {
        User sourceUser = userManager.getUser(source);
        if (sourceUser == null) return;

        User toMessageUser = userManager.getUser(toMessage);
        if (toMessageUser == null) {
            sourceUser.sendMessage("not-active");
            return;
        }

        if (!sourceUser.isFriendWith(toMessageUser)) {
            source.sendMessage(Utils.getMessage("not-friends-with", source)
                    .replace("%user%", toMessageUser.getName()));
            return;
        }

        String senderMessage = Utils.getMessage("msg-format.sender", source).replace("%user%", toMessage.getName());
        String receiverMessage = Utils.getMessage("msg-format.receiver", source).replace("%user%", source.getName());

        source.sendMessage(senderMessage.replace("%message%", message));
        toMessage.sendMessage(receiverMessage.replace("%message%", message));
    }

    private void mailCommand(ProxiedPlayer source, String toMail, String message) {
        User sourceUser = userManager.getUser(source);
        if (sourceUser == null) return;

        User toMessageUser = userManager.getUser(toMail);
        if (toMessageUser != null) {
            source.chat("/f msg " + toMail + " " + message);
            return;
        }

        if (!sourceUser.isFriendWith(toMail)) {
            source.sendMessage(Utils.getMessage("not-friends-with", source)
                    .replace("%user%", toMail));
            return;
        }

        User toMessageOffline = userManager.getOfflineUser(toMail);
        int status = toMessageOffline.addMailBy(source.getName(), message);
        if (status == 1) {
            String senderMessage = Utils.getMessage("msg-format.mail-sent", source).replace("%user%", toMail);
            source.sendMessage(senderMessage.replace("%message%", message));
            try {
                toMessageOffline.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (status == 0) {
            source.sendMessage(Utils.getMessage("general-mail-amount", source));
        } else if (status == -1) {
            source.sendMessage(Utils.getMessage("mail-amount", source));
        }
    }


    private void broadcastCommand(ProxiedPlayer source, String message) {
        if (cooldown.contains(source)) {
            source.sendMessage(Utils.getMessage("cooldown", source));
            return;
        }

        User sourceUser = userManager.getUser(source);
        String broadcast = Utils.getMessage("msg-format.broadcast", source)
                .replace("%user%", source.getName())
                .replace("%message%", message);

        source.sendMessage(broadcast);
        List<ProxiedPlayer> onlineFriends = sourceUser.getOnlineFriends();
        onlineFriends.forEach(f -> f.sendMessage(broadcast));
        cooldown.add(source);
        HCore.asyncScheduler()
                .after(Utils.getInt("broadcast-cooldown"), TimeUnit.SECONDS)
                .run(() -> cooldown.remove(source));
    }

    private void blockCommand(ProxiedPlayer source) {

    }

}