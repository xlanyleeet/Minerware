package org.gr_code.minerware.games.microgames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.gr_code.minerware.api.events.CowDamageByPlayerEvent;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties.Square;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class MilkCow extends MicroGame {

	private double firstTime;
	private final List<Inventory> chestInventory;
	public HashMap<String, GamePlayer> achievement;

	public MilkCow(Arena arena) {
		super(280, arena, "milk-the-cow");
		chestInventory = new ArrayList<>();
		achievement = new HashMap<>();
	}

    @Override
    public void onWin(Player player, boolean teleport) {
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		super.onWin(player, teleport);
		double currentTime = ((double) System.currentTimeMillis()) / 1000;
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
		assert gamePlayer != null;
		String name = gamePlayer.getPlayer().getName();
		return requireNonNull(achievementMsg).replace("<name>", name)
				.replace("<seconds>", getSeconds(maximum));
	}

	private void generateFarm() {
		Location first = getArena().getProperties().getFirstLocation();
		Location center = getArena().getProperties().getCuboid().getCenter();
		getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.PODZOL.parseItem()), l.getBlock()));
		int j = 0;
		for (Square sq : getArena().getProperties().getSquares()) {
			sq.getLocations().forEach(l -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.HAY_BLOCK.parseItem()), l.getBlock()));
			if (!getArena().getProperties().getType().equals("MICRO") && j == 4) {
				j++;
				continue;
			}
			if (!getArena().isHardMode() || Math.random() <= 0.5) {
				Location random = sq.getLocations().get(new Random().nextInt(sq.getLocations().size()));
				ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.CHEST.parseItem()), random.getBlock());
				Chest chest = (Chest) random.getBlock().getState();
				for (int i = 0; i < chest.getInventory().getSize(); i++)
					chest.getInventory().setItem(i, XMaterial.BUCKET.parseItem());
				chestInventory.add(chest.getInventory());
			}
			j++;
		}
		if (getArena().getProperties().getType().equals("MICRO")) {
			ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.HAY_BLOCK.parseItem()), center.getBlock().getLocation().clone().add(0, -1, 0).getBlock());
			ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.HAY_BLOCK.parseItem()), center.getBlock().getLocation().clone().add(1, -1, 0).getBlock());
			ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.HAY_BLOCK.parseItem()), center.getBlock().getLocation().clone().add(1, -1, 1).getBlock());
			ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.HAY_BLOCK.parseItem()), center.getBlock().getLocation().clone().add(0, -1, 1).getBlock());
		}
	}

	private void spawnCow() {
		Location first = getArena().getProperties().getFirstLocation();
		Location center = getArena().getProperties().getCuboid().getCenter();
		Cow cow;
		if (getArena().getProperties().getType().equals("MEGA"))
			cow = (Cow) requireNonNull(first.getWorld()).spawnEntity(center.getBlock().getLocation().clone().add(0.5, 0, 0.5), EntityType.COW);
		else cow = (Cow) requireNonNull(first.getWorld()).spawnEntity(center.getBlock().getLocation().clone().add(1, 0, 1), EntityType.COW);
		if (ManageHandler.getNMS().oldVersion()) ManageHandler.getNMS().setNoAI(cow);
		else {
			cow.setSilent(true);
			cow.setAI(false);
		}
	}

    @Override
    public void secondStartGame() {
		double currentTime = ((double)System.currentTimeMillis()) / 1000;
		firstTime = Double.parseDouble(getSeconds(currentTime));
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
    	generateFarm(); spawnCow();
    	Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    }

    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		List<GamePlayer> playerList = getArena().getPlayers();
    	if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
    	playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
    		if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		chestInventory.forEach(Inventory::clear);
		chestInventory.clear();
		getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.MILK_COW;
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
    	return ItemBuilder.start(requireNonNull(XMaterial.MILK_BUCKET.parseItem())).setDisplayName("&f&lMILK THE COW").build();
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntityDamageByEntityEvent) entityDamageByEntity(event);
		else if (event instanceof PlayerInteractEntityEvent) playerInteractEntity(event);
		else if (event instanceof CreatureSpawnEvent) creatureSpawn(event);
		else if (event instanceof InventoryClickEvent) ((InventoryClickEvent) event).setCancelled(false);
	}

	private void entityDamageByEntity(Event event) {
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
		if (!(e.getEntity() instanceof Cow)) return;
		if (!(e.getDamager() instanceof Player)) return;
		Player player = (Player) e.getDamager();
		UUID uuid = player.getUniqueId();
		Arena arena = ServerManager.getArena(uuid);
		if (e.getEntity().getLocation().distance(player.getLocation()) > 3) return;
		double cow_x = e.getEntity().getLocation().getX();
		double cow_y = e.getEntity().getLocation().getY();
		double cow_z = e.getEntity().getLocation().getZ();
		Vector vector = new Vector((cow_x - player.getLocation().getX()) * -1,
				(cow_y - player.getLocation().getY()) * -1 + 2,
				(cow_z - player.getLocation().getZ()) * -1).normalize().multiply(1.5);
		CowDamageByPlayerEvent eventCow = new CowDamageByPlayerEvent(player, arena, vector);
		Bukkit.getPluginManager().callEvent(eventCow);
		player.setVelocity(eventCow.getVector());
	}

	@SuppressWarnings("deprecation")
	private void playerInteractEntity(Event event) {
		PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if (requireNonNull(getArena().getPlayer(uuid)).getState() != State.PLAYING_GAME) return;
		if (!(e.getRightClicked() instanceof Cow)) return;
		ItemStack is = ManageHandler.getNMS().oldVersion() ? e.getPlayer().getInventory().getItemInHand() : e.getPlayer().getInventory().getItemInMainHand();
		if (!is.isSimilar(XMaterial.BUCKET.parseItem())) return;
		onWin(player, false);
	}

	private void creatureSpawn(Event event) {
		CreatureSpawnEvent e = (CreatureSpawnEvent) event;
		if (!(e.getEntity() instanceof Cow)) return;
		e.setCancelled(false);
	}

}