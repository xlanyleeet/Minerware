package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

@SuppressWarnings("deprecation")
public class RideCow extends MicroGame {
	
    public HashMap<String, GamePlayer> achievement;
    public List<Cow> cowList;

	public RideCow(Arena arena) {
		super(380, arena, "ride-cow");
		achievement = new HashMap<>();
		cowList = new ArrayList<>();
	}

    @Override
    public void startGame() {
		if (!ManageHandler.getNMS().oldVersion())
			getArena().getPlayers().forEach(gamePlayer -> gamePlayer.getPlayer().setCollidable(false));
        super.startGame();
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
	
	private void spawnCows() {
		int halfPlayers = getArena().getCurrentPlayers() / 2;
		int countCows = getArena().isHardMode() ? halfPlayers : (getArena().getCurrentPlayers() - halfPlayers) / 3 + halfPlayers;
		for (int i = 0; i < countCows; i ++) {
			Location random = getRandomLocation(getArena());
			random.setPitch(0);
			Cow cow = (Cow) requireNonNull(getArena().getProperties().getFirstLocation().getWorld()).spawnEntity(random, EntityType.COW);
			if (!ManageHandler.getNMS().oldVersion()) {
				cow.setCollidable(false);
				if (!getArena().isHardMode()) cow.setAI(false);
				cow.setSilent(true);
			} else if (!getArena().isHardMode()) ManageHandler.getNMS().setNoAI(cow);
			cowList.add(cow);
		}
	}

    @Override
    public void secondStartGame() {
		getArena().getProperties().destroySquares();
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    	spawnCows();
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = requireNonNull(player).getLocation().getBlockY();
        	if (y <= param_y) onLose(player, true);
    	});
    }

    private List<Cow> getFilterCows() {
		if (ManageHandler.getNMS().oldVersion())
			return cowList.stream().filter(cow -> cow.getPassenger() != null && cow.getPassenger() instanceof Player)
					.filter(cow -> isInGame(cow.getPassenger().getUniqueId())).collect(Collectors.toList());
		return cowList.stream().filter(cow -> cow.getPassengers().size() == 1 && cow.getPassengers().get(0) instanceof Player)
				.filter(cow -> isInGame(cow.getPassengers().get(0).getUniqueId())).collect(Collectors.toList());
	}

	private void setWinners() {
		if (ManageHandler.getNMS().oldVersion()) getFilterCows().forEach(cow-> {
			Player player = requireNonNull(((Player) cow.getPassenger()));
			GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
			onWin(player, false);
			if (gamePlayer.getAchievement() != null) {
				double firstAch = Double.parseDouble(gamePlayer.getAchievement());
				double currentTime = ((double)System.currentTimeMillis()) / 1000;
				double secondAch = Double.parseDouble(getSeconds(currentTime));
				if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
				gamePlayer.setAchievement(null);
			}
		});
		else getFilterCows().forEach(cow-> {
			Player player = requireNonNull(((Player) cow.getPassengers().get(0)));
			GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
			onWin(player, false);
			if (gamePlayer.getAchievement() != null) {
				double firstAch = Double.parseDouble(gamePlayer.getAchievement());
				double currentTime = ((double)System.currentTimeMillis()) / 1000;
				double secondAch = Double.parseDouble(getSeconds(currentTime));
				if (secondAch - firstAch > 0) achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
				gamePlayer.setAchievement(null);
			}
		});
	}

     @Override
    public void end() {
		setWinners();
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
		cowList.forEach(Entity::remove);
		cowList.clear();
		super.end();
	}
    
    @Override
    public Game getGame() {
        return Game.RIDE_COW;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
    public void aFinish(boolean forceStop) {
    	cowList.forEach(Entity::remove);
        cowList.clear();
        super.aFinish(forceStop);
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.LEATHER.parseItem())).setDisplayName("&e&lRIDE THE COW").build();
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntityDamageByEntityEvent) entityDamageByEntity(event);
		else if (event instanceof PlayerInteractEntityEvent) playerInteractEntity(event);
		else if (event instanceof EntityDismountEvent) entityDismount(event);
		else if (event instanceof CreatureSpawnEvent) creatureSpawn(event);
	}

	private void entityDamageByEntity(Event event) {
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getEntity() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		e.setCancelled(false);
		player.setHealth(20);
		if (cowList.size() == 0) return;
		if (ManageHandler.getNMS().oldVersion()) {
			cowList.stream().filter(cow -> cow.getPassenger() != null && cow.getPassenger() == player).forEach(cow->{
				cow.eject();
				player.setVelocity(e.getDamager().getLocation().getDirection().normalize().multiply(1.5));
			});
			if (gamePlayer.getAchievement() == null) return;
			double firstAch = Double.parseDouble(gamePlayer.getAchievement());
			double currentTime = ((double)System.currentTimeMillis()) / 1000;
			double secondAch = Double.parseDouble(getSeconds(currentTime));
			if (secondAch - firstAch > 0) achievement.put((secondAch - firstAch) + "", gamePlayer);
			gamePlayer.setAchievement(null);
			return;
		}
		cowList.stream().filter(cow -> cow.getPassengers().size() == 1 && cow.getPassengers().get(0) == player).forEach(cow->{
			cow.removePassenger(player);
			player.setVelocity(e.getDamager().getLocation().getDirection().normalize().multiply(1.5));
		});
		if (gamePlayer.getAchievement() == null) return;
		double firstAch = Double.parseDouble(gamePlayer.getAchievement());
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		double secondAch = Double.parseDouble(getSeconds(currentTime));
		if (secondAch - firstAch > 0) achievement.put((secondAch - firstAch) + "", gamePlayer);
		gamePlayer.setAchievement(null);
	}

	private void playerInteractEntity(Event event) {
		PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
		Player player = e.getPlayer();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		if (!(e.getRightClicked() instanceof Cow)) return;
		if (ManageHandler.getNMS().oldVersion()) {
			if (e.getRightClicked().getPassenger() != null) return;
			e.getRightClicked().setPassenger(player);
			e.setCancelled(true);
			double currentTime = ((double)System.currentTimeMillis()) / 1000;
			String achievement = getSeconds(currentTime);
			gamePlayer.setAchievement(achievement);
			return;
		}
		if (e.getRightClicked().getPassengers().size() != 0) return;
		e.getRightClicked().addPassenger(player);
		e.setCancelled(true);
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		String achievement = getSeconds(currentTime);
		gamePlayer.setAchievement(achievement);
	}

	private void entityDismount(Event event) {
		EntityDismountEvent e = (EntityDismountEvent) event;
		if (!(e.getDismounted() instanceof Cow)) return;
		Player player = (Player) e.getEntity();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		if (gamePlayer.getAchievement() == null) return;
		double firstAch = Double.parseDouble(gamePlayer.getAchievement());
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		double secondAch = Double.parseDouble(getSeconds(currentTime));
		if (secondAch - firstAch > 0) achievement.put((secondAch - firstAch) + "", gamePlayer);
		gamePlayer.setAchievement(null);
	}

	private void creatureSpawn(Event event) {
		CreatureSpawnEvent e = (CreatureSpawnEvent) event;
		if (!(e.getEntity() instanceof Cow)) return;
		e.setCancelled(false);
	}

}