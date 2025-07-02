package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
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
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class FallingArrows extends MicroGame {

    public FallingArrows(Arena arena) {
        super(180, arena, "falling-arrows");
    }

    private void generateBlocks() {
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getProperties().destroySquares();
        ItemStack itemStack = getItem(getRandomLocation(getArena()).add(0,-1,0).getBlock());
        int count = 1;
        switch (getArena().getProperties().getType()) {
            case "MEGA": count = 4; break;
            case "DEFAULT": count = 3; break;
            case "MINI": count = 2; break;
        }
        if (getArena().isHardMode() && !getArena().getProperties().getType().equals("MICRO")) count --;
        for (int i = 0; i < count; i ++) {
            Location random = getRandomLocation(getArena()).add(-1,3,-1);
            int firstX = Math.random() < 0.5 ? 3 : 2;
            int firstZ = firstX == 3 ? 2 : 3;
            for (int x = 0 ; x < firstX; x ++) for (int z = 0; z < firstZ; z ++) {
                Location location = random.clone().add(x, 0, z);
                if (cuboid.notInside(location)) continue;
                ManageHandler.getModernAPI().setBlock(itemStack, location.getBlock());
            }
        }
    }

    @Override
    public void secondStartGame() {
        int count = getArena().isHardMode() ? 3 : 2;
        ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
                .addEnchantment(Enchantment.KNOCKBACK, count, true).build();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        generateBlocks();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            player.getInventory().setItem(0, item);
            player.getInventory().setHeldItemSlot(0);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    private void spawnArrows() {
        Location first = getArena().getProperties().getFirstLocation();
        World world = requireNonNull(first.getWorld());
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 15)
                .forEach(location -> world.spawnEntity(location.clone().add(Math.random() / 2, 0, Math.random() / 2), EntityType.ARROW));
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        if (getTime() < 70 && getTime() % 10 == 0) spawnArrows();
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
                .forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.FALLING_ARROWS;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.DIRT.parseItem())).setDisplayName("&c&lFALLING ARROWS").build();
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
        if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) onLose(player, true);
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        e.setCancelled(false);
        e.setDamage(0);
        player.setHealth(20);
    }

}


