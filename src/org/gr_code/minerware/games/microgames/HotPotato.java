package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class HotPotato extends MicroGame {

	public HashMap<String, GamePlayer> achievement;
	private final Map<Block, Integer> fires;
	private int count;

	public HotPotato(Arena arena) {
		super(280, arena, "hot-potato");
		fires = new ConcurrentHashMap<>();
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
			gamePlayer = newHash.get(key);
			maximum = doubleKey;
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name)
				.replace("<seconds>", getSeconds(maximum));
	}

	private void chooseHot() {
		int count = getArena().isHardMode() ? getArena().getCurrentPlayers() / 2 : getArena().getCurrentPlayers() / 3;
		if (count == 0)
			count++;
		for (int i = 0; i < count; i++) {
			GamePlayer x = getArena().getPlayers().get(new Random().nextInt(getArena().getPlayers().size()));
			x.setTask("hot");
			Player player = x.getPlayer();
			for (int j = 0; j < 9; j++)
				player.getInventory().setItem(j, XMaterial.POTATO.parseItem());
			double currentTime = ((double) System.currentTimeMillis()) / 1000;
			String achievement = getSeconds(currentTime);
			x.setAchievement(achievement);
		}
	}

	@Override
	public void secondStartGame() {
		count = getArena().isHardMode() ? 2 : 0;
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		chooseHot();
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

	@Override
	public void check() {
		if (getTime() % 5 != 0)
			return;
		if (fires.containsValue(getTime()))
			for (Block bl : fires.keySet()) {
				if (fires.get(bl) != getTime())
					continue;
				bl.setType(Material.AIR);
				fires.remove(bl);
			}
		List<GamePlayer> playerList = getArena().getPlayers();
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME))
			setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			int y = player.getLocation().getBlockY();
			if (y <= param_y)
				onLose(player, true);
			if (gamePlayer.getTask() != null && gamePlayer.getTask().equals("hot"))
				hotIsRunning(player);
		});
	}

	private void hotIsRunning(Player player) {
		Location l = player.getLocation();
		if (l.clone().add(0, -0.5, 0).getBlock().getType() != Material.AIR
				&& l.getBlock().getType() == Material.AIR) {
			ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.FIRE.parseItem()), l.getBlock());
			fires.put(l.getBlock(), getTime() - 20);
		}
		player.addPotionEffect(PotionEffectType.SPEED.createEffect(60, 1 + count));
		ManageHandler.getModernAPI().playOutParticle(l, 1F, ModernMinerAPI.MinerParticle.LAVA, 1F, 20);
		ManageHandler.getModernAPI().playOutParticle(l, 1F, ModernMinerAPI.MinerParticle.FLAME, 1F, 20);
		if (!ManageHandler.getModernAPI().oldVersion())
			player.setFireTicks(40);
	}

	@Override
	public void end() {
		fires.keySet().forEach(b -> b.setType(Material.AIR));
		fires.clear();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (gamePlayer.getTask() != null && gamePlayer.getTask().equals("hot")) {
				onLose(player, false);
				double firstAch = Double.parseDouble(gamePlayer.getAchievement());
				double currentTime = ((double) System.currentTimeMillis()) / 1000;
				double secondAch = Double.parseDouble(getSeconds(currentTime));
				if (secondAch - firstAch > 0)
					achievement.put(Double.toString(secondAch - firstAch), gamePlayer);
				gamePlayer.setAchievement(null);
			}
			if (gamePlayer.getState() == State.PLAYING_GAME)
				onWin(player, false);
		});
		super.end();
	}

	@Override
	public Game getGame() {
		return Game.HOT_POTATO;
	}

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
	public void aFinish(boolean forceStop) {
		fires.keySet().forEach(b -> b.setType(Material.AIR));
		fires.clear();
		super.aFinish(forceStop);
	}

	@Override
	public ItemStack getGameItemStack() {
		return ItemBuilder.start(requireNonNull(XMaterial.POTATO.parseItem())).setDisplayName("&6&lHOT POTATO").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageByEntityEvent))
			return;
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getEntity() instanceof Player))
			return;
		if (!(e.getDamager() instanceof Player))
			return;
		Player player = (Player) e.getDamager(), p2 = (Player) e.getEntity();
		UUID uuid2 = p2.getUniqueId(), uuid = player.getUniqueId();
		GamePlayer x = requireNonNull(getArena().getPlayer(uuid)), x2 = getArena().getPlayer(uuid2);
		if (requireNonNull(x).getState() != State.PLAYING_GAME)
			return;
		if (requireNonNull(x2).getState() != State.PLAYING_GAME)
			return;
		if (!(x.getTask() != null && x2.getTask() == null && x.getTask().equals("hot")))
			return;
		HotPotato hotP = (HotPotato) getArena().getMicroGame();
		x2.setTask("hot");
		x.setTask(null);
		double firstAch = Double.parseDouble(x.getAchievement());
		double currentTime = ((double) System.currentTimeMillis()) / 1000;
		String achForEnt = getSeconds(currentTime);
		double secondAch = Double.parseDouble(achForEnt);
		if (secondAch - firstAch > 0)
			requireNonNull(hotP).achievement.put((secondAch - firstAch) + "", x);
		x.setAchievement(null);
		x2.setAchievement(achForEnt);
		for (int j = 0; j < 9; j++)
			p2.getInventory().setItem(j, XMaterial.POTATO.parseItem());
		player.getInventory().clear();
	}

}
