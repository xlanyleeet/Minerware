package org.gr_code.minerware.games;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public abstract class MicroGame {

    private int time;
    private final int gameDuration;
    private final Arena arena;
    private final String config;
    private FileConfiguration fileConfig;
    private final Timer timer;
    private boolean defaultUsageTimer = false;
    private final DetailedPlayer showPlayer;
    private final DetailedPlayer hidePlayer;

    @SuppressWarnings("deprecation")
    public MicroGame(int time, Arena arena, String config) {
        this.time = time;
        this.config = config;
        gameDuration = time;
        this.arena = arena;
        fileConfig = MinerPlugin.getInstance().getGames();
        if (ManageHandler.getNMS().isLegacy()) {
            showPlayer = Player::showPlayer;
            hidePlayer = Player::hidePlayer;
        } else {
            showPlayer = (Player player, Player target) -> player.showPlayer(MinerPlugin.getInstance(), target);
            hidePlayer = (Player player, Player target) -> player.hidePlayer(MinerPlugin.getInstance(), target);
        }
        if (fileConfig.getString(config + ".timer.enabled") == null) {
            timer = null;
            return;
        }
        if (fileConfig.getBoolean(config + ".timer.enabled")) {
            defaultUsageTimer = true;
            if (getString("timer.type").equals("level"))
                timer = ((player, time1, nowTime) -> player.setLevel(nowTime / 20));
            else if (getString("timer.type").equals("experience"))
                timer = ((player, time1, nowTime) -> player.setExp((float) nowTime / (float) time1));
            else
                timer = ((player, time1, nowTime) -> {
                    player.setLevel(nowTime / 20);
                    player.setExp((float) nowTime / (float) time1);
                });
        } else timer = null;
    }

    public MicroGame(int time, Arena arena, String config, boolean isBoss) {
        this(time, arena, config);
        if (isBoss) fileConfig = MinerPlugin.getInstance().getBossGames();
    }

    public MicroGame(int time, Arena arena, String config, boolean isBoss, boolean useDefaultTimer) {
        this(time, arena, config, isBoss);
        defaultUsageTimer = useDefaultTimer;
    }

    public String getAchievementForMsg() {
        return "";
    }

    public String getWhoWon() {
        int players = arena.getCurrentPlayers();
        List<GamePlayer> winners = arena.getPlayers().stream()
                .filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.WINNER_IN_GAME)
                .collect(Collectors.toList());
        if (players == winners.size()) return (getString("messages.who-won.all-won"));
        String firstPlace, secondPlace, thirdPlace;
        switch (winners.size()) {
            case 3:
                firstPlace = winners.get(0).getPlayer().getName();
                secondPlace = winners.get(1).getPlayer().getName();
                thirdPlace = winners.get(2).getPlayer().getName();
                return (requireNonNull(getString("messages.who-won.winners")))
                        .replace("<list_winners>", firstPlace + ", " + secondPlace + ", " + thirdPlace);
            case 2:
                firstPlace = winners.get(0).getPlayer().getName();
                secondPlace = winners.get(1).getPlayer().getName();
                return (requireNonNull(getString("messages.who-won.winners")))
                        .replace("<list_winners>", firstPlace + ", " + secondPlace);
            case 1:
                return (requireNonNull(getString("messages.who-won.one-winner")))
                        .replace("<winner>", winners.get(0).getPlayer().getName());
            case 0:
                return (requireNonNull(getString("messages.who-won.all-lost")));
            default:
                return (requireNonNull(getString("messages.who-won.winners-more-then-three")))
                        .replace("<winners>", Integer.toString(winners.size()))
                        .replace("<players>", Integer.toString(players));
        }
    }

    public void printEndMessage() {
        String whoWon = getWhoWon(), achievement = getAchievementForMsg();
        List<String> endMsg = new ArrayList<>();
        for (String msg : getStringList("messages.end-message")) {
            if (msg.equals("<achievement>") && achievement.equals("")) continue;
            endMsg.add(msg.replace("<whoWon>", whoWon).replace("<achievement>", achievement));
        }
        String winMessage = translate(getString("messages.on-win"));
        String loseMessage = translate(getString("messages.on-lose"));
        String winTitle = translate(getString("titles.on-win"));
        String loseTitle = translate(getString("titles.on-lose"));
        arena.getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            boolean isWinner = gamePlayer.getState() == GamePlayer.State.WINNER_IN_GAME;
            String winOrLose = isWinner ? winMessage : loseMessage;
            String title = isWinner ? winTitle : loseTitle;
            endMsg.forEach(msg -> sendMessage(player, translate(msg.replace("<winOrLose>", requireNonNull(winOrLose)))));
            sendTitle(player, title);
        });
    }

    public void startGame() {
        Cuboid cuboid = getArena().getProperties().getCuboid();
        arena.getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            setupToGame(player);
            player.setGameMode(GameMode.ADVENTURE);
            if (cuboid.notInside(player.getLocation()))
                player.teleport(getRandomLocation(getArena()));
        });
    }

    public final void update() {
        if (time == 0) end();
        else if (time < gameDuration - 21 && time > gameDuration - 80) countdown();
        else if (time == gameDuration - 80) secondStartGame();
        else if (time < gameDuration - 80 && time > 0) {
            if (defaultUsageTimer && getTime() % 20 == 0) getArena().getPlayers()
                    .forEach(gamePlayer -> timer.setTimer(gamePlayer.getPlayer(), gameDuration-80, getTime()));
            check();
        }
        time --;
    }

    public void end() {
        printEndMessage();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        arena.getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.setGameMode(GameMode.ADVENTURE);
            gamePlayer.reset();
            setupToGame(player);
            clearInventory(player);
            player.closeInventory();
            for (GamePlayer target : getArena().getPlayers()) {
                if (target.equals(gamePlayer)) continue;
                showPlayer().call(player, target.getPlayer());
            }
            if (cuboid.notInside(player.getLocation()))
                player.teleport(getRandomLocation(getArena()));
            if (!ManageHandler.getNMS().oldVersion()) {
                requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(4);
                player.saveData();
                player.setCollidable(true);
            }
        });
        deleteAllBuildings();
        removeAllEntitiesInCuboid();
        arena.setStage(Arena.Stage.NEW_GAME_STARTING);
    }


    public final void deleteAllBuildings() {
        Location first = getArena().getProperties().getFirstLocation();
        List<Location> squares = new ArrayList<>();
        for (Properties.Square square : getArena().getProperties().getSquares())
            squares.addAll(square.getLocations());
        getArena().getProperties().getCuboid().getLocations().stream()
                .filter(location -> location.getBlockY() != first.getBlockY())
                .filter(location -> location.getBlock().getType() != Material.AIR)
                .filter(location -> !contains(squares, location))
                .forEach(location -> location.getBlock().setType(Material.AIR));
    }

    public final void removeAllEntitiesInCuboid() {
        Location first = getArena().getProperties().getFirstLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        requireNonNull(first.getWorld()).getEntities().stream().filter(entity -> !(entity instanceof Player))
                .filter(entity -> !cuboid.notInside(entity.getLocation())).forEach(Entity::remove);
    }

    private boolean contains(List<Location> listLocations, Location location) {
        for (Location loc : listLocations) {
            boolean equalsX = loc.getBlockX() == location.getBlockX();
            boolean equalsY = loc.getBlockY() == location.getBlockY();
            boolean equalsZ = loc.getBlockZ() == location.getBlockZ();
            if (equalsX && equalsY && equalsZ) return true;
        }
        return false;
    }

    public void aFinish(boolean forceStop) {
        deleteAllBuildings();
        removeAllEntitiesInCuboid();
        arena.getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            gamePlayer.reset();
            setupToGame(player);
            player.closeInventory();
            clearInventory(player);
            if (!ManageHandler.getNMS().oldVersion()) {
                requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(4);
                player.saveData();
                player.setCollidable(true);
            }
        });
    }

    public void countdown() {
        //OBFUSCATED PRIMERO
        ChatColor[] IIi = new ChatColor[] {null, ChatColor.RED, ChatColor.GOLD, ChatColor.GREEN};
        String[] iII = {"", "&4&k|", "&4&k||", "&4&k|||"};
        int iiI = (time - (gameDuration - 100)) / 20;
        String Iii = IIi[iiI] + Integer.toString(iiI);
        int iIi = time - (gameDuration - 100) - (iiI * 20);
        if (!(iIi == 19 || iIi == 13 || iIi == 7 || iIi == 1)) return;
        Sound III = requireNonNull(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound());
        int ii = iIi / 6;
        arena.getPlayers().stream().filter(gamePlayer -> !getTask(gamePlayer).equals("")).forEach(gamePlayer -> {
            Player iIiI = gamePlayer.getPlayer();
            if (iIi == 19) iIiI.playSound(iIiI.getLocation(), III, 5, 1);
            sendTitle(iIiI, translate(iII[ii]+Iii+iII[ii]), translate(getTask(gamePlayer)), 0, 70, 20);
        });
    }

    public abstract void check();

    public abstract void secondStartGame();

    public void onLose(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(arena.getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        player.playSound(player.getLocation(), requireNonNull(XSound.BLOCK_ANVIL_PLACE.parseSound()), 5, 12);
        gamePlayer.setState(GamePlayer.State.LOSER_IN_GAME);
        setupToGame(player);
        player.closeInventory();
        if (!teleport) return;
        clearInventory(player);
        player.teleport(getArena().getProperties().getLobbyLocationLoser().clone().add(0,1,0));
    }

    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(arena.getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        gamePlayer.addPoints(1);
        player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_PLAYER_LEVELUP.parseSound()), 5, 12);
        gamePlayer.setState(GamePlayer.State.WINNER_IN_GAME);
        setupToGame(player);
        player.closeInventory();
        if (!teleport) return;
        clearInventory(player);
        player.teleport(getArena().getProperties().getLobbyLocationWinner().clone().add(0,1,0));
    }

    public final String getString(String path) {
        return fileConfig.getString(config + "." + path);
    }

    public final List<String> getStringList(String path) {
        return fileConfig.getStringList(config + "." + path);
    }

    public final ConfigurationSection getSection(String path) {
        return fileConfig.getConfigurationSection(config + "." + path);
    }

    public void event(Event event) {

    }

    public final Arena getArena() {
        return arena;
    }

    public final void setTime(int time) {
        this.time = time;
    }

    public final int getTime() {
        return time;
    }

    public String getName() {
        return getString("messages.name");
    }

    public abstract ItemStack getGameItemStack();

    public abstract Game getGame();

    public abstract String getTask(GamePlayer gamePlayer);

    public final void clearInventory(Player player) {
        for (int slot = 0; slot < 9; slot ++)
            player.getInventory().setItem(slot, null);
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().clear();
    }

    public Timer getTimer() {
        return timer;
    }

    public DetailedPlayer showPlayer() {
        return showPlayer;
    }

    public DetailedPlayer hidePlayer() {
        return hidePlayer;
    }

    public interface Timer {
        void setTimer(Player player, int time, int nowTime);
    }

    public interface DetailedPlayer {
        void call(Player player, Player target);
    }

}
