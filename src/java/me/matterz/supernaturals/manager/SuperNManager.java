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
import me.matterz.supernaturals.io.SNWhitelistHandler;
import me.matterz.supernaturals.util.EntityUtil;
import me.matterz.supernaturals.util.SNTaskTimer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class SuperNManager {

	public static SupernaturalsPlugin plugin;
	public static final String WORLD_PERMISSION = "supernatural.world.enabled";
	public static final String INFINITE_POWER_PERMISSION = "supernatural.admin.infinitepower";

	public static List<SuperNPlayer> supernaturals = new ArrayList<>();
	public transient int taskCounter = 0;
	public static int timer;

	public SuperNManager(SupernaturalsPlugin plugin) {
		SuperNManager.plugin = plugin;
	}

	// -------------------------------------------- //
	// Data Management //
	// -------------------------------------------- //

	public static List<SuperNPlayer> getSupernaturals() {
		return supernaturals;
	}

	public static void setSupernaturals(List<SuperNPlayer> supernaturals) {
		if (supernaturals != null) {
			SuperNManager.supernaturals = supernaturals;
		}
	}

	public static SuperNPlayer get(String playername) {
		for (SuperNPlayer supernatural : supernaturals) {
			if (supernatural.getName().equalsIgnoreCase(playername)) {
				return supernatural;
			}
		}

		SuperNPlayer snplayer = new SuperNPlayer(playername);
		supernaturals.add(snplayer);
		return snplayer;
	}

	public static SuperNPlayer get(Player player) {
		for (SuperNPlayer supernatural : supernaturals) {
			if (supernatural.getName().equalsIgnoreCase(player.getName())) {
				return supernatural;
			}
		}
		SuperNPlayer snplayer = new SuperNPlayer(player.getName());
		supernaturals.add(snplayer);
		return snplayer;
	}

	public static Set<SuperNPlayer> findAllOnline() {
		Set<SuperNPlayer> snplayers = new HashSet<>();
		for (Player player : SupernaturalsPlugin.instance.getServer().getOnlinePlayers()) {
			snplayers.add(get(player));
		}
		return snplayers;
	}

	// -------------------------------------------- //
	// Supernatural Conversions //
	// -------------------------------------------- //

	public static void convert(SuperNPlayer snplayer, String superType) {
		convert(snplayer, superType, 0);
	}

	public static void convert(SuperNPlayer snplayer, String superType, int powerLevel) {
		if (!SNConfigHandler.supernaturalTypes.contains(superType)) {
			return;
		}
		if (!SNWhitelistHandler.isWhitelisted(snplayer) && !snplayer.isHuman()) {
			SuperNManager.sendMessage(snplayer, "You have not used the \"/sn join\" command!");
			return;
		}
		String type = superType.toLowerCase();
		snplayer.setOldType(snplayer.getType());
		snplayer.setOldPower(snplayer.getPower());

		snplayer.setType(type);
		if (SupernaturalsPlugin.hasPermissions(plugin.getServer().getPlayer(snplayer.getName()), INFINITE_POWER_PERMISSION)) {
			snplayer.setPower(10000);
		} else {
			snplayer.setPower(powerLevel);
		}

		snplayer.setTruce(true);

		SuperNManager.sendMessage(snplayer, "You are now a " + ChatColor.WHITE
				+ superType + ChatColor.RED + "!");
		SupernaturalsPlugin.log(snplayer.getName() + " turned into a "
				+ ChatColor.WHITE + superType + ChatColor.RED + "!");

		updateName(snplayer);
		HunterManager.updateBounties();
		if (snplayer.getOldType().equals("werewolf")) {
			WereManager.removePlayer(snplayer);
		}
		SupernaturalsPlugin.instance.getGhoulManager().removeBond(snplayer);
		SupernaturalsPlugin.instance.getDataHandler().removeAngel(snplayer);

		SupernaturalsPlugin.saveData();
	}

	public static void cure(SuperNPlayer snplayer) {
		if (snplayer.getOldType().equals("priest")) {
			revert(snplayer);
			return;
		}
		snplayer.setOldType(snplayer.getType());
		snplayer.setOldPower(snplayer.getPower());

		snplayer.setType("human");
		snplayer.setPower(0);

		snplayer.setTruce(true);

		updateName(snplayer);
		HunterManager.updateBounties();
		if (snplayer.getOldType().equals("werewolf")) {
			WereManager.removePlayer(snplayer);
		}
		SupernaturalsPlugin.instance.getGhoulManager().removeBond(snplayer);
		SupernaturalsPlugin.instance.getDataHandler().removeAngel(snplayer);

		SuperNManager.sendMessage(snplayer, "You have been restored to humanity!");
		SupernaturalsPlugin.log(snplayer.getName()
				+ " was restored to humanity!");
		SupernaturalsPlugin.saveData();
	}

	public static void revert(SuperNPlayer snplayer) {
		String oldType = snplayer.getOldType();
		double oldPower = snplayer.getOldPower();

		snplayer.setOldType(snplayer.getType());
		snplayer.setOldPower(snplayer.getPower());

		snplayer.setType(oldType);
		snplayer.setPower(oldPower);

		snplayer.setTruce(true);

		updateName(snplayer);
		HunterManager.updateBounties();
		if (snplayer.getOldType().equals("werewolf")) {
			WereManager.removePlayer(snplayer);
		}
		SupernaturalsPlugin.instance.getGhoulManager().removeBond(snplayer);
		SupernaturalsPlugin.instance.getDataHandler().removeAngel(snplayer);

		SuperNManager.sendMessage(snplayer, "You been reverted to your previous state of being a "
				+ ChatColor.WHITE + oldType + ChatColor.RED + "!");
		SupernaturalsPlugin.log(snplayer.getName()
				+ " was reverted to the previous state of being a " + oldType
				+ "!");
		SupernaturalsPlugin.saveData();

	}

	// -------------------------------------------- //
	// Power Altering //
	// -------------------------------------------- //

	public static void alterPower(SuperNPlayer snplayer, double delta) {
		if (SupernaturalsPlugin.hasPermissions(SupernaturalsPlugin.instance.getServer().getPlayer(snplayer.getName()), INFINITE_POWER_PERMISSION)) {
			if (delta < 0) {
				return;
			}
		}
		snplayer.setPower(snplayer.getPower() + delta);
	}

	public static void alterPower(SuperNPlayer snplayer, double delta, String reason) {
		if (SupernaturalsPlugin.hasPermissions(SupernaturalsPlugin.instance.getServer().getPlayer(snplayer.getName()), INFINITE_POWER_PERMISSION)) {
			if (delta < 0) {
				return;
			}
		}
		alterPower(snplayer, delta);
		SuperNManager.sendMessage(snplayer, "Power: " + ChatColor.WHITE
				+ (int) snplayer.getPower() + ChatColor.RED + " ("
				+ ChatColor.WHITE + (int) delta + ChatColor.RED + ") " + reason);
	}

	// -------------------------------------------- //
	// Movement //
	// -------------------------------------------- //

	public static void jump(Player player, double deltaSpeed, boolean upOnly) {
		SuperNPlayer snplayer = SuperNManager.get(player);

		if (upOnly) {
			if (snplayer.getPower() - SNConfigHandler.jumpBloodCost <= 0) {
				SuperNManager.sendMessage(snplayer, "Not enough Power to jump.");
				return;
			} else {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.jumpBloodCost, "SuperJump!");
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log(snplayer.getName() + " used jump!");
				}
			}
		} else {
			if (snplayer.getPower() - SNConfigHandler.dashBloodCost <= 0) {
				SuperNManager.sendMessage(snplayer, "Not enough Power to dash.");
				return;
			} else {
				SuperNManager.alterPower(snplayer, -SNConfigHandler.dashBloodCost, "Dash!");
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log(snplayer.getName() + " used dash!");
				}
			}
		}

		Vector vjadd;
		if (upOnly) {
			vjadd = new Vector(0, 1, 0);
		} else {
			Vector vhor = player.getLocation().getDirection();
			vjadd = new Vector(vhor.getX(), 0, vhor.getZ());
			vjadd.normalize();
		}
		vjadd.multiply(deltaSpeed);

		player.setVelocity(player.getVelocity().add(vjadd));
	}

	// -------------------------------------------- //
	// Monster Truce Feature (Passive) //
	// -------------------------------------------- //

	public void truceBreak(SuperNPlayer snplayer) {
		if (!snplayer.isSuper()) {
			snplayer.setTruce(true);
			return;
		}
		if (snplayer.getTruce()) {
			SuperNManager.sendMessage(snplayer, "You temporarily broke your truce with monsters!");
		}
		snplayer.setTruce(false);
		snplayer.setTruceTimer(SNConfigHandler.truceBreakTime);
	}

	public static void truceRestore(SuperNPlayer snplayer) {
		SuperNManager.sendMessage(snplayer, "Your truce with monsters has been restored!");
		snplayer.setTruce(true);
		snplayer.setTruceTimer(0);

		// Untarget the player.
		Player player = SupernaturalsPlugin.instance.getServer().getPlayer(snplayer.getName());
		for (LivingEntity entity : player.getWorld().getLivingEntities()) {
			if (!(entity instanceof Creature)) {
				continue;
			}

			if (snplayer.isVampire()
					&& SNConfigHandler.vampireTruce.contains(EntityUtil.entityTypeFromEntity(entity))) {
				Creature creature = (Creature) entity;
				LivingEntity target = creature.getTarget();
				if (target != null && creature.getTarget().equals(player)) {
					creature.setTarget(null);
				}
			} else if (snplayer.isGhoul()
					&& SNConfigHandler.ghoulTruce.contains(EntityUtil.entityTypeFromEntity(entity))) {
				Creature creature = (Creature) entity;
				LivingEntity target = creature.getTarget();
				if (target != null && creature.getTarget().equals(player)) {
					creature.setTarget(null);
				}
			} else if (snplayer.isWere() && SNConfigHandler.wolfTruce
					&& entity instanceof Wolf) {
				Creature creature = (Creature) entity;
				LivingEntity target = creature.getTarget();
				if (target != null && creature.getTarget().equals(player)) {
					creature.setTarget(null);
				}
			}
		}
	}

	public void truceBreakAdvanceTime(SuperNPlayer snplayer, int milliseconds) {
		if (snplayer.getTruce()) {
			return;
		}

		truceBreakTimeLeftAlter(snplayer, -milliseconds);
	}

	private void truceBreakTimeLeftAlter(SuperNPlayer snplayer, int delta) {
		if (snplayer.getTruceTimer() + delta < 0) {
			truceRestore(snplayer);
		} else {
			snplayer.setTruceTimer(snplayer.getTruceTimer() + delta);
		}
		SupernaturalsPlugin.saveData();
	}

	// -------------------------------------------- //
	// Regenerate Feature //
	// -------------------------------------------- //

	public void regenAdvanceTime(Player player, int milliseconds) {
		if (player.isDead()) {
			return;
		}

		SuperNPlayer snplayer = SuperNManager.get(player);
		double currentHealth = player.getHealth();

		if (currentHealth == 20) {
			return;
		}

		double deltaSeconds = milliseconds / 1000D;
		double deltaHeal;

		if (snplayer.isVampire()) {
			if (snplayer.getPower() <= SNConfigHandler.vampireHealthCost) {
				if (SNConfigHandler.debugMode) {
					SupernaturalsPlugin.log("Regen Event: Vampire player " + player.getName() + " not enough power!");
				}
				return;
			}

			deltaHeal = deltaSeconds * SNConfigHandler.vampireTimeHealthGained;
			SuperNManager.alterPower(snplayer, -SNConfigHandler.vampireHealthCost, "Healing!");

		} else if (snplayer.isGhoul()) {
			if (player.getWorld().hasStorm()
					&& !plugin.getGhoulManager().isUnderRoof(player)) {
				return;
			}
			deltaHeal = deltaSeconds * SNConfigHandler.ghoulHealthGained;
		} else {
			if (!worldTimeIsNight(player)) {
				return;
			}
			deltaHeal = deltaSeconds * SNConfigHandler.wereHealthGained;
		}

		double targetHealth = currentHealth + deltaHeal;
		if (targetHealth > 20) {
			targetHealth = 20;
		}
		try {
			player.setHealth(targetHealth);
		} catch (IllegalArgumentException e) {
			SupernaturalsPlugin.log("Attempted to regen dead player.");
		}
		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log("Regen Event: player " + player.getName()
					+ " gained " + deltaHeal + " health.");
		}
	}

	// -------------------------------------------- //
	// Targetting //
	// -------------------------------------------- //

	public Player getTarget(Player player) {
		List<Block> blocks = player.getLineOfSight(SNConfigHandler.transparent, 20);
		List<Entity> entities = player.getNearbyEntities(21, 21, 21);
		for (Block block : blocks) {
			for (Entity entity : entities) {
				if (entity instanceof Player victim) {
					Location location = victim.getLocation();
					Location feetLocation = new Location(location.getWorld(), location.getX(), location.getY() - 1, location.getZ());
					Location groundLocation = new Location(location.getWorld(), location.getX(), location.getY() - 2, location.getZ());
					if (location.getBlock().equals(block)
							|| feetLocation.getBlock().equals(block)
							|| groundLocation.getBlock().equals(block)) {
						return victim;
					}
				}
			}
		}
		return null;
	}

	// -------------------------------------------- //
	// Messages //
	// -------------------------------------------- //

	public static void sendMessage(SuperNPlayer snplayer, String message) {
		Player player = SupernaturalsPlugin.instance.getServer().getPlayer(snplayer.getName());
		if (player == null) {
			return;
		}
		player.sendMessage(ChatColor.RED + message);
	}

	public static void sendMessage(SuperNPlayer snplayer, List<String> messages) {
		for (String message : messages) {
			SuperNManager.sendMessage(snplayer, message);
		}
	}

	public static void updateName(SuperNPlayer snplayer) {
		Player player = SupernaturalsPlugin.instance.getServer().getPlayer(snplayer.getName());
		String name = player.getName();
		String displayname = player.getDisplayName().trim();
		String updatedname;
		ChatColor color;

		if (snplayer.isPriest()) {
			color = ChatColor.GOLD;
		} else if (snplayer.isVampire()) {
			color = ChatColor.DARK_PURPLE;
		} else if (snplayer.isGhoul()) {
			color = ChatColor.DARK_GRAY;
		} else if (snplayer.isWere()) {
			color = ChatColor.BLUE;
		} else if (snplayer.isHunter()) {
			color = ChatColor.GREEN;
		} else if (snplayer.isDemon()) {
			color = ChatColor.RED;
		} else if (snplayer.isEnderBorn()) {
			color = ChatColor.LIGHT_PURPLE;
		} else if (snplayer.isAngel()) {
			color = ChatColor.AQUA;
		} else {
			color = ChatColor.WHITE;
		}

		if (displayname.contains("�." + name)) {
			updatedname = displayname.replaceFirst(" �." + name, " " + color
					+ name);
		} else {
			updatedname = displayname.replaceFirst(name, color + name);
		}

		if (SNConfigHandler.enableColors) {
			player.setDisplayName(updatedname);
		}
	}

	// -------------------------------------------- //
	// TimeKeeping //
	// -------------------------------------------- //

	public static boolean worldTimeIsNight(Player player) {
		long time = player.getWorld().getTime() % 24000;

		return time < 0 || time > 12400;
	}

	public static void startTimer() {
		timer = SupernaturalsPlugin.instance.getServer().getScheduler().scheduleSyncRepeatingTask(SupernaturalsPlugin.instance, new SNTaskTimer(SupernaturalsPlugin.instance), 0, 20);
		if (timer == -1) {
			SupernaturalsPlugin.log(Level.WARNING, "Timer failed!");
		}
	}

	public static void cancelTimer() {
		SupernaturalsPlugin.instance.getServer().getScheduler().cancelTask(timer);
	}

	public void advanceTime(SuperNPlayer snplayer) {
		Player player = plugin.getServer().getPlayer(snplayer.getName());

		if (player == null) {
			return;
		}

		if (!SupernaturalsPlugin.hasPermissions(player, WORLD_PERMISSION)
				&& SNConfigHandler.multiworld) {
			return;
		}

		taskCounter++;
		if (taskCounter >= 30) {
			taskCounter = 0;
		}

		if (snplayer.isVampire()) {
			if (taskCounter % 3 == 0) {
				regenAdvanceTime(player, 3000);
			}
			if (taskCounter % 3 == 0) {
				plugin.getVampireManager().combustAdvanceTime(player, 3000);
				plugin.getVampireManager().gainPowerAdvanceTime(snplayer, 3000);
			}
		} else if (snplayer.isGhoul()) {
			if (taskCounter % 10 == 0) {
				regenAdvanceTime(player, 10000);
			}
			plugin.getGhoulManager().waterAdvanceTime(player);
		} else if (snplayer.isWere()) {
			if (taskCounter % 5 == 0) {
				regenAdvanceTime(player, 5000);
			}
		} else if (snplayer.isAngel()) {
			if (taskCounter % 10 == 0) {
				plugin.getAngelManager().waterAdvanceTime(player);
			}
		}

		plugin.getClassManager(player).armorCheck(player);

		if (snplayer.isDemon()) {
			if (taskCounter % 5 == 0) {
				plugin.getDemonManager().powerAdvanceTime(player, 5);
			}
		}

		if (snplayer.isSuper()) {
			if (taskCounter % 3 == 0) {
				truceBreakAdvanceTime(snplayer, 3000);
			}
		}

	}
}
