package org.gr_code.minerware.games.microgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class HighPlatform extends MicroGame {

    private Location first;
    private String nameStand;
    private double firstTime;

    private final static String[] wools = {
            "BLUE_WOOL", "BLACK_WOOL", "BROWN_WOOL", "CYAN_WOOL", "GRAY_WOOL", "GREEN_WOOL",
            "LIGHT_BLUE_WOOL", "LIGHT_GRAY_WOOL", "LIME_WOOL", "MAGENTA_WOOL", "ORANGE_WOOL",
            "PINK_WOOL", "PURPLE_WOOL", "RED_WOOL", "WHITE_WOOL", "YELLOW_WOOL"};
    private final static String[] terracotta = {"ORANGE_TERRACOTTA", "MAGENTA_TERRACOTTA", "LIGHT_BLUE_TERRACOTTA",
            "YELLOW_TERRACOTTA", "LIME_TERRACOTTA", "PINK_TERRACOTTA", "GRAY_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA",
            "CYAN_TERRACOTTA", "PURPLE_TERRACOTTA", "BLUE_TERRACOTTA", "BROWN_TERRACOTTA", "GREEN_TERRACOTTA",
            "RED_TERRACOTTA", "BLACK_TERRACOTTA", "WHITE_TERRACOTTA"};

    public HighPlatform(Arena arena) {
        super(580, arena, "high-platform");
    }

    @Override
    public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> x.getAchievement() != null).collect(Collectors.toList());
        if (achievement.isEmpty()) return "";
        double maximum = 0;
        GamePlayer gamePlayer = null;
        for (GamePlayer key : achievement) {
            double doubleKey = Double.parseDouble(key.getAchievement());
            if (doubleKey <= maximum) continue;
            maximum = doubleKey;
            gamePlayer = key;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", Double.toString(maximum));
    }

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != GamePlayer.State.PLAYING_GAME) return;
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        double secondAch = Double.parseDouble(getSeconds(currentTime));
        if (secondAch - firstTime > 0) gamePlayer.setAchievement(getSeconds(secondAch - firstTime));
        super.onWin(player, teleport);
    }

    private void generate() {
        getArena().getProperties().destroySquares();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        Location center = cuboid.getCenter().clone().add(0, Math.min(Cuboid.getSize(getArena()) - 3, 10), 0);
        Location[] locations = new Location[4];
        first = getArena().getProperties().getFirstLocation().add(0,1,0);
        Location second = getArena().getProperties().getSecondLocation().add(0,1,0);
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() - 1).forEach(l -> ManageHandler.getModernAPI()
                .setBlock(requireNonNull(XMaterial.valueOf(terracotta[new Random().nextInt(terracotta.length)]).parseItem()), l.getBlock()));
        locations[0] = new Location(first.getWorld(), Math.min(first.getBlockX(), second.getBlockX()), first.getBlockY(), Math.min(first.getBlockZ(), second.getBlockZ()));
        locations[1] = new Location(first.getWorld(), Math.max(first.getBlockX(), second.getBlockX()), first.getBlockY(), Math.max(first.getBlockZ(), second.getBlockZ()));
        locations[2] = new Location(first.getWorld(), Math.min(first.getBlockX(), second.getBlockX()), first.getBlockY(), Math.max(first.getBlockZ(), second.getBlockZ()));
        locations[3] = new Location(first.getWorld(), Math.max(first.getBlockX(), second.getBlockX()), first.getBlockY(), Math.min(first.getBlockZ(), second.getBlockZ()));
        for (Location location : locations) {
            ArmorStand stand = (ArmorStand) requireNonNull(location.getWorld()).spawnEntity(location.clone().add(0.5,3,0.5), EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setCustomName(nameStand);
            stand.setCustomNameVisible(true);
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.CHEST.parseItem()), location.getBlock());
        }
        for (int x = 0; x < 3; x ++) for (int z = 0; z < 3; z ++)
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GOLD_BLOCK.parseItem()), center.clone().add(-1 + x, 0, -1 + z).getBlock());
    }

    @Override
    public void secondStartGame() {
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        firstTime = Double.parseDouble(getSeconds(currentTime));
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        nameStand = translate(getString("messages.name-chest"));
        generate();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            int count = Math.min(getArena().getPlayers().indexOf(gamePlayer), wools.length - 1);
            gamePlayer.setTask(wools[count]);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            player.setGameMode(GameMode.SURVIVAL);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != GamePlayer.State.PLAYING_GAME)) setTime(1);
        int param_y = first.getBlockY() - 2;
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            ItemStack IS = getItem(player.getLocation().add(0,-1,0).getBlock());
            if (y > param_y + 5 && IS.isSimilar(XMaterial.GOLD_BLOCK.parseItem())) onWin(player, true);
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.HIGH_PLATFORM;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.OAK_DOOR.parseItem())).setDisplayName("&a&lHIGH PLATFORM").build();
    }

    @Override
    public void event(Event event) {
        if (event instanceof PlayerInteractEvent) playerInteract(event);
        else if (event instanceof BlockPlaceEvent) blockPlace(event);
        else if (event instanceof EntityDamageByEntityEvent) entityDamage(event);
    }

    private void entityDamage(Event event) {
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getEntity() instanceof Player) || e.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) return;
        Player player = (Player) e.getEntity();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        player.setVelocity(e.getDamager().getVelocity().normalize());
        player.damage(1);
        player.setHealth(20);
    }

    private void playerInteract(Event event) {
        PlayerInteractEvent e = (PlayerInteractEvent) event;
        Player player = e.getPlayer();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.CHEST) return;
        e.setCancelled(true);
        if (gamePlayer.getTask() == null) return;
        int countBlocks = getArena().isHardMode() ? 20 : 60;
        int countBalls = getArena().isHardMode() ? 8 : 4;
        player.getInventory().setItem(0, ItemBuilder.start(requireNonNull(XMaterial.valueOf(gamePlayer.getTask()).parseItem())).setAmount(countBlocks).build());
        player.getInventory().setItem(1, ItemBuilder.start(requireNonNull(XMaterial.SNOWBALL.parseItem())).setAmount(countBalls).build());
        player.getInventory().setHeldItemSlot(0);
        gamePlayer.setTask(null);
    }

    private void blockPlace(Event event) {
        BlockPlaceEvent e = (BlockPlaceEvent) event;
        Player player = e.getPlayer();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        ItemStack item = getItem(e.getBlock().getLocation().add(0,-1,0).getBlock());
        ItemStack itemSecond = getItem(e.getBlock().getLocation().add(0,-2,0).getBlock());
        ItemStack gold = XMaterial.GOLD_BLOCK.parseItem();
        if (e.getBlock().getY() > first.getBlockY() + 5 && (item.isSimilar(gold) || itemSecond.isSimilar(gold))) return;
        e.setCancelled(false);
    }

}


