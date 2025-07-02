package org.gr_code.minerware.games.microgames;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class MathGame extends MicroGame {

	private double firstTime;
	public HashMap<String, GamePlayer> achievement;

	public MathGame(Arena arena) {
		super(180, arena, "math-game");
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

    @Override
    public void secondStartGame() {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		firstTime = Double.parseDouble(getSeconds(currentTime));
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
		int count = getArena().isHardMode() ? 5 : 10;
		String str = getArena().isHardMode() ? " x " : " + ";
		HardMath<Integer> calculate = getArena().isHardMode() ? (x, y) -> x * y : Integer::sum;
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
    		int paramInt_1 = new Random().nextInt(count) + 6;
            int paramInt_2 = new Random().nextInt(count) + 6;
			sendTitle(player, title.replace("<task>", paramInt_1 + str + paramInt_2), subtitle,0,70,20);
            gamePlayer.setTask(Integer.toString(calculate.calculate(paramInt_1, paramInt_2)));
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
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
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.MATH;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.OAK_SIGN.parseItem())).setDisplayName("&6&lMATH").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof AsyncPlayerChatEvent)) return;
		AsyncPlayerChatEvent e = (AsyncPlayerChatEvent) event;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		e.setCancelled(true);
		if (!e.getMessage().equalsIgnoreCase(gamePlayer.getTask())) {
			if (getArena().isHardMode()) onLose(player, true);
			return;
		}
		onWin(player, true);
	}

	private interface HardMath<T>{
		T calculate(T x, T y);
	}

}


