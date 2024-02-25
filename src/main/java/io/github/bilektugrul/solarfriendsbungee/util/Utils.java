package io.github.bilektugrul.solarfriendsbungee.util;

import io.github.bilektugrul.solarfriendsbungee.SolarFriends;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.#");

    public static final DateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");

    public static Configuration getConfig() {
        return SolarFriends.INSTANCE.getConfig();
    }

    public static int getInt(String path) {
        return SolarFriends.INSTANCE.getConfig().getInt(path);
    }

    public static long getLong(String path) {
        return SolarFriends.INSTANCE.getConfig().getLong(path);
    }

    public static String getString(String string) {
        return SolarFriends.INSTANCE.getConfig().getString(string);
    }

    public static String getColoredString(String string) {
        return colored(getString(string));
    }

    public static Boolean getBoolean(String string) {
        return SolarFriends.INSTANCE.getConfig().getBoolean(string);
    }

    public static List<String> getStringList(String string) {
        return SolarFriends.INSTANCE.getConfig().getStringList(string);
    }

    public static String getMessage(String msg, CommandSender sender) {
        String message = listToString(colored(getStringList("messages." + msg)));
        if (sender instanceof ProxiedPlayer player) {
            message = message.replace("%player%", player.getName());
        }

        return message
                .replace("%prefix%", getColoredString("prefix"))
                .replace("%prefix-2%", getColoredString("prefix-2"));
    }

    public static String colored(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colored(List<String> strings) {
        List<String> list = new ArrayList<>();
        for (String str : strings) {
            list.add(colored(str));
        }
        return list;
    }

    public static String arrayToString(String[] array) {
        return String.join(" ", array);
    }

    public static String listToString(List<String> list) {
        return String.join("\n", list);
    }

    public static String listToStringNoNl(List<String> list) {
        return String.join(" ", list);
    }

    public static String fileToString(File file) throws IOException {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<String> content = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null)
            content.add(line);

        return listToString(content);
    }

    public static String millisToString(long millis) {
        Date date = new Date(millis);
        return dateFormat.format(date);
    }

    public static String moneyWithCommas(long l) {
        return decimalFormat.format(l);
    }

    public static void sendMessage(String msg, CommandSender sendTo) {
        String message = getMessage(msg, sendTo);
        sendTo.sendMessage(message);
    }

}