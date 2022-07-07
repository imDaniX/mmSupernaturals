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

package me.matterz.supernaturals.commands;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.manager.SuperNManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SNCommandReset extends SNCommand {

	public SNCommandReset() {
		requiredParameters = new ArrayList<>();
		optionalParameters = new ArrayList<>();
		senderMustBePlayer = false;
		optionalParameters.add("playername");
		permissions = "supernatural.admin.command.reset";
		helpNameAndParams = "reset | reset [playername]";
		helpDescription = "Reset a player's power to zero";
	}

	@Override
	public void perform() {
		if (!(sender instanceof Player senderPlayer)) {
			if (parameters.isEmpty()) {
				this.sendMessage("Missing player!");
			} else {
				String playername = parameters.get(0);
				Player player = Bukkit.getPlayer(playername);

				if (player == null) {
					this.sendMessage("Player not found!");
					return;
				}
				SuperNPlayer snplayer = SuperNManager.get(player);
				SuperNManager.alterPower(snplayer, -10000, "Admin");
				this.sendMessage("Power reset for player: "
						+ snplayer.getName());
			}
			return;
		}

		if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions)) {
			this.sendMessage("You do not have permissions to use this command.");
			return;
		}
		if (parameters.isEmpty()) {
			SuperNPlayer snplayer = SuperNManager.get(senderPlayer);
			SuperNManager.alterPower(snplayer, -10000, "Admin");
		} else {
			String playername = parameters.get(0);
			Player player = Bukkit.getPlayer(playername);

			if (player == null) {
				this.sendMessage("Player not found!");
				return;
			}
			SuperNPlayer snplayer = SuperNManager.get(player);
			SuperNManager.alterPower(snplayer, -10000, "Admin");
			this.sendMessage("Power reset for player: " + snplayer.getName());
		}
	}
}
