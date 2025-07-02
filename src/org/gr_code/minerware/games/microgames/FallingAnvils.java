package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class FallingAnvils extends MicroGame {

	private int countAnvils;
	private Predicate<Location> predicateRandom;
	private Consumer<Location> predicateForEach;

	public FallingAnvils(Arena arena) {
		super(480, arena, "falling-anvils");
		countAnvils = 0;
	}

	@Override
	public String getAchievementForMsg() {
		return requireNonNull(getString("messages.achievement")).replace("<count>", Integer.toString(countAnvils));
	}

	private void createFloor() {
		getArena().getProperties().destroySquares();
		Location first = getArena().getProperties().getFirstLocation();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.BEDROCK.parseItem()),
						l.getBlock()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void secondStartGame() {
		predicateRandom = getArena().isHardMode() ? l -> Math.random() <= 0.12 : l -> Math.random() <= 0.08;
		predicateForEach = ManageHandler.getModernAPI().oldVersion() ? l -> {
			FallingBlock block = requireNonNull(l.getWorld()).spawnFallingBlock(l, Material.ANVIL, (byte) 0);
			ManageHandler.getModernAPI().setHurtEntities(block);
			countAnvils++;
		} : l -> {
			// Use BlockData for modern versions instead of deprecated MaterialData
			FallingBlock block = requireNonNull(l.getWorld()).spawnFallingBlock(l, Material.ANVIL.createBlockData());
			block.setHurtEntities(true);
			countAnvils++;
		};
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		createFloor();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation()))
				player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
			gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
		});
	}

	private void spawnAnvils() {
		Location second = getArena().getProperties().getSecondLocation();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == second.getBlockY())
				.filter(predicateRandom).forEach(predicateForEach);
	}

	@Override
	public void check() {
		if (getTime() % 5 != 0)
			return;
		if (getTime() % 10 == 0)
			spawnAnvils();
		List<GamePlayer> playerList = getArena().getPlayers();
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME))
			setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			int y = player.getLocation().getBlockY();
			if (y <= param_y)
				onLose(player, true);
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
		return Game.FALLING_ANVILS;
	}

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
	public ItemStack getGameItemStack() {
		return ItemBuilder.start(requireNonNull(XMaterial.ANVIL.parseItem())).setDisplayName("&8&lFALLING ANVILS")
				.build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageEvent))
			return;
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (e.getCause() != EntityDamageEvent.DamageCause.FALLING_BLOCK)
			return;
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME)
			return;
		onLose(player, true);
	}

}
