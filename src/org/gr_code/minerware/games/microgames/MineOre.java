package org.gr_code.minerware.games.microgames;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
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
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class MineOre extends MicroGame {

	private double firstTime;
	private ItemStack randItemForTask;
	private String randItemForMsg;
	public HashMap<String, GamePlayer> achievement;
	private static final ItemStack[] ores = {XMaterial.COAL_ORE.parseItem(), XMaterial.DIAMOND_ORE.parseItem(), XMaterial.EMERALD_ORE.parseItem(),
    		XMaterial.GOLD_ORE.parseItem(), XMaterial.IRON_ORE.parseItem(), XMaterial.LAPIS_ORE.parseItem(), XMaterial.REDSTONE_ORE.parseItem()};

	public MineOre(Arena arena) {
		super(280, arena, "miner");
		achievement = new HashMap<>();
	}

    @Override
    public void onWin(Player player, boolean teleport) {
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
    	if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		super.onWin(player, teleport);
		player.setGameMode(GameMode.ADVENTURE);
    	double currentTime = ((double)System.currentTimeMillis()) / 1000;
    	double secondAch = Double.parseDouble(getSeconds(currentTime));
    	if (secondAch - firstTime >= 0) achievement.put(Double.toString(secondAch - firstTime), gamePlayer);
    }

    @Override
    public void startGame() {
		List<String> oresConfig = getStringList("ores");
		int rand = new Random().nextInt(6);
		randItemForTask = XMaterial.valueOf(oresConfig.get(rand).split(":")[0]).parseItem();
		randItemForMsg = oresConfig.get(rand).split(":")[1];
        super.startGame();
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
			maximum = doubleKey;
			gamePlayer = newHash.get(key);
		}
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name)
				.replace("<seconds>", getSeconds(maximum))
				.replace("<ore>", randItemForMsg);
	}

	private void generateOres() {
		Location first = getArena().getProperties().getFirstLocation();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		cuboid.getLocations().stream().filter(l -> l.getBlockY() <= first.getBlockY() + 4 && l.getBlockY() != first.getBlockY())
				.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(ores[new Random().nextInt(7)]), l.getBlock()));
		cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 5)
				.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.COBBLESTONE.parseItem()), l.getBlock()));
		if (getArena().isHardMode())
			cuboid.getLocations().stream().filter(l -> getItem(l.getBlock()).isSimilar(randItemForTask)).filter(l -> Math.random() <= 0.5)
				.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(ores[new Random().nextInt(7)]), l.getBlock()));
	}

    @Override
    public void secondStartGame() {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		firstTime = Double.parseDouble(getSeconds(currentTime));
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(requireNonNull(getString("titles.task")).replace("<ore>", randItemForMsg));
		ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.DIAMOND_PICKAXE.parseItem())).setUnbreakable(true).build();
		generateOres();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			player.teleport(getRandomLocation(getArena()).add(0, 5, 0));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().setItem(0, item);
            player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = requireNonNull(player).getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.MINE_ORE;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(requireNonNull(getString("titles.task")).replace("<ore>", randItemForMsg));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.GOLDEN_PICKAXE.parseItem())).setDisplayName("&9&lMINER").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof BlockBreakEvent)) return;
		BlockBreakEvent e = (BlockBreakEvent) event;
		Player player = e.getPlayer();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		ItemStack block = getItem(e.getBlock());
		if (e.getBlock().getY() == getArena().getProperties().getFirstLocation().getBlockY()) return;
		if (block.isSimilar(randItemForTask)) onWin(player, true);
		e.setCancelled(false);
	}

}