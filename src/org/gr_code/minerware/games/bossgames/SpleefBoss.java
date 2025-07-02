package org.gr_code.minerware.games.bossgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.games.BossGame;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;

public class SpleefBoss extends BossGame {

	public SpleefBoss(Arena arena) {
		super(2480, arena, "spleef");
	}
    
	@Override
    public void secondStartGame() {
		List<Location> locations = getArena().getProperties().getCuboid().getLocations();
		Location first = getArena().getProperties().getFirstLocation();
		if (getArena().isHardMode()) {
			getArena().getProperties().destroySquares();
			locations.stream().filter(l -> l.getBlockY() == first.getBlockY()).forEach(l -> l.getBlock().setType(Material.AIR));
		}
		Predicate<Location> predicate = !getArena().isHardMode() ? l -> l.getBlockY() <= first.getBlockY() + 4
				 : l -> l.getBlockY() != first.getBlockY() && l.getBlockY() <= first.getBlockY() + 4;
		locations.stream().filter(predicate)
			.forEach(loc-> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.SNOW_BLOCK.parseItem()), loc.getBlock()));
		ItemStack shovel = ItemBuilder.start(requireNonNull(XMaterial.DIAMOND_SHOVEL.parseItem()))
				.setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3, true).build();
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			gamePlayer.setState(GamePlayer.State.PLAYING_GAME);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			player.teleport(getRandomLocation(getArena()).add(0, 5, 0));
			player.getInventory().setItem(0, shovel);
			player.getInventory().setHeldItemSlot(0);
    	});
    }
    
    @Override
    public void check() {
		if (getTime() % 10 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
    	if (getTime() % 20 == 0) playerList.forEach(gamePlayer -> gamePlayer.getPlayer().setLevel(getTime() / 20));
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    		if (player.getFoodLevel() > 1) player.setFoodLevel(player.getFoodLevel() - 1);
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
        return Game.BOSS_SPLEEF;
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.SNOW_BLOCK.parseItem())).setDisplayName("&f&lSPLEEF").build();
	}

	@Override
	public void event(Event event) {
    	if (!(event instanceof BlockBreakEvent)) return;
		BlockBreakEvent e = (BlockBreakEvent) event;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() !=State.PLAYING_GAME) return;
		ItemStack ItemStack = Utils.getItem(e.getBlock());
		if (!ItemStack.isSimilar(XMaterial.SNOW_BLOCK.parseItem())) return;
		if (player.getFoodLevel() < 20) player.setFoodLevel(player.getFoodLevel() + 1);
		e.setCancelled(false);
	}

}


