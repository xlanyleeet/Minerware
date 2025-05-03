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
import org.gr_code.minerware.manager.type.nms.NMS;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ManageHandler {

    private static final int timer = MinerPlugin.getInstance().getOptions().getInt("server-tick");;

    private static int lastUpdate = 0;

    public static final SortManager SORT_MANAGER = new SortManager();

    public static void setupMinerware(){
        load();
        SetupManager.load();
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getMessages();
        String string = fileConfiguration.getString("game-finished.lobby-location");
        register();
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            PlaceholderManager.setup();
        if (string == null || string.equals("")) {
            setLocation(Objects.requireNonNull(Bukkit.getWorlds().get(0)).getSpawnLocation());
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

    private static Location lobbyLocation;

    private static NMS versionManager;

    public static NMS getNMS() {
        return versionManager;
    }

    public static void setLocation(Location location) {
        lobbyLocation = location;
    }

    private static void updater() {
        Set<Arena> arenaSet = MinerPlugin.getARENA_REGISTRY();
        for(Arena arena : arenaSet){
            arena.updateArena();
        }
        serverTick();
    }

    private static void serverTick(){
        if(lastUpdate == timer){
            if(!ManageHandler.notBungeeMode())
                notifyWatcher();
            StatisticManager.loadTops();
            StatisticManager.updateLeaderboards();
            lastUpdate = 0;
            return;
        }
        lastUpdate++;
    }

    public static Location getLobbyLocation() {
        return lobbyLocation;
    }

    private static void load() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        try {
            Class<?> ob = Class.forName("org.gr_code.minerware.manager.type.nms.version." + version);
            Object objective = ob.getConstructors()[0].newInstance();
            versionManager = (NMS) objective;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        optionsInit();
    }

    private static void optionsInit(){
        FileConfiguration dataConfiguration = MinerPlugin.getInstance().getOptions();
        Utils.LEAVE_THE_ARENA = ItemBuilder.start(Objects.requireNonNull(XMaterial.valueOf(dataConfiguration.getString("leave-item.material")).
                parseItem()))
                .setGlowing(dataConfiguration.getBoolean("leave-item.glowing"))
                .setDisplayName(Utils.translate(dataConfiguration.getString("leave-item.name")))
                .setUnbreakable(dataConfiguration.getBoolean("leave-item.unbreakable")).build();
        Utils.VOTE = ItemBuilder.start(Objects.requireNonNull(XMaterial.valueOf(dataConfiguration.getString("vote-item.material")).
                parseItem()))
                .setGlowing(dataConfiguration.getBoolean("vote-item.glowing"))
                .setDisplayName(Utils.translate(dataConfiguration.getString("vote-item.name")))
                .setUnbreakable(dataConfiguration.getBoolean("vote-item.unbreakable")).build();
        MySQL.start();
        LobbyHelper.load();
    }

    public static boolean notBungeeMode() {
        return SetupManager.notBungeeMode();
    }

    private static void register(){
        HashSet<PluginCommand> pluginCommands = new HashSet<>();
        pluginCommands.add(new PluginCommand());
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
            pluginCommands.forEach(x->commandMap.register(x.getName(), x));
        }catch (Exception ignored){
        }
        ManagerListener.register();
    }

    public static void stop(){
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
        if(!SetupManager.notBungeeMode())
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(MinerPlugin.getInstance(), "BungeeCord");
    }

    public static void restart(){
        stop();
        MinerPlugin.getInstance().onEnable();
    }

    private static void notifyWatcher(){
        for(Arena arena : MinerPlugin.getARENA_REGISTRY()){
            SocketManager.sendArenaUpdate(arena);
        }
    }

}
