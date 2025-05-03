package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class LandOnTheFloor extends MicroGame {

	private double firstTime;
	public HashMap<String, GamePlayer> achievement;

	public LandOnTheFloor(Arena arena) {
		super(480, arena, "land-on-the-floor");
		achievement = new HashMap<>();
	}

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
    	if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		super.onWin(player, teleport);
    	double currentTime = ((double)System.currentTimeMillis()) / 1000;
    	double secondAch = Double.parseDouble(getSeconds(currentTime));
    	if (secondAch - firstTime >= 0) achievement.put(Double.toString(secondAch - firstTime), gamePlayer);
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
		return requireNonNull(achievementMsg).replace("<name>", name)
				.replace("<seconds>", getSeconds(maximum));
	}
	
	private void generateBlocks() {
		Cuboid cuboid = getArena().getProperties().getCuboid();
		getArena().getProperties().destroySquares();
		Location first = getArena().getProperties().getFirstLocation();
		Location second = getArena().getProperties().getSecondLocation();
		Location center = cuboid.getCenter().clone().add(0, Cuboid.getSize(getArena()) - 2, 0);
		cuboid.getLocations().stream().filter(l -> l.getBlockY() != first.getBlockY() && l.getBlockY() <= second.getBlockY() - 2)
				.filter(l -> Math.random() > 0.95).forEach(l -> {
			double random = Math.random();
			if (random <= 0.9) ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.STONE.parseItem()), l.getBlock());
			else if (random <= 0.98) ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.HAY_BLOCK.parseItem()), l.getBlock());
			else ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.COBWEB.parseItem()), l.getBlock());
		});
		for (int x = 0; x < 3; x ++) for (int z = 0; z < 3; z ++) ManageHandler.getNMS()
				.setBlock(requireNonNull(XMaterial.GOLD_BLOCK.parseItem()), center.clone().add(-1 + x, 0, -1 + z).getBlock());
	}

    @Override
    public void secondStartGame() {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		firstTime = Double.parseDouble(getSeconds(currentTime));
		Cuboid cuboid = getArena().getProperties().getCuboid();
		Location center = cuboid.getCenter().clone().add(0, Cuboid.getSize(getArena()) - 2, 0);
		generateBlocks();
		getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			player.teleport(center.clone().add(0, 1, 0));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1;
		int right_y = getArena().getProperties().getFirstLocation().getBlockY() + 1;
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = requireNonNull(player).getLocation().getBlockY();
    		if (y == right_y) onWin(player, false);
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (gamePlayer.getState() == State.PLAYING_GAME) onLose(player, false);
			int right_y = getArena().getProperties().getFirstLocation().getBlockY() + 2;
			if (player.getLocation().getBlockY() > right_y) player.teleport(getRandomLocation(getArena()));
		});
		super.end();
	}
    
    @Override
    public Game getGame() {
        return Game.LAND_ON_THE_FLOOR;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.FEATHER.parseItem())).setDisplayName("&7&lLAND ON THE FLOOR").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageEvent)) return;
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
		if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
		e.setCancelled(false);
		int count = getArena().isHardMode() ? 4 : 3;
		e.setDamage(e.getDamage() * count);
		if (e.getDamage() < player.getHealth()) return;
		e.setCancelled(true);
		player.setHealth(20);
		Location center = getArena().getProperties().getCuboid().getCenter().clone().add(0, Cuboid.getSize(getArena()) - 2, 0);
		player.teleport(center.clone().add(0, 1, 0));
	}

}