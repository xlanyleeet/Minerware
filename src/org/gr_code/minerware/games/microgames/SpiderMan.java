package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.modern.ModernMinerAPI;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class SpiderMan extends MicroGame {

    private double firstTime;
    public HashMap<String, GamePlayer> achievement;
    private static PlayerFishEvent.State rightState;

    public SpiderMan(Arena arena) {
        super(480, arena, "spider-man");
        achievement = new HashMap<>();
    }

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME)
            return;
        double currentTime = ((double) System.currentTimeMillis()) / 1000;
        double secondAch = Double.parseDouble(getSeconds(currentTime));
        if (secondAch - firstTime >= 0)
            achievement.put(Double.toString(secondAch - firstTime), gamePlayer);
        super.onWin(player, teleport);
    }

    @Override
    public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        HashMap<String, GamePlayer> newHash = new HashMap<>();
        achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
                .forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
        if (newHash.isEmpty())
            return "";
        double maximum = 10000;
        GamePlayer gamePlayer = null;
        for (String key : newHash.keySet()) {
            double doubleKey = Double.parseDouble(key);
            if (doubleKey >= maximum)
                continue;
            gamePlayer = newHash.get(key);
            maximum = doubleKey;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
    }

    private void generateBlocks() {
        getArena().getProperties().destroySquares();
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        Location center = getArena().getProperties().getCuboid().getCenter().clone().add(0,
                Cuboid.getSize(getArena()) - 2, 0);
        float count = getArena().isHardMode() ? 0.96f : 0.98f;
        getArena().getProperties().getCuboid().getLocations().stream()
                .filter(l -> l.getBlockY() != first.getBlockY() && l.getBlockY() <= second.getBlockY() - 2)
                .filter(l -> Math.random() > count).forEach(l -> {
                    double random = Math.random();
                    if (random <= 0.9)
                        ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.STONE.parseItem()),
                                l.getBlock());
                    else if (random <= 0.98)
                        ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.SLIME_BLOCK.parseItem()),
                                l.getBlock());
                    else
                        ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.COBWEB.parseItem()),
                                l.getBlock());
                });
        for (int x = 0; x < 3; x++)
            for (int z = 0; z < 3; z++)
                ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GOLD_BLOCK.parseItem()),
                        center.clone().add(-1 + x, 0, -1 + z).getBlock());
    }

    @Override
    public void secondStartGame() {
        rightState = ManageHandler.getModernAPI().isLegacy() ? PlayerFishEvent.State.FAILED_ATTEMPT
                : PlayerFishEvent.State.REEL_IN;
        double currentTime = ((double) System.currentTimeMillis()) / 1000;
        firstTime = Double.parseDouble(getSeconds(currentTime));
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        generateBlocks();
        ItemStack item = ItemBuilder.start(Objects.requireNonNull(XMaterial.FISHING_ROD.parseItem()))
                .setUnbreakable(true).build();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation()))
                player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            player.getInventory().setItem(0, item);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0)
            return;
        Location first = getArena().getProperties().getFirstLocation();
        int param_y = first.getBlockY() - 1;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME))
            setTime(1);
        ItemStack gold = XMaterial.GOLD_BLOCK.parseItem();
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y > first.getBlockY() + 3 && getItem(player.getLocation().add(0, -1, 0).getBlock()).isSimilar(gold))
                onWin(player, true);
            if (y <= param_y)
                onLose(player, true);
        });
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.SPIDER_MAN;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.STRING.parseItem())).setDisplayName("&6&lSPIDER MAN").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof PlayerFishEvent))
            return;
        try {
            playerFish(event);
        } catch (Exception ignored) {
        }
    }

    private void playerFish(Event event)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PlayerFishEvent e = (PlayerFishEvent) event;
        Player p = e.getPlayer();
        if (requireNonNull(getArena().getPlayer(p.getUniqueId())).getState() != State.PLAYING_GAME)
            return;
        Field field = e.getClass().getDeclaredField("hookEntity");
        field.setAccessible(true);
        Object object = field.get(e);
        Class<?> objectClass = object.getClass();
        Location location = (Location) objectClass.getMethod("getLocation").invoke(object);
        Method setVelocity = objectClass.getMethod("setVelocity", Vector.class);
        if (e.getState() == PlayerFishEvent.State.FISHING)
            setVelocity.invoke(object, p.getLocation().getDirection().normalize().multiply(2));
        if (!(e.getState() == PlayerFishEvent.State.IN_GROUND || e.getState() == rightState))
            return;
        if (getArena().getProperties().getCuboid().notInside(location))
            return;
        World world = location.getWorld();
        Vector start = location.toVector();
        Vector direction = new Vector(location.getX() - p.getLocation().getX(),
                location.getY() - p.getLocation().getY(), location.getZ() - p.getLocation().getZ());
        BlockIterator iterator = new BlockIterator(requireNonNull(world), start, direction.normalize(), 0, 4);
        Block hitBlock = null;
        while (iterator.hasNext()) {
            hitBlock = iterator.next();
            if (hitBlock.getType() != Material.AIR)
                break;
        }
        if (hitBlock == null || requireNonNull(hitBlock).getType() == Material.AIR)
            return;
        ManageHandler.getModernAPI().playOutParticle(p.getLocation().add(0, 1, 0), 0.5F,
                ModernMinerAPI.MinerParticle.CLOUD, 0F, 35);
        double vecX = location.getX() - p.getLocation().getX();
        double vecY = location.getY() - p.getLocation().getY();
        double vecZ = location.getZ() - p.getLocation().getZ();
        p.setVelocity(new Vector(vecX, vecY, vecZ).normalize().multiply(2.5));
    }

}
