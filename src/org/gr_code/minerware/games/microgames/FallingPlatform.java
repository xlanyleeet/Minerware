package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.games.resources.Platform;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class FallingPlatform extends MicroGame {
    
    private final List<Platform> platforms;

    private final static XMaterial normalQuarts = XMaterial.QUARTZ_BLOCK;
    private final static XMaterial firstQuarts = XMaterial.POLISHED_DIORITE;
    private final static XMaterial secondQuarts = XMaterial.DIORITE;

    private final static XMaterial normalAndesite = XMaterial.POLISHED_ANDESITE;
    private final static XMaterial firstAndesite = XMaterial.ANDESITE;
    private final static XMaterial secondAndesite = XMaterial.COBBLESTONE;

    public FallingPlatform(Arena arena) {
        super(180, arena, "falling-platform");
        platforms = new ArrayList<>();
    }

    private void generatePlatforms() {
        getArena().getProperties().destroySquares();
        Location first = getArena().getProperties().getFirstLocation().getBlock().getLocation();
        Location second = getArena().getProperties().getSecondLocation().getBlock().getLocation();
        int maxX =  Math.max(first.getBlockX(), second.getBlockX());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .filter(l -> l.getBlockX() == maxX || l.getBlockZ() == maxZ).forEach(l -> l.getBlock().setType(Material.AIR));
        Location right = new Location(first.getWorld(), Math.min(first.getX(), second.getX()), first.getY(), Math.min(first.getZ(), second.getZ()));
        int size = Cuboid.getSize(getArena());
        int count = getArena().isHardMode() ? 3 : 2;
        int twoCount = count * 2;
        for (int x = 0; x < size; x += twoCount) for (int z = 0; z < size; z += twoCount)
            platforms.add(new Platform(right.clone().add(x,0, z), normalQuarts, firstQuarts, secondQuarts, count));

        right.add(count,0,0);
        for (int x = 0; x < size; x += twoCount) for (int z = 0; z < size; z += twoCount)
            platforms.add(new Platform(right.clone().add(x,0, z), normalAndesite, firstAndesite, secondAndesite, count));

        right.add(0,0,count);
        for (int x = 0; x < size; x += twoCount) for (int z = 0; z < size; z += twoCount)
            platforms.add(new Platform(right.clone().add(x,0, z), normalQuarts, firstQuarts, secondQuarts, count));

        right.add(-count,0,0);
        for (int x = 0; x < size; x += twoCount) for (int z = 0; z < size; z += twoCount)
            platforms.add(new Platform(right.clone().add(x,0, z), normalAndesite, firstAndesite, secondAndesite, count));

        platforms.forEach(platform -> platform.generate(getArena()));
        int hard = getArena().isHardMode() ? 3 : 6;
        int randCount = new Random().nextInt(3) + hard;
        for (int i = 0; i < randCount; i ++) platforms.remove(new Random().nextInt(platforms.size()));
    }

    @Override
    public void secondStartGame() {
        generatePlatforms();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), Objects.requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    private void destroyPlatforms() {
        List<Platform> platformsSecondStage = platforms.stream().filter(platform -> platform.getStage() == Platform.Stage.SECOND).collect(Collectors.toList());
        if (!platformsSecondStage.isEmpty()) {
            platformsSecondStage.forEach(Platform::nextStage);
            platforms.removeAll(platformsSecondStage);
        } else setTime(81);
    }

    private void secondStage() {
        List<Platform> platformsFirstStage = platforms.stream().filter(platform -> platform.getStage() == Platform.Stage.FIRST).collect(Collectors.toList());
        if (!platformsFirstStage.isEmpty())
            platformsFirstStage.forEach(Platform::nextStage);
        else setTime(71);
    }

    private void firstStage() {
        List<Platform> platformsFullStage = platforms.stream().filter(platform -> platform.getStage() == Platform.Stage.FULL).collect(Collectors.toList());
        int randCount = Math.min(new Random().nextInt(5) + 8, platformsFullStage.size());
        for (int i = 0; i < randCount; i ++) {
            Platform platform = platformsFullStage.get(new Random().nextInt(platformsFullStage.size()));
            platform.nextStage();
            platformsFullStage.remove(platform);
        }
        if (!(platforms.isEmpty())) setTime(100);
    }

    @Override
    public void check() {
        switch (getTime()) {
            case 90: destroyPlatforms(); break;
            case 80: secondStage(); break;
            case 70: firstStage(); break;
        }
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.FALLING_PLATFORMS;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(Objects.requireNonNull(XMaterial.POLISHED_ANDESITE.parseItem())).setDisplayName("&6&lFALLING PLATFORM").build();
    }

}