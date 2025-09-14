package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.modern.ModernMinerAPI;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class JumpAndStay extends MicroGame {

	private final HashMap<String, GamePlayer> achievement;

	public JumpAndStay(Arena arena) {
		super(280, arena, "jump-and-stay");
		achievement = new HashMap<>();
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		HashMap<String, GamePlayer> newHash = new HashMap<>();
		achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
				.forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
		if (newHash.isEmpty())
			return "";
		double maximum = 0;
		GamePlayer gamePlayer = null;
		for (String key : newHash.keySet()) {
			double doubleKey = Double.parseDouble(key);
			if (doubleKey <= maximum)
				continue;
			maximum = doubleKey;
			gamePlayer = newHash.get(key);
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
	}

	private void generateSlimes() {
		getArena().getProperties().destroySquares();
		Location f = getArena().getProperties().getFirstLocation();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		cuboid.getLocations().stream().filter(l -> l.getBlockY() == f.getBlockY())
				.forEach(loc -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.SLIME_BLOCK.parseItem()),
						loc.getBlock()));
		cuboid.getLocations().stream().filter(l -> l.getBlockY() == f.getBlockY() + 10)
				.filter(l -> Math.random() <= 0.3)
				.forEach(loc -> ManageHandler.getModernAPI()
						.setBlock(requireNonNull(XMaterial.EMERALD_BLOCK.parseItem()), loc.getBlock()));
	}

	@Override
	public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		generateSlimes();
		ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
				.addEnchantment(Enchantment.KNOCKBACK, 2, true).build();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation()))
				player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
			gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			player.getInventory().setItem(0, item);
			player.getInventory().setHeldItemSlot(0);
		});
	}

	private void destroyEmeralds() {
		Sound sound = requireNonNull(XSound.BLOCK_GLASS_BREAK.parseSound());
		Location first = getArena().getProperties().getFirstLocation();
		getArena().getProperties().getCuboid().getLocations().stream()
				.filter(l -> l.getBlockY() == first.getBlockY() + 10)
				.filter(l -> l.getBlock().getType() != Material.AIR).forEach(l -> {
					if (getItem(l.getBlock()).isSimilar(XMaterial.GLASS.parseItem())) {
						requireNonNull(l.getWorld()).playSound(l, sound, 0.25f, 1);
						ManageHandler.getModernAPI().playOutParticle(l, 1f, ModernMinerAPI.MinerParticle.CLOUD, 5);
						l.getBlock().setType(Material.AIR);
					} else if (Math.random() <= 0.2)
						ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GLASS.parseItem()),
								l.getBlock());
				});
	}

	@Override
	public void check() {
		if (getTime() % 5 != 0)
			return;
		if (getArena().isHardMode() && getTime() % 30 == 0)
			destroyEmeralds();
		List<GamePlayer> playerList = getArena().getPlayers();
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME))
			setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			int y = player.getLocation().getBlockY();
			Block b = requireNonNull(player).getLocation().add(0, -1, 0).getBlock();
			ItemStack IS = getItem(b);
			if (IS.isSimilar(XMaterial.EMERALD_BLOCK.parseItem()) && gamePlayer.getAchievement() == null) {
				double currentTime = ((double) System.currentTimeMillis()) / 1000;
				String achievement = getSeconds(currentTime);
				gamePlayer.setAchievement(achievement);
			} else if (!IS.isSimilar(XMaterial.EMERALD_BLOCK.parseItem()) && gamePlayer.getAchievement() != null) {
				double firstAch = Double.parseDouble(gamePlayer.getAchievement());
				double currentTime = ((double) System.currentTimeMillis()) / 1000;
				double secondAch = Double.parseDouble(getSeconds(currentTime));
				if (secondAch - firstAch > 0)
					achievement.put(Double.toString((secondAch - firstAch)), gamePlayer);
				gamePlayer.setAchievement(null);
			}
			if ((y == param_y + 2 || y == param_y + 3)
					&& !getArena().getProperties().getCuboid().notInside(player.getLocation()))
				setVelocityUP(player);
			else if (y <= param_y)
				onLose(player, true);
		});
	}

	private void setVelocityUP(Player player) {
		double yVelocity = player.getVelocity().getY();
		if (yVelocity < 0)
			player.setVelocity(new Vector(0, Math.abs(yVelocity), 0));
		player.setVelocity(new Vector(0, 1.5, 0));
	}

	@Override
	public void end() {
		int blockY = getArena().getProperties().getFirstLocation().getBlockY();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (player.getLocation().getBlockY() == blockY + 11
					|| player.getLocation().getBlockY() == blockY + 12)
				onWin(player, false);
			if (gamePlayer.getAchievement() != null) {
				double firstAch = Double.parseDouble(gamePlayer.getAchievement());
				double currentTime = ((double) System.currentTimeMillis()) / 1000;
				double secondAch = Double.parseDouble(getSeconds(currentTime));
				if (secondAch - firstAch > 0)
					achievement.put((Double.toString(secondAch - firstAch)), gamePlayer);
				gamePlayer.setAchievement(null);
			}
			if (gamePlayer.getState() == State.PLAYING_GAME)
				onLose(player, false);
		});
		super.end();
	}

	@Override
	public Game getGame() {
		return Game.JUMP_AND_STAY;
	}

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
	public ItemStack getGameItemStack() {
		return ItemBuilder.start(requireNonNull(XMaterial.EMERALD_BLOCK.parseItem())).setDisplayName("&2&lJUMP & STAY")
				.build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageEvent))
			return;
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME)
			return;
		e.setCancelled(false);
		e.setDamage(0);
		player.setHealth(20);
	}

}
