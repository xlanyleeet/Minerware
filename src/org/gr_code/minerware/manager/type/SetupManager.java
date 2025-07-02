package org.gr_code.minerware.manager.type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.manager.ManageHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetupManager {

    private static boolean bungeeMode;

    private static final MinerPlugin minerPlugin = MinerPlugin.getInstance();

    public static String toString(Location location) {
        return Objects.requireNonNull(location.getWorld()).getName() + ":" + format(location.getX()) + ":"
                + format(location.getY()) + ":" + format(location.getZ()) + ":" + format(location.getYaw());
    }

    public static Location fromString(String string) {
        String[] paramStrings = string.split(":");
        return new Location(Bukkit.getWorld(paramStrings[0]), Double.parseDouble(paramStrings[1]),
                Double.parseDouble(paramStrings[2]), Double.parseDouble(paramStrings[3]),
                Float.parseFloat(paramStrings[4]), 0.0F);
    }

    public static Location fromString(String string, String world) {
        Location location = fromString(string);
        location.setWorld(Bukkit.getWorld(world));
        return location;
    }

    private static final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getArenas();

    public static boolean notBungeeMode() {
        return !bungeeMode;
    }

    public static void saveArena(Arena arena) {
        Properties properties = arena.getProperties();
        Location selection_1 = properties.getFirstLocation().getBlock().getLocation();
        Location losers = properties.getLobbyLocationLoser();
        Location winners = properties.getLobbyLocationWinner();
        String name = properties.getName();
        int minPlayers = properties.getMinPlayers();
        List<String> strings = new ArrayList<>(properties.getDisabledGames());
        fileConfiguration.addDefault(name, "Type");
        fileConfiguration.set("arenas." + name + ".Type", properties.getType());
        fileConfiguration.set("arenas." + name + ".Display-Name",
                properties.getDisplayName().equals(properties.getName()) ? null : properties.getDisplayName());
        fileConfiguration.set("arenas." + name + ".Cuboid_First_Location", toString(selection_1));
        fileConfiguration.set("arenas." + name + ".Losers_Location", toString(losers));
        fileConfiguration.set("arenas." + name + ".Winners_Location", toString(winners));
        fileConfiguration.set("arenas." + name + ".Min_Players", minPlayers);
        fileConfiguration.set("arenas." + name + ".Rotation", properties.getBaseYaw());
        fileConfiguration.set("arenas." + name + ".Disabled-Games", strings);
        try {
            fileConfiguration.save(minerPlugin.getArenasFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection("arenas");
        bungeeMode = MinerPlugin.getInstance().getBungee().getBoolean("enabled");
        if (configurationSection == null) {
            bungeeMode = false;
            internal();
            return;
        }
        for (String string : configurationSection.getKeys(false)) {
            Utils.loadWorld(string);
            Location selection_1 = fromString(
                    Objects.requireNonNull(fileConfiguration.getString("arenas." + string + ".Cuboid_First_Location")),
                    string);
            Location losers = fromString(
                    Objects.requireNonNull(fileConfiguration.getString("arenas." + string + ".Losers_Location")),
                    string);
            Location winners = fromString(
                    Objects.requireNonNull(fileConfiguration.getString("arenas." + string + ".Winners_Location")),
                    string);
            int minPlayers = fileConfiguration.getInt("arenas." + string + ".Min_Players");
            float yaw = Float
                    .parseFloat(Objects.requireNonNull(fileConfiguration.getString("arenas." + string + ".Rotation")));
            String type = fileConfiguration.getString("arenas." + string + ".Type");
            String displayName = fileConfiguration.getString("arenas." + string + ".Display-Name");
            List<String> strings = fileConfiguration.getStringList("arenas." + string + ".Disabled-Games");
            Properties properties = new Properties(Arena.parseType(type), yaw, selection_1, losers, winners, minPlayers,
                    strings, string, displayName);
            properties.doRestore();
            Properties.finish(properties);
        }
        if (bungeeMode)
            ServerManager.startBungeeMode();
        internal();
    }

    private static void internal() {
        StatisticManager.load();
        SignManager.load();
        SocketManager.load();
        ManageHandler.tick();
    }

    private static String format(double d) {
        return String.format("%.2f", d).replace(",", ".");
    }

}
