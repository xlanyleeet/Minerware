package org.gr_code.minerware.games.microgames;

import org.bukkit.entity.Player;
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

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class JumpOrFly extends MicroGame {

	private double firstTime;
	private final HashMap<String, GamePlayer> achievement = new HashMap<>();

	public JumpOrFly(Arena arena) {
		super(280, arena, "jump-or-fly-game");
	}

    @Override
    public void onWin(Player player, boolean teleport) {
        GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
        if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
        player.setAllowFlight(false);
        player.setFlying(false);
        double currentTime = ((double)System.currentTimeMillis()) / 1000;
        double secondAch = Double.parseDouble(getSeconds(currentTime));
        if (secondAch - firstTime >= 0) achievement.put(Double.toString(secondAch - firstTime), gamePlayer);
    	super.onWin(player, teleport);
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsgFly = getString("messages.achievement-fly");
		String achievementMsgJump = getString("messages.achievement-jump");
		String achievementMsg = null;
		HashMap<String, GamePlayer> newHash = new HashMap<>();
		HashMap<String, GamePlayer> newHashFly = new HashMap<>();
		HashMap<String, GamePlayer> newHashJump = new HashMap<>();
		achievement.entrySet().stream().filter(entry -> getArena().getPlayers().contains(entry.getValue()))
				.forEach(entry -> newHash.put(entry.getKey(), entry.getValue()));
		if (newHash.isEmpty()) return "";
		newHash.forEach((key, value) -> {
			if (value.getTask().equals("MOON")) newHashFly.put(key, value);
			else if (value.getTask().equals("VOID")) newHashJump.put(key, value);
		});
		if (!newHashFly.isEmpty()) {
			double maximumFly = 100000;
			GamePlayer gamePlayer = null;
			for (String key : newHashFly.keySet()) {
				double doubleKey = Double.parseDouble(key);
				if (doubleKey >= maximumFly) continue;
				maximumFly = doubleKey;
				gamePlayer = newHashFly.get(key);
			}
			String name = gamePlayer.getPlayer().getName();
			achievementMsg = requireNonNull(achievementMsgFly).replace("<name>", name)
					.replace("<seconds>", getSeconds(maximumFly));
		}
		if (!newHashJump.isEmpty()) {
			double maximumJump = 100000;
			GamePlayer gamePlayer = null;
			for (String key : newHashJump.keySet()) {
				double doubleKey = Double.parseDouble(key);
				if (doubleKey >= maximumJump) continue;
				gamePlayer = newHashJump.get(key);
				maximumJump = doubleKey;
			}
			String name = gamePlayer.getPlayer().getName();
			assert  achievementMsgJump != null;
			String msgJump = achievementMsgJump.replace("<name>", name)
					.replace("<seconds>", getSeconds(maximumJump));
			if (achievementMsg == null) achievementMsg = msgJump;
			else achievementMsg = achievementMsg + "\n" + msgJump;
		}
		if (achievementMsg == null) return "";
		return achievementMsg;
	}

    @Override
    public void secondStartGame() {
		String titleFly = translate(getString("titles.start-game-fly"));
		String titleJump = translate(getString("titles.start-game-jump"));
		String subtitle = translate(getString("titles.task"));
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		firstTime = Double.parseDouble(getSeconds(currentTime));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
        	if (Math.random() < 0.5) {
    			sendTitle(player, titleFly, subtitle, 0, 70, 20);
        		gamePlayer.setTask("MOON");
        		player.setAllowFlight(true);
        		player.setFlying(true);
        		player.setPlayerTime(18000, false);
        	} else {
    			sendTitle(player, titleJump, subtitle, 0, 70, 20);
        		gamePlayer.setTask("VOID");
        	}
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        	gamePlayer.setState(State.PLAYING_GAME);
    	});
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
		int blockY = getArena().getProperties().getFirstLocation().getBlockY();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            boolean checker;
            if (gamePlayer.getTask().equalsIgnoreCase("VOID")) checker = y <= blockY - 10;
            else checker = y >= blockY + 20;
            if (checker) onWin(player, true);
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
        return Game.JUMP_OR_FLY;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.IRON_BOOTS.parseItem())).setDisplayName("&f&lJUMP OR FLY").build();
	}

}


