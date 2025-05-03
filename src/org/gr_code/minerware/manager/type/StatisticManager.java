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
import org.gr_code.minerware.manager.type.nms.hologram.IHologram;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.*;

public class StatisticManager {

    private static boolean isLeaderboards = false;

    private static final String[][] topCached = new String[MySQL.Path.values().length][10];

    public static String replace(String paramString, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return "";
        Cached cached = CachedMySQL.get(uuid);
        if (cached == null)
            return "";
        return Utils.translate(Utils.request(paramString.
                replace("<games_played>", cached.getGamesPlayed() + "").
                replace("<wins>", cached.getWins() + "").
                replace("<max_points>", cached.getMaxPoints() + "").
                replace("<name>", player.getName()).
                replace("<level>", cached.getLevel() + "").
                replace("<exp>", cached.getExp() + "").
                replace("<percentage>", cached.getPercentage()).
                replace("<level_bar>", cached.createLevelBar()).
                replace("<online>", ServerManager.getOnline() + ""), player));
    }

    public static void add(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;
        CachedMySQL.add(player);
    }

    public static void spawnHolograms(UUID uuid) {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        try {
            Cached cached = CachedMySQL.get(uuid);
            if(cached != null) {
                IHologram iHologram = (IHologram) Class.forName("org.gr_code.minerware.manager.type.nms.hologram.version." + version).getConstructors()[0].newInstance();
                iHologram.setOwner(uuid);
                iHologram.spawnAll();
                cached.setStatsHologram(iHologram);
                return;
            }
            Bukkit.getLogger().warning("[MinerWare] Found unknown cached object with uuid: "+uuid.toString());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
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
        CachedMySQL.getCached().stream().filter(cached -> cached.getIHologram() != null).forEach(cached -> cached.getIHologram().update());
    }

    public static void update(UUID uuid) {
        Cached cached = CachedMySQL.get(uuid);
        IHologram iHologram = cached.getIHologram();
        if (iHologram == null) {
            StatisticManager.spawnHolograms(cached.getUUID());
            return;
        }
        iHologram.update();
    }

    public static void spawn(Location location) {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("holograms.locations");
        strings.add(SetupManager.toString(location));
        fileConfiguration.set("holograms.locations", strings);
        try {
            fileConfiguration.save(MinerPlugin.getInstance().getOptionsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
    }

    public static void load() {
        loadTops();
        Collection<? extends Player> collection = Bukkit.getOnlinePlayers();
        removeAll();
        collection.forEach(player -> add(player.getUniqueId()));
    }

    public static void removeAll() {
        CachedMySQL.getCached().stream().filter(cached -> cached.getIHologram() != null).forEach(cached -> cached.getIHologram().destroyAll());
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
        strings.remove(a - 1);
        fileConfiguration.set("holograms.locations", strings);
        try {
            fileConfiguration.save(MinerPlugin.getInstance().getOptionsFile());
        } catch (IOException e) {
            return false;
        }
        load();
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
        ArmorStand armorStand = (ArmorStand) Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.ARMOR_STAND);
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

    private static ArmorStand[] armorStands = new ArmorStand[]{};

}
