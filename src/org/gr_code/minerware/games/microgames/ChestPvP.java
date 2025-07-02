package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties.Square;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class ChestPvP extends MicroGame {

	private final List<Inventory> chestInventory;
	private final static ItemStack[] listItem =
			{XMaterial.DIAMOND_SWORD.parseItem(), XMaterial.GOLDEN_SWORD.parseItem(), XMaterial.IRON_SWORD.parseItem(),
			XMaterial.STONE_SWORD.parseItem(), XMaterial.WOODEN_SWORD.parseItem(), XMaterial.WOODEN_AXE.parseItem(),
			XMaterial.GOLDEN_SWORD.parseItem(), XMaterial.IRON_SWORD.parseItem(), XMaterial.STONE_SWORD.parseItem(),
			XMaterial.WOODEN_SWORD.parseItem(), XMaterial.WOODEN_AXE.parseItem(), XMaterial.DIAMOND_AXE.parseItem(),
			XMaterial.GOLDEN_AXE.parseItem(), XMaterial.IRON_AXE.parseItem(), XMaterial.STONE_AXE.parseItem(),
			XMaterial.LEATHER_HELMET.parseItem(), XMaterial.LEATHER_CHESTPLATE.parseItem(), XMaterial.LEATHER_LEGGINGS.parseItem(),
			XMaterial.LEATHER_BOOTS.parseItem(), XMaterial.CHAINMAIL_HELMET.parseItem(), XMaterial.CHAINMAIL_CHESTPLATE.parseItem(),
			XMaterial.CHAINMAIL_LEGGINGS.parseItem(), XMaterial.CHAINMAIL_BOOTS.parseItem(), XMaterial.GOLDEN_HELMET.parseItem(),
			XMaterial.GOLDEN_CHESTPLATE.parseItem(), XMaterial.GOLDEN_LEGGINGS.parseItem(), XMaterial.GOLDEN_BOOTS.parseItem(),
			XMaterial.IRON_HELMET.parseItem(), XMaterial.IRON_CHESTPLATE.parseItem(), XMaterial.IRON_LEGGINGS.parseItem(),
			XMaterial.IRON_BOOTS.parseItem(), XMaterial.DIAMOND_HELMET.parseItem(), XMaterial.DIAMOND_CHESTPLATE.parseItem(),
			XMaterial.DIAMOND_LEGGINGS.parseItem(), XMaterial.DIAMOND_BOOTS.parseItem()};


	public ChestPvP(Arena arena) {
		super(580, arena, "chest-pvp");
		chestInventory = new ArrayList<>();
	}

	@Override
	public String getAchievementForMsg() {
		String achievementMsg = requireNonNull(getString("messages.achievement"));
		List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> x.getAchievement() != null).collect(Collectors.toList());
		if (achievement.isEmpty()) return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = parseInt(key.getAchievement());
			if (doubleKey <= maximum) continue;
			maximum = doubleKey;
			gamePlayer = key;
		}
		assert gamePlayer != null;
		String name = gamePlayer.getPlayer().getName();
		return achievementMsg.replace("<name>", name).replace("<count>", maximum + "");
	}
	
	@Override
	public void secondStartGame() {
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		if (getArena().isHardMode()) createChestsHard();
		else createChestsNormal();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		getArena().getPlayers().forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) 
				player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
			gamePlayer.setState(State.PLAYING_GAME);
			player.getInventory().setHeldItemSlot(0);
			assert XSound.ENTITY_ARROW_HIT_PLAYER.parseSound() != null;
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
			if (!ManageHandler.getModernAPI().oldVersion()) {
				AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
				assert attribute != null;
				attribute.setBaseValue(30);
				player.saveData();
			}
		});
	}

	private void createChestsHard() {
		List<Square> squares = new ArrayList<>(Arrays.asList(getArena().getProperties().getSquares()));
		int count = getArena().getProperties().getSquares().length;
		for (int j = 0; j < count - 2; j ++) {
			Square sq = squares.get(new Random().nextInt(squares.size()));
			createChest(sq);
			squares.remove(sq);
		}
	}

	private void createChest(Square sq) {
		Location random = sq.getLocations().get(new Random().nextInt(sq.getLocations().size()));
		ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.CHEST.parseItem()), random.getBlock());
		Chest chest = (Chest) random.getBlock().getState();
		for (int i = 0; i < new Random().nextInt(4) + 1; i ++) chest.getInventory()
				.setItem(new Random().nextInt(chest.getInventory().getSize()), listItem[new Random().nextInt(listItem.length)]);
		chestInventory.add(chest.getInventory());
	}
	
	private void createChestsNormal() {
		for (Square sq : getArena().getProperties().getSquares()) createChest(sq);
	}

	@Override
	public void check() {
		if (getTime() % 5 != 0) return;
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		List<GamePlayer> list = getArena().getPlayers();
		if (list.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		if (list.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
		list.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			int y = player.getLocation().getBlockY();
			if (y <= param_y) onLose(player, true);
		});
	}

	@Override
	public void end() {
		chestInventory.forEach(Inventory::clear);
		chestInventory.clear();
		getArena().getPlayers().stream()
				.filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
		super.end();
	}
	
	
	@Override
	public Game getGame() {
		return Game.CHEST_PVP;
	}

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
	public void aFinish(boolean forceStop) {
		chestInventory.forEach(Inventory::clear);
		chestInventory.clear();
		super.aFinish(forceStop);
	}

	@Override
	public ItemStack getGameItemStack() {
		return ItemBuilder.start(requireNonNull(XMaterial.STONE_SWORD.parseItem())).setDisplayName("&4&lCHEST PVP").build();
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntityDamageByEntityEvent) entityDamage(event);
		else if (event instanceof InventoryClickEvent) ((InventoryClickEvent) event).setCancelled(false);
	}

	private void entityDamage(Event event) {
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		boolean entityIsPlayer = e.getEntity() instanceof Player;
		boolean damagerIsPlayer = e.getDamager() instanceof Player;
		if (!(entityIsPlayer && damagerIsPlayer)) return;
		Player player = (Player) e.getEntity(), damager = (Player) e.getDamager();
		UUID uuidDamager = damager.getUniqueId(), uuid = player.getUniqueId();
		GamePlayer damagerPlayer = getArena().getPlayer(uuidDamager), entityPlayer = requireNonNull(getArena().getPlayer(uuid));
		e.setCancelled(true);
		boolean isRightCause = e.getCause() == EntityDamageByEntityEvent.DamageCause.ENTITY_ATTACK;
		boolean entityIsPlaying = requireNonNull(entityPlayer).getState() == State.PLAYING_GAME;
		boolean damagerIsPlaying = requireNonNull(damagerPlayer).getState() == State.PLAYING_GAME;
		if (!(isRightCause && entityIsPlaying && damagerIsPlaying)) return;
		setVelocity(player, damager);
		if (e.getDamage() < player.getHealth()) {
			player.damage(e.getDamage());
			return;
		}
		player.setHealth(20); damager.setHealth(20);
		if (damagerPlayer.getAchievement() == null) damagerPlayer.setAchievement("1");
		else damagerPlayer.setAchievement((parseInt(damagerPlayer.getAchievement()) + 1) + "");
		onLose(player, true);
	}

}


