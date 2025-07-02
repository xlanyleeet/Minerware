package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class LandOnSlime extends MicroGame {

    public LandOnSlime(Arena arena) {
        super(180, arena, "land-on-slime");
    }
    
    private void generateBuilding() {
        getArena().getProperties().destroySquares();
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        ItemStack lava = getArena().isHardMode() ? XMaterial.EMERALD_BLOCK.parseItem() : XMaterial.LAVA.parseItem();
        assert lava != null;
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY()).forEach(l -> {
            if (Math.random() <= 0.2) ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.SLIME_BLOCK.parseItem()), l.getBlock());
            else ManageHandler.getModernAPI().setBlock(lava, l.getBlock());
        });
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() || l.getBlockY() == second.getBlockY() - 1)
                .filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
                        || l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() - 2)
                .forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()), l.getBlock()));

    }

    @Override
    public void secondStartGame() {
        generateBuilding();
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.setFallDistance(0);
            player.teleport(getRandomLocation(getArena()).add(0, second.getBlockY() - first.getBlockY() - 2,0));
            player.setFallDistance(0);
            sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        if (getTime() == 60) destroyRoof();
        List<GamePlayer> playerList = getArena().getPlayers();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    private void destroyRoof() {
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() || l.getBlockY() == second.getBlockY() - 1)
                .filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
                        || l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ()).forEach(l -> l.getBlock().setType(Material.AIR));
        cuboid.getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY() - 2).forEach(l -> l.getBlock().setType(Material.AIR));
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.LAND_ON_SLIME;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.SLIME_BLOCK.parseItem())).setDisplayName("&6&lLAND ON SLIME").build();
    }

    @Override
    public void event(Event event) {
        if (event instanceof EntityDamageEvent) entityDamage(event);
    }

    private void entityDamage(Event event) {
        EntityDamageEvent e = (EntityDamageEvent) event;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            boolean isFall = e.getCause() != EntityDamageEvent.DamageCause.FALL;
            boolean isLava = e.getCause() != EntityDamageEvent.DamageCause.LAVA;
            if (!(isFall || isLava)) return;
            onLose(player, true);
            return;
        }
        e.setCancelled(false);
        e.setDamage(0);
        player.setHealth(20);
    }

}


