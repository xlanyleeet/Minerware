package org.gr_code.minerware.games.microgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class DontCrash extends MicroGame {

    private final static String[] wools = {
            "BLUE_WOOL", "BLACK_WOOL", "BROWN_WOOL", "CYAN_WOOL", "GRAY_WOOL", "GREEN_WOOL",
            "LIGHT_BLUE_WOOL", "LIGHT_GRAY_WOOL", "LIME_WOOL", "MAGENTA_WOOL", "ORANGE_WOOL",
            "PINK_WOOL", "PURPLE_WOOL", "RED_WOOL", "WHITE_WOOL", "YELLOW_WOOL"};

    public DontCrash(Arena arena) {
        super(680, arena, "dont-crash");
    }
    
    private void generateBuilding() {
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1)
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.WATER.parseItem()), l.getBlock()));
        cuboid.getLocations().stream()
                .filter(l -> l.getBlockY() == first.getBlockY() + 1 || l.getBlockY() == second.getBlockY() || l.getBlockY() == second.getBlockY() - 1)
                .filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
                        || l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() - 2)
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
    }

    @Override
    public void secondStartGame() {
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        generateBuilding();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.setFallDistance(0);
            player.teleport(getRandomLocation(getArena()).add(0, second.getBlockY() - first.getBlockY() - 2,0));
            player.setFallDistance(0);
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            int count = Math.min(getArena().getPlayers().indexOf(gamePlayer), wools.length - 1);
            gamePlayer.setTask(wools[count]);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    private void setBlocksHard(Player player, GamePlayer gamePlayer) {
        Cuboid cuboid = getArena().getProperties().getCuboid();
        ItemStack wool = requireNonNull(XMaterial.valueOf(gamePlayer.getTask()).parseItem());
        Location location = player.getLocation().add(-2, 0, -2);
        List<Location> locations = new ArrayList<>();
        for (int x = 0; x < 5; x ++) for (int z = 0; z < 5; z ++) locations.add(location.clone().add(x, 0, z));
        locations.stream().filter(l -> l.distance(player.getLocation()) <= 2)
                .filter(l -> !getItem(l.getBlock()).isSimilar(XMaterial.GLASS.parseItem()))
                .filter(l -> !cuboid.notInside(l))
                .forEach(l -> ManageHandler.getModernAPI().setBlock(wool, l.getBlock()));
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(player.getLocation().clone().add(0,1,0));
    }
    
    private void setBlocks(Player player, GamePlayer gamePlayer) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (getArena().isHardMode()) {
            setBlocksHard(player, gamePlayer);
            return;
        }
        ItemStack wool = requireNonNull(XMaterial.valueOf(gamePlayer.getTask()).parseItem());
        Block block1 = player.getLocation().clone().add(1,0,0).getBlock();
        Block block2 = player.getLocation().clone().add(-1,0,0).getBlock();
        Block block3 = player.getLocation().clone().add(0,0,1).getBlock();
        Block block4 = player.getLocation().clone().add(0,0,-1).getBlock();
        ManageHandler.getModernAPI().setBlock(wool, player.getLocation().getBlock());
        if (!getItem(block1).isSimilar(XMaterial.GLASS.parseItem())) ManageHandler.getModernAPI().setBlock(wool, block1);
        if (!getItem(block2).isSimilar(XMaterial.GLASS.parseItem())) ManageHandler.getModernAPI().setBlock(wool, block2);
        if (!getItem(block3).isSimilar(XMaterial.GLASS.parseItem())) ManageHandler.getModernAPI().setBlock(wool, block3);
        if (!getItem(block4).isSimilar(XMaterial.GLASS.parseItem())) ManageHandler.getModernAPI().setBlock(wool, block4);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(player.getLocation().clone().add(0,1,0));
    }

    private void destroyOrBuildRoof() {
        switch (getTime()) {
            case 500:
            case 400:
            case 300:
            case 200:
            case 100:
                setRoof(); break;
            case 550:
            case 450:
            case 350:
            case 250:
            case 150:
            case 50:
                destroyRoof(); break;
        }
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        destroyOrBuildRoof();
        List<GamePlayer> list = getArena().getPlayers();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        Cuboid cuboid = getArena().getProperties().getCuboid();
        if (list.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        list.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .filter(gamePlayer -> gamePlayer.getPlayer().getGameMode() != GameMode.SPECTATOR)
                .forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y == param_y + 2 && !cuboid.notInside(player.getLocation())) setBlocks(player, gamePlayer);
            if (y <= param_y) onLose(player, true);
        });
    }
    
    private void setRoof() {
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() - 2)
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() || l.getBlockY() == second.getBlockY() - 1)
                .filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
                        || l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (gamePlayer.getState() == State.PLAYING_GAME) {
                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(getRandomLocation(getArena()).add(0, second.getBlockY() - first.getBlockY() - 2, 0));
            } else if (!cuboid.notInside(player.getLocation()))
                player.teleport(getArena().getProperties().getLobbyLocationLoser().clone().add(0,1,0));
        });
    }
    
    private void destroyRoof() {
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() || l.getBlockY() == second.getBlockY() - 1)
                .filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
                        || l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ()).forEach(l -> l.getBlock().setType(Material.AIR));
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() - 2).forEach(l -> l.getBlock().setType(Material.AIR));
    }

    @Override
    public void end() {
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.setGameMode(GameMode.ADVENTURE);
            if (gamePlayer.getState() == State.PLAYING_GAME) onWin(player, false);
        });
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.DONT_CRASH;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.LEATHER_BOOTS.parseItem())).setDisplayName("&6&lDONT CRASH").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageEvent)) return;
        EntityDamageEvent e = (EntityDamageEvent) event;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
        onLose(player, true);
    }

}


