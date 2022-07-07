/*
 * Supernatural Players Plugin for Bukkit
 * Copyright (C) 2011  Matt Walker <mmw167@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package me.matterz.supernaturals.manager;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.io.SNConfigHandler;
import me.matterz.supernaturals.util.ArrowUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class HunterManager extends HumanManager {

	public HunterManager() {
		super();
	}

	private final HashMap<Arrow, String> arrowMap = new HashMap<>();
	private final HashMap<SuperNPlayer, String> hunterMap = new HashMap<>();
	private final ArrayList<Player> grapplingPlayers = new ArrayList<>();
	private final ArrayList<Player> drainedPlayers = new ArrayList<>();
	private final ArrayList<Location> hallDoors = new ArrayList<>();
	private final ArrayList<SuperNPlayer> playerInvites = new ArrayList<>();
	private static final ArrayList<SuperNPlayer> bountyList = new ArrayList<>();

	private String arrowType = "normal";

	// -------------------------------------------- //
	// Damage Events //
	// -------------------------------------------- //

	@Override
	public double victimEvent(EntityDamageEvent event, double damage) {
		if (event.getCause().equals(DamageCause.FALL)) {
			damage -= SNConfigHandler.hunterFallReduction;
			if (damage < 0) {
				damage = 0;
			}
		}
		return damage;
	}

	@Override
	public boolean shootArrow(Player shooter) {
		return shoot(shooter);
	}

	@Override
	public double damagerEvent(EntityDamageByEntityEvent event, double damage) {
		if (event.getDamager() instanceof Projectile) {
			Entity victim = event.getEntity();
			if (event.getDamager() instanceof Arrow arrow) {
				if (getArrowMap().containsKey(arrow)) {
					arrowType = getArrowType(arrow);
				} else {
					arrowType = "normal";
				}
			}
			if (arrowType.equalsIgnoreCase("power")) {
				damage += damage * SNConfigHandler.hunterPowerArrowDamage;
			} else if (arrowType.equalsIgnoreCase("fire")) {
				victim.setFireTicks(SNConfigHandler.hunterFireArrowFireTicks);
			}
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log("Arrow event with " + damage
						+ " damage!");
			}
			return damage;
		} else {
			Player pDamager = (Player) event.getDamager();
			SuperNPlayer snDamager = SuperNManager.get(pDamager);
			ItemStack item = pDamager.getInventory().getItemInMainHand();

			// Check Weapons and Modify Damage
			if (SNConfigHandler.hunterWeapons.contains(item.getType())) {
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log(pDamager.getName()
							+ " was not allowed to use "
							+ item.getType());
				}
				SuperNManager.sendMessage(snDamager, "WitchHunters cannot use this weapon!");
				return 0;
			}
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log("Bow Event with " + damage + " damage");
			}
			return damage;
		}
	}

	@Override
	public void deathEvent(Player player) {
		super.deathEvent(player);
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNManager.alterPower(snplayer, -SNConfigHandler.hunterDeathPowerPenalty, "You died!");
	}

	// -------------------------------------------- //
	// Interact //
	// -------------------------------------------- //

	@Override
	public boolean playerInteract(PlayerInteractEvent event) {

		Action action = event.getAction();
		Player player = event.getPlayer();
		SuperNPlayer snplayer = SuperNManager.get(player);

		if (action.equals(Action.LEFT_CLICK_AIR)
				|| action.equals(Action.LEFT_CLICK_BLOCK)) {
			if (player.getInventory().getItemInMainHand().getType() == Material.BOW) {
				changeArrowType(snplayer);
				return true;
			}
		}
		return false;
	}

	// -------------------------------------------- //
	// Armor //
	// -------------------------------------------- //

	// -------------------------------------------- //
	// Bounties //
	// -------------------------------------------- //

	public static ArrayList<SuperNPlayer> getBountyList() {
		return bountyList;
	}

	public static boolean checkBounty(SuperNPlayer snplayer) {
		return bountyList.contains(snplayer);
	}

	public static boolean removeBounty(SuperNPlayer snplayer) {
		if (bountyList.contains(snplayer)) {
			bountyList.remove(snplayer);
			return true;
		}
		return false;
	}

	public static void addBounty() {
		List<SuperNPlayer> targets = SuperNManager.getSupernaturals();
		boolean bountyFound = false;
		Random generator = new Random();
		int count = 0;

		while (!bountyFound) {
			int randomIndex = generator.nextInt(targets.size());
			SuperNPlayer sntarget = targets.get(randomIndex);

			if (!bountyList.contains(sntarget) && sntarget.isSuper()) {
				bountyList.add(sntarget);
				Bukkit.broadcastMessage(ChatColor.WHITE
						+ sntarget.getName()
						+ ChatColor.RED
						+ " has been added to the WitchHunter target list!");
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log("Bounty created on "
							+ sntarget.getName());
				}
				return;
			}
			count++;
			if (count > 50) {
				return;
			}
		}
	}

	public static void updateBounties() {
		List<SuperNPlayer> snplayers = new ArrayList<>();

		if (bountyList.isEmpty()) {
			return;
		}

		for (SuperNPlayer snplayer : bountyList) {
			if (!snplayer.isSuper()) {
				snplayers.add(snplayer);
			}
		}

		for (SuperNPlayer snplayer : snplayers) {
			removeBounty(snplayer);
			addBounty();
		}
	}

	public static void createBounties() {
		List<SuperNPlayer> targets = SuperNManager.getSupernaturals();
		if (targets.size() == 0) {
			SupernaturalsPlugin.log(Level.WARNING, "No targets found for WitchHunters!");
			return;
		}

		int numberFound = 0;
		Random generator = new Random();
		int count = 0;

		while (numberFound < SNConfigHandler.hunterMaxBounties) {
			int randomIndex = generator.nextInt(targets.size());
			SuperNPlayer sntarget = targets.get(randomIndex);
			if (!bountyList.contains(sntarget) && sntarget.isSuper()) {
				bountyList.add(sntarget);
				numberFound++;
				Bukkit.broadcastMessage(ChatColor.WHITE
						+ sntarget.getName()
						+ ChatColor.RED
						+ " has been added to the WitchHunter target list!");
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log("Bounty created on "
							+ sntarget.getName());
				}
			}
			count++;
			if (count > 100) {
				return;
			}
		}
	}

	// -------------------------------------------- //
	// Doors //
	// -------------------------------------------- //

	private void addDoorLocation(Location location) {
		if (!hallDoors.contains(location)) {
			hallDoors.add(location);
		}
	}

	private void removeDoorLocation(Location location) {
		hallDoors.remove(location);
	}

	public boolean doorIsOpening(Location location) {
		return hallDoors.contains(location);
	}

	public boolean doorEvent(Player player, Block block, Door door) {
		if (door.isOpen()) {
			return true;
		}

		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log(player.getName()
					+ " activated a WitchHunters' Hall.");
		}

		SuperNPlayer snplayer = SuperNManager.get(player);
		boolean open = false;

		final Location loc = block.getLocation();

		if (snplayer.isHuman()) {
			open = join(snplayer);
		}

		if (snplayer.isHunter() || snplayer.isHuman() && open) {
			door.setOpen(!door.isOpen());
			block.setBlockData(door);

			addDoorLocation(loc);
			addDoorLocation(door.getHalf() == Bisected.Half.TOP ? loc.clone().subtract(0, 1, 0) : loc.clone().add(0, 1, 0));

			Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> closeDoor(loc), 20);
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log("WitchHunter door is set open.");
			}
			return true;
		}
		SuperNManager.sendMessage(snplayer, "WitchHunters Only!");
		return true;
	}

	private void closeDoor(Location loc) {
		Block block = loc.getBlock();
		Door door = (Door) block.getBlockData();
		if (!door.isOpen()) {
			return;
		}

		door.setOpen(!door.isOpen());
		block.setBlockData(door);

		removeDoorLocation(loc);
		removeDoorLocation(door.getHalf() == Bisected.Half.TOP ? loc.clone().subtract(0, 1, 0) : loc.clone().add(0, 1, 0));
	}

	// -------------------------------------------- //
	// Join Event //
	// -------------------------------------------- //

	public void invite(final SuperNPlayer snplayer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> {
			SuperNManager.sendMessage(snplayer, "You have been invited to join the WitchHunter society!");
			SuperNManager.sendMessage(snplayer, "If you wish to accept this invitation visit a WitchHunters' Hall");
			if (!playerInvites.contains(snplayer)) {
				playerInvites.add(snplayer);
			}
		}, 200);
	}

	public boolean join(SuperNPlayer snplayer) {
		if (playerInvites.contains(snplayer)) {
			SuperNManager.sendMessage(snplayer, "Welcome to the WitchHunter society!");
			SuperNManager.convert(snplayer, "witchhunter", SNConfigHandler.hunterPowerStart);
			return true;
		}
		return false;
	}

	// -------------------------------------------- //
	// Arrow Management //
	// -------------------------------------------- //

	public void changeArrowType(SuperNPlayer snplayer) {
		String currentType = hunterMap.get(snplayer);
		if (currentType == null) {
			currentType = "normal";
		}

		String nextType = "normal";

		for (int i = 0; i < SNConfigHandler.hunterArrowTypes.size(); i++) {
			if (SNConfigHandler.hunterArrowTypes.get(i).equalsIgnoreCase(currentType)) {
				int newI = i + 1;
				if (newI >= SNConfigHandler.hunterArrowTypes.size()) {
					newI = 0;
				}
				nextType = SNConfigHandler.hunterArrowTypes.get(newI);
				break;
			}
		}

		hunterMap.put(snplayer, nextType);
		SuperNManager.sendMessage(snplayer, "Changed to arrow type: "
				+ ChatColor.WHITE + nextType);
		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log(snplayer.getName()
					+ " changed to arrow type: " + nextType);
		}
	}

	public String getArrowType(Arrow arrow) {
		return arrowMap.get(arrow);
	}

	public HashMap<Arrow, String> getArrowMap() {
		return arrowMap;
	}

	public void removeArrow(final Arrow arrow) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> arrowMap.remove(arrow), 20);
	}

	// -------------------------------------------- //
	// Attacks //
	// -------------------------------------------- //

	public boolean shoot(final Player player) {

		final SuperNPlayer snplayer = SuperNManager.get(player);

		if (!player.getInventory().contains(Material.ARROW)) {
			return false;
		}

		if (!SupernaturalsPlugin.instance.getPvP(player)) {
			String arrowType = hunterMap.get(snplayer);
			if (arrowType == null) {
				hunterMap.put(snplayer, "normal");
				return false;
			}
			if (!arrowType.equalsIgnoreCase("normal")) {
				SuperNManager.sendMessage(snplayer, "You cannot use special arrows in non-PvP areas.");
			}
			return false;
		}

		if (drainedPlayers.contains(player)) {
			player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.ARROW, 1));
			SuperNManager.sendMessage(snplayer, "You are still recovering from Power Shot.");
			return true;
		}

		String arrowType = hunterMap.get(snplayer);
		if (arrowType == null) {
			arrowType = "normal";
		}

		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log(snplayer.getName() + " is firing "
					+ arrowType + " arrows.");
		}

		if (arrowType.equalsIgnoreCase("fire")) {
			if (snplayer.getPower() > SNConfigHandler.hunterPowerArrowFire) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.hunterPowerArrowFire, "Fire Arrow!");
				Arrow arrow = player.launchProjectile(Arrow.class);
				arrowMap.put(arrow, arrowType);
				arrow.setFireTicks(SNConfigHandler.hunterFireArrowFireTicks);
				return true;
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to shoot Fire Arrows!");
				SuperNManager.sendMessage(snplayer, "Switching to normal arrows.");
				hunterMap.put(snplayer, "normal");
				return false;
			}
		} else if (arrowType.equalsIgnoreCase("triple")) {
			if (snplayer.getPower() > SNConfigHandler.hunterPowerArrowTriple) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.hunterPowerArrowTriple, "Triple Arrow!");
				final Arrow arrow = player.launchProjectile(Arrow.class);
				arrowMap.put(arrow, arrowType);
				Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> splitArrow(player, arrow), 4);
				return true;
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to shoot Triple Arrows!");
				SuperNManager.sendMessage(snplayer, "Switching to normal arrows.");
				hunterMap.put(snplayer, "normal");
				return false;
			}
		} else if (arrowType.equalsIgnoreCase("power")) {
			if (snplayer.getPower() > SNConfigHandler.hunterPowerArrowPower) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.hunterPowerArrowPower, "Power Arrow!");
				Arrow arrow = player.launchProjectile(Arrow.class);
				arrowMap.put(arrow, arrowType);
				drainedPlayers.add(player);
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log(snplayer.getName() + " is drained.");
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> {
					drainedPlayers.remove(player);
					if (player.isOnline()) {
						SuperNManager.sendMessage(snplayer, "You can shoot again!");
					}
					SupernaturalsPlugin.log(snplayer.getName()
							+ " is no longer drained.");
				}, SNConfigHandler.hunterCooldown / 50);
				return true;
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to shoot Power Arrows!");
				SuperNManager.sendMessage(snplayer, "Switching to normal arrows.");
				hunterMap.put(snplayer, "normal");
				return false;
			}
		} else if (arrowType.equalsIgnoreCase("grapple")) {
			if (snplayer.getPower() > SNConfigHandler.hunterPowerArrowGrapple) {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.hunterPowerArrowGrapple, "Grapple Arrow!");
				Arrow arrow = player.launchProjectile(Arrow.class);
				arrowMap.put(arrow, arrowType);
				return true;
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to shoot Grapple Arrow!");
				SuperNManager.sendMessage(snplayer, "Switching to normal arrows.");
				hunterMap.put(snplayer, "normal");
				return false;
			}
		} else {
			return false;
		}
	}

	public void splitArrow(final Player player, final Arrow arrow) {
		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log(player.getName() + "'s triple arrow event.");
		}
		player.launchProjectile(Arrow.class);
		String arrowType = arrowMap.get(arrow);
		if (arrowType.equals("triple")) {
			arrowMap.put(arrow, "double");
			Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> splitArrow(player, arrow), 4);
		} else {
			arrowMap.remove(arrow);
		}
	}

	public boolean isGrappling(Player player) {
		return grapplingPlayers.contains(player);
	}

	public void startGrappling(Player player, Location targetLocation) {
		if (isGrappling(player)) {
			return;
		}
		ArrowUtil gh = new ArrowUtil(player, targetLocation);
		grapplingPlayers.add(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, gh);
	}

	public void stopGrappling(final Player player) {
		if (isGrappling(player)) {
			Vector v = new Vector(0, 0, 0);
			player.setVelocity(v);
			Bukkit.getScheduler().scheduleSyncDelayedTask(SupernaturalsPlugin.instance, () -> grapplingPlayers.remove(player), 20 * 2);
		}
	}
}
