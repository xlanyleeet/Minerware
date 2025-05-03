package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class SwimToThePlatform extends MicroGame {
    
    private final HashMap<String, GamePlayer> achievement;

    public SwimToThePlatform(Arena arena) {
        super(480, arena, "swim-to-the-platform");
        achievement = new HashMap<>();
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
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
    }
    
    private void generatePlatform() {
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() > first.getBlockY() && l.getBlockY() <= first.getBlockY() + 5)
                .forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.WATER.parseItem()), l.getBlock()));
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() > first.getBlockY() && l.getBlockY() <= first.getBlockY() + 5)
                .filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
                        || l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
                .forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
        String typeArena = getArena().getProperties().getType();
        int size = typeArena.equals("MICRO") || typeArena.equals("MINI") ? 3 : typeArena.equals("DEFAULT") ? 5 : 6;
        Location center = getArena().getProperties().getCuboid().getCenter().getBlock().getLocation().add(-((int)(size/2)), 3, -((int)(size/2)));
        for (int x = 0; x < size; x ++)
            for (int z = 0; z < size; z ++)
                ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.GOLD_BLOCK.parseItem()), center.clone().add(x,0,z).getBlock());
    }

    @Override
    public void secondStartGame() {
        int count = getArena().isHardMode() ? 2 : 1;
        ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
                .addEnchantment(Enchantment.KNOCKBACK, count, true).build();
        generatePlatform();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            player.getInventory().setItem(0, item);
            player.getInventory().setHeldItemSlot(0);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            Block plB = player.getLocation().add(0,-1,0).getBlock();
            if (getItem(plB).isSimilar(XMaterial.GOLD_BLOCK.parseItem()) && gamePlayer.getAchievement() == null) {
                double currentTime = ((double) System.currentTimeMillis()) / 1000;
                String achievement = getSeconds(currentTime);
                gamePlayer.setAchievement(achievement);
            } else if (!getItem(plB).isSimilar(XMaterial.GOLD_BLOCK.parseItem()) && gamePlayer.getAchievement() != null) {
                double firstAch = Double.parseDouble(gamePlayer.getAchievement());
                double currentTime = ((double) System.currentTimeMillis()) / 1000;
                double secondAch = Double.parseDouble(getSeconds(currentTime));
                if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
                gamePlayer.setAchievement(null);
            }
            if (!ManageHandler.getNMS().isLegacy() && player.isSwimming()) player.setSwimming(false);
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (gamePlayer.getAchievement() != null) {
                double firstAch = Double.parseDouble(gamePlayer.getAchievement());
                double currentTime = ((double) System.currentTimeMillis()) / 1000;
                double secondAch = Double.parseDouble(getSeconds(currentTime));
                if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
                gamePlayer.setAchievement(null);
            }
            if (gamePlayer.getState() == State.PLAYING_GAME) {
                Block plB = player.getLocation().add(0,-1,0).getBlock();
                if (getItem(plB).isSimilar(XMaterial.GOLD_BLOCK.parseItem())) onWin(player, false);
                else onLose(player, false);
            }
        });
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.SWIM_TO_THE_PLATFORM;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.LAPIS_BLOCK.parseItem())).setDisplayName("&b&lSWIM TO THE PLATFORM").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageEvent)) return;
        EntityDamageEvent e = (EntityDamageEvent) event;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
        e.setCancelled(false);
        e.setDamage(0);
        player.setHealth(20);
    }

}