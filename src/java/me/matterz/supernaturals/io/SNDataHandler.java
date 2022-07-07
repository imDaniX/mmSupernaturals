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

package me.matterz.supernaturals.io;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.util.StrictLocation;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class SNDataHandler implements Serializable {

	/**
	 * Auto-Generated serialVersionUID
	 */
	private static final long serialVersionUID = 2266551481298554973L;

	private final HashMap<SuperNPlayer, StrictLocation> teleportLocations = new HashMap<>();
	private final HashMap<SuperNPlayer, SuperNPlayer> angels = new HashMap<>();
	private final HashMap<SuperNPlayer, ArrayList<String>> hunterApps = new HashMap<>();

	private static final String path = "plugins/mmSupernaturals/storage.dat";

	// -------------------------------------------- //
	// Read/Write //
	// -------------------------------------------- //

	public void write() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			SupernaturalsPlugin.log(Level.WARNING, "Storage Data could not be written!");
			e.printStackTrace();
		}
	}

	public static SNDataHandler read() {
		SNDataHandler handler = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			handler = (SNDataHandler) ois.readObject();
			ois.close();
		} catch (Exception e) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(Level.WARNING, "Storage Data not found.");
			}
		}
		return handler;
	}

	// -------------------------------------------- //
	// Teleportation //
	// -------------------------------------------- //

	public void addTeleport(SuperNPlayer player) {
		teleportLocations.put(player, new StrictLocation(Bukkit.getPlayer(player.getName()).getLocation()));
	}

	public boolean checkPlayer(SuperNPlayer player) {
		return teleportLocations.containsKey(player);
	}

	public org.bukkit.Location getTeleport(SuperNPlayer player) {
		StrictLocation location = teleportLocations.get(player);
		return new org.bukkit.Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
	}

	// -------------------------------------------- //
	// Guardian Angels //
	// -------------------------------------------- //

	public boolean hasAngel(SuperNPlayer snplayer) {
		return angels.containsValue(snplayer);
	}

	public void removeAngel(SuperNPlayer snplayer) {
		for (SuperNPlayer player : angels.keySet()) {
			if (angels.get(player).equals(snplayer)) {
				angels.remove(player);
			}
		}
	}

	public SuperNPlayer getAngelPlayer(SuperNPlayer snplayer) {
		return angels.get(snplayer);
	}

	public void addAngel(SuperNPlayer snplayer, SuperNPlayer sntarget) {
		angels.put(snplayer, sntarget);
	}

	// -------------------------------------------- //
	// WitchHunter Apps //
	// -------------------------------------------- //

	public ArrayList<String> getPlayerApp(SuperNPlayer player) {
		return hunterApps.get(player);
	}

	public void addPlayerApp(SuperNPlayer player, ArrayList<String> kills) {
		hunterApps.put(player, kills);
	}

	public boolean playerHasApp(SuperNPlayer player) {
		return hunterApps.containsKey(player);
	}

	public void removePlayerApp(SuperNPlayer player) {
		hunterApps.remove(player);
	}

}
