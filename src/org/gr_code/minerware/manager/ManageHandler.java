package org.gr_code.minerware.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.listeners.ManagerListener;
import org.gr_code.minerware.manager.type.*;
import org.gr_code.minerware.manager.type.database.type.MySQL;
import org.gr_code.minerware.manager.type.modern.ModernMinerAPI;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.lang.reflect.Field;
import java.util.HashSet;

public class ManageHandler {

    private static final int timer = MinerPlugin.getInstance().getOptions().getInt("server-tick");
    private static int lastUpdate = 0;
    public static final SortManager SORT_MANAGER = new SortManager();

    // Modern API instance to replace NMS
    private static final ModernMinerAPI MODERN_API = new ModernMinerAPI();

    private static Location lobbyLocation;

    /**
     * Get the modern API instance (replacement for NMS)
     * 
     * @return ModernMinerAPI instance
     */
    public static ModernMinerAPI getModernAPI() {
        return MODERN_API;
    }

    public static void setupMinerware() {
        load();
        SetupManager.load();
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getMessages();
        String string = fileConfiguration.getString("game-finished.lobby-location");
        register();
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            PlaceholderManager.setup();
        if (string == null || string.equals("")) {
            setLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
            return;
        }
        setLocation(SetupManager.fromString(string));
    }

    public static void tick() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updater();
            }
        }.runTaskTimer(MinerPlugin.getInstance(), 20, 1);
    }

    public static Location getLobbyLocation() {
        return lobbyLocation;
    }

    private static void updater() {
        for (Arena arena : MinerPlugin.getARENA_REGISTRY()) {
            arena.updateArena();
        }
        serverTick();
    }

    private static void serverTick() {
        if (lastUpdate == timer) {
            if (!ManageHandler.notBungeeMode())
                notifyWatcher();
            StatisticManager.loadTops();
            StatisticManager.updateLeaderboards();
            lastUpdate = 0;
            return;
        }
        lastUpdate++;
    }

    public static void setLocation(Location location) {
        lobbyLocation = location;
    }

    private static void load() {
        // Initialize modern API - no need for version-specific NMS classes
        try {
            // Get detailed server information like /about command
            String serverVersion = Bukkit.getVersion(); // Full server version string
            String bukkitVersion = Bukkit.getBukkitVersion(); // API version
            String serverName = Bukkit.getName(); // Server name (CraftBukkit/Paper/Spigot)

            MinerPlugin.getInstance().getLogger().info("Server Information:");
            MinerPlugin.getInstance().getLogger().info("  " + serverVersion);
            MinerPlugin.getInstance().getLogger().info("  API Version: " + bukkitVersion);
            MinerPlugin.getInstance().getLogger().info("  Server Type: " + serverName);
            MinerPlugin.getInstance().getLogger().info("Using modern Bukkit API (NMS-free)");
        } catch (Exception e) {
            MinerPlugin.getInstance().getLogger().warning("Could not detect server version, using defaults");
        }
        optionsInit();
    }

    private static void optionsInit() {
        FileConfiguration dataConfiguration = MinerPlugin.getInstance().getOptions();
        Utils.LEAVE_THE_ARENA = ItemBuilder
                .start(XMaterial.valueOf(dataConfiguration.getString("leave-item.material")).parseItem())
                .setGlowing(dataConfiguration.getBoolean("leave-item.glowing"))
                .setDisplayName(Utils.translate(dataConfiguration.getString("leave-item.name")))
                .setUnbreakable(dataConfiguration.getBoolean("leave-item.unbreakable")).build();
        Utils.VOTE = ItemBuilder.start(XMaterial.valueOf(dataConfiguration.getString("vote-item.material")).parseItem())
                .setGlowing(dataConfiguration.getBoolean("vote-item.glowing"))
                .setDisplayName(Utils.translate(dataConfiguration.getString("vote-item.name")))
                .setUnbreakable(dataConfiguration.getBoolean("vote-item.unbreakable")).build();
        MySQL.start();
        LobbyHelper.load();
    }

    public static boolean notBungeeMode() {
        return SetupManager.notBungeeMode();
    }

    private static void register() {
        HashSet<PluginCommand> pluginCommands = new HashSet<>();
        pluginCommands.add(new PluginCommand());
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
            pluginCommands.forEach(x -> commandMap.register(x.getName(), x));
        } catch (Exception ignored) {
        }
        ManagerListener.register();
    }

    public static void stop() {
        Utils.clear();
        SocketManager.disable();
        StatisticManager.removeAll();
        LobbyHelper.clear();
        StatisticManager.destroyLeaderboards();
        SignManager.getSignLocations().clear();
        PluginCommand.clear();
        HashSet<Arena> arenas = MinerPlugin.getARENA_REGISTRY();
        arenas.forEach(Arena::forceStopArena);
        arenas.clear();
        if (!SetupManager.notBungeeMode())
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(MinerPlugin.getInstance(), "BungeeCord");
    }

    public static void restart() {
        stop();
        MinerPlugin.getInstance().onEnable();
    }

    private static void notifyWatcher() {
        for (Arena arena : MinerPlugin.getARENA_REGISTRY()) {
            SocketManager.sendArenaUpdate(arena);
        }
    }
}
