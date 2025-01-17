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

import me.matterz.supernaturals.SupernaturalsPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SNCommandReload extends SNCommand {
	public String msgLoadSuccess = "Loaded %s.";
	public String msgLoadFail = "FAILED to load %s.";
	public String msgSaveSuccess = "Saved %s.";
	public String msgSaveFail = "FAILED to save %s.";

	public SNCommandReload() {
		requiredParameters = new ArrayList<>();
		optionalParameters = new ArrayList<>();
		optionalParameters.add("type");
		senderMustBePlayer = false;
		permissions = "supernatural.admin.command.reload";
		helpNameAndParams = "";
		helpDescription = "Reload Config or Data files";
	}

	@Override
	public void perform() {
		if (!(sender instanceof Player senderPlayer)) {
			if (parameters.isEmpty()) {
				SupernaturalsPlugin.reConfig();
				this.sendMessage("Config file has been reloaded");
			} else {
				if (parameters.get(0).equalsIgnoreCase("config")) {
					SupernaturalsPlugin.reConfig();
					this.sendMessage("Config file has been reloaded");
				} else if (parameters.get(0).equalsIgnoreCase("data")) {
					SupernaturalsPlugin.reloadData();
					this.sendMessage("Data file has been reloaded");
				} else {
					this.sendMessage("Invalid option.");
				}
			}
			return;
		}
		if (!SupernaturalsPlugin.hasPermissions(senderPlayer, permissions)) {
			this.sendMessage("You do not have permissions to use this command.");
			return;
		}
		if (parameters.isEmpty()) {
			SupernaturalsPlugin.reConfig();
			this.sendMessage("Config file has been reloaded");
		} else {
			if (parameters.get(0).equalsIgnoreCase("config")) {
				SupernaturalsPlugin.reConfig();
				this.sendMessage("Config file has been reloaded");
			} else if (parameters.get(0).equalsIgnoreCase("data")) {
				SupernaturalsPlugin.reloadData();
				this.sendMessage("Data file has been reloaded");
			} else {
				this.sendMessage("Invalid option.");
			}
		}
	}
}
