package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class DangerousCactus extends MicroGame {

    public DangerousCactus(Arena arena) {
        super(480, arena, "dangerous-cactus");
    }

    private void generateCacti() {
        Cuboid cuboid = getArena().getProperties().getCuboid();
        Location first = getArena().getProperties().getFirstLocation();
        Location second = getArena().getProperties().getSecondLocation();
        List<Location> locationsY = cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1).collect(Collectors.toList());
        locationsY.forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.SAND.parseItem()), l.getBlock()));
        List<Location> locations = new ArrayList<>();
        locations.addAll(locationsY.stream().filter(l -> l.getBlockX() == first.getBlockX()).collect(Collectors.toList()));
        locations.addAll(locationsY.stream().filter(l -> l.getBlockZ() == first.getBlockZ()).collect(Collectors.toList()));
        locations.addAll(locationsY.stream().filter(l -> l.getBlockX() == second.getBlockX()).collect(Collectors.toList()));
        locations.addAll(locationsY.stream().filter(l -> l.getBlockZ() == second.getBlockZ()).collect(Collectors.toList()));
        for (int i = 0; i < getArena().getProperties().getSquares().length; i ++) {
            Properties.Square square = getArena().getProperties().getSquares()[i];
            if (getArena().getProperties().getType().equals("MICRO") || i != 4)
                for (int j = 0; j < square.getLocations().size(); j += 3) for (int k = 0; k < 3; k ++)
                    ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.CACTUS.parseItem()), square.getLocations().get(j).clone().add(0,1 + k,0).getBlock());
        }
        for (int i = 0; i < locations.size(); i += 2) for (int k = 0; k < 3; k ++)
            ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.CACTUS.parseItem()), locations.get(i).clone().add(0,1 + k,0).getBlock());

    }

    @Override
    public void secondStartGame() {
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        int count = getArena().isHardMode() ? 3 : 2;
        ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
                .addEnchantment(Enchantment.KNOCKBACK, count, true).build();
        generateCacti();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            player.teleport(cuboid.getCenter());
            player.getInventory().setItem(0, item);
            player.getInventory().setHeldItemSlot(0);
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == GamePlayer.State.PLAYING_GAME).count() == 1) setTime(1);
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
                .forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.DANGEROUS_CACTUS;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.CACTUS.parseItem())).setDisplayName("&6&lDANGEROUS CACTUS").build();
    }

    @Override
    public void event(Event event) {
        if (event instanceof EntityDamageEvent) entityDamage(event);
    }

    private void entityDamage(Event event) {
        EntityDamageEvent e = (EntityDamageEvent) event;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
        if (gamePlayer.getState() != GamePlayer.State.PLAYING_GAME) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.CONTACT) onLose(player, true);
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        e.setCancelled(false);
        e.setDamage(0);
        player.setHealth(20);
    }

}


