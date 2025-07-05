package org.gr_code.minerware.games.microgames;

import org.bukkit.enchantments.Enchantment;
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
import java.util.Random;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class BlockParty extends MicroGame {

    private String random_wool;
    private final List<String> list_wools;
    private final HashMap<String, GamePlayer> achievement = new HashMap<>();

	public BlockParty(Arena arena) {
		super(180, arena, "block-party");
		list_wools = getStringList("blocks");
	}

	@Override
    public void startGame() {
		int rand = new Random().nextInt(list_wools.size());
		random_wool = list_wools.get(rand);
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
			maximum = doubleKey;
			gamePlayer = newHash.get(key);
		}
		assert gamePlayer != null;
		String name = gamePlayer.getPlayer().getName();
		assert achievementMsg != null;
		return achievementMsg.replace("<name>", name)
				.replace("<block>", random_wool.split(":")[1])
				.replace("<seconds>", getSeconds(maximum));
	}

	@Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(requireNonNull(getString("titles.task"))
				.replace("<block>", random_wool.split(":")[1]));
		int level = getArena().isHardMode() ? 3 : 2;
		ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
				.addEnchantment(Enchantment.KNOCKBACK, level, true).build();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		generateRandomFloor();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation()))
				player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setItem(0, item);
    		player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }

    private void generateRandomFloor() {
		getArena().getProperties().destroySquares();
		int first_y = getArena().getProperties().getFirstLocation().getBlockY();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first_y).forEach(l -> {
			String stringBlock = list_wools.get(new Random().nextInt(list_wools.size())).split(":")[0];
			ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.valueOf(stringBlock).parseItem()), l.getBlock());
		});
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first_y)
				.filter(l -> getItem(l.getBlock()).isSimilar(XMaterial.valueOf(random_wool.split(":")[0]).parseItem()))
				.filter(l -> Math.random() <= 0.5).forEach(l -> {
			String stringBlock = list_wools.get(new Random().nextInt(list_wools.size())).split(":")[0];
			ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.valueOf(stringBlock).parseItem()), l.getBlock());
		});
	}

    @Override
    public void check() {
    	if (getTime() % 5 != 0) return;
    	List<GamePlayer> players = getArena().getPlayers();
		int loseY = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (players.stream().allMatch(x -> x.getState() != State.PLAYING_GAME)) setTime(1);
    	ItemStack rightItem = XMaterial.valueOf(random_wool.split(":")[0]).parseItem();
    	players.stream().filter(x -> x.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		org.bukkit.block.Block playerBlock = player.getLocation().add(0, -1, 0).getBlock();
    		
    		// Check if player is standing on the correct block type
    		boolean isOnCorrectBlock = false;
    		if (rightItem != null) {
    			isOnCorrectBlock = playerBlock.getType() == rightItem.getType();
    		}
    		
    		if (isOnCorrectBlock && gamePlayer.getAchievement() == null)
    			startAchievement(gamePlayer);
    		else if (!isOnCorrectBlock && gamePlayer.getAchievement() != null)
    			stopAchievement(gamePlayer);
    		if (player.getLocation().getBlockY() <= loseY) onLose(player, true);
    	});
    }

    private void startAchievement(GamePlayer gamePlayer) {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		gamePlayer.setAchievement(getSeconds(currentTime));
	}

	private void stopAchievement(GamePlayer gamePlayer) {
		double first = Double.parseDouble(gamePlayer.getAchievement());
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		double second = Double.parseDouble(getSeconds(currentTime));
		if (second - first > 0) achievement.put(Double.toString(second - first), gamePlayer);
		gamePlayer.setAchievement(null);
	}

    @Override
    public void end() {
    	ItemStack rightItem = XMaterial.valueOf(random_wool.split(":")[0]).parseItem();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			org.bukkit.block.Block playerBlock = player.getLocation().add(0, -1, 0).getBlock();
			if (gamePlayer.getAchievement() != null) stopAchievement(gamePlayer);
			
			// Check if player is standing on the correct block type
			boolean isOnCorrectBlock = false;
			if (rightItem != null) {
				isOnCorrectBlock = playerBlock.getType() == rightItem.getType();
			}
			
			if (isOnCorrectBlock) onWin(player, false);
			if (gamePlayer.getState() == State.PLAYING_GAME) onLose(player, false);
		});
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.BLOCK_PARTY;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(requireNonNull(getString("titles.task"))).replace("<block>", random_wool.split(":")[1]);
	}
    
    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.ORANGE_WOOL.parseItem())).setDisplayName("&6&lBLOCK-PARTY").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageEvent)) return;
		EntityDamageEvent e = (EntityDamageEvent) event;
		Player player = requireNonNull((Player) e.getEntity());
		UUID uuid = player.getUniqueId();
		boolean isPlaying = requireNonNull(getArena().getPlayer(uuid)).getState() == State.PLAYING_GAME;
		boolean isRightCause = e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;
		if (!(isPlaying && isRightCause)) return;
		e.setCancelled(false);
		e.setDamage(0);
		player.setHealth(20);
	}

}



