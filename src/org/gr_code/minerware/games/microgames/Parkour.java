package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class Parkour extends MicroGame {

    private double firstTime;
    private final List<Location> stolb = new ArrayList<>();
    private final List<Integer> stolbInt = new ArrayList<>();
    public HashMap<String, GamePlayer> achievement = new HashMap<>();
    private Location toList, toListSec;
    private ItemStack ter;
    private List<Location> locCub;
    private final List<Location> fullLocations = new ArrayList<>();

    public Parkour(Arena arena) {
        super(480, arena, "parkour");
    }

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
        super.onWin(player, teleport);
        String win = translate(getString("messages.ate-cake"));
        sendMessage(player, win);
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        double secondAch = Double.parseDouble(getSeconds(currentTime));
        if (secondAch - firstTime >= 0) achievement.put(Double.toString(secondAch - firstTime), gamePlayer);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void startGame() {
        super.startGame();
        ter = XMaterial.LIGHT_BLUE_TERRACOTTA.parseItem();
        Location center = getArena().getProperties().getCuboid().getCenter();
        int len = getArena().getProperties().getSquares().length;

        Properties.Square randSq = getArena().getProperties().getSquares()[new Random().nextInt(len)];
        Location randL = randSq.getLocations().get(new Random().nextInt(randSq.getLocations().size()));
        Properties.Square randSqSec = getArena().getProperties().getSquares()[new Random().nextInt(len)];
        Location randLSec = randSqSec.getLocations().get(new Random().nextInt(randSqSec.getLocations().size()));

        locCub = getArena().getProperties().getCuboid().getLocations().stream()
                .filter(l -> l.getBlockY() == randL.getBlockY()).filter(l -> l.distance(center) <= Cuboid.getSize(getArena())/2)
                .collect(Collectors.toList());
        List<Location> allLocSec = locCub.stream().filter(l -> l.distance(randLSec) <= 3)
                .filter(l -> l.distance(randLSec) > 1).collect(Collectors.toList());
        List<Location> allLoc = locCub.stream().filter(l -> l.distance(randL) <= 3)
                .filter(l -> l.distance(randL) > 1).collect(Collectors.toList());
        toList = allLoc.get(new Random().nextInt(allLoc.size()));
        toListSec = allLocSec.get(new Random().nextInt(allLocSec.size()));
        stolb.add(toList); stolbInt.add(2); stolb.add(toListSec); stolbInt.add(2);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location paramFirst = toList;
                Location paramSecond = toListSec;
                while (((int) paramFirst.distance(paramSecond) / 4) != 0) {
                    Location finalParamFirst = paramFirst;
                    Location finalParamSecond = paramSecond;
                    List<Location> paramLocs = locCub.stream().filter(l -> l.distance(finalParamFirst) <= 3 && l.distance(finalParamFirst) > 2.5)
                            .filter(l -> l.distance(finalParamSecond) < finalParamFirst.distance(finalParamSecond) - 1).collect(Collectors.toList());
                    int count = stolbInt.get(stolb.indexOf(finalParamFirst)) + 1;
                    paramFirst = paramSecond;
                    paramSecond = paramLocs.get(new Random().nextInt(paramLocs.size()));
                    stolb.add(paramSecond);
                    stolbInt.add(count);
                    for (int i = 0; i < count; i++) fullLocations.add(paramSecond.clone().add(0, i, 0));
                }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    @Override
	public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        HashMap<String, GamePlayer> newHash = new HashMap<>();
        achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
                .forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
        if (newHash.isEmpty()) return "";
        double maximum = 10000;
        GamePlayer gamePlayer = null;
        for (String key : newHash.keySet()) {
            double doubleKey = Double.parseDouble(key);
            if (doubleKey >= maximum) continue;
            gamePlayer = newHash.get(key);
            maximum = doubleKey;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
    }

    private void spawnParkour() {
        for (int i = 0; i < 2; i ++) {
            ManageHandler.getNMS().setBlock(requireNonNull(ter), toListSec.clone().add(0, i,0).getBlock());
            ManageHandler.getNMS().setBlock(ter, toList.clone().add(0, i,0).getBlock());
        }
        fullLocations.forEach(location -> ManageHandler.getNMS().setBlock(ter, location.getBlock()));
        for (int i = 0; i < Cuboid.getSize(getArena()) / 5; i ++) {
            Location fParam = stolb.get(stolb.size() - 1);
            List<Location> locSt = locCub.stream().filter(l -> l.distance(fParam) <= 3).filter(l -> l.distance(fParam) > 2.5)
                    .filter(l -> !Utils.getItem(l.clone().add(0,1,0).getBlock()).isSimilar(ter)).collect(Collectors.toList());
            for (int j = 0; j < i; j ++) {
                Location pParam = stolb.get(stolb.size() - 1 - j);
                int finalJ = j;
                locSt = locSt.stream().filter(l -> l.distance(pParam) > (finalJ + 1) * 1.5).collect(Collectors.toList());
            }
            if (locSt.isEmpty()) break;
            Location newL = locSt.get(new Random().nextInt(locSt.size()));
            int count = stolbInt.get(stolb.indexOf(fParam)) + 1;
            stolb.add(newL); stolbInt.add(count);
            for (int j = 0; j < count; j ++) ManageHandler.getNMS().setBlock(ter, newL.clone().add(0, j,0).getBlock());
        }
        Location cake = stolb.get(stolb.size() - 1).clone().add(0, stolbInt.get(stolbInt.size() - 1), 0);
        String materialCake = ManageHandler.getNMS().isLegacy() ? "CAKE_BLOCK" : "CAKE";
        cake.getBlock().setType(Material.valueOf(materialCake));
        System.gc();
    }

    @Override
    public void secondStartGame() {
        spawnParkour();
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        firstTime = Double.parseDouble(getSeconds(currentTime));
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
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
            if (y <= param_y) onLose(player, true);
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
        return Game.PARKOUR;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.BLAZE_ROD.parseItem())).setDisplayName("&e&lPARKOUR").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof PlayerInteractEvent)) return;
        PlayerInteractEvent e = (PlayerInteractEvent) event;
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        String material = ManageHandler.getNMS().isLegacy() ? "CAKE_BLOCK" : "CAKE";
        ItemStack cake = new ItemStack(Material.valueOf(material));
        if (!Utils.getItem(e.getClickedBlock()).isSimilar(cake)) return;
        onWin(player, false);
    }

}