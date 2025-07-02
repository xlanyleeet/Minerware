package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.gr_code.minerware.api.events.PlayerCollectedEmeraldsEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.bukkit.Particle;
import org.gr_code.minerware.manager.type.modern.ModernMinerAPI;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.bukkit.Bukkit.getPluginManager;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class CatchEmeralds extends MicroGame {

	private final List<ArmorStand> standList = new ArrayList<>();
	private float yaw;

	public CatchEmeralds(Arena arena) {
		super(580, arena, "catch-emeralds");
		yaw = 360;
	}

	@Override
	public void onWin(Player player, boolean teleport) {
		UUID uuid = player.getUniqueId();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME)
			return;
		super.onWin(player, teleport);
		String all = getString("messages.collected-four-emeralds");
		sendMessage(player, translate(all));
	}

	@Override
	public void startGame() {
		getArena().getPlayers().forEach(gamePlayer -> gamePlayer.setTask("0"));
		super.startGame();
	}

	private void generatePlatform() {
		Properties properties = getArena().getProperties();
		properties.destroySquares();
		Location first = properties.getFirstLocation();
		properties.getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.forEach(loc -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.SLIME_BLOCK.parseItem()),
						loc.getBlock()));
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		List<GamePlayer> achievement = getArena().getPlayers().stream()
				.filter(x -> Integer.parseInt(x.getTask()) > 0).collect(Collectors.toList());
		if (achievement.isEmpty())
			return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = Integer.parseInt(key.getTask());
			if (doubleKey <= maximum)
				continue;
			maximum = doubleKey;
			gamePlayer = key;
		}
		assert gamePlayer != null;
		String name = gamePlayer.getPlayer().getName();
		assert achievementMsg != null;
		return achievementMsg.replace("<name>", name)
				.replace("<count>", Integer.toString(maximum));
	}

	@Override
	public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
		generatePlatform();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation()))
				player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
			gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
		});
		spawnAllStands();
	}

	private void spawnAllStands() {
		for (int i = 0; i < getArena().getCurrentPlayers() * 2; i++) {
			Location location = getRandomLocation(getArena()).add(0, 10, 0);
			location.setYaw(yaw);
			ArmorStand ar = (ArmorStand) requireNonNull(location.getWorld()).spawnEntity(location,
					EntityType.ARMOR_STAND);
			ar.setGravity(false);
			requireNonNull(ar.getEquipment()).setHelmet(XMaterial.EMERALD_BLOCK.parseItem());
			ar.setVisible(false);
			standList.add(ar);
		}
	}

	@Override
	public void check() {
		if (yaw - 3 <= 0)
			yaw = 360;
		else
			yaw -= 3;
		standList.forEach(ar -> {
			Location l = ar.getLocation();
			l.setYaw(yaw);
			ar.teleport(l);
		});
		if (getTime() % 2 != 0)
			return;
		checkToSpawnStand();
		String plus = translate(getString("messages.plus-emerald"));
		List<GamePlayer> players = getArena().getPlayers();
		if (players.stream().allMatch(x -> x.getState() != State.PLAYING_GAME))
			setTime(1);
		players.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME
				|| gamePlayer.getState() == State.WINNER_IN_GAME)
				.forEach(gamePlayer -> updateForEachInCheck(gamePlayer, plus));
	}

	private void checkToSpawnStand() {
		int countPlayers = getArena().getCurrentPlayers();
		int hard = getArena().isHardMode() ? 5 : 0;
		int count = countPlayers <= 5 ? 25 + hard : countPlayers <= 10 ? 20 + hard : 10 + hard;
		if (getTime() % count == 0)
			spawnStand();
	}

	private void updateForEachInCheck(GamePlayer gamePlayer, String plus) {
		Player player = gamePlayer.getPlayer();
		double loseY = getArena().getProperties().getFirstLocation().getBlockY();
		int playerY = player.getLocation().getBlockY();
		Stream<ArmorStand> streamArmor = standList.stream()
				.filter(ar -> ar.getLocation().distance(player.getLocation()) <= 1.5);
		for (ArmorStand ar : streamArmor.collect(Collectors.toList()))
			collectEmerald(ar, player, gamePlayer, plus);
		if ((playerY == loseY + 1 || playerY == loseY + 2)
				&& !getArena().getProperties().getCuboid().notInside(player.getLocation()))
			setVelocityUP(player);
		else if (playerY <= loseY - 1)
			onLose(player, true);
	}

	private void setVelocityUP(Player player) {
		double yVelocity = player.getVelocity().getY();
		if (yVelocity < 0)
			player.setVelocity(new Vector(0, Math.abs(yVelocity), 0));
		player.setVelocity(new Vector(0, 1.5, 0));
	}

	private void collectEmerald(ArmorStand ar, Player player, GamePlayer gamePlayer, String plus) {
		ManageHandler.getModernAPI().playOutParticle(ar.getLocation(), player, 0.5F,
				ModernMinerAPI.MinerParticle.HAPPY_VILLAGER, 0, 20);
		player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
		gamePlayer.setTask(Integer.toString(Integer.parseInt(gamePlayer.getTask()) + 1));
		if (Integer.parseInt(gamePlayer.getTask()) == 4)
			onWin(player, false);
		else
			Utils.sendMessage(player, translate(plus.replace("<emeralds>", gamePlayer.getTask())));
		if (Integer.parseInt(gamePlayer.getTask()) == 8) {
			PlayerCollectedEmeraldsEvent event = new PlayerCollectedEmeraldsEvent(player, getArena(), ar.getLocation());
			getPluginManager().callEvent(event);
		}
		ar.remove();
		standList.remove(ar);
	}

	private void spawnStand() {
		if (standList.size() >= (getArena().getCurrentPlayers() + 1) * 2)
			return;
		Location location = getRandomLocation(getArena()).add(0, 10, 0);
		location.setYaw(yaw);
		ArmorStand ar = (ArmorStand) requireNonNull(location.getWorld()).spawnEntity(location, EntityType.ARMOR_STAND);
		ar.setGravity(false);
		requireNonNull(ar.getEquipment()).setHelmet(XMaterial.EMERALD_BLOCK.parseItem());
		ar.setVisible(false);
		standList.add(ar);
	}

	@Override
	public void end() {
		getArena().getPlayers().stream().filter(x -> x.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
		standList.forEach(Entity::remove);
		standList.clear();
		super.end();
	}

	@Override
	public Game getGame() {
		return Game.CATCH_EMERALDS;
	}

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
	public void aFinish(boolean forceStop) {
		super.aFinish(forceStop);
		standList.forEach(Entity::remove);
		standList.clear();
	}

	@Override
	public ItemStack getGameItemStack() {
		return ItemBuilder.start(requireNonNull(XMaterial.EMERALD.parseItem())).setDisplayName("&a&lCATCH EMERALDS")
				.build();
	}

}
