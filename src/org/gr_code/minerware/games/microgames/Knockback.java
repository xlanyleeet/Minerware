package org.gr_code.minerware.games.microgames;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class Knockback extends MicroGame {

	public Knockback(Arena arena) {
		super(480, arena, "knock-everyone");
	}

    @Override
    public void startGame() {
        if (!ManageHandler.getModernAPI().oldVersion())
        	getArena().getPlayers().forEach(gamePlayer -> {
            	Player player = gamePlayer.getPlayer();
            	AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            	assert attribute != null;
            	attribute.setBaseValue(30);
            	player.saveData();
        	});
        super.startGame();
    }
    
    private void checkExp(Player player) {
    	if (player.getExp() < 0.99F) {
			float exp = player.getExp();
			player.setExp(exp + 0.11F);
		}
		if (player.getExp() <= 0.33F) {
			ItemStack item = player.getInventory().getItem(0);
			if (item == null) return;
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.removeEnchant(Enchantment.KNOCKBACK);
			meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
			item.setItemMeta(meta);
			player.getInventory().setItem(0, item);
		} else if (player.getExp() <= 0.66F) {
			ItemStack item = player.getInventory().getItem(0);
			if (item == null) return;
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.removeEnchant(Enchantment.KNOCKBACK);
			meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
			item.setItemMeta(meta);
			player.getInventory().setItem(0, item);
		} else if (player.getExp() <= 0.99F) {
			ItemStack item = player.getInventory().getItem(0);
			if (item == null) return;
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.removeEnchant(Enchantment.KNOCKBACK);
			meta.addEnchant(Enchantment.KNOCKBACK, 3, true);
			item.setItemMeta(meta);
			player.getInventory().setItem(0, item);
		}
    }
    
    @Override
    public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		int count = getArena().isHardMode() ? 2 : 1;
		ItemStack item = ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setUnbreakable(true)
				.addEnchantment(Enchantment.KNOCKBACK, count, true).build();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		if (getArena().isHardMode()) getArena().getProperties().destroySquares();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle,0,70,20);
            player.getInventory().setItem(0, item);
    		player.getInventory().setHeldItemSlot(0);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }

    @Override
    public void check() {
    	if (getTime() % 10 != 0) return;
    	List<GamePlayer> playerList = getArena().getPlayers();
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
    	getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		checkExp(player);
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.KNOCKBACK;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.STICK.parseItem())).setDisplayName("&d&lKNOCK EVERYONE").build();
	}

	@Override
	public void event(Event event) {
		if (!(event instanceof EntityDamageByEntityEvent)) return;
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getEntity() instanceof Player)) return;
		if (!(e.getDamager() instanceof Player)) return;
		Player player = (Player) e.getEntity();
		Player damager = (Player) e.getDamager();
		GamePlayer damagerGame = requireNonNull(getArena().getPlayer(damager.getUniqueId()));
		GamePlayer playerGame = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		boolean damagerIsPlaying = damagerGame.getState() == State.PLAYING_GAME;
		boolean playerIsPlaying = playerGame.getState() == State.PLAYING_GAME;
		if (!(damagerIsPlaying && playerIsPlaying)) return;
		e.setDamage(0);
		e.setCancelled(false);
		damager.setHealth(20);
		player.setHealth(20);
		damager.setExp(0);
	}

}



