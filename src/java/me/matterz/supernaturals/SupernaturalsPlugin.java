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

package me.matterz.supernaturals;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.matterz.supernaturals.commands.*;
import me.matterz.supernaturals.io.*;
import me.matterz.supernaturals.listeners.*;
import me.matterz.supernaturals.manager.*;
import me.matterz.supernaturals.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SupernaturalsPlugin extends JavaPlugin {
	public static SupernaturalsPlugin instance;

	private final SNConfigHandler snConfig = new SNConfigHandler(this);
	private SNDataHandler snData = new SNDataHandler();
	private final SNWhitelistHandler snWhitelist = new SNWhitelistHandler(this);

	private final SuperNManager superManager = new SuperNManager(this);
	private final HumanManager humanManager = new HumanManager(this);
	private final VampireManager vampManager = new VampireManager();
	private final PriestManager priestManager = new PriestManager();
	private final WereManager wereManager = new WereManager();
	private final GhoulManager ghoulManager = new GhoulManager();
	private final HunterManager hunterManager = new HunterManager();
	private final DemonManager demonManager = new DemonManager();
	private final EnderBornManager enderManager = new EnderBornManager(this);
	private final AngelManager angelManager = new AngelManager();

	public final List<SNCommand> commands = new ArrayList<>();

	private static File dataFolder;

	public static boolean foundPerms = false;

	public PluginManager pm;

	public SNDataHandler getDataHandler() {
		return snData;
	}

	// -------------------------------------------- //
	// Managers //
	// -------------------------------------------- //

	public SuperNManager getSuperManager() {
		return superManager;
	}

	public SNConfigHandler getConfigManager() {
		return snConfig;
	}

	public SNWhitelistHandler getWhitelistHandler() {
		return snWhitelist;
	}

	public VampireManager getVampireManager() {
		return vampManager;
	}

	public AngelManager getAngelManager() {
		return angelManager;
	}

	public PriestManager getPriestManager() {
		return priestManager;
	}

	public WereManager getWereManager() {
		return wereManager;
	}

	public GhoulManager getGhoulManager() {
		return ghoulManager;
	}

	public HunterManager getHunterManager() {
		return hunterManager;
	}

	public DemonManager getDemonManager() {
		return demonManager;
	}

	public ClassManager getClassManager(Player player) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		return switch (snplayer.getType().toLowerCase(Locale.ROOT)) {
			case "demon" -> demonManager;
			case "ghoul" -> ghoulManager;
			case "angel" -> angelManager;
			case "witchhunter" -> hunterManager;
			case "priest" -> priestManager;
			case "vampire" -> vampManager;
			case "werewolf" -> wereManager;
			case "enderborn" -> enderManager;
			default -> humanManager;
		};
	}

	// -------------------------------------------- //
	// Plugin Enable/Disable //
	// -------------------------------------------- //

	@Override
	public void onDisable() {
		SuperNManager.cancelTimer();
		snData.write();

		saveData();
		demonManager.removeAllWebs();
		PluginDescriptionFile pdfFile = getDescription();
		log(pdfFile.getName() + " version " + pdfFile.getVersion()
				+ " disabled.");

	}

	@Override
	public void onEnable() {

		SupernaturalsPlugin.instance = this;
		getDataFolder().mkdir();
		pm = getServer().getPluginManager();

		// Add the commands
		commands.add(new SNCommandHelp());
		commands.add(new SNCommandAdmin());
		commands.add(new SNCommandPower());
		commands.add(new SNCommandReload());
		commands.add(new SNCommandSave());
		commands.add(new SNCommandConvert());
		commands.add(new SNCommandCure());
		commands.add(new SNCommandList());
		commands.add(new SNCommandClasses());
		commands.add(new SNCommandSetChurch());
		commands.add(new SNCommandSetBanish());
		commands.add(new SNCommandReset());
		commands.add(new SNCommandKillList());
		commands.add(new SNCommandRmTarget());
		commands.add(new SNCommandRestartTask());
		commands.add(new SNCommandJoin());

		SNEntityListener entityListener = new SNEntityListener(this);
		SNPlayerListener playerListener = new SNPlayerListener(this);
		SNPlayerMonitor playerMonitor = new SNPlayerMonitor(this);
		SNEntityMonitor entityMonitor = new SNEntityMonitor(this);
		SNBlockListener blockListener = new SNBlockListener(this);
		SNServerMonitor serverMonitor = new SNServerMonitor(this);

		PluginDescriptionFile pdfFile = getDescription();
		log(pdfFile.getName() + " version " + pdfFile.getVersion()
				+ " enabled.");

		if (!SNVersionHandler.versionFile.exists()) {
			SNVersionHandler.writeVersion();
		}

		dataFolder = getDataFolder();
		SNConfigHandler.getConfiguration();

		loadData();
		snData = SNDataHandler.read();

		SNWhitelistHandler.reloadWhitelist();

		if (snData == null) {
			snData = new SNDataHandler();
		}

		SuperNManager.startTimer();
		HunterManager.createBounties();
		setupPermissions();
	}

	// -------------------------------------------- //
	// Chat Commands //
	// -------------------------------------------- //

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> parameters = new ArrayList<>(Arrays.asList(args));
		if (sender instanceof Player) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(sender.getName()
						+ " used command: " + commandLabel + " with args: "
						+ TextUtil.implode(parameters, ", "));
			}
			handleCommand(sender, parameters, true);
		} else {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(sender.getName()
						+ " used command: " + commandLabel + " with args: "
						+ TextUtil.implode(parameters, ", "));
			}
			handleCommand(sender, parameters, false);
		}
		return true;
	}

	public void handleCommand(CommandSender sender, List<String> parameters, boolean isPlayer) {
		if (parameters.size() == 0) {
			for (SNCommand vampcommand : commands) {
				if (vampcommand.getName().equalsIgnoreCase("help")) {
					vampcommand.execute(sender, parameters);
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "Unknown command. Try /sn help");
			return;
		}

		String command = parameters.get(0).toLowerCase();
		parameters.remove(0);

		for (SNCommand vampcommand : commands) {
			if (command.equals(vampcommand.getName())) {
				if (!isPlayer && vampcommand.senderMustBePlayer) {
					sender.sendMessage("This command, sn " + command
							+ ", is player-only");
				}
				vampcommand.execute(sender, parameters);
				return;
			}
		}

		sender.sendMessage(ChatColor.RED + "Unknown command \"" + command
				+ "\". Try /sn help");
	}

	// -------------------------------------------- //
	// Data Management //
	// -------------------------------------------- //

	public static void saveAll() {
		File file = new File(dataFolder, "data.yml");
		SNPlayerHandler.save(SuperNManager.getSupernaturals(), file);

		SNConfigHandler.saveConfig();
	}

	public static void saveData() {
		File file = new File(dataFolder, "data.yml");
		SNPlayerHandler.save(SuperNManager.getSupernaturals(), file);
	}

	public static void loadData() {
		File file = new File(dataFolder, "data.yml");
		SuperNManager.setSupernaturals(SNPlayerHandler.load(file));
	}

	public static void reConfig() {
		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log("Reloading config...");
		}
		SNConfigHandler.reloadConfig();
	}

	public static void reloadData() {
		File file = new File(dataFolder, "data.yml");
		SuperNManager.setSupernaturals(SNPlayerHandler.load(file));
	}

	public static void restartTask() {
		SuperNManager.cancelTimer();
		SuperNManager.startTimer();
	}

	// -------------------------------------------- //
	// Permissions //
	// -------------------------------------------- //

	private void setupPermissions() {
		if (pm.isPluginEnabled("PermissionsEx")) {
			log("Found PermissionsEx!");
			foundPerms = true;
		} else if (pm.isPluginEnabled("PermissionsBukkit")) {
			log("Found PermissionsBukkit!");
			foundPerms = true;
		} else if (pm.isPluginEnabled("bPermissions")) {
			log("Found bPermissions.");
			log(Level.WARNING, "If something goes wrong with bPermissions and this plugin, I will not help!");
			foundPerms = true;
		} else if (pm.isPluginEnabled("GroupManager")) {
			log("Found GroupManager.");
			foundPerms = true;
		}

		if (!foundPerms) {
			log("Permission system not detected, defaulting to SuperPerms");
			log("A permissions system may be detected later, just wait.");
		}
	}

	public static boolean hasPermissions(Player player, String permissions) {
		return player.hasPermission(permissions);
	}

	private WorldGuardPlatform getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (!(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return WorldGuard.getInstance().getPlatform();
	}

	public boolean getPvP(Player player) {
		WorldGuardPlatform worldGuard = SupernaturalsPlugin.instance.getWorldGuard();
		if (worldGuard == null) {
			return true;
		}
		BlockVector3 pt = toVector(player.getLocation());
		RegionManager regionManager = worldGuard.getRegionContainer().get(toWeWorld(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
		return set.testState(null, Flags.PVP);
	}

	public boolean getSpawn(Player player) {
		WorldGuardPlatform worldGuard = SupernaturalsPlugin.instance.getWorldGuard();
		if (worldGuard == null) {
			return true;
		}
		BlockVector3 pt = toVector(player.getLocation());
		RegionManager regionManager = worldGuard.getRegionContainer().get(toWeWorld(player.getWorld()));
		ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
		return set.testState(null, Flags.MOB_SPAWNING);
	}

	// -------------------------------------------- //
	// Logging //
	// -------------------------------------------- //

	public static void log(String msg) {
		log(Level.INFO, msg);
	}

	public static void log(Level level, String msg) {
		Logger.getLogger("Minecraft").log(level, "["
				+ instance.getDescription().getFullName() + "] " + msg);
	}

	private static BlockVector3 toVector(Location loc) {
		return BukkitAdapter.asBlockVector(loc);
	}

	private static com.sk89q.worldedit.world.World toWeWorld(World world) {
		return BukkitAdapter.adapt(world);
	}
}
