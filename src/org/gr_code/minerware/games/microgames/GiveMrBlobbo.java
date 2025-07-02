package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.manager.type.Utils.*;

public class GiveMrBlobbo extends MicroGame {
    
    private final HashMap<ArmorStand, Integer> standList = new HashMap<>();
    private final static ItemStack[] heads = {setItem(XMaterial.COOKED_CHICKEN), setItem(XMaterial.APPLE), setItem(XMaterial.COOKED_BEEF),
            setItem(XMaterial.BREAD), setItem(XMaterial.COOKED_PORKCHOP), setItem(XMaterial.COOKED_COD), setItem(XMaterial.COOKED_SALMON),
            setItem(XMaterial.BAKED_POTATO), setItem(XMaterial.MUSHROOM_STEW)};
    private float yaw;
    private String actionBar, positive, negative, enoughNutrition;

    public GiveMrBlobbo(Arena arena) {
        super(580, arena, "give-mr-blobbo");
    }

    private static ItemStack setItem(XMaterial xMaterial) {
        return ItemBuilder.start(requireNonNull(xMaterial.parseItem())).setGlowing(true).build();
    }

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
        String win = translate(getString("messages.fed-mrblobo"));
        sendMessage(player, win);
        super.onWin(player, teleport);
    }

    @Override
    public void startGame() {
        actionBar = getString("action-bar.amount-nutrition");
        negative = translate(getString("messages.negative-nutrition"));
        positive = translate(getString("messages.positive-nutrition"));
        enoughNutrition = translate(getString("messages.collected-enough-nutrition"));
        getArena().getPlayers().forEach(gamePlayer -> {
            gamePlayer.setTask("0");
            gamePlayer.setAchievement("0");
        });
        super.startGame();
    }

    @Override
    public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> Integer.parseInt(x.getAchievement()) > 0).collect(Collectors.toList());
        if (achievement.isEmpty()) return "";
        int maximum = 0;
        GamePlayer gamePlayer = null;
        for (GamePlayer key : achievement) {
            int doubleKey = Integer.parseInt(key.getAchievement());
            if (doubleKey <= maximum) continue;
            maximum = doubleKey;
            gamePlayer = key;
        }
        assert gamePlayer != null;
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<nutrition>", Integer.toString(maximum));
    }

    private Location getRandomLocation() {
        List<Location> squares = new ArrayList<>();
        for (Properties.Square sq : getArena().getProperties().getSquares())
            squares.addAll(sq.getLocations());
        Location first = getArena().getProperties().getFirstLocation();
        Location center = getArena().getProperties().getCuboid().getCenter().getBlock().getLocation();
        List<Location> list = getArena().getProperties().getCuboid().getLocations().stream()
                .filter(location -> location.getBlockY() == first.getBlockY() + 1)
                .filter(location -> center.distance(location) >= 2.5)
                .filter(location -> center.distance(location) <= ((float) Cuboid.getSize(getArena()) / 2) - 2)
                .filter(location -> !squares.contains(location.getBlock().getLocation()))
                .collect(Collectors.toList());
        int size = list.size();
        int randomA = new Random().nextInt(size);
        Location random = list.get(randomA);
        random.setYaw(yaw);
        random.setPitch(0);
        return random.clone();
    }

    private void generatePlatform() {
        Location center = getArena().getProperties().getCuboid().getCenter().getBlock().getLocation().add(-1, -1, -1);
        ItemStack gold = XMaterial.GOLD_BLOCK.parseItem();
        assert gold != null;
        for (int x = 0; x < 3; x ++) for (int z = 0; z < 3; z ++) ManageHandler.getModernAPI().setBlock(gold, center.clone().add(x, 0, z).getBlock());
        ManageHandler.getModernAPI().setBlock(gold, center.add(1, 1, 1).getBlock());
    }

    private void spawnSlime() {
        String nameSlime = translate(getString("messages.name-slime"));
        Location center = getArena().getProperties().getCuboid().getCenter().getBlock().getLocation();
        center.setYaw(0);
        Slime slime = (Slime) requireNonNull(center.getWorld()).spawnEntity(center.clone().add(0.5,1,0.5), EntityType.SLIME);
        slime.setSize(2);
        slime.setCustomName(nameSlime);
        slime.setCustomNameVisible(true);
        if (!ManageHandler.getModernAPI().oldVersion()) slime.setAI(false);
        else ManageHandler.getModernAPI().setNoAI(slime);
    }

    private void spawnNutrition() {
        for (int i = 0; i < getArena().getCurrentPlayers() * 2; i++) {
            ArmorStand ar = (ArmorStand) requireNonNull(getArena().getProperties().getFirstLocation().getWorld())
                    .spawnEntity(getRandomLocation().add(0,-1,0), EntityType.ARMOR_STAND);
            ar.setGravity(false);
            ar.setCustomNameVisible(true);
            int random = new Random().nextInt(10) - 2;
            if (random >= 0 && random <= 2) random += 3;
            if (random > 0) ar.setCustomName(translate(positive.replace("<nutrition>", random + "")));
            else ar.setCustomName(translate(negative.replace("<nutrition>", random + "")));
            requireNonNull(ar.getEquipment()).setHelmet(heads[new Random().nextInt(heads.length)]);
            ar.setVisible(false);
            standList.put(ar, random);
        }
    }

    @Override
    public void secondStartGame() {
        yaw = 360;
        getArena().getProperties().destroySquares();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        generatePlatform();
        spawnSlime();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(Cuboid.getRandomLocation(getArena()));
            sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
        spawnNutrition();
    }

    private void rotateStands() {
        if (yaw - 3 <= 0) yaw = 360;
        else yaw -= 3;
        standList.keySet().forEach(ar -> {
            Location l = ar.getLocation();
            l.setYaw(yaw);
            ar.teleport(l);
        });
    }

    @Override
    public void check() {
        rotateStands();
        if (getTime() % 2 != 0) return;
        int countPlayers = getArena().getCurrentPlayers();
        int hard = getArena().isHardMode() ? 5 : 0;
        int countSpawn = countPlayers <= 5 ? 20 + hard: countPlayers <= 10 ? 10 + hard: 5 + hard;
        if (getTime() % countSpawn == 0) spawnStand();
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        double yp = getArena().getProperties().getFirstLocation().getBlockY();
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME
                || gamePlayer.getState() == State.WINNER_IN_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            checkStand(player, gamePlayer);
            if (gamePlayer.getTask().equals("0")) player.getInventory().clear();
            ManageHandler.getModernAPI().sendActionBar(player, translate(actionBar.replace("<nutrition>", gamePlayer.getTask())));
            if (y <= yp - 1) onLose(player, true);
        });
    }

    private void checkStand(Player player, GamePlayer gamePlayer) {
        List<ArmorStand> list = standList.keySet().stream()
                .filter(ar -> ar.getLocation().add(0,1,0).distance(player.getLocation()) <= 1.5).collect(Collectors.toList());
        for (ArmorStand ar : list) {
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            int nutrition = Math.max(Integer.parseInt(gamePlayer.getTask()) + standList.get(ar), 0);
            if (Integer.parseInt(gamePlayer.getTask()) < 20 && nutrition >= 20) sendMessage(player, enoughNutrition);
            gamePlayer.setTask(Integer.toString(nutrition));
            if (standList.get(ar) > 0) player.getInventory().addItem(ItemBuilder.start(requireNonNull(
                    requireNonNull(ar.getEquipment()).getHelmet())).setAmount(standList.get(ar)).build());
            ar.remove();
            standList.remove(ar);
        }
    }

    private void spawnStand() {
        if (standList.size() > (getArena().getCurrentPlayers() + 5) * 3) return;
        ArmorStand ar = (ArmorStand) requireNonNull(getArena().getProperties().getFirstLocation().getWorld())
                .spawnEntity(getRandomLocation().add(0,-1,0), EntityType.ARMOR_STAND);
        ar.setGravity(false);
        ar.setCustomNameVisible(true);
        int random = new Random().nextInt(10) - 2;
        if (random >= 0 && random <= 2) random += 3;
        if (random > 0) ar.setCustomName(translate(positive.replace("<nutrition>", random + "")));
        else ar.setCustomName(translate(negative.replace("<nutrition>", random + "")));
        requireNonNull(ar.getEquipment()).setHelmet(heads[new Random().nextInt(heads.length)]);
        ar.setVisible(false);
        standList.put(ar, random);
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        standList.keySet().forEach(Entity::remove);
        standList.clear();
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.GIVE_MR_BLOBBO;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public void aFinish(boolean forceStop) {
        standList.keySet().forEach(Entity::remove);
        standList.clear();
        super.aFinish(forceStop);
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.COOKED_PORKCHOP.parseItem())).setDisplayName("&d&lGIVE MRBLOBBO NUTRITION").build();
    }

    @Override
    public void event(Event event) {
        if (event instanceof PlayerInteractEntityEvent) playerInteractEntity(event);
        else if (event instanceof CreatureSpawnEvent) creatureSpawn(event);
    }

    private void playerInteractEntity(Event event) {
        PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
        if (!(e.getRightClicked() instanceof Slime)) return;
        if (requireNonNull(gamePlayer).getTask() == null || gamePlayer.getTask().equals("0")) return;
        String nut = translate(getString("messages.gave-nutrition"));
        player.getInventory().clear();
        sendMessage(player, nut.replace("<nutrition>", gamePlayer.getTask()));
        gamePlayer.setAchievement((Integer.parseInt(gamePlayer.getAchievement()) + Integer.parseInt(gamePlayer.getTask())) + "");
        gamePlayer.setTask("0");
        if (gamePlayer.getState() != State.PLAYING_GAME) return;
        if (Integer.parseInt(gamePlayer.getAchievement()) < 20) return;
        onWin(player, false);
    }

    private void creatureSpawn(Event event) {
        CreatureSpawnEvent e = (CreatureSpawnEvent) event;
        if (!(e.getEntity() instanceof Slime)) return;
        e.setCancelled(false);
    }

}


