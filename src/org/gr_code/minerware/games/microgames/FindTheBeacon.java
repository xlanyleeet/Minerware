package org.gr_code.minerware.games.microgames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class FindTheBeacon extends MicroGame {

    private List<Location> blocks;
    private final Location[] spawnLocation;

    public FindTheBeacon(Arena arena) {
        super(580, arena, "find-the-beacon");
        spawnLocation = new Location[2];
    }

    @Override
    public void startGame() {
        generateLocations();
        super.startGame();
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != GamePlayer.State.PLAYING_GAME)) setTime(1);
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (player.getLocation().add(0,-1,0).getBlock().getType() == Material.BEACON) onWin(player, true);
            if (y <= param_y) onLose(player, true);
        });
    }

    private List<Location> getRandomLocations() {
        List<Location> list = new ArrayList<>();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        Location centre = cuboid.getCenter().add(0, 1, 0).getBlock().getLocation();
        List<Location> firstRandomLocations = cuboid.getLocations().stream().filter(l -> l.getBlockY() == centre.getBlockY()).filter(l -> l.distance(centre) < 5).collect(Collectors.toList());
        List<Location> randomLocations = cuboid.getLocations().stream().filter(l -> l.getBlockY() == centre.getBlockY()).filter(l -> l.distance(centre) > 6).collect(Collectors.toList());
        list.add(firstRandomLocations.get(new Random().nextInt(firstRandomLocations.size())));
        for (int i = 0; i < 3; i ++) {
            if (randomLocations.size() == 0) {
                list.add(getRandomLocation(getArena()).add(0,2,0));
                continue;
            }
            Location location = randomLocations.get(new Random().nextInt(randomLocations.size()));
            list.add(location);
            randomLocations.removeAll(randomLocations.stream().filter(l -> l.distance(location) <= 4).collect(Collectors.toList()));
        }
        list.add(list.get(0).clone().add(1, 0, 0));
        list.add(list.get(0).clone().add(-1, 0, 0));
        list.add(list.get(0).clone().add(0, 0, 1));
        list.add(list.get(0).clone().add(0, 0, -1));
        return list;
    }

    private void generateLocations() {
        blocks = getRandomLocations();
        new BukkitRunnable() {
            @Override
            public void run() {
                Location location;
                int random = new Random().nextInt(3) + 1;
                for (int i = 1; i < 4; i++) {
                    Vector vector = blocks.get(0).clone().subtract(blocks.get(i).clone()).toVector().normalize();
                    double x = vector.getX(), z = vector.getZ();
                    location = blocks.get(i).clone();
                    if (i == random) {
                        spawnLocation[0] = location.clone();
                        spawnLocation[1] = location.clone().add(vector);
                    }
                    blocks.add(location.clone().add(vector.clone().setX(-z).setZ(x).clone().multiply(0.75)));
                    blocks.add(location.clone().add(vector.clone().setX(x).setZ(-z).clone().multiply(0.75)));
                    while (location.distance(blocks.get(0).clone()) > 1) {
                        location.add(vector);
                        blocks.add(location.clone());
                        blocks.add(location.clone().add(vector.clone().setX(-z).setZ(x).clone().multiply(0.75)));
                        blocks.add(location.clone().add(vector.clone().setX(x).setZ(-z).clone().multiply(0.75)));
                    }
                }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    public void createDoors() {
        blocks.forEach(ManageHandler.getNMS().getGeneratorDoors());
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getProperties().destroySquares();
        Location first = getArena().getProperties().getFirstLocation();
        List<Location> beaconLocations = blocks.stream().filter(l -> l.distance(spawnLocation[0]) > 8).collect(Collectors.toList());
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY()).forEach(l -> l.getBlock().setType(Material.AIR));
        if (beaconLocations.size() == 0) beaconLocations = blocks.stream().filter(l -> l.distance(spawnLocation[0]) > 4).collect(Collectors.toList());
        Location beaconLocation = beaconLocations.get(new Random().nextInt(beaconLocations.size()));
        beaconLocation.getBlock().setType(Material.BEACON); beaconLocation.add(-1,-1,-1);
        for (int x = 0; x < 3; x ++) for (int z = 0; z < 3; z ++) {
            Location location = beaconLocation.clone().add(x, 0, z);
            if (cuboid.notInside(location)) continue;
            ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.IRON_BLOCK.parseItem()), location.getBlock());
        }
        double k = (spawnLocation[1].getZ() - spawnLocation[0].getZ()) / (spawnLocation[1].getX() - spawnLocation[0].getX());
        spawnLocation[0].setYaw((float) Math.toDegrees(Math.atan(k)) + 90);
        spawnLocation[0].add(0.5, 1, 0.5);
    }

    @Override
    public void secondStartGame() {
        createDoors();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            for (GamePlayer target : getArena().getPlayers()) {
                if (target.equals(gamePlayer)) continue;
                hidePlayer().call(player, target.getPlayer());
            }
            player.teleport(spawnLocation[0]);
            player.setVelocity(player.getVelocity().multiply(-1));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    @Override
    public void end() {
        blocks.forEach(l -> {
            l.clone().add(0,1,0).getBlock().setType(Material.AIR);
            l.clone().add(0,2,0).getBlock().setType(Material.AIR);
            l.getBlock().setType(Material.AIR);
        });
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public void aFinish(boolean forceStop) {
        blocks.forEach(l -> {
            l.clone().add(0,1,0).getBlock().setType(Material.AIR);
            l.clone().add(0,2,0).getBlock().setType(Material.AIR);
            l.getBlock().setType(Material.AIR);
        });
        super.aFinish(forceStop);
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.BEACON.parseItem())).setDisplayName("&6&lFIND THE BEACON").build();
    }

    @Override
    public Game getGame() {
        return Game.FIND_THE_BEACON;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

}
