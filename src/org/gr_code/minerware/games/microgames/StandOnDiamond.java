package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties.Square;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class StandOnDiamond extends MicroGame {
	
	private final HashMap<String, GamePlayer> achievement;

	public StandOnDiamond(Arena arena) {
		super(280, arena, "stand-on-the-diamond");
		achievement = new HashMap<>();
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		HashMap<String, GamePlayer> newHash = new HashMap<>();
		achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
				.forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
		if (newHash.isEmpty()) return "";
		double maximum = 0;
		GamePlayer gamePlayer = null;
		for (String key : newHash.keySet()) {
			double doubleKey = Double.parseDouble(key);
			if (doubleKey <= maximum) continue;
			gamePlayer = newHash.get(key);
			maximum = doubleKey;
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
	}
	
	private void generateDiamondSquares() {
		int[] sizesCuboid;
		switch (getArena().getProperties().getType()) {
			case "MICRO":
				sizesCuboid = new int[]{4};
				break;
			case "MINI":
				sizesCuboid = new int[]{9, 9};
				break;
			default:
				sizesCuboid = new int[]{9, 9, 9};
		}
		for (int i : sizesCuboid)
			getArena().getProperties().getSquares()[new Random().nextInt(i)].getLocations()
			.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.DIAMOND_BLOCK.parseItem()), l.getBlock()));
	}

    @Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		generateDiamondSquares();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
				.addEnchantment(Enchantment.KNOCKBACK, 2, true).build();
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setItem(0, item);
    		player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
		if (getArena().isHardMode()) setTime(160);
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
    	Location first = getArena().getProperties().getFirstLocation();
		if (getTime() == 100) removeFloor();
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y >= first.getBlockY() + 2 && gamePlayer.getAchievement() == null) {
    			double currentTime = ((double)System.currentTimeMillis()) / 1000;
    			String achievement = getSeconds(currentTime);
    			gamePlayer.setAchievement(achievement);
    		} else if (y < first.getBlockY() + 2 && gamePlayer.getAchievement() != null) {
    			double firstAch = Double.parseDouble(gamePlayer.getAchievement());
    			double currentTime = ((double)System.currentTimeMillis()) / 1000;
    			double secondAch = Double.parseDouble(getSeconds(currentTime));
    			if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
    			gamePlayer.setAchievement(null);
    		}
    		if (y <= param_y) onLose(player, true);
    	});
    }

    private void removeFloor() {
		Location first = getArena().getProperties().getFirstLocation();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.AIR.parseItem()), l.getBlock()));
		for (Square sq : getArena().getProperties().getSquares()) 
			sq.getLocations().stream().filter(l -> !getItem(l.getBlock()).isSimilar(XMaterial.DIAMOND_BLOCK.parseItem()))
					.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.AIR.parseItem()), l.getBlock()));
	}

	@Override
	public void end() {
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (gamePlayer.getAchievement() != null) {
				double firstAch = Double.parseDouble(gamePlayer.getAchievement());
				double currentTime = ((double) System.currentTimeMillis()) / 1000;
				double secondAch = Double.parseDouble(getSeconds(currentTime));
				if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
				gamePlayer.setAchievement(null);
			}
			if (gamePlayer.getState() == State.PLAYING_GAME) onWin(player, false);
		});
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.STAND_ON_DIAMOND;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.DIAMOND_BLOCK.parseItem())).setDisplayName("&b&lSTAND ON THE DIAMOND").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageEvent)) return;
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		if (requireNonNull(getArena().getPlayer(player.getUniqueId())).getState() != State.PLAYING_GAME) return;
		e.setCancelled(false);
		e.setDamage(0);
		player.setHealth(20);
	}

}