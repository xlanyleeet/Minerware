package org.gr_code.minerware.manager.type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.database.type.MySQL;
import org.gr_code.minerware.manager.type.database.cached.Cached;
import org.gr_code.minerware.manager.type.database.cached.CachedMySQL;
import org.gr_code.minerware.api.hologram.IHologram;
import org.gr_code.minerware.api.hologram.ModernHologram;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

public class StatisticManager {

    private static boolean isLeaderboards = false;

    private static final String[][] topCached = new String[MySQL.Path.values().length][10];

    // Static holograms for showing statistics at specific locations
    private static final List<IHologram> staticHolograms = new ArrayList<>();

    public static String replace(String paramString, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return "";
        Cached cached = CachedMySQL.get(uuid);
        if (cached == null)
            return "";
        return Utils.translate(Utils.request(paramString.replace("<games_played>", cached.getGamesPlayed() + "")
                .replace("<wins>", cached.getWins() + "").replace("<max_points>", cached.getMaxPoints() + "")
                .replace("<name>", player.getName()).replace("<level>", cached.getLevel() + "")
                .replace("<exp>", cached.getExp() + "").replace("<percentage>", cached.getPercentage())
                .replace("<level_bar>", cached.createLevelBar()).replace("<online>", ServerManager.getOnline() + ""),
                player));
    }

    public static void add(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;
        CachedMySQL.add(player);
    }

    public static void spawnHolograms(UUID uuid) {
        try {
            Cached cached = CachedMySQL.get(uuid);
            if (cached == null) {
                MinerPlugin.getInstance().getLogger()
                        .warning("Found unknown cached object with uuid: " + uuid.toString());
                return;
            }

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }

            // Get hologram locations from config
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            List<String> locations = fileConfiguration.getStringList("holograms.locations");

            if (locations.isEmpty()) {
                // If no specific locations configured, create hologram above player
                Location playerLocation = player.getLocation().add(0, 2.5, 0);
                createPlayerHologram(cached, playerLocation, player);
            } else {
                // Use first configured location for this player's hologram
                Location hologramLocation = SetupManager.fromString(locations.get(0));
                if (hologramLocation != null) {
                    createPlayerHologram(cached, hologramLocation, player);
                }
            }
        } catch (Exception e) {
            MinerPlugin.getInstance().getLogger()
                    .warning("Error spawning holograms for UUID " + uuid + ": " + e.getMessage());
        }
    }

    private static void createPlayerHologram(Cached cached, Location location, Player player) {
        try {
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            List<String> format = fileConfiguration.getStringList("holograms.format");

            if (format.isEmpty()) {
                return;
            }

            ModernHologram hologram = new ModernHologram(location);
            hologram.setOwner(cached.getUUID());

            // Add each line from the format
            for (String line : format) {
                String formattedLine = replace(line, cached.getUUID());
                hologram.addLine(formattedLine);
            }

            hologram.spawnAll();
            cached.setStatsHologram(hologram);
        } catch (Exception e) {
            MinerPlugin.getInstance().getLogger()
                    .warning("Error creating hologram for player " + player.getName() + ": " + e.getMessage());
        }
    }

    public static void remove(UUID uuid) {
        Cached cached = get(uuid);
        if (cached == null)
            return;
        IHologram iHologram = cached.getIHologram();
        if (iHologram == null)
            return;
        iHologram.destroyAll();
        CachedMySQL.getCached().remove(cached);
    }

    private static Cached get(UUID uuid) {
        return CachedMySQL.get(uuid);
    }

    public static void update() {
        CachedMySQL.getCached().stream().filter(cached -> cached.getIHologram() != null)
                .forEach(cached -> cached.getIHologram().update());
    }

    public static void update(UUID uuid) {
        Cached cached = CachedMySQL.get(uuid);
        IHologram iHologram = cached.getIHologram();
        if (iHologram == null) {
            // StatisticManager.spawnHolograms(cached.getUUID()); // Removed - no longer
            // spawn holograms automatically
            return;
        }
        iHologram.update();
    }

    public static void spawn(Location location) {
        try {
            // Create static hologram at specified location
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            List<String> format = fileConfiguration.getStringList("holograms.format");

            if (format.isEmpty()) {
                format = Arrays.asList(
                        "&8--------------------------",
                        "&e&lMiner&6&lWare &aStatistics",
                        "&aTotal players online: &c" + Bukkit.getOnlinePlayers().size(),
                        "&8--------------------------");
            }

            // Create hologram with general server statistics
            IHologram hologram = new ModernHologram(location);
            for (String line : format) {
                // Replace general placeholders
                String processedLine = line
                        .replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("<n>", "Server")
                        .replace("<games_played>", "N/A")
                        .replace("<wins>", "N/A")
                        .replace("<max_points>", "N/A")
                        .replace("<level>", "N/A")
                        .replace("<exp>", "N/A");

                hologram.addLine(Utils.translate(processedLine));
            }

            staticHolograms.add(hologram);

            // Save location to config
            List<String> locations = fileConfiguration.getStringList("holograms.locations");
            locations.add(SetupManager.toString(location));
            fileConfiguration.set("holograms.locations", locations);

            try {
                fileConfiguration.save(MinerPlugin.getInstance().getOptionsFile());
            } catch (IOException e) {
                MinerPlugin.getInstance().getLogger().warning("Could not save hologram location: " + e.getMessage());
            }

        } catch (Exception e) {
            MinerPlugin.getInstance().getLogger().warning("Error creating static hologram: " + e.getMessage());
        }
    }

    public static void load() {
        loadTops();
        loadStaticHolograms();
        Collection<? extends Player> collection = Bukkit.getOnlinePlayers();
        removeAll();
        collection.forEach(player -> add(player.getUniqueId()));
    }

    private static void loadStaticHolograms() {
        try {
            // Clear existing static holograms
            staticHolograms.forEach(hologram -> {
                if (hologram != null) {
                    hologram.destroyAll();
                }
            });
            staticHolograms.clear();

            // Load holograms from config
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            List<String> locations = fileConfiguration.getStringList("holograms.locations");
            List<String> format = fileConfiguration.getStringList("holograms.format");

            if (format.isEmpty()) {
                format = Arrays.asList(
                        "&8--------------------------",
                        "&e&lMiner&6&lWare &aStatistics",
                        "&aTotal players online: &c" + Bukkit.getOnlinePlayers().size(),
                        "&8--------------------------");
            }

            for (String locationString : locations) {
                Location location = SetupManager.fromString(locationString);
                if (location != null) {
                    IHologram hologram = new ModernHologram(location);
                    for (String line : format) {
                        // Replace general placeholders
                        String processedLine = line
                                .replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))
                                .replace("<n>", "Server")
                                .replace("<games_played>", "N/A")
                                .replace("<wins>", "N/A")
                                .replace("<max_points>", "N/A")
                                .replace("<level>", "N/A")
                                .replace("<exp>", "N/A");

                        hologram.addLine(Utils.translate(processedLine));
                    }
                    staticHolograms.add(hologram);
                }
            }
        } catch (Exception e) {
            MinerPlugin.getInstance().getLogger().warning("Error loading static holograms: " + e.getMessage());
        }
    }

    public static void removeAll() {
        // Remove player-specific holograms
        CachedMySQL.getCached().stream().filter(cached -> cached.getIHologram() != null)
                .forEach(cached -> cached.getIHologram().destroyAll());

        // Remove static holograms
        staticHolograms.forEach(hologram -> {
            if (hologram != null) {
                hologram.destroyAll();
            }
        });
        staticHolograms.clear();
        CachedMySQL.clearAll();
    }

    public static void destroyLeaderboards() {
        for (int i = 0; i < armorStands.length; i++) {
            ArmorStand armorStand = armorStands[i];
            if (armorStand != null) {
                armorStand.remove();
                armorStands[i] = null;
            }
        }
    }

    public static boolean remove(int a) {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("holograms.locations");
        if (strings.size() <= (a - 1))
            return false;
        if (a < 1)
            return false;

        // Remove the static hologram if it exists
        if (a <= staticHolograms.size()) {
            IHologram hologram = staticHolograms.get(a - 1);
            if (hologram != null) {
                hologram.destroyAll();
                staticHolograms.remove(a - 1);
            }
        }

        strings.remove(a - 1);
        fileConfiguration.set("holograms.locations", strings);
        try {
            fileConfiguration.save(MinerPlugin.getInstance().getOptionsFile());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static final BukkitRunnable bukkitRunnable = new BukkitRunnable() {
        @Override
        public void run() {
            spawnLeaderboards();
        }
    };

    private static final MySQL.CallBack<String[][], ResultSet[]> callback = (object, manager) -> {
        try {
            int i = 0;
            for (ResultSet rs : manager) {
                int i1 = 0;
                while (rs.next()) {
                    object[i][i1] = rs.getString("NAME") + ":" +
                            rs.getInt(MySQL.Path.values()[i].name());
                    i1++;
                }
                i++;
            }
            isLeaderboards = true;
            bukkitRunnable.runTask(MinerPlugin.getInstance());
        } catch (Exception ignored) {
        }
    };

    public static void loadTops() {
        MySQL.loadLeaderboards(callback, topCached);
    }

    public static String format(String paramString, int place) {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        String string = Objects.requireNonNull(fileConfiguration.getString("leaderboards.format"))
                .replace("<place>", place + "")
                .replace("<points>", paramString.split(":")[1])
                .replace("<name>", paramString.split(":")[0]);
        return Utils.translate(string);
    }

    private static void spawnLeaderboards() {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("leaderboards.locations");
        armorStands = new ArmorStand[strings.size() * 11];
        int i1 = 0;
        for (String s : strings) {
            int i2 = Integer.parseInt(s.split("=")[0]);
            Location l = SetupManager.fromString(s.split("=")[1]);
            for (int i = 0; i < topCached[i2].length + 1; i++) {
                if (i == 0) {
                    String s1 = fileConfiguration.getString("leaderboards.title."
                            + MySQL.Path.values()[i2].name().toLowerCase());
                    s1 = Utils.translate(s1);
                    ArmorStand a = spawn(l, s1);
                    armorStands[i1] = a;
                    i1++;
                    continue;
                }
                l = l.clone().add(0, -0.26, 0);
                String s1 = topCached[i2][i - 1];
                if (s1 == null) {
                    i1++;
                    continue;
                }
                ArmorStand armorStand = spawn(l, StatisticManager.format(s1, i));
                armorStands[i1] = armorStand;
                i1++;
            }
        }
    }

    public static void updateLeaderboards() {
        if (!isLeaderboards)
            return;
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("leaderboards.locations");
        int updater = 0;
        for (String s : strings) {
            int i2 = Integer.parseInt(s.split("=")[0]);
            Location location = SetupManager.fromString(s.split("=")[1]);
            for (int i = 0; i < topCached[i2].length + 1; i++) {
                if (i == 0) {
                    updater++;
                    continue;
                }
                location = location.clone().add(0, -0.26, 0);
                String s2 = topCached[i2][i - 1];
                if (s2 == null) {
                    updater++;
                    continue;
                }
                ArmorStand armorStand = armorStands[updater];
                String s1 = format(s2, i);
                if (armorStand == null) {
                    armorStand = spawn(location, s1);
                    armorStands[updater] = armorStand;
                    updater++;
                    continue;
                }
                armorStand.setCustomName(s1);
                updater++;
            }
        }
    }

    public static void spawnLeaderboard(Location location, MySQL.Path path) {
        if (!isLeaderboards)
            return;
        isLeaderboards = false;
        destroyLeaderboards();
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("leaderboards.locations");
        strings.add(decodeType(path) + "=" + SetupManager.toString(location));
        fileConfiguration.set("leaderboards.locations", strings);
        saveFile();
        spawnLeaderboards();
        isLeaderboards = true;
    }

    public static boolean removeLeaderboard(int paramInt) {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("leaderboards.locations");
        if (strings.size() <= (paramInt - 1))
            return false;
        if (paramInt < 1)
            return false;
        strings.remove(paramInt - 1);
        fileConfiguration.set("leaderboards.locations", strings);
        saveFile();
        isLeaderboards = false;
        destroyLeaderboards();
        spawnLeaderboards();
        isLeaderboards = true;
        return true;
    }

    private static int decodeType(MySQL.Path path) {
        for (int i = 0; i < MySQL.Path.values().length; i++) {
            if (MySQL.Path.values()[i] == path)
                return i;
        }
        return 0;
    }

    private static void saveFile() {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        try {
            fileConfiguration.save(MinerPlugin.getInstance().getOptionsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArmorStand spawn(Location location, String name) {
        ArmorStand armorStand = (ArmorStand) Objects.requireNonNull(location.getWorld()).spawnEntity(location,
                EntityType.ARMOR_STAND);
        armorStand.setCustomName(name);
        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setGravity(false);
        armorStand.setArms(false);
        armorStand.setBasePlate(false);
        armorStand.setCustomNameVisible(true);
        return armorStand;
    }

    public static boolean isLeaderboards() {
        return isLeaderboards;
    }

    private static ArmorStand[] armorStands = new ArmorStand[] {};

    public static void updateStaticHolograms() {
        try {
            FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
            List<String> format = fileConfiguration.getStringList("holograms.format");

            if (format.isEmpty()) {
                format = Arrays.asList(
                        "&8--------------------------",
                        "&e&lMiner&6&lWare &aStatistics",
                        "&aTotal players online: &c" + Bukkit.getOnlinePlayers().size(),
                        "&8--------------------------");
            }

            for (IHologram hologram : staticHolograms) {
                if (hologram != null) {
                    hologram.clearLines();
                    for (String line : format) {
                        // Replace general placeholders with current data
                        String processedLine = line
                                .replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))
                                .replace("<n>", "Server")
                                .replace("<games_played>", "N/A")
                                .replace("<wins>", "N/A")
                                .replace("<max_points>", "N/A")
                                .replace("<level>", "N/A")
                                .replace("<exp>", "N/A");

                        hologram.addLine(Utils.translate(processedLine));
                    }
                    hologram.update();
                }
            }
        } catch (Exception e) {
            MinerPlugin.getInstance().getLogger().warning("Error updating static holograms: " + e.getMessage());
        }
    }

}
