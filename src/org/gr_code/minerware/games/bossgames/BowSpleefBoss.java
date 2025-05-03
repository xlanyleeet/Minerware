package org.gr_code.minerware.games.bossgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.BossGame;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.listeners.game.ProjectileHit_Games;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;

public class BowSpleefBoss extends BossGame {

    public BowSpleefBoss(Arena arena) {
        super(2480, arena, "bow-spleef");
    }

    private void generatePlatform() {
        getArena().getProperties().destroySquares();
        Location first = getArena().getProperties().getFirstLocation();
        getArena().getProperties().getCuboid().getLocations().stream().filter(loc-> loc.getBlockY() == first.getY())
                .forEach(loc-> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.TNT.parseItem()), loc.getBlock()));
    }
    
    @Override
    public void secondStartGame() {
        generatePlatform();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        ItemStack bow = ItemBuilder.start(requireNonNull(XMaterial.BOW.parseItem())).setUnbreakable(true)
                .addEnchantment(Enchantment.ARROW_FIRE, 1, true).addEnchantment(Enchantment.ARROW_INFINITE, 1, true).build();
        getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            gamePlayer.setState(State.PLAYING_GAME);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setItem(1, XMaterial.ARROW.parseItem());
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            player.getInventory().setItem(0, bow);
            player.getInventory().setHeldItemSlot(0);
    	});
    }
    
    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
        if (getTime() % 20 == 0) playerList.forEach(x-> x.getPlayer().setLevel(getTime() / 20));
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
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
        return Game.BOSS_BOW_SPLEEF;
    }

	@Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.BOW.parseItem())).setDisplayName("&3&lBOW-SPLEEF").build();
	}

	@Override
    public void event(Event event) {
        if (!(event instanceof ProjectileHitEvent)) return;
        ProjectileHitEvent e = (ProjectileHitEvent) event;
        Location firstLocation = getArena().getProperties().getFirstLocation();
        Block hitBlock = requireNonNull(ProjectileHit_Games.getHitBlockNMS(e));
        if (hitBlock.getY() > firstLocation.getBlockY() + 2) return;
        World world = hitBlock.getWorld();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        float randomDistance = (((float)new Random().nextInt(7)) + 10f )/ 10f;
        Location first = hitBlock.getLocation().add(-3, 0, -3);
        List<Location> locations = new ArrayList<>();
        ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.AIR.parseItem()), hitBlock);
        world.spawnEntity(hitBlock.getLocation().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
        for (int x = 0; x < 7; x ++) for (int z = 0; z < 7; z ++) locations.add(first.clone().add(x, 0, z));
        locations.stream().filter(l -> l.distance(hitBlock.getLocation()) <= randomDistance)
                .filter(l -> l.getBlock().getType() == Material.TNT && !cuboid.notInside(l) && Math.random() <= 0.6)
                .forEach(location -> {
                    ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.AIR.parseItem()), location.getBlock());
                    world.spawnEntity(location.add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
                });
    }

}