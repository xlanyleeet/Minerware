package org.gr_code.minerware.games.bossgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.BossGame;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.listeners.game.ProjectileHit_Games;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class ColouredFloorBoss extends BossGame {
    
    private final static String[] terracotta = {"ORANGE_TERRACOTTA", "MAGENTA_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA",
            "YELLOW_TERRACOTTA", "LIME_TERRACOTTA", "PINK_TERRACOTTA", "GRAY_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA",
            "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "GREEN_TERRACOTTA",
            "RED_TERRACOTTA", "BLACK_TERRACOTTA", "WHITE_TERRACOTTA"};

    public ColouredFloorBoss(Arena arena) {
        super(1280, arena, "coloured-floor");
    }
    
    private void generatePlatform() {
        getArena().getProperties().destroySquares();
        Location first = getArena().getProperties().getFirstLocation();
        String floorM = getArena().getProperties().getType().equals("MEGA") ? "QUARTZ_BLOCK" : "WHITE_TERRACOTTA";
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.valueOf(floorM).parseItem()), l.getBlock()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void secondStartGame() { 
        generatePlatform();
        int count = getArena().isHardMode() ? 5 : 2;
        ItemStack hoe = ItemBuilder.start(requireNonNull(XMaterial.DIAMOND_HOE.parseItem())).setUnbreakable(true).build();
        ItemStack potion = ManageHandler.getNMS().oldVersion() ? new ItemStack(Material.POTION, count, (short)16384) :
                ItemBuilder.start(requireNonNull(XMaterial.SPLASH_POTION.parseItem())).setAmount(count).build();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            int i = getArena().getPlayers().indexOf(gamePlayer);
            Player player = gamePlayer.getPlayer();
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setHeldItemSlot(0);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            if (cuboid.notInside(player.getLocation())) player.teleport(Cuboid.getRandomLocation(getArena()));
            gamePlayer.setTask("0");
            player.getInventory().setItem(0, hoe);
            player.getInventory().setHeldItemSlot(0);
            player.getInventory().setItem(1, potion);
            gamePlayer.setAchievement(terracotta[i]);
        });
    }

    @Override
    public void check() {
        if (getTime() % 10 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        if (getTime() % 20 == 0) playerList.forEach(x -> x.getPlayer().setLevel(getTime() / 20));
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        Player[] players = new Player[3];
        int[] maximums = new int[3];
        for (GamePlayer gamePlayer : getArena().getPlayers()) {
            int current = Integer.parseInt(gamePlayer.getTask());
            if (current > maximums[0]) {
                maximums[2] = maximums[1];
                players[2] = players[1];
                maximums[1] = maximums[0];
                players[1] = players[0];
                maximums[0] = current;
                players[0] = gamePlayer.getPlayer();
            } else if (current > maximums[1]) {
                maximums[2] = maximums[1];
                players[2] = players[1];
                maximums[1] = current;
                players[1] = gamePlayer.getPlayer();
            } else if (current > maximums[2]) {
                maximums[2] = current;
                players[2] = gamePlayer.getPlayer();
            }
        }
        for (Player player : players) onWin(player, false);
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.BOSS_COLOURED_FLOOR;
    }


    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.PINK_TERRACOTTA.parseItem())).setDisplayName("&9&lCOLOURED FLOOR").build();
    }

    @Override
    public void event(Event event) {
        if (event instanceof PlayerInteractEvent) playerInteract(event);
        else if (event instanceof ProjectileHitEvent) projectileHit(event);
    }

    private void playerInteract(Event event) {
        PlayerInteractEvent e = (PlayerInteractEvent) event;
        Player player = e.getPlayer();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != State.PLAYING_GAME) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) return;
        if (!e.getItem().isSimilar(ItemBuilder.start(requireNonNull(XMaterial.DIAMOND_HOE.parseItem())).setUnbreakable(true).build())) return;
        String material = getArena().getProperties().getType().equals("MEGA") ? "QUARTZ_BLOCK" : "WHITE_TERRACOTTA";
        if (!Utils.getItem(e.getClickedBlock()).isSimilar(XMaterial.valueOf(material).parseItem())) return;
        ItemStack plMaterial = XMaterial.valueOf(gamePlayer.getAchievement()).parseItem();
        if (gamePlayer.getTask().equals("0")) {
            ManageHandler.getNMS().setBlock(requireNonNull(plMaterial), requireNonNull(e.getClickedBlock()));
            gamePlayer.setTask((Integer.parseInt(gamePlayer.getTask()) + 1) + "");
        }
        ItemStack firstB = Utils.getItem(requireNonNull(e.getClickedBlock()).getLocation().clone().add(1,0,0).getBlock());
        ItemStack secondB = Utils.getItem(e.getClickedBlock().getLocation().clone().add(-1,0,0).getBlock());
        ItemStack thirdB = Utils.getItem(e.getClickedBlock().getLocation().clone().add(0,0,1).getBlock());
        ItemStack fourthB = Utils.getItem(e.getClickedBlock().getLocation().clone().add(0,0,-1).getBlock());
        Sound sound = XSound.ENCHANT_THORNS_HIT.parseSound();
        if (!firstB.isSimilar(plMaterial) && !secondB.isSimilar(plMaterial) && !thirdB.isSimilar(plMaterial) && !fourthB.isSimilar(plMaterial)) {
            if (sound != null) player.playSound(player.getLocation(), sound, 5, 1);
            return;
        }
        gamePlayer.setTask((Integer.parseInt(gamePlayer.getTask()) + 1) + "");
        ManageHandler.getNMS().setBlock(requireNonNull(plMaterial), e.getClickedBlock());
        player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ITEM_PICKUP.parseSound()), 5, 1);
    }

    private void projectileHit(Event event) {
        ProjectileHitEvent e = (ProjectileHitEvent) event;
        Block hitBlock = requireNonNull(ProjectileHit_Games.getHitBlockNMS(e));
        if (!(e.getEntity().getShooter() instanceof org.bukkit.entity.Player)) return;
        Player player = (org.bukkit.entity.Player) e.getEntity().getShooter();
        UUID uuid = player.getUniqueId();
        if (!Utils.isInGame(uuid)) return;
        GamePlayer x = requireNonNull(getArena().getPlayer(uuid));
        if (x.getState() != State.PLAYING_GAME) return;
        ItemStack material = requireNonNull(XMaterial.valueOf(x.getAchievement()).parseItem());
        int[][] point = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        Location[] locations = new Location[4];
        for (int i = 0; i < locations.length; i ++)
            locations[i] = hitBlock.getLocation().clone().add(point[i][0], 0, point[i][1]);
        Cuboid cuboid = getArena().getProperties().getCuboid();
        for (Location location : locations) {
            if (cuboid.notInside(location)) continue;
            ManageHandler.getNMS().setBlock(material, location.getBlock());
        }
        ManageHandler.getNMS().setBlock(material, hitBlock);
        x.setTask((Integer.parseInt(x.getTask()) + 5) + "");
    }

}