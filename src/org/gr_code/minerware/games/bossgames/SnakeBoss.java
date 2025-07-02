package org.gr_code.minerware.games.bossgames;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.BossGame;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.resources.Snake;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class SnakeBoss extends BossGame {

    private final HashMap<UUID, Snake> snakes = new HashMap<>();

    public SnakeBoss(Arena arena) {
        super(880, arena, "snake");
    }

    @Override
    public void secondStartGame() {
        String nameInventory = translate(getString("inventory-name"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        ItemStack arrow = requireNonNull(XMaterial.ARROW.parseItem());
        ItemStack arrowUP = ItemBuilder.start(arrow).setDisplayName(translate(getString("arrows.up"))).build();
        ItemStack arrowDOWN = ItemBuilder.start(arrow).setDisplayName(translate(getString("arrows.down"))).build();
        ItemStack arrowLEFT = ItemBuilder.start(arrow).setDisplayName(translate(getString("arrows.left"))).build();
        ItemStack arrowRIGHT = ItemBuilder.start(arrow).setDisplayName(translate(getString("arrows.right"))).build();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            clearInventory(player);
            player.getInventory().setItem(21, arrowLEFT);
            player.getInventory().setItem(23, arrowRIGHT);
            player.getInventory().setItem(13, arrowUP);
            player.getInventory().setItem(31, arrowDOWN);
            snakes.put(gamePlayer.getPlayer().getUniqueId(), new Snake(gamePlayer, nameInventory));
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (getTime() % 20 == 0) {
            snakes.values().forEach(Snake::update);
            if (getTime() % 60 == 0) for (Snake snake : snakes.values())
                if (snake.getCountApple() < 5) snake.generateApple();
        }
        if (getTime() == 5 && playerList.stream().anyMatch(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME)) setTime(700);
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != GamePlayer.State.PLAYING_GAME)) setTime(1);
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        GamePlayer[] players = new GamePlayer[3];
        int[] maximums = new int[3];
        for (GamePlayer gamePlayer : getArena().getPlayers()) {
            int current = snakes.get(gamePlayer.getPlayer().getUniqueId()).getSize();
            if (current > maximums[0]) {
                maximums[2] = maximums[1];
                players[2] = players[1];
                maximums[1] = maximums[0];
                players[1] = players[0];
                maximums[0] = current;
                players[0] = gamePlayer;
            } else if (current > maximums[1]) {
                maximums[2] = maximums[1];
                players[2] = players[1];
                maximums[1] = current;
                players[1] = gamePlayer;
            } else if (current > maximums[2]) {
                maximums[2] = current;
                players[2] = gamePlayer;
            }
        }
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer == null) continue;
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            onWin(gamePlayer.getPlayer(), false);
        }
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.BOSS_SNAKE;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.LIME_WOOL.parseItem())).setDisplayName("&4&lSNAKE").build();
    }

    @Override
    public void event(Event event) {
        if (event instanceof InventoryClickEvent) inventoryClick(event);
        else if (event instanceof InventoryCloseEvent) inventoryClose(event);
    }

    private void inventoryClose(Event event) {
        InventoryCloseEvent e = (InventoryCloseEvent) event;
        Player player = (Player) e.getPlayer();
        GamePlayer gamePlayer = getArena().getPlayer(player.getUniqueId());
        assert gamePlayer != null;
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME || e.getInventory() == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(e.getInventory());
            }
        }.runTaskLater(MinerPlugin.getInstance(), 1);
    }

    private void inventoryClick(Event event) {
        InventoryClickEvent e = (InventoryClickEvent) event;
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getType() != Material.ARROW) return;
        switch (e.getSlot()) {
            case 21:
                snakes.get(e.getWhoClicked().getUniqueId()).setDirection(Snake.SnakeDirection.LEFT);
                break;
            case 13:
                snakes.get(e.getWhoClicked().getUniqueId()).setDirection(Snake.SnakeDirection.UP);
                break;
            case 31:
                snakes.get(e.getWhoClicked().getUniqueId()).setDirection(Snake.SnakeDirection.DOWN);
                break;
            case 23:
                snakes.get(e.getWhoClicked().getUniqueId()).setDirection(Snake.SnakeDirection.RIGHT);
                break;
            default: return;
        }
        e.getWhoClicked().getInventory().setItem(e.getSlot(), XMaterial.RED_WOOL.parseItem());
        new BukkitRunnable() {
            @Override
            public void run() {
                e.getWhoClicked().getInventory().setItem(e.getSlot(), XMaterial.ARROW.parseItem());
            }
        }.runTaskLater(MinerPlugin.getInstance(), 10);
    }

}

