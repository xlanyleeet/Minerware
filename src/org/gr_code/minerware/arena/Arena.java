package org.gr_code.minerware.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.api.EventManager;
import org.gr_code.minerware.api.arena.IArena;
import org.gr_code.minerware.api.events.EventA;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.type.*;
import org.gr_code.minerware.manager.type.resources.XSound;
import org.gr_code.minerware.manager.type.resources.effects.WinEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.gr_code.minerware.manager.type.Utils.*;

public final class Arena implements IArena {

    protected enum Type {

        MICRO(12, 2, 4, 2, 4),

        MINI(16, 2, 4, 1, 8),

        DEFAULT(24, 2, 6, 3, 12),

        MEGA(29, 3, 7, 3, 16);

        Type(int sizeArena, int sizeSquares, int distanceSquares, int distanceCorners, int players) {
            this.sizeSquares = sizeSquares;
            this.sizeArena = sizeArena;
            this.distanceSquares = distanceSquares;
            this.distanceCorners = distanceCorners;
            this.players = players;
            this.cuboids = toString().equals("MICRO") ? 2 : 3;
        }

        private final int sizeArena, sizeSquares, distanceSquares, distanceCorners, players, cuboids;

        public int getDistanceSquares() {
            return distanceSquares;
        }

        public int getSizeArena() {
            return sizeArena;
        }

        public int getSizeSquares() {
            return sizeSquares;
        }

        public int getDistanceCorners() {
            return distanceCorners;
        }

        public int getPlayers() {
            return players;
        }

        public int getCuboids() {
            return cuboids;
        }

    }

    public static Type parseType(String string) {
        try {
            return Type.valueOf(string);
        } catch (Exception exception) {
            return Type.DEFAULT;
        }
    }

    public enum Stage {

        WAITING, PLAYING, STARTING, NEW_GAME_STARTING, FINISHED;

        public String getSignString() {
            return getObjectString(name().toLowerCase());
        }

        private static final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getBungee();

        private static String getObjectString(String path) {
            return translate(fileConfiguration.getString("game-stage." + path));
        }

    }

    /* |Boolean| */

    private boolean validateTimer(boolean isScoreBoard) {
        if (!isScoreBoard) {
            return countDown == 400 || countDown == 200 || (countDown % 20 == 0 && countDown <= 100)
                    && countDown > 0;
        }
        return countDown % 20 == 0;
    }

    private boolean canStartNewGame() {
        return gameLinkedList.size() > 0;
    }

    private boolean canStart() {
        tryUnload();
        return getCurrentPlayers() >= properties.getMinPlayers();
    }

    public boolean canJoin(UUID uuid) {
        return getCurrentPlayers() < properties.getMaxPlayers()
                && (!isStarted())
                && getPlayer(uuid) == null;
    }

    public boolean isEmpty() {
        return playerList.size() == 0;
    }

    public boolean canFinish() {
        return getCurrentPlayers() < properties.getMinPlayers();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Arena arena = (Arena) o;
        return toString().equals(arena.toString());
    }

    /* |Void| */

    private void decreaseCountDown() {
        countDown -= 1;
    }

    private void tryUnload() {
        if (unloaded)
            return;
        unload();
    }

    private void tryLoad() {
        if (!unloaded)
            return;
        load();
    }

    private void onCheckComplete() {
        playerList.forEach(x -> {
            Player player = Bukkit.getPlayer(x.getUUID());
            assert player != null;
            player.playSound(properties.getLobbyLocation(),
                    Objects.requireNonNull(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound()), 5, 1);
            if (MinerPlugin.getInstance().getMessages().getBoolean("messages.arena.on-countdown.title-show"))
                sendTitle(player, COLORS[getColour()] + "" + getSeconds(), null, 2, 15, 3);
            sendCountDown(player, this);
        });
    }

    public void generateLinkedList() {
        ArrayList<Game> games = Game.a(properties.getDisabledGames());
        gameLinkedList = Game.b(games, 16);
    }

    private void defaultCountDown() {
        countDown = 400;
    }

    private void setCountDown(int a) {
        countDown = a;
    }

    private void updateGame() {
        if (canFinish()) {
            sendWarningMessages();
            forceStopArena();
            return;
        }
        microGame.update();
    }

    private void updateScoreBoard() {
        if (getStage() == Stage.STARTING) {
            if (validateTimer(true))
                playerList.forEach(GamePlayer::update);
            return;
        }
        updateAllScoreBoardSync();
    }

    private void updateAllScoreBoardSync() {
        if (isEmpty())
            return;
        playerList.forEach(GamePlayer::update);
    }

    /* |PlayerEvent| */

    public void addPlayer(UUID uuid) {
        GamePlayer gamePlayer = new GamePlayer(uuid, this);
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        onAdd(player, gamePlayer);
    }

    private void sendActionMessage(UUID uuid, String path) {
        String message = SocketManager.decode(uuid.toString(), path.toUpperCase(), getName());
        SocketManager.sendMessage(message);
    }

    public void removePlayer(UUID uuid, boolean sendMessage) {
        GamePlayer gamePlayer = getPlayer(uuid);
        assert gamePlayer != null;
        Player player = Bukkit.getPlayer(gamePlayer.getUUID());
        onRemove(player, gamePlayer, sendMessage);
    }

    private void onAdd(Player player, GamePlayer gamePlayer) {
        tryLoad();
        LobbyHelper.removePlayer(player);
        Utils.apply(player, gamePlayer);
        player.teleport(Cuboid.getRandomLocation(this));
        playerList.add(gamePlayer);
        showPlayers(player, playerList);
        doTask();
        sendMessage(player, "join", true);
        player.getInventory().setItem(8, LEAVE_THE_ARENA);
        if (MinerPlugin.getInstance().getOptions().getBoolean("voting.enabled"))
            player.getInventory().setItem(0, VOTE);
        EventManager.craftArenaEvent(player, EventA.JOIN, this);
    }

    private void onRemove(Player player, GamePlayer gamePlayer, boolean sendMessage) {
        EventManager.craftArenaEvent(player, EventA.PREPARE_LEAVE, this);
        Utils.apply(gamePlayer, player);
        playerList.remove(gamePlayer);
        doTask();
        sendMessage(player, "leave", sendMessage);
        teleportToLobby(player);
        LobbyHelper.addPlayer(player);
        voting.remove(gamePlayer.getVote());
        EventManager.craftArenaEvent(player, EventA.LEAVE, this);
    }

    private void removeWithCommand() {
        for (GamePlayer gamePlayer : playerList) {
            Player player = Bukkit.getPlayer(gamePlayer.getUUID());
            EventManager.craftArenaEvent(player, EventA.PREPARE_LEAVE, this);
            assert player != null;
            Utils.apply(gamePlayer, player);
            int place = gamePlayer.getPlace();
            if (place <= 3 && place > 0) {
                Utils.performCommand(player, place);
                EventManager.craftArenaEvent(player, EventA.WIN_MATCH, this);
            }
            sendActionMessage(gamePlayer.getUUID(), "leave");
            teleportToLobby(player);
            LobbyHelper.addPlayer(player);
            EventManager.craftArenaEvent(player, EventA.LEAVE, this);
        }
        playerList.clear();
    }

    private void sendMessage(Player player, String path, boolean bool) {
        sendActionMessage(player.getUniqueId(), path);
        playerList.forEach(x -> {
            org.bukkit.entity.Player paramPlayer = Bukkit.getPlayer(x.getUUID());
            Utils.sendMessage(this, paramPlayer, "messages.arena.on-" + path, player, bool);
        });
    }

    /* |Start| */

    private void tryStart() {
        if (!canStart()) {
            failedStart();
            return;
        }
        if (validateTimer(false)) {
            onCheckComplete();
        }
        if (countDown == 0) {
            onStart();
            return;
        }
        decreaseCountDown();
    }

    private void failedStart() {
        if (!isEmpty())
            playerList.forEach(x -> {
                Player player = Bukkit.getPlayer(x.getUUID());
                sendPathMessage(player, "on-failed-start");
            });
        defaultCountDown();
        setStage(Stage.WAITING);
    }

    private void onStart() {
        Objects.requireNonNull(properties.getFirstLocation().getWorld()).setTime(1000);
        microGame = gameLinkedList.get(0).createGame(this);
        playerList.forEach(x -> {
            Player player = Bukkit.getPlayer(x.getUUID());
            assert player != null;
            player.getInventory().setItem(8, null);
            player.getInventory().setItem(0, null);
            player.playSound(properties.getLobbyLocation(),
                    Objects.requireNonNull(XSound.BLOCK_NOTE_BLOCK_PLING.parseSound()), 5, 1);
            sendPathMessage(player, "on-start");
            player.setFallDistance(0);
        });
        setCountDown(50);
        setStage(Stage.PLAYING);
        microGame.startGame();
    }

    /* |Updating| */

    public void updateArena() {
        switch (stage) {
            case WAITING:
                if (canStart())
                    setStage(Stage.STARTING);
                break;
            case STARTING:
                tryStart();
                break;
            case NEW_GAME_STARTING:
                tryChange();
                break;
            case PLAYING:
                updateGame();
                break;
            case FINISHED:
                doFinishTask();
                break;
        }
        updateScoreBoard();
    }

    /* |Changing| */

    private void tryChange() {
        if (!canStartNewGame()) {
            normalFinish();
            return;
        }
        if (canFinish()) {
            failedChange();
            return;
        }
        if (countDown == 0) {
            onChangeGame();
            return;
        }
        decreaseCountDown();
    }

    private void failedChange() {
        sendWarningMessages();
        forceStopArena();
    }

    private void onChangeGame() {
        microGame = gameLinkedList.get(0).createGame(this);
        if (!isEmpty())
            playerList.forEach(x -> {
                Player player = Bukkit.getPlayer(x.getUUID());
                assert player != null;
                sendPathMessage(player, "on-change-game");
                player.setFallDistance(0);
            });
        setStage(Stage.PLAYING);
        setCountDown(50);
        microGame.startGame();
    }

    /* |Finish| */

    private void stopEffect() {
        if (winEffect != null)
            winEffect.stop();
    }

    private void doFinishTask() {
        if (isEmpty()) {
            forceStopArena();
            return;
        }
        if (countDown == 0) {
            removeWithCommand();
            forceStopArena();
            return;
        }
        performWinEffect();
        decreaseCountDown();
    }

    private void finishPlayer(Player player) {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getMessages();
        if (properties.getCuboid().notInside(player.getLocation()))
            player.teleport(Cuboid.getRandomLocation(this));
        GamePlayer gamePlayer = getPlayer(player.getUniqueId());
        assert gamePlayer != null;
        if (gamePlayer.getPlace() == 1) {
            sendTitle(player, translate(fileConfiguration.getString("game-finished.winner.title")), null, 10, 100, 10);
            return;
        }
        sendTitle(player, translate(fileConfiguration.getString("game-finished.title")), null, 10, 100, 10);
        Utils.performWinEffect(player);
    }

    private void normalFinish() {
        setCountDown(325);
        properties.doRestore();
        gameLinkedList.clear();
        sendWinners();
        playerList.forEach(playerInGame -> {
            finishPlayer(Objects.requireNonNull(Bukkit.getPlayer(playerInGame.getUUID())));
        });
        setStage(Stage.FINISHED);
        if (isEnabledWinEffect) {
            int randomInt = new Random().nextInt(WinEffect.Type.values().length);
            winEffect = WinEffect.Type.values()[randomInt].getInstance(this, 300);
        }
        EventManager.craftArenaEvent(null, EventA.FINISH_MATCH, this);
    }

    private void performWinEffect() {
        if (!isEnabledWinEffect)
            return;
        winEffect.update();
    }

    private void sendWinners() {
        playerList.forEach(playerInGame -> {
            Player player = Bukkit.getPlayer(playerInGame.getUUID());
            assert player != null;
            for (String prizeString : Utils.replaceWinners(this)) {
                Utils.sendMessage(player, prizeString);
            }
        });
    }

    /* |Utils| */

    public void forceStopArena() {
        forceStopGame();
        stopEffect();
        for (GamePlayer player : playerList) {
            removePlayer(player.getUUID(), false);
        }
        defaultCountDown();
        properties.doRestore();
        setStage(Stage.WAITING);
        voting.reset();
    }

    public boolean forceStartArena() {
        if (!(getCurrentPlayers() >= properties.getMinPlayers() && getStage().equals(Stage.WAITING)
                || getStage().equals(Stage.STARTING)))
            return false;
        generateLinkedList();
        onStart();
        return true;
    }

    private void sendPathMessage(Player player, String path) {
        String string = "messages.arena." + path + ".";
        String title = Utils.sendMessage(this, player, string + "title", null, false);
        String message = Utils.sendMessage(this, player, string + "message", null, false);
        Utils.send(path, player, title, message);
    }

    public void sendWarningMessages() {
        for (GamePlayer gamePlayer : playerList)
            sendPathMessage(Bukkit.getPlayer(gamePlayer.getUUID()), "on-failed-change");
    }

    private void forceStopGame() {
        if (microGame != null)
            microGame.aFinish(true);
    }

    private void deleteGame() {
        gameLinkedList.remove(0);
    }

    private void doTask() {
        SignManager.update();
        StatisticManager.update();
        SocketManager.sendArenaUpdate(this);
    }

    /* |Performance| */

    @Override
    public String toString() {
        return "Arena{name=" + getName() + ",unloaded=" + unloaded + ",stage=" + getStage().name() + "}";
    }

    private void load() {
        unloaded = false;
    }

    private void unload() {
        unloaded = true;
        properties.unloadChunks();
    }

    /* |Class| */

    public static final ChatColor[] COLORS = new ChatColor[] { ChatColor.GREEN, ChatColor.GREEN, ChatColor.YELLOW,
            ChatColor.GOLD, ChatColor.RED };

    private final Properties properties;

    private int countDown = 400;

    private Stage stage;

    private WinEffect winEffect;

    protected MicroGame microGame;

    private final List<GamePlayer> playerList;

    protected LinkedList<Game> gameLinkedList;

    protected boolean unloaded;

    private final Voting voting;

    private final boolean isEnabledWinEffect;

    public Arena(Properties properties) {
        this.properties = properties;
        Objects.requireNonNull(properties.getFirstLocation().getWorld()).setAutoSave(false);
        playerList = new CopyOnWriteArrayList<>();
        stage = Stage.WAITING;
        voting = new Voting();
        isEnabledWinEffect = MinerPlugin.getInstance().getMessages().getBoolean("game-finished.enable-win-effects");
        unload();
    }

    @NotNull
    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        switch (stage) {
            case NEW_GAME_STARTING:
                deleteGame();
                properties.doRestore();
                break;
            case STARTING:
                generateLinkedList();
                break;
        }
        doTask();
    }

    @NotNull
    public List<GamePlayer> getPlayers() {
        return playerList;
    }

    @Nullable
    public GamePlayer getPlayer(UUID uuid) {
        return playerList.stream().filter(x -> x.getUUID().equals(uuid)).findAny().orElse(null);
    }

    public int getCurrentPlayers() {
        return playerList.size();
    }

    public int getSeconds() {
        return countDown / 20;
    }

    @Override
    public int getGamesRemaining() {
        return gameLinkedList.size();
    }

    public int getColour() {
        return Math.abs((getSeconds() - 5) % 5);
    }

    public void setAbstractGame(MicroGame microGame) {
        this.microGame = microGame;
    }

    public int getRequiredPlayers() {
        int players = properties.getMinPlayers() - getCurrentPlayers();
        return Math.max(players, 0);
    }

    @Nullable
    public WinEffect.Type getCurrentWinEffect() {
        if (winEffect == null)
            return null;
        return winEffect.getType();
    }

    @NotNull
    public LinkedList<Game> getGames() {
        return gameLinkedList;
    }

    @NotNull
    public String getName() {
        return properties.getName();
    }

    @NotNull
    public Properties getProperties() {
        return properties;
    }

    @Nullable
    public MicroGame getMicroGame() {
        return microGame;
    }

    @NotNull
    public Voting getVotingSession() {
        return voting;
    }

    public boolean isStarted() {
        return stage.equals(Stage.PLAYING) || stage.equals(Stage.NEW_GAME_STARTING) || stage.equals(Stage.FINISHED);
    }

    public boolean isHardMode() {
        return voting.getHard() > voting.getNormal();
    }

}
