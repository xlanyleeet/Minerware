package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class ShearSheeps extends MicroGame {
	
    public List<Sheep> sheepList;

	public ShearSheeps(Arena arena) {
		super(380, arena, "shear-sheep");
		sheepList = new ArrayList<>();
	}

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
    	if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		super.onWin(player, teleport);
		String feedAll = translate(requireNonNull(getString("messages.sheared-eight-sheep")));
    	sendMessage(player, feedAll);
    }

    private void generateFarm() {
    	getArena().getProperties().destroySquares();
    	Location first = getArena().getProperties().getFirstLocation();
    	Location second = getArena().getProperties().getSecondLocation();
    	Cuboid cuboid = getArena().getProperties().getCuboid();
    	cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
    	.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.GRASS_BLOCK.parseItem()), l.getBlock()));
		cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1)
				.filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
						|| l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
				.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.OAK_FENCE.parseItem()), l.getBlock()));
    }

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> Integer.parseInt(x.getTask()) > 0).collect(Collectors.toList());
		if (achievement.isEmpty()) return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = Integer.parseInt(key.getTask());
			if (doubleKey <= maximum) continue;
			maximum = doubleKey;
			gamePlayer = key;
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<count>", Integer.toString(maximum));
	}

    @Override
    public void secondStartGame() {
		generateFarm();
		for (int i = 0; i < getArena().getCurrentPlayers() * 4; i ++) spawnSheep();
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
		ItemStack item = ItemBuilder.start(requireNonNull(requireNonNull(XMaterial.SHEARS.parseItem()))).setUnbreakable(true).build();
		getArena().getPlayers().forEach(gamePlayer -> {
    		gamePlayer.setTask("0");
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setItem(0, item);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    	
    }
    
    private void spawnSheep() {
	    Sheep sheep = (Sheep) requireNonNull(getArena().getProperties().getFirstLocation().getWorld()).spawnEntity(getRandomLocation(getArena()), EntityType.SHEEP);
		if (!ManageHandler.getNMS().oldVersion()) {
			sheep.setSilent(true);
			sheep.setAI(false);
		} else ManageHandler.getNMS().setNoAI(sheep);
		sheepList.add(sheep);
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		int countPlayers = getArena().getCurrentPlayers();
		int hard = getArena().isHardMode() ? 5 : 0;
		int count = countPlayers <= 5 ? 20 + hard : countPlayers <= 10 ? 15 + hard : 10 + hard;
		if (getTime() % count == 0) spawnSheep();
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
		sheepList.forEach(Entity::remove);
		sheepList.clear();
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.SHEAR_SHEEP;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
    public void aFinish(boolean forceStop) {
    	sheepList.forEach(Entity::remove);
        sheepList.clear();
    	super.aFinish(forceStop);
    }

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.SHEARS.parseItem())).setDisplayName("&e&lSHEAR SHEEPS").build();
	}

	@Override
	public void event(Event event) {
    	if (event instanceof PlayerShearEntityEvent) playerShearEntity(event);
    	else if (event instanceof CreatureSpawnEvent) creatureSpawn(event);

	}

	@SuppressWarnings("SuspiciousMethodCalls")
	private void playerShearEntity(Event event) {
		PlayerShearEntityEvent e = (PlayerShearEntityEvent) event;
		Player player = e.getPlayer();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (sheepList.size() == 0) return;
		if (!sheepList.contains(e.getEntity())) return;
		gamePlayer.setTask((Integer.parseInt(gamePlayer.getTask()) + 1) + "");
		String msg = translate(requireNonNull(getString("messages.plus-sheep")).replace("<countSheep>", gamePlayer.getTask()));
		if (Integer.parseInt(gamePlayer.getTask()) == 8) onWin(player, false);
		else {
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			sendMessage(player, msg);
		}
	}

	private void creatureSpawn(Event event) {
		CreatureSpawnEvent e = (CreatureSpawnEvent) event;
		if (!(e.getEntity() instanceof Sheep)) return;
		e.setCancelled(false);
	}

}