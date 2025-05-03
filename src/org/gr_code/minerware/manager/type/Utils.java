package org.gr_code.minerware.manager.type;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.gr_code.minerware.cuboid.WorldGenerator;
import org.gr_code.minerware.listeners.statistic.PluginEnable_Statistic;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.nms.NMS;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    private static final float[] math = new float[65536*2];

    static {
        for (int var0 = 0; var0 < 65536; ++var0) {
            math[var0] = (float) Math.sin((double) var0 * Math.PI * 2d / 65536.0D);
        }
    }

    private static final MinerPlugin minerPlugin;

    private static final FileConfiguration fileConfiguration;

    static {
        minerPlugin = MinerPlugin.getInstance();
        fileConfiguration = minerPlugin.getMessages();
    }

    public static boolean notExists(String paramString) {
        return MinerPlugin.getARENA_REGISTRY().stream().noneMatch(arena -> arena.getProperties()
                .getName().equals(paramString));
    }

    public static boolean isInSession(UUID uuid) {
        return PluginCommand.getArenaHashMap().containsKey(uuid);
    }

    public static String translate(String paramString) {
        return ManageHandler.getNMS().translate(paramString);
    }

    public static void deleteWorld(String paramString) {
        World world = Bukkit.getWorld(paramString);
        if (world == null) return;
        if (!world.getPlayers().isEmpty())
            world.getPlayers().forEach(Utils::teleportToLobby);
        Bukkit.unloadWorld(paramString, false);
        try {
            if (ManageHandler.getNMS().isLegacy())
                org.apache.commons.io.FileUtils.deleteDirectory(new File(paramString));
            else {
                FileUtils.deleteDirectory(new File(paramString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack CLOSED_MENU = ItemBuilder.
            start(Objects.requireNonNull(XMaterial.CLOCK.parseItem())).
            setGlowing(true).
            setDisplayName("&e&lCLICK &7to open GUI")
            .build();

    public static ItemStack LEAVE_THE_ARENA = null;

    public static ItemStack VOTE = null;

    public static void apply(Player player, GamePlayer gamePlayer) {
        player.setGameMode(GameMode.ADVENTURE);
        NMS nms = ManageHandler.getNMS();
        gamePlayer.setItemStacks(nms.getInventoryContents(player));
        gamePlayer.setLevel(player.getExp());
        gamePlayer.setExp(player.getLevel());
        Utils.setupToGame(player);
    }

    public static void apply(GamePlayer gamePlayer, Player player) {
        NMS nms = ManageHandler.getNMS();
        nms.restoreInventory(player, gamePlayer.getItemStacks());
        player.setExp(gamePlayer.getLevel());
        player.setLevel(gamePlayer.getExp());
        gamePlayer.resetScoreBoard();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.resetPlayerTime();
    }

    public static void loadWorld(String paramString) {
        new WorldCreator(paramString).generator(new WorldGenerator()).createWorld();
    }

    public static boolean isInGame(UUID uuid) {
        return MinerPlugin.getARENA_REGISTRY().stream().anyMatch(x -> x.getPlayer(uuid) != null);
    }

    public static String sendMessage(Arena arena, Player player, String stringPath, Player consumer, boolean sendMessage) {
        String string = fileConfiguration.getString(stringPath);
        string = replacePlaceholders(arena, string);
        if (consumer != null)
            string = request(string.replace("<name>", consumer.getName()), consumer);
        if (sendMessage)
            player.sendMessage(string);
        return string;
    }

    public static void setupToGame(org.bukkit.entity.Player player) {
        player.getActivePotionEffects().forEach(x -> player.removePotionEffect(x.getType()));
        player.setFallDistance(0);
        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setHealth(20);
        player.resetPlayerTime();
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    public static void teleportToLobby(org.bukkit.entity.Player player) {
        player.setFallDistance(0);
        if(ManageHandler.notBungeeMode()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(ManageHandler.getLobbyLocation());
            return;
        }
        ServerManager.sendLobby(player);
    }

    private static boolean canSendTitle(String path) {
        return fileConfiguration.getBoolean("messages.arena." + path + ".title-show");
    }

    private static boolean canSendMessage(String path) {
        return fileConfiguration.getBoolean("messages.arena." + path + ".message-show");
    }

    public static void send(String path, org.bukkit.entity.Player player, String title, String message) {
        if (canSendMessage(path))
            player.sendMessage(message);
        if (canSendTitle(path))
            sendTitle(player, title, null, 10, 40, 10);
    }

    public static void sendTitle(org.bukkit.entity.Player player, String string){
        ManageHandler.getNMS().sendTitle(player, string, null, 10, 70, 20);
    }

    public static boolean notParsable(String input) {
        try {
            Integer.parseInt(input);
            return false;
        } catch (final NumberFormatException e) {
            return true;
        }
    }

    public static boolean isAlreadyCreating(String arenaName) {
        HashMap<UUID, Properties> arenaHashMap = PluginCommand.getArenaHashMap();
        return arenaHashMap.keySet().stream()
                .anyMatch(UUID -> arenaHashMap.get(UUID).getName().equals(arenaName));
    }

    public static void sendCountDown(org.bukkit.entity.Player paramPlayer, Arena arena) {
        if (!fileConfiguration.getBoolean("messages.arena.on-countdown.message-show"))
            return;
        String string = Objects.requireNonNull(fileConfiguration.getString("messages.arena.on-countdown.message"))
                .replace("<seconds>", Arena.COLORS[arena.getColour()] + "" + arena.getSeconds());
        paramPlayer.sendMessage(translate(string));
    }

    public static String replacePlaceholders(Arena arena, String string) {
        string = string
                .replace("<current_players>", arena.getPlayers().size() + "")
                .replace("<players>", arena.getProperties().getMaxPlayers() + "")
                .replace("<arena_name>", arena.getProperties().getName());
        switch (arena.getStage()) {
            case WAITING:
                string = translate(string.replace("<event>",
                        Objects.requireNonNull(fileConfiguration.getString("placeholders.waiting-players"))));
                break;
            case STARTING:
                string = translate(string.replace("<event>",
                        Objects.requireNonNull(fileConfiguration.getString("placeholders.starting-in"))).replace("<seconds>", "" + arena.getSeconds()));
                break;
            case PLAYING:
                string = translate(string.replace("<game>",
                        Objects.requireNonNull(Objects.requireNonNull(fileConfiguration.getString("placeholders.game"))
                        .replace("<current>", 16 - arena.getGames().size()+""))));
                break;
            case NEW_GAME_STARTING:
                string = translate(string.replace("<game>",
                        Objects.requireNonNull(Objects.requireNonNull(fileConfiguration.getString("placeholders.in-between"))
                        .replace("<current>", 16 - arena.getGames().size()+""))));
                break;
            case FINISHED:
                string = translate(string.replace("<game>",
                        Objects.requireNonNull(Objects.requireNonNull(fileConfiguration.getString("placeholders.finished")))));
                break;
        }
        return string;
    }

    public static String[] getLeaders(Arena arena) {
        List<GamePlayer> playerList = arena.getPlayers();
        playerList.sort(ManageHandler.SORT_MANAGER);
        int players = arena.getCurrentPlayers();
        String[] strings = players > 2 ? new String[3] : players == 1 ? new String[1] : new String[2];
        for (int count = 0; count < strings.length; count++) {
            strings[count] = Objects.requireNonNull(Bukkit.getPlayer(playerList.get(players - count - 1).getUUID())).getName();
        }
        return strings;
    }

    private static String[][] getWinners(Arena arena) {
        List<GamePlayer> playerList = arena.getPlayers();
        playerList.sort(ManageHandler.SORT_MANAGER);
        int a = arena.getCurrentPlayers() - 1;
        int maxPoints = playerList.get(a).getPoints();
        int finalMaxPoints = maxPoints;
        int count = Math.toIntExact(playerList.stream().filter(playerInGame -> playerInGame.getPoints() == finalMaxPoints).count());
        String[][] prizes = new String[3][];
        int current;
        for (int i = 0; i < 3; i++) {
            current = 0;
            prizes[i] = new String[count];
            for (int j = 0; j < count; j++) {
                GamePlayer player = playerList.get(a);
                String name = Objects.requireNonNull(Bukkit.getPlayer(player.getUUID())).getName();
                prizes[i][j] = name+":"+maxPoints;
                player.setPlace(i+1);
                current++;
                a -= 1;
                if(a == -1) {
                    return prizes;
                }
            }
            if(current > 1)
                i+=Math.max(1, current / 3 + 1);
            maxPoints = playerList.get(a).getPoints();
            int finalM = maxPoints;
            count = Math.toIntExact(playerList.stream().filter(playerInGame -> playerInGame.getPoints() == finalM).count());
        }
        return prizes;
    }

    public static String[] replaceWinners(Arena arena) {
        List<String> stringList = fileConfiguration.getStringList("game-finished.list");
        String[][] strings = getWinners(arena);
        for(int i = 0; i < strings.length; i++){
            String[] array = strings[i];
            String placeHolderName = "<"+(i+1)+"_place>";
            String placeHolderPoints = "<"+(i+1)+"_points>";
            if(array == null)
                stringList = deleteString(stringList, placeHolderName, placeHolderPoints);
        }
        return replaceWinners(stringList, strings);
    }

    public static boolean containsPlaceHolders(List<String> strings, String placeHolder){
        return strings.stream().anyMatch(s -> s.contains(placeHolder));
    }

    private static String[] replaceWinners(List<String> list, String[][] base){
        String[] strings = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            String string = list.get(i);
            for(int j = 0; j < 3; j++){
                string = replaceWinners(j+1, string, base[j]);
            }
            strings[i] = string;
        }
        return strings;
    }

    public static List<String> deleteString(List<String> strings, String placeHolder, String placeHolder2){
        return strings.stream().filter(s -> !s.contains(placeHolder)).filter(s -> !s.contains(placeHolder2)).collect(Collectors.toList());
    }

    private static String replaceWinners(int paramInt, String given, String... paramStrings){
        String placeHolderName = "<"+paramInt+"_place>";
        String placeHolderPoints = "<"+paramInt+"_points>";
        StringBuilder stringBuilder = new StringBuilder();
        if(paramStrings == null)
            return translate(given);
        if(paramStrings.length == 1)
            return translate(given.replace(placeHolderName, paramStrings[0].split(":")[0]).replace(placeHolderPoints, paramStrings[0].split(":")[1]));
        for (int i = 0; i < paramStrings.length; i++) {
            String paramString = paramStrings[i];
            stringBuilder.append(paramString.split(":")[0]);
            if(i < paramStrings.length - 1)
                stringBuilder.append(", ");
        }
        String string = given.replace(placeHolderName, stringBuilder.toString()).replace(placeHolderPoints, paramStrings[0].split(":")[1]);
        return translate(string);
    }

    public static void performWinEffect(org.bukkit.entity.Player paramPlayer) {
        if (!paramPlayer.isOnline())
            return;
        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE).flicker(true)
                .trail(true).withColor(Color.fromRGB(new Random().nextInt(256),
                        new Random().nextInt(256), new Random().nextInt(256))).build();
        paramPlayer.playSound(paramPlayer.getLocation(), Objects.requireNonNull(XSound.ENTITY_PLAYER_LEVELUP.parseSound()), 10, 1);
        Firework firework = (Firework) paramPlayer.getWorld().spawnEntity(paramPlayer.getLocation().add(0, 2, 0), EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.clearEffects();
        fireworkMeta.setPower(2);
        fireworkMeta.addEffect(effect);
        firework.setFireworkMeta(fireworkMeta);
    }

    public static Vector getVector(float yaw, int size) {
        Vector vector;
        switch (yawToFace(yaw, false)) {
            case WEST:
                vector = new Vector(size * -1, size, size);
                break;
            case EAST:
                vector = new Vector(size, size, size * -1);
                break;
            case SOUTH:
                vector = new Vector(size, size, size);
                break;
            default:
                vector = new Vector(size * -1, size, size * -1);
        }
        return vector;
    }

    public static boolean isNullable(String aString) {
        return Bukkit.getWorld(aString.split(":")[0]) == null;
    }

    public static final List<String> DEPRECATED_WORLDS = Arrays.asList("world", "world_nether", "world_the_end", "randomJoin");

    public static void performCommand(Player player, int place) {
        String path = place == 1 ? "winner" : place+"-place";
            for(String s : MinerPlugin.getInstance().getMessages()
                    .getStringList("game-finished.<p>.commands".replace("<p>", path))){
                if(s.isEmpty())
                    continue;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("<name>", player.getName()));
        }
    }

    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections)
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();
        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    public static boolean containsIllegalCharacters(String string) {
        List<Character> strings = Arrays.asList('`', '~', '!', '@', '#', '%', '^', '&', '*', '(', ')', '=', '?', '<', '>', '.', '¹', ';', '\'');
        char[] chars = string.toCharArray();
        for (char aChar : chars) {
            if (strings.contains(aChar))
                return true;
        }
        return false;
    }

    public static void sendTitle(org.bukkit.entity.Player player, String title, String subTitle, int fadeIn, int showTime, int fadeOut) {
        ManageHandler.getNMS().sendTitle(player, title, subTitle, fadeIn, showTime, fadeOut);
    }

    public static void sendMessage(org.bukkit.entity.Player player, String string) {
        StringBuilder space = new StringBuilder();
        if (string.contains("<center>") && string.length() < 58) {
            string = string.replace("<center>", "");
            int count = (50 - string.length()) / 2;
            space = new StringBuilder(" ");
            for (int i = 0; i < count; i++) {
                space.append(" ");
            }
        }
        player.sendMessage(space + string.replace("<center>", ""));
    }

    public static List<String> getStringList(FileConfiguration fileConfiguration, String path) {
        return fileConfiguration.getStringList(path);
    }

    public static String getString(FileConfiguration fileConfiguration, String path) {
        return fileConfiguration.getString(path);
    }

    private static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private static final BlockFace[] radial = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    public static Vector getToSquare(float yaw) {
        Vector vector;
        switch (yawToFace(yaw, false)) {
            case WEST:
                vector = new Vector(-1, 0, 0);
                break;
            case EAST:
                vector = new Vector(1, 0, 0);
                break;
            case SOUTH:
                vector = new Vector(0, 0, 1);
                break;
            default:
                vector = new Vector(0, 0, -1);
        }
        return vector;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getItem(Block block) {
        return ManageHandler.getNMS().isLegacy() ? block.getState().getData().toItemStack() : new ItemStack(block.getType());
    }

    public static float sin(float var0) {
        return math[(int)(var0 * 10430.378F) & '\uffff'];
    }

    public static float cos(float var0) {
        return math[(int)(var0 * 10430.378F + 16384.0F) & '\uffff'];
    }

    public static void showPlayers(org.bukkit.entity.Player player, List<GamePlayer> players) {
        for(GamePlayer gamePlayer : players){
            org.bukkit.entity.Player hidden = Bukkit.getPlayer(gamePlayer.getUUID());
            assert hidden != null;
            if(ManageHandler.getNMS().isLegacy()) {
                //noinspection deprecation
                player.showPlayer(hidden);
                //noinspection deprecation
                hidden.showPlayer(player);
                continue;
            }
            player.showPlayer(minerPlugin, hidden);
            hidden.showPlayer(minerPlugin, player);
        }
    }

    public static boolean clear(){
        return PluginEnable_Statistic.ENABLED && PlaceholderManager.getInstance().unregister();
    }

    public static String request(String s, OfflinePlayer player){
        return !PluginEnable_Statistic.ENABLED ? s : PlaceholderAPI.setPlaceholders(player, s);
    }

    public static void setVelocity(Player player, Player damager) {
        float p = 3.1415927F;
        float random = (new Random().nextInt(300 - 150) + 150f) / 1000f;
        float sprint = damager.isSprinting() ? 1F : 0.8F;
        float x = -sin(damager.getLocation().getYaw() * p / 180.0F) * 0.5F * sprint;
        float z = cos(damager.getLocation().getYaw() * p / 180.0F) * 0.5F * sprint;
        player.setVelocity(new Vector(x, random, z));
    }

    @SuppressWarnings("MalformedFormatString")
    public static String getSeconds(Object object) {
        return String.format("%.2f", object).replace(",", ".");
    }

    public static boolean isFluid(Block block) {
        int idBlock = ManageHandler.getNMS().getTypeId(block);
        Material materialBlock = block.getType();
        if (!ManageHandler.getNMS().isLegacy())
            return (materialBlock == Material.WATER || materialBlock == Material.LAVA);
        return (idBlock == 8 || idBlock == 9 || idBlock == 10 || idBlock == 11);
    }


}
