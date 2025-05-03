package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.builders.ItemBuilder;
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

public class InvisibleButton extends MicroGame {

    private double firstTime;
    public InvisibleButton(Arena arena) {
        super(380, arena, "invisible-button");
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

    private void generateStones() {
        getArena().getProperties().destroySquares();
        int countPole = getArena().isHardMode() ? 14 + new Random().nextInt(5) : 10 + new Random().nextInt(5);
        switch (getArena().getProperties().getType()) {
            case "MEGA": countPole += 4; break;
            case "MINI": countPole -= 4; break;
            case "MICRO": countPole -= 8; break;
        }
        Location first = getArena().getProperties().getFirstLocation();
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .forEach(l -> l.getBlock().setType(Material.STONE));
        List<Location> locations = getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1).collect(Collectors.toList());
        for (int i = 0; i < countPole; i ++) {
            Location random = locations.get(new Random().nextInt(locations.size()));
            locations.removeAll(locations.stream().filter(l -> l.distance(random) <= 1).collect(Collectors.toList()));
            for (int j = 0; j < 4; j ++) random.clone().add(0, j, 0).getBlock().setType(Material.STONE);
        }
        Location buttonLocation = getRandomLocation(getArena());
        while (buttonLocation.getBlock().getType() != Material.AIR) buttonLocation = getRandomLocation(getArena());
        buttonLocation.getBlock().setType(Material.STONE_BUTTON);
        ManageHandler.getNMS().setUpDirectionButton(buttonLocation.getBlock());
    }

    @Override
    public void secondStartGame() {
        generateStones();
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        firstTime = Double.parseDouble(getSeconds(currentTime));
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
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
        return Game.INVISIBLE_BUTTON;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.STONE_BUTTON.parseItem())).setDisplayName("&3&lFIND THE BUTTON").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof PlayerInteractEvent)) return;
        PlayerInteractEvent e = (PlayerInteractEvent) event;
        Player player = e.getPlayer();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        if (e.getClickedBlock() == null) return;
        if ((e.getClickedBlock()).getType() == Material.STONE_BUTTON) onWin(player, true);
    }

}