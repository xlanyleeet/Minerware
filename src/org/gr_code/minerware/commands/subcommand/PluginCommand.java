package org.gr_code.minerware.commands.subcommand;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ComponentBuilder;
import org.gr_code.minerware.commands.ACommand;
import org.gr_code.minerware.gui.ArenaSelectionGUI;
import org.gr_code.minerware.manager.type.*;
import org.gr_code.minerware.manager.type.database.type.MySQL;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PluginCommand extends ACommand {

    public PluginCommand() {
        super("minerware");
        setAliases(Arrays.asList("mw", "minerware"));
    }

    public static void clear() {
        getArenaHashMap().forEach((key, value) -> {
            org.bukkit.entity.Player player = Bukkit.getPlayer(key);
            assert player != null;
            value.destroyCuboid();
            player.getInventory().setItem(8, null);
            Utils.teleportToLobby(player);
            World world = Bukkit.getWorld(value.getName());
            assert world != null;
            world.save();
            Bukkit.unloadWorld(world, true);
        });
        ARENA_HASH_MAP.clear();
    }

    private static final MinerPlugin minerPlugin = MinerPlugin.getInstance();

    private static final HashMap<UUID, Properties> ARENA_HASH_MAP = new HashMap<>();

    public enum Language {

        INCORRECT("messages.unknown-command"),
        ILLEGAL("messages.illegal-characters"),
        ALREADY_EXISTS("messages.already-exists"),
        ALREADY_CREATING("messages.already-creating"),
        NO_PERMISSIONS("messages.no-permissions"),
        FINISH_CREATING("messages.finish-creating"),
        IN_GAME("messages.in-game"),
        DEPRECATED_WORLD("messages.deprecated-world"),
        NOT_EXIST("messages.not-exist"),
        LOBBY_SET("messages.lobby-set"),
        SPAWNED_STATISTIC("messages.spawned-statistic"),
        SPAWNED_LEADERBOARD("messages.spawned-leaderboard"),
        UNKNOWN_LEADERBOARD("messages.unknown-leaderboard"),
        ARENA_IN_GAME("messages.arena-in-game"),
        LEAVE("messages.leave"),
        NOT_IN_GAME("messages.not-in-game"),
        CONNECT_ERROR("messages.connect-error"),
        ENABLE_DATABASE("messages.enable-database"),
        FAILED_LEADERBOARD("messages.failed-leaderboard"),
        FAILED_HOLOGRAM("messages.failed-hologram"),
        NOT_NUMBER("messages.not-number"),
        REMOVED_LEADERBOARD("messages.removed-leaderboard"),
        REMOVED_HOLOGRAM("messages.removed-hologram"),
        FAILED_START("messages.failed-start"),
        START("messages.start"),
        STOP("messages.stop"),
        BUNGEE_KICK("messages.bungee-disconnect"),
        BUNGEE_JOIN("messages.bungee-join"),
        SAVED("messages.saved"),
        NOT_READY("messages.not-ready"),
        TYPE_SELECT("messages.type-select"),
        CUBOID_NOT_SELECTED("messages.cuboid-not-selected"),
        CUBOID_FAIL("messages.cuboid-fail"),
        LOCATION_NULL("messages.location-null"),
        BOSS_FAIL("messages.boss-fail"),
        GAMES_FAIL("messages.games-fail"),
        DELETED("messages.deleted"),
        LOCATION_AND_TYPE("messages.location-and-type"),
        INSIDE_CUBOID("messages.inside-cuboid"),
        LOCATION_SET("messages.location-set"),
        TELEPORTED("messages.teleported"),
        VOTED_HARD("messages.hard-voted"),
        VOTED_NORMAL("messages.normal-voted"),
        ALREADY_VOTED("messages.already-voted"),
        CAN_NOT_VOTE("messages.can-not-vote"),
        CAN_NOT_OPEN("messages.can-not-open");

        Language(String string) {
            this.string = Utils.translate(minerPlugin.getLanguage().getString(string));
        }

        private final String string;

        public String getString() {
            return string;
        }

    }

    @Override
    public void onCommand(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            UUID uuid = player.getUniqueId();
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("create")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.containsIllegalCharacters(args[1])) {
                        sendMessage(Language.ILLEGAL.getString(), player);
                        return;
                    }
                    if (minerPlugin.getArenas().get("arenas." + args[1]) != null) {
                        sendMessage(Language.ALREADY_EXISTS.getString(), player);
                        return;
                    }
                    if (Utils.isAlreadyCreating(args[1])) {
                        sendMessage(Language.ALREADY_CREATING.getString(), player);
                        return;
                    }
                    if (Utils.isInSession(uuid)) {
                        sendMessage(Language.FINISH_CREATING.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid)) {
                        sendMessage(Language.IN_GAME.getString(), player);
                        return;
                    }
                    if (Utils.DEPRECATED_WORLDS.contains(args[1])) {
                        sendMessage(Language.DEPRECATED_WORLD.getString(), player);
                        return;
                    }
                    WorldManager worldManager = WorldManager.GenerateWorld(args[1]);
                    World world = worldManager.getWorld();
                    // noinspection deprecation
                    world.setGameRuleValue("DO_MOB_SPAWNING", "false");
                    // noinspection deprecation
                    world.setGameRuleValue("DO_DAY_LIGHT_CYCLE", "false");
                    world.setThundering(false);
                    worldManager.teleport(uuid);
                    player.setGameMode(GameMode.CREATIVE);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    Properties properties = new Properties(args[1]);
                    ARENA_HASH_MAP.put(uuid, properties);
                    properties.setTask("OPENED DEFAULT");
                    player.getInventory().setItem(8, Utils.CLOSED_MENU);
                    player.openInventory(properties.getEditGUI());
                    return;
                }
                if (args[0].equalsIgnoreCase("edit")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.containsIllegalCharacters(args[1])) {
                        sendMessage(Language.ILLEGAL.getString(), player);
                        return;
                    }
                    if (ServerManager.getArena(args[1]) == null) {
                        sendMessage(Language.NOT_EXIST.getString(), player);
                        return;
                    }
                    if (Utils.isAlreadyCreating(args[1])) {
                        sendMessage(Language.NOT_EXIST.getString(), player);
                        return;
                    }
                    if (Utils.isInSession(uuid)) {
                        sendMessage(Language.FINISH_CREATING.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid)) {
                        sendMessage(Language.IN_GAME.getString(), player);
                        return;
                    }
                    Arena arena = ServerManager.getArena(args[1]);
                    assert arena != null;
                    arena.forceStopArena();
                    MinerPlugin.getARENA_REGISTRY().remove(arena);
                    player.setGameMode(GameMode.CREATIVE);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.teleport(arena.getProperties().getFirstLocation());
                    ARENA_HASH_MAP.put(uuid, arena.getProperties());
                    arena.getProperties().setTask("OPENED DEFAULT");
                    player.getInventory().setItem(8, Utils.CLOSED_MENU);
                    player.openInventory(arena.getProperties().getEditGUI());
                    return;
                }
                if (args[0].equalsIgnoreCase("join")) {
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (getArenaHashMap().containsKey(uuid)) {
                        sendMessage(Language.FINISH_CREATING.getString(), player);
                        return;
                    }
                    if (Utils.containsIllegalCharacters(args[1])) {
                        sendMessage(Language.ILLEGAL.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid)) {
                        sendMessage(Language.IN_GAME.getString(), player);
                        return;
                    }
                    if (Utils.notExists(args[1])) {
                        sendMessage(Language.NOT_EXIST.getString(), player);
                        return;
                    }
                    Arena arena = ServerManager.getArena(args[1]);
                    assert arena != null;
                    if (!(arena.canJoin(uuid))) {
                        sendMessage(Language.ARENA_IN_GAME.getString(), player);
                        return;
                    }
                    arena.addPlayer(player.getUniqueId());
                    return;
                }
                if (args[0].equalsIgnoreCase("leave")) {
                    if (!Utils.isInGame(uuid)) {
                        sendMessage(Language.NOT_IN_GAME.getString(), player);
                        return;
                    }
                    Arena arena = ServerManager.getArena(uuid);
                    Utils.setupToGame(player);
                    assert arena != null;
                    sendMessage(
                            Language.LEAVE.getString().replace("<arena_name>", arena.getProperties().getDisplayName()),
                            player);
                    arena.removePlayer(uuid, true);
                    return;
                }
                if (args[0].equalsIgnoreCase("setLobby")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length != 1) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    minerPlugin.getMessages().set("game-finished.lobby-location",
                            SetupManager.toString(player.getLocation()));
                    try {
                        minerPlugin.getMessages().save(minerPlugin.getMessagesFile());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendMessage(Language.LOBBY_SET.getString(), player);
                    return;
                }
                if (args[0].equalsIgnoreCase("randomJoin")) {
                    if (args.length != 1) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid)) {
                        sendMessage(Language.IN_GAME.getString(), player);
                        return;
                    }
                    Arena arena = ServerManager.getRandomArena(uuid);
                    if (arena == null) {
                        sendMessage(Language.CONNECT_ERROR.getString(), player);
                        return;
                    }
                    arena.addPlayer(uuid);
                    return;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    if (args.length != 1) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid)) {
                        sendMessage(Language.IN_GAME.getString(), player);
                        return;
                    }
                    ArenaSelectionGUI gui = new ArenaSelectionGUI();
                    gui.openGUI(player);
                    return;
                }
                if (args[0].equalsIgnoreCase("spawnStatistic")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    StatisticManager.spawn(player.getLocation().clone());
                    sendMessage(Language.SPAWNED_STATISTIC.getString(), player);
                    return;
                }
                if (args[0].equalsIgnoreCase("removeStatistic")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.notParsable(args[1])) {
                        sendMessage(Language.NOT_NUMBER.getString(), player);
                        return;
                    }
                    if (!StatisticManager.remove(Integer.parseInt(args[1]))) {
                        sendMessage(Language.FAILED_HOLOGRAM.getString(), player);
                        return;
                    }
                    sendMessage(Language.REMOVED_HOLOGRAM.getString().replace("<number>", args[1]), player);
                    return;
                }
                if (args[0].equalsIgnoreCase("spawnLeaderboard")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    MySQL.Path path = MySQL.parsePath(args[1]);
                    if (path == null) {
                        sendMessage(Language.UNKNOWN_LEADERBOARD.getString(), player);
                        return;
                    }
                    if (!StatisticManager.isLeaderboards()) {
                        sendMessage(Language.ENABLE_DATABASE.getString(), player);
                        return;
                    }
                    StatisticManager.spawnLeaderboard(player.getLocation().clone(), path);
                    sendMessage(Language.SPAWNED_LEADERBOARD.getString(), player);
                    return;
                }
                if (args[0].equalsIgnoreCase("removeLeaderboard")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.notParsable(args[1])) {
                        sendMessage(Language.NOT_NUMBER.getString(), player);
                        return;
                    }
                    if (!StatisticManager.removeLeaderboard(Integer.parseInt(args[1]))) {
                        sendMessage(Language.FAILED_LEADERBOARD.getString(), player);
                        return;
                    }
                    sendMessage(Language.REMOVED_LEADERBOARD.getString().replace("<number>", args[1]), player);
                    return;
                }
                if (args[0].equalsIgnoreCase("version")) {
                    String strings = Utils.translate(
                            "&e&lMiner&6&lWare &c" + minerPlugin.getDescription().getVersion() + " &7by &9Gr_Code");
                    player.sendMessage(strings);
                    return;
                }
                if (args[0].equalsIgnoreCase("help")) {
                    sendMessage(player);
                    return;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    // Перезавантажуємо всі конфігурації плагіна
                    try {
                        minerPlugin.reloadConfig();

                        sendMessage("&aКонфігурацію плагіна перезавантажено!", player);
                        minerPlugin.getLogger().info("Plugin configuration reloaded by " + player.getName());
                    } catch (Exception e) {
                        sendMessage("&cПомилка при перезавантаженні конфігурації: " + e.getMessage(), player);
                        minerPlugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return;
                }
                if (args[0].equalsIgnoreCase("openGUI")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    Properties properties = PluginCommand.getArenaHashMap().get(uuid);
                    if (properties == null) {
                        player.sendMessage(Language.CAN_NOT_OPEN.getString());
                        return;
                    }
                    if (!properties.closed)
                        return;
                    properties.setTask("OPENED DEFAULT");
                    player.openInventory(properties.getEditGUI());
                    properties.closed = false;
                    return;
                }
                if (args[0].equalsIgnoreCase("forceStop")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid) && args.length == 1) {
                        Arena arena = ServerManager.getArena(uuid);
                        assert arena != null;
                        arena.forceStopArena();
                        sendMessage(Language.STOP.getString().replace("<arena_name>",
                                arena.getProperties().getDisplayName()), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.containsIllegalCharacters(args[1])) {
                        sendMessage(Language.ILLEGAL.getString(), player);
                        return;
                    }
                    Arena arena = ServerManager.getArena(args[1]);
                    if (arena == null) {
                        sendMessage(Language.NOT_EXIST.getString(), player);
                        return;
                    }
                    arena.forceStopArena();
                    sendMessage(
                            Language.STOP.getString().replace("<arena_name>", arena.getProperties().getDisplayName()),
                            player);
                    return;
                }
                if (args[0].equalsIgnoreCase("forceStart")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (Utils.isInGame(uuid) && args.length == 1) {
                        Arena arena = ServerManager.getArena(uuid);
                        assert arena != null;
                        boolean started = arena.forceStartArena();
                        if (started) {
                            sendMessage(Language.START.getString().replace("<arena_name>",
                                    arena.getProperties().getDisplayName()), player);
                            return;
                        }
                        sendMessage(Language.FAILED_START.getString(), player);
                        return;
                    }
                    if (args.length != 2) {
                        sendMessage(Language.INCORRECT.getString(), player);
                        return;
                    }
                    if (Utils.containsIllegalCharacters(args[1])) {
                        sendMessage(Language.ILLEGAL.getString(), player);
                        return;
                    }
                    Arena arena = ServerManager.getArena(args[1]);
                    if (arena == null) {
                        sendMessage(Language.NOT_EXIST.getString(), player);
                        return;
                    }
                    boolean started = arena.forceStartArena();
                    if (started) {
                        sendMessage(Language.START.getString().replace("<arena_name>",
                                arena.getProperties().getDisplayName()), player);
                        return;
                    }
                    sendMessage(Language.FAILED_START.getString(), player);
                    return;
                }
                if (args[0].equalsIgnoreCase("setDisplayName")) {
                    if (!player.hasPermission("minerware.admin")) {
                        sendMessage(Language.NO_PERMISSIONS.getString(), player);
                        return;
                    }
                    if (args.length < 3) {
                        sendMessage("&7▪ Usage: /mw setDisplayName <arena> <display name>", player);
                        return;
                    }
                    Arena arena = ServerManager.getArena(args[1]);
                    if (arena == null) {
                        sendMessage(Language.NOT_EXIST.getString(), player);
                        return;
                    }
                    // Join all arguments from index 2 onwards to support multi-word names
                    StringBuilder displayName = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        if (i > 2)
                            displayName.append(" ");
                        displayName.append(args[i]);
                    }
                    String newDisplayName = Utils.translate(displayName.toString());
                    arena.getProperties().setDisplayName(newDisplayName);
                    SetupManager.saveArena(arena);
                    sendMessage("&7▪ Arena display name changed to: &6" + newDisplayName, player);
                    return;
                }
            }
        }
        commandSender.sendMessage(Language.INCORRECT.getString());
    }

    private void sendMessage(String string, Player player) {
        BaseComponent baseComponent = ComponentBuilder.newComponentBuilder(string).build();
        player.spigot().sendMessage(baseComponent);
    }

    private void sendMessage(Player player) {
        String path = player.hasPermission("minerware.admin") ? "help" : "user-help";
        List<String> strings = minerPlugin.getLanguage().getStringList(path);
        for (String string : strings)
            sendMessage(string, player);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args)
            throws IllegalArgumentException {
        return super.tabComplete(sender, alias, args);
    }

    public static HashMap<UUID, Properties> getArenaHashMap() {
        return ARENA_HASH_MAP;
    }

}
