package org.gr_code.minerware.games.microgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class GrabAFlower extends MicroGame {
    
    private final HashMap<String, GamePlayer> achievement;
    private final List<String> flowers;
    private double firstTime;
    private String rightFlower;

    public GrabAFlower(Arena arena) {
        super(280, arena, "grab-flower");
        achievement = new HashMap<>();
        flowers = getStringList("flowers");
    }

    @Override
    public void onLose(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
        String attempt = translate(getString("messages.attempts-ended"));
        sendMessage(player, attempt);
        super.onLose(player, teleport);
    }

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
        String win = translate(getString("messages.grabbed-right-flower"));
        sendMessage(player, win);
        player.getInventory().addItem(ItemBuilder.start(requireNonNull(XMaterial.valueOf(rightFlower.split(":")[0]).parseItem()))
                .setDisplayName(rightFlower.split(":")[1]).build());
        player.getInventory().setHeldItemSlot(0);
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        double secondAch = Double.parseDouble(getSeconds(currentTime));
        if (secondAch - firstTime >= 0) achievement.put(Double.toString(secondAch - firstTime), gamePlayer);
        super.onWin(player, teleport);
    }

    @Override
    public void startGame() {
        rightFlower = flowers.get(new Random().nextInt(flowers.size()));
        super.startGame();
    }

    @Override
    public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        HashMap<String, GamePlayer> newHash = new HashMap<>();
        achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
                .forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
        if (newHash.isEmpty()) return "";
        double maximum = 10000;
        GamePlayer gamePlayer = null;
        for (String key : newHash.keySet()) {
            double doubleKey = Double.parseDouble(key);
            if (doubleKey >= maximum) continue;
            gamePlayer = newHash.get(key);
            maximum = doubleKey;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<flower>", rightFlower.split(":")[1])
                .replace("<seconds>", getSeconds(maximum));
    }

    private void generateFlowers() {
        Location first = getArena().getProperties().getFirstLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1).forEach(l -> {
            String stringFlower = flowers.get(new Random().nextInt(flowers.size())).split(":")[0];
            ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.valueOf(stringFlower).parseItem()), l.getBlock());
        });
    }

    private void generateArena() {
        Location first = getArena().getProperties().getFirstLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.GRASS_BLOCK.parseItem()), l.getBlock()));
        generateFlowers();
    }

    @Override
    public void secondStartGame() {
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        firstTime = Double.parseDouble(getSeconds(currentTime));
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(requireNonNull(getString("titles.task"))
                .replace("<flower>", rightFlower.split(":")[1]));
        generateArena();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            gamePlayer.setTask("0");
            player.setGameMode(GameMode.SURVIVAL);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        int count  = getArena().isHardMode() ? 20 : 30;
        if (getTime() % count == 0) generateFlowers();
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        Location first = getArena().getProperties().getFirstLocation();
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1)
                .forEach(l -> l.getBlock().setType(Material.AIR));
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.GRAB_A_FLOWER;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(requireNonNull(getString("titles.task"))
                .replace("<flower>", rightFlower.split(":")[1]));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.DANDELION.parseItem())).setDisplayName("&3&lGRAB A FLOWER").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof BlockBreakEvent)) return;
        BlockBreakEvent e = (BlockBreakEvent) event;
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
        ItemStack ItemStack = Utils.getItem(e.getBlock());
        gamePlayer.setTask((Integer.parseInt(gamePlayer.getTask()) + 1) + "");
        ItemStack rightItem = XMaterial.valueOf(rightFlower.split(":")[0]).parseItem();
        if (!ItemStack.isSimilar(rightItem)) {
            String attempt = translate(getString("messages.attempt"));
            if (getArena().isHardMode()) {
                onLose(player, false);
                return;
            }
            if (gamePlayer.getTask().equals("5")) onLose(player, false);
            else sendMessage(player, attempt.replace("<number>", gamePlayer.getTask()));
            return;
        }
        e.setCancelled(false);
        onWin(player, false);
    }

}