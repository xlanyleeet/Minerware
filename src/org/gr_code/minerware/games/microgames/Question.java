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
import java.util.List;
import java.util.Random;

import static java.util.Objects.requireNonNull;
import static org.bukkit.Bukkit.getPlayer;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class Question extends MicroGame {

	private int time = 300;

	public Question(Arena arena) {
		super(380, arena, "question-game", false, false);
	}

	@Override
    public void secondStartGame() {
		List<String> question = new ArrayList<>(requireNonNull(getSection("questions")).getKeys(false));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
    		String quest = question.get(new Random().nextInt(question.size()));
    		String first_question_msg = translate(getString("questions." + quest + ".first_question_msg"));
    		String first_question_tle = translate(getString("questions." + quest + ".first_question_tle"));
    		String material_first_block = translate(getString("questions." + quest + ".material_first_block"));
    		String material_second_block = translate(getString("questions." + quest + ".material_second_block"));
    		String name_first_block = translate(getString("questions." + quest + ".name_first_block"));
    		String name_second_block = translate(getString("questions." + quest + ".name_second_block"));
    		ItemStack first = ItemBuilder.start(requireNonNull(XMaterial.valueOf(material_first_block).parseItem())).setDisplayName(name_first_block).build();
    		ItemStack second = ItemBuilder.start(requireNonNull(XMaterial.valueOf(material_second_block).parseItem())).setDisplayName(name_second_block).build();
    		player.getInventory().setItem(2, first);
    		player.getInventory().setItem(6, second);
    		gamePlayer.setTask("1:" + quest);
			sendTitle(player, first_question_tle);
			sendMessage(player, first_question_msg);
            gamePlayer.setState(State.PLAYING_GAME);
    		player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
		if (getArena().isHardMode()) setTime(250);
	}
    
    private void secondQuestion() {
		time = 150;
    	String didnt_answ = translate(getString("messages.didnt-answer"));
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		String[] task = gamePlayer.getTask().split(":");
    		if (Integer.parseInt(task[0]) == 1) {
    			String new_task = "2:" + task[1] + ":" + new Random().nextInt(2);
    			gamePlayer.setTask(new_task);
				clearInventory(player);
    			sendMessage(player, didnt_answ);
    		}
			ArrayList<GamePlayer> list = new ArrayList<>(getArena().getPlayers());
    		list.remove(gamePlayer);
    		GamePlayer px = list.get(new Random().nextInt(list.size()));
    		Player player_x = px.getPlayer();
			String name = player_x.getName();
    		String[] quest = px.getTask().split(":");
    		String second_question_msg = translate(requireNonNull(getString("questions." + quest[1] + ".second_question_msg")).replace("<nick>", name));
    		String second_question_tle = translate(requireNonNull(getString("questions." + quest[1] + ".second_question_tle")).replace("<nick>", name));
    		String material_first_block = translate(getString("questions." + quest[1] + ".material_first_block"));
    		String material_second_block = translate(getString("questions." + quest[1] + ".material_second_block"));
    		String name_first_block = translate(getString("questions." + quest[1] + ".name_first_block"));
    		String name_second_block = translate(getString("questions." + quest[1] + ".name_second_block"));
    		ItemStack first = ItemBuilder.start(requireNonNull(XMaterial.valueOf(material_first_block).parseItem())).setDisplayName(name_first_block).build();
    		ItemStack second = ItemBuilder.start(requireNonNull(XMaterial.valueOf(material_second_block).parseItem())).setDisplayName(name_second_block).build();
    		String t = gamePlayer.getTask() + ":" + name;
    		gamePlayer.setTask(t);
			player.getInventory().setItem(2, first);
    		player.getInventory().setItem(6, second);
			sendTitle(player, second_question_tle);
			sendMessage(player, second_question_msg);
    	});
    	if (getArena().isHardMode()) setTime(100);
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		if (getTime() % 20 == 0) getArena().getPlayers().forEach(gamePlayer -> getTimer().setTimer(gamePlayer.getPlayer(), time, getTime()));
		if (getTime() == 150) secondQuestion();
		List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
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
        return Game.QUESTION;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.ACTIVATOR_RAIL.parseItem())).setDisplayName("&c&lQUESTION").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof PlayerInteractEvent)) return;
		PlayerInteractEvent e = (PlayerInteractEvent) event;
		Player player = e.getPlayer();
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (gamePlayer.getState() != State.PLAYING_GAME) return;
		if (e.getItem() == null) return;
		int time = getTime();
		FileConfiguration fileConfiguration = MinerPlugin.getInstance().getGames();
		String msg = fileConfiguration.getString("question-game.messages.answ-to-question");
		assert msg != null;
		if (time <= 300 && time > 150) {
			String task = gamePlayer.getTask();
			String[] t = task.split(":");
			String name_first_block = translate(fileConfiguration.getString("question-game.questions." + t[1] + ".name_first_block"));
			String name_second_block = translate(fileConfiguration.getString("question-game.questions." + t[1] + ".name_second_block"));
			String question = translate(fileConfiguration.getString("question-game.questions." + t[1] + ".first_question_msg"));
			if (requireNonNull(e.getItem().getItemMeta()).getDisplayName().equals(name_first_block)) {
				gamePlayer.setTask("2:" + t[1] + ":0");
				clearInventory(player);
				sendMessage(player, translate(msg.replace("<answer>", name_first_block).replace("<question>", question)));
			} else if (e.getItem().getItemMeta().getDisplayName().equals(name_second_block)) {
				gamePlayer.setTask("2:" + t[1] + ":1");
				clearInventory(player);
				sendMessage(player, translate(msg.replace("<answer>", name_second_block).replace("<question>", question)));
			}
		} else if (time <= 150 && time > 20) {
			String[] t = gamePlayer.getTask().split(":");
			Player px = getPlayer(t[3]);
			assert px != null;
			GamePlayer xx = requireNonNull(getArena().getPlayer(px.getUniqueId()));
			String[] tx = xx.getTask().split(":");
			String name_first_block = translate(fileConfiguration.getString("question-game.questions." + tx[1] + ".name_first_block"));
			String name_second_block = translate(fileConfiguration.getString("question-game.questions." + tx[1] + ".name_second_block"));
			String question = translate(requireNonNull(fileConfiguration.getString("question-game.questions." + tx[1] + ".second_question_msg")).replace("<nick>", t[3]));
			msg = msg.replace("<question>", question);
			boolean isFirstBlock = requireNonNull(e.getItem().getItemMeta()).getDisplayName().equals(name_first_block);
			boolean isSecondBlock = e.getItem().getItemMeta().getDisplayName().equals(name_second_block);
			if (isFirstBlock) msg = translate(msg.replace("<answer>", name_first_block));
			else if (isSecondBlock) msg = translate(msg.replace("<answer>", name_second_block));
			if (msg.contains("<answer>")) return;
			clearInventory(player);
			sendMessage(player, msg);
			if (isFirstBlock && tx[2].equals("0")) onWin(player, true);
			else if (isSecondBlock && tx[2].equals("1")) onWin(player, true);
			else if (isFirstBlock && tx[2].equals("1")) onLose(player, true);
			else if (isSecondBlock && tx[2].equals("0")) onLose(player, true);
		}
	}

}


