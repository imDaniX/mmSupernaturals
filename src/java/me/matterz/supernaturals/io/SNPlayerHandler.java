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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SNPlayerHandler {
	public static List<SuperNPlayer> load(File file) {
		Constructor constructor = new Constructor();
		constructor.addTypeDescription(new TypeDescription(SuperNPlayer.class, new Tag("player")));

		Yaml yaml = new Yaml(constructor);

		try {
			List<SuperNPlayer> supernaturals = yaml.load(new FileReader(file));

			if (supernaturals == null) {
				return new ArrayList<>();
			}

			return supernaturals;
		} catch (FileNotFoundException e) {
			SupernaturalsPlugin.log(Level.WARNING, "Player data not found!");
			return null;
		}
	}

	public static void save(List<SuperNPlayer> supernaturals, File file) {
		Representer representer = new Representer();
		representer.addClassTag(SuperNPlayer.class, new Tag("player"));

		DumperOptions options = new DumperOptions();
		options.setWidth(300);
		options.setIndent(4);

		Yaml yaml = new Yaml(representer, options);

		try {
			yaml.dump(supernaturals, new FileWriter(file));
		} catch (IOException e) {
			SupernaturalsPlugin.log(Level.WARNING, "Player data could not be written!");
			e.printStackTrace();
		}
	}
}
