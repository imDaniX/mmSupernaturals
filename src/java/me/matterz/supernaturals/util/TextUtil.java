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

package me.matterz.supernaturals.util;

import org.bukkit.Material;

import java.util.List;

public class TextUtil {
	public static String implode(List<String> list, String glue) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				ret.append(glue);
			}
			ret.append(list.get(i));
		}
		return ret.toString();
	}

	public static String getMaterialName(Material material) {
		String ret = material.toString();
		ret = ret.replace('_', ' ');
		ret = ret.toLowerCase();
		return ret;
	}
}
