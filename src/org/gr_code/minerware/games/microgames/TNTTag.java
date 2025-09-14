package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.games.resources.Building;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.modern.ModernMinerAPI;
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

public class TNTTag extends MicroGame {

    public HashMap<String, GamePlayer> achievement;
    private Building building;

    public TNTTag(Arena arena) {
        super(780, arena, "tnt-tag");
        achievement = new HashMap<>();
    }

    @Override
    public void onLose(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME)
            return;
        ManageHandler.getModernAPI().playOutParticle(player.getLocation(), 2F,
                ModernMinerAPI.MinerParticle.LARGE_EXPLOSION, 1F, 10);
        player.getWorld().playSound(player.getLocation(), requireNonNull(XSound.ENTITY_GENERIC_EXPLODE.parseSound()), 5,
                1);
        if (gamePlayer.getTask() != null && gamePlayer.getTask().equals("hot")) {
            double firstAch = Double.parseDouble(gamePlayer.getAchievement());
            double currentTime = ((double) System.currentTimeMillis()) / 1000;
            double secondAch = Double.parseDouble(getSeconds(currentTime));
            if (secondAch - firstAch > 0)
                achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
            gamePlayer.setAchievement(null);
        }
        String title = translate(requireNonNull(getString("titles.exploded")));
        sendTitle(player, title);
        super.onLose(player, teleport);
    }

    @Override
    public void startGame() {
        building = new Building("tnt_tag", getArena());
        Properties.Square randSq = getArena().getProperties().getSquares()[new Random()
                .nextInt(getArena().getProperties().getSquares().length)];
        building.setItemSquare(
                getItem(randSq.getLocations().get(new Random().nextInt(randSq.getLocations().size())).getBlock()));
        building.setItemFloor(getItem(getRandomLocation(getArena()).clone().add(0, -1, 0).getBlock()));
        super.startGame();
    }

    @Override
    public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        HashMap<String, GamePlayer> newHash = new HashMap<>();
        achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
                .forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
        if (newHash.isEmpty())
            return "";
        double maximum = 0;
        GamePlayer gamePlayer = null;
        for (String key : newHash.keySet()) {
            double doubleKey = Double.parseDouble(key);
            if (doubleKey <= maximum)
                continue;
            gamePlayer = newHash.get(key);
            maximum = doubleKey;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private List<Location> generateLocations() {
        Location first = getArena().getProperties().getFirstLocation();
        return getArena().getProperties().getCuboid().getLocations().stream()
                .filter(l -> Utils.getItem(l.clone().add(0, -1, 0).getBlock()).isSimilar(building.getItemSquare()))
                .filter(l -> Utils.getItem(l.getBlock()).isSimilar(XMaterial.AIR.parseItem()))
                .filter(l -> Utils.getItem(l.clone().add(0, 1, 0).getBlock()).isSimilar(XMaterial.AIR.parseItem()))
                .filter(l -> l.getBlockY() > first.getBlockY() + 1)
                .filter(l -> l.distance(getArena().getProperties().getCuboid().getCenter().clone().add(0, 3,
                        0)) <= Cuboid.getSize(getArena()) / 2)
                .collect(Collectors.toList());
    }

    @Override
    public void secondStartGame() {
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        getArena().getProperties().destroySquares();
        building.generateBuilding();
        List<Location> allLoc = generateLocations();
        int count = getArena().isHardMode() ? getArena().getCurrentPlayers() / 2 : getArena().getCurrentPlayers() / 3;
        if (count == 0)
            count++;
        for (int i = 0; i < count; i++) {
            GamePlayer x = getArena().getPlayers().get(new Random().nextInt(getArena().getPlayers().size()));
            x.setTask("hot");
            Player p = x.getPlayer();
            for (int j = 0; j < 9; j++)
                p.getInventory().setItem(j, XMaterial.TNT.parseItem());
            p.getInventory().setHelmet(XMaterial.TNT.parseItem());
            double currentTime = ((double) System.currentTimeMillis()) / 1000;
            String achievement = getSeconds(currentTime);
            x.setAchievement(achievement);
        }
        getArena().getPlayers().forEach(x -> {
            Player player = x.getPlayer();
            player.teleport(allLoc.get(new Random().nextInt(allLoc.size())).clone().add(0.5, 0, 0.5));
            sendTitle(player, title, subtitle, 0, 70, 20);
            x.setState(State.PLAYING_GAME);
            assert XSound.ENTITY_ARROW_HIT_PLAYER.parseSound() != null;
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    private void thirdStartGame() {
        String bar = translate(getString("action-bar.boom"));
        getArena().getPlayers().forEach(x -> {
            Player player = x.getPlayer();
            if (x.getTask() != null && x.getTask().equals("hot"))
                onLose(player, true);
            ManageHandler.getModernAPI().sendActionBar(player, bar);
        });
        List<GamePlayer> players = getArena().getPlayers().stream().filter(x -> x.getState() == State.PLAYING_GAME)
                .collect(Collectors.toList());
        if (players.size() == 1) {
            setTime(59);
            return;
        }
        int count = getArena().isHardMode() ? players.size() / 2 : players.size() / 3;
        if (count == 0)
            count++;
        for (int i = 0; i < count; i++) {
            GamePlayer x = players.get(new Random().nextInt(players.size()));
            x.setTask("hot");
            Player p = x.getPlayer();
            for (int j = 0; j < 9; j++)
                p.getInventory().setItem(j, XMaterial.TNT.parseItem());
            p.getInventory().setHelmet(XMaterial.TNT.parseItem());
            double currentTime = ((double) System.currentTimeMillis()) / 1000;
            String achievement = getSeconds(currentTime);
            x.setAchievement(achievement);
        }
    }

    private void fifthStartGame() {
        String bar = translate(getString("action-bar.boom"));
        getArena().getPlayers().forEach(x -> {
            Player player = x.getPlayer();
            if (x.getTask() != null && x.getTask().equals("hot"))
                onLose(player, true);
            ManageHandler.getModernAPI().sendActionBar(player, bar);
        });
    }

    private void tick(int t) {
        String[] cd = { "", "&4&l|", "&4&l||", "&4&l|||", "&4&l||||", "&4&l|||||", "&4&l||||||", "&4&l|||||||",
                "&4&l||||||||", "&4&l|||||||||", "&4&l||||||||||" };
        String bar = translate(getString("action-bar.timer"));
        getArena().getPlayers().forEach(x -> {
            Player p = x.getPlayer();
            ManageHandler.getModernAPI().sendActionBar(p, translate(cd[t] + bar + cd[t]));
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0)
            return;
        switch (getTime()) {
            case 500:
            case 300:
                thirdStartGame();
                break;
            case 100:
                fifthStartGame();
                break;
        }
        List<GamePlayer> playerList = getArena().getPlayers();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1;
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME))
            setTime(1);
        if (getTime() % 20 == 0) {
            if (getTime() >= 500)
                tick((getTime() - 500) / 20);
            else if (getTime() >= 300)
                tick((getTime() - 300) / 20);
            else if (getTime() >= 100)
                tick((getTime() - 100) / 20);
        }
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y)
                onLose(player, true);
            if (gamePlayer.getTask() != null && gamePlayer.getTask().equals("hot")) {
                ManageHandler.getModernAPI().playOutParticle(player.getLocation(), 1F,
                        ModernMinerAPI.MinerParticle.LAVA, 1F, 10);
                player.addPotionEffect(PotionEffectType.SPEED.createEffect(10, 1));
            }
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
        return Game.TNT_TAG;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.GUNPOWDER.parseItem())).setDisplayName("&5&lTNT TAG").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageByEntityEvent))
            return;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getEntity() instanceof Player))
            return;
        if (!(e.getDamager() instanceof Player))
            return;
        Player player = (Player) e.getDamager(), p2 = (Player) e.getEntity();
        UUID uuid2 = p2.getUniqueId(), uuid = player.getUniqueId();
        GamePlayer x = requireNonNull(getArena().getPlayer(uuid)), x2 = requireNonNull(getArena().getPlayer(uuid2));
        if (x.getState() != State.PLAYING_GAME)
            return;
        if (x2.getState() != State.PLAYING_GAME)
            return;
        if (!(x.getTask() != null && x2.getTask() == null && x.getTask().equals("hot")))
            return;
        x2.setTask("hot");
        x.setTask(null);
        player.getInventory().setHelmet(null);
        p2.getInventory().setHelmet(XMaterial.TNT.parseItem());
        double firstAch = Double.parseDouble(x.getAchievement());
        double currentTime = ((double) System.currentTimeMillis()) / 1000;
        String achForEnt = getSeconds(currentTime);
        double secondAch = Double.parseDouble(achForEnt);
        if (secondAch - firstAch > 0)
            achievement.put((secondAch - firstAch) + "", x);
        x.setAchievement(null);
        x2.setAchievement(achForEnt);
        for (int j = 0; j < 9; j++)
            p2.getInventory().setItem(j, XMaterial.TNT.parseItem());
        player.getInventory().clear();
    }

}
