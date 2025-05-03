package org.gr_code.minerware.games.microgames;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class DropItem extends MicroGame {
	
	private final List<String> list_item;
	public HashMap<String, GamePlayer> achievement;
	private double firstTime;

	public DropItem(Arena arena) {
		super(280, arena, "drop-item-game");
		list_item = getStringList("items");
		achievement = new HashMap<>();
	}

    @Override
    public void startGame() {
        getArena().getPlayers().forEach(gamePlayer -> gamePlayer.setTask(list_item.get(new Random().nextInt(list_item.size()))));
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
		assert gamePlayer != null;
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name)
				.replace("<seconds>", getSeconds(maximum))
				.replace("<item>", gamePlayer.getTask().split(":")[1]);
	}

	@Override
	public void secondStartGame() {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		firstTime = Double.parseDouble(getSeconds(currentTime));
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			String subSubtitle = subtitle.replace("<item>", translate(gamePlayer.getTask().split(":")[1]));
			sendTitle(player, title, subSubtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			createRandomInventory(gamePlayer, player);
    	});
    }

    private void createRandomInventory(GamePlayer gamePlayer, Player player) {
		List<String> newListWithItems = new ArrayList<>(list_item);
		Collections.shuffle(newListWithItems);
		ItemStack firstRandomIS = XMaterial.valueOf(gamePlayer.getTask().split(":")[0]).parseItem();
		player.getInventory().setItem(new Random().nextInt(9), firstRandomIS);
		newListWithItems.remove(gamePlayer.getTask());
		for (int i = 0; i < 8; i ++)
			player.getInventory().addItem(XMaterial.valueOf(newListWithItems.get(i).split(":")[0]).parseItem());
	}
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(x->{
    		Player player = x.getPlayer();
    		int y = player.getLocation().getBlockY();
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
        return Game.DROP_ITEM;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task").replace("<item>", gamePlayer.getTask().split(":")[1]));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.OBSIDIAN.parseItem())).setDisplayName("&8&lDROP THE ITEM").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof PlayerDropItemEvent)) return;
		PlayerDropItemEvent e = (PlayerDropItemEvent) event;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
		e.setCancelled(false);
		Item item = e.getItemDrop();item.remove();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		ItemStack DropMaterial = item.getItemStack();
		ItemStack right = XMaterial.valueOf(requireNonNull(gamePlayer).getTask().split(":")[0]).parseItem();
		if (!DropMaterial.isSimilar(right)) {
			if (getArena().isHardMode()) onLose(player, true);
			else e.setCancelled(true);
			return;
		}
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		double secondAch = Double.parseDouble(getSeconds(currentTime));
		if (secondAch - firstTime >= 0) achievement.put((secondAch - firstTime) + "", gamePlayer);
		onWin(player, true);
	}

}
