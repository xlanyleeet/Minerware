package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.games.resources.Building;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class KingOfTheHill extends MicroGame {
    
    private final HashMap<String, GamePlayer> achievement;
    private Building building;
    
    public KingOfTheHill(Arena arena) {
        super(480, arena, "king-of-the-hill");
        achievement = new HashMap<>();
    }

    @Override
    public void startGame() {
        building = new Building("hill", getArena());
        Properties.Square randSq = getArena().getProperties().getSquares()[new Random().nextInt(getArena().getProperties().getSquares().length)];
        building.setItemSquare(getItem(randSq.getLocations().get(new Random().nextInt(randSq.getLocations().size())).getBlock()));
        building.setItemFloor(getItem(getRandomLocation(getArena()).clone().add(0, -1, 0).getBlock()));
        super.startGame();
    }

    private int getAchievY() {
        switch (getArena().getProperties().getType()) {
            case "MEGA": return getArena().getProperties().getFirstLocation().getBlockY() + 11;
            case "DEFAULT": return getArena().getProperties().getFirstLocation().getBlockY() + 10;
            case "MINI": return getArena().getProperties().getFirstLocation().getBlockY() + 8;
            default: return getArena().getProperties().getFirstLocation().getBlockY() + 6;
        }
    }

    @Override
	public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        HashMap<String, GamePlayer> newHash = new HashMap<>();
        achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
                .forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
        if (newHash.isEmpty()) return "";
        double maximum = 0;
        GamePlayer gamePlayer = null;
        for (String key : newHash.keySet()) {
            double doubleKey = Double.parseDouble(key);
            if (doubleKey <= maximum) continue;
            gamePlayer = newHash.get(key);
            maximum = doubleKey;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name)
                .replace("<seconds>", getSeconds(maximum));
    }
    
    private List<Location> generateLocations() {
        Location first = getArena().getProperties().getFirstLocation();
        return getArena().getProperties().getCuboid().getLocations().stream()
                .filter(l -> !getItem(l.clone().add(0, -1, 0).getBlock()).isSimilar(XMaterial.AIR.parseItem()))
                .filter(l -> getItem(l.getBlock()).isSimilar(XMaterial.AIR.parseItem()))
                .filter(l -> getItem(l.clone().add(0, 1, 0).getBlock()).isSimilar(XMaterial.AIR.parseItem()))
                .filter(l -> l.getBlockY() > first.getBlockY() + 2)
                .collect(Collectors.toList());
    }
    
    @Override
    public void secondStartGame() {
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        int count = getArena().isHardMode() ? 1 : 0;
        ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
                .addEnchantment(Enchantment.KNOCKBACK, 1 + count, true).build();
        building.generateBuilding();
        List<Location> locations = generateLocations();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.teleport(locations.get(new Random().nextInt(locations.size())).clone().add(0.5, 0, 0.5));
            sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setItem(0, item);
            player.getInventory().setHeldItemSlot(0);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        int AchievY = getAchievY();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y >= AchievY && gamePlayer.getAchievement() == null) {
                double currentTime = ((double)System.currentTimeMillis()) / 1000;
                String achievement = getSeconds(currentTime);
                gamePlayer.setAchievement(achievement);
            } else if (y < AchievY && gamePlayer.getAchievement() != null) {
                double firstAch = Double.parseDouble(gamePlayer.getAchievement());
                double currentTime = ((double)System.currentTimeMillis()) / 1000;
                double secondAch = Double.parseDouble(getSeconds(currentTime));
                if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
                gamePlayer.setAchievement(null);
            }
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        int winY = getAchievY();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (gamePlayer.getAchievement() != null) {
                double firstAch = Double.parseDouble(gamePlayer.getAchievement());
                double currentTime = ((double)System.currentTimeMillis()) / 1000;
                double secondAch = Double.parseDouble(getSeconds(currentTime));
                if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
                gamePlayer.setAchievement(null);
            }
            if (gamePlayer.getState() == State.PLAYING_GAME) {
                if (player.getLocation().getBlockY() >= winY) onWin(player, false);
                else onLose(player, false);
            }
        });
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.KING_OF_THE_HILL;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.STONE_BRICK_STAIRS.parseItem())).setDisplayName("&a&lKING OF THE HILL").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageEvent)) return;
        EntityDamageEvent e = (EntityDamageEvent) event;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
        player.setHealth(20);
        e.setDamage(0);
        e.setCancelled(false);
    }

}

