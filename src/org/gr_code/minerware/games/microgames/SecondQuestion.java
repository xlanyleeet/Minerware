package org.gr_code.minerware.games.microgames;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class SecondQuestion extends MicroGame {

	private double firstTime;
	public HashMap<String, GamePlayer> achievement;

	public SecondQuestion(Arena arena) {
		super(280, arena, "answer-the-question");
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
		return requireNonNull(achievementMsg).replace("<name>", name).replace("<seconds>", getSeconds(maximum));
	}

    @Override
    public void secondStartGame() {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		firstTime = Double.parseDouble(getSeconds(currentTime));
		List<String> question = new ArrayList<>(requireNonNull(getSection("questions")).getKeys(false));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
    		String quest = question.get(new Random().nextInt(question.size()));
    		String question_msg = translate(getString("questions." + quest + ".question_msg"));
    		String question_tle = translate(getString("questions." + quest + ".question_tle"));
    		String material_right_block = translate(getString("questions." + quest + ".material_right_block"));
    		String material_wrong_block = translate(getString("questions." + quest + ".material_wrong_block"));
    		String name_right_block = translate(getString("questions." + quest + ".name_right_block"));
    		String name_wrong_block = translate(getString("questions." + quest + ".name_wrong_block"));
    		ItemStack first = ItemBuilder.start(requireNonNull(XMaterial.valueOf(material_right_block).parseItem())).setDisplayName(name_right_block).build();
    		ItemStack second = ItemBuilder.start(requireNonNull(XMaterial.valueOf(material_wrong_block).parseItem())).setDisplayName(name_wrong_block).build();
    		int[] slots = Math.random() <= 0.5 ? new int[]{2, 6} : new int[]{6, 2};
			player.getInventory().setItem(slots[0], first);
			player.getInventory().setItem(slots[1], second);
    		gamePlayer.setTask("1:" + quest);
			sendTitle(player, question_tle);
			sendMessage(player, question_msg);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    	if (getArena().isHardMode()) setTime(150);
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) player.teleport(getRandomLocation(getArena()));
    	});
    }

    @Override
	public void end() {
		String didnt_answ = getString("messages.didnt-answer");
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			sendMessage(player, translate(didnt_answ));
			onLose(player, false);
		});
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.SECOND_QUESTION;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.RAIL.parseItem())).setDisplayName("&c&lANSWER THE QUESTION").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof PlayerInteractEvent)) return;
		PlayerInteractEvent e = (PlayerInteractEvent) event;
		Player player = e.getPlayer();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		if (e.getItem() == null) return;
		int time = requireNonNull(getArena().getMicroGame()).getTime();
		FileConfiguration fileConfiguration = MinerPlugin.getInstance().getGames();
		String msg = fileConfiguration.getString("answer-the-question.messages.answ-to-question");
		if (!(time <= 200 && time > 20)) return;
		String task = gamePlayer.getTask();
		String[] t = task.split(":");
		String name_right_block = translate(fileConfiguration.getString("answer-the-question.questions." + t[1] + ".name_right_block"));
		String name_wrong_block = translate(fileConfiguration.getString("answer-the-question.questions." + t[1] + ".name_wrong_block"));
		String question = translate(fileConfiguration.getString("answer-the-question.questions." + t[1] + ".question_msg"));
		if (requireNonNull(e.getItem().getItemMeta()).getDisplayName().equals(name_right_block)) {
			gamePlayer.setTask("2:" + t[1] + ":0");
			sendMessage(player, translate(requireNonNull(msg)
					.replace("<answer>", name_right_block).replace("<question>", question)));
			onWin(player, true);
		} else if (e.getItem().getItemMeta().getDisplayName().equals(name_wrong_block)) {
			gamePlayer.setTask("2:" + t[1] + ":1");
			sendMessage(player, translate(requireNonNull(msg)
					.replace("<answer>", name_wrong_block).replace("<question>", question)));
			onLose(player, true);
		}
	}

}


