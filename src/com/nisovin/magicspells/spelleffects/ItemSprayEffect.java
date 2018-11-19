package com.nisovin.magicspells.spelleffects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.materials.MagicUnknownMaterial;

/**
 * ItemSprayEffect<br>
 * <table border=1>
 *     <tr>
 *         <th>
 *             Config Field
 *         </th>
 *         <th>
 *             Data Type
 *         </th>
 *         <th>
 *             Description
 *         </th>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>type</code>
 *         </td>
 *         <td>
 *             String
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>quantity</code>
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>duration</code>
 *         </td>
 *         <td>
 *             Integer
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>
 *             <code>force</code>
 *         </td>
 *         <td>
 *             Double
 *         </td>
 *         <td>
 *             ???
 *         </td>
 *     </tr>
 * </table>
 */
public class ItemSprayEffect extends SpellEffect {

	MagicMaterial mat;
	
	int num = 15;
	
	int duration = 10;
	
	float force = 1.0F;

	@Override
	public void loadFromString(String string) {
		if (string != null) {
			String[] data = string.split(" ");
			int type = 331;
			short dura = 0;
			if (data.length >= 1) {
				if (data[0].contains(":")) {
					try {
						String[] typeData = data[0].split(":");
						type = Integer.parseInt(typeData[0]);
						dura = Short.parseShort(typeData[1]);
					} catch (NumberFormatException e) {
						DebugHandler.debugNumberFormat(e);
					}
				} else {
					try {
						type = Integer.parseInt(data[0]);
					} catch (NumberFormatException e) {
						DebugHandler.debugNumberFormat(e);
					}
				}
			}
			mat = new MagicUnknownMaterial(type, dura);
			if (data.length >= 2) {
				try {
					num = Integer.parseInt(data[1]);
				} catch (NumberFormatException e) {
					DebugHandler.debugNumberFormat(e);
				}
			}
			if (data.length >= 3) {
				try {
					duration = Integer.parseInt(data[2]);
				} catch (NumberFormatException e) {
					DebugHandler.debugNumberFormat(e);
				}
			}
			if (data.length >= 4) {
				try {
					force = Float.parseFloat(data[3]);
				} catch (NumberFormatException e) {
					DebugHandler.debugNumberFormat(e);
				}
			}
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		mat = MagicSpells.getItemNameResolver().resolveItem(config.getString("type", "redstone"));
		num = config.getInt("quantity", num);
		duration = config.getInt("duration", duration);
		force = (float)config.getDouble("force", force);
	}
	
	@Override
	public Runnable playEffectLocation(Location location) {
		if (mat == null) return null;
		
		// Spawn items
		Random rand = new Random();
		Location loc = location.clone().add(0, 1, 0);
		final Item[] items = new Item[num];
		for (int i = 0; i < num; i++) {
			items[i] = loc.getWorld().dropItem(loc, mat.toItemStack(1));
			items[i].setVelocity(new Vector((rand.nextDouble() - .5) * force, (rand.nextDouble() - .5) * force, (rand.nextDouble() - .5) * force));
			items[i].setPickupDelay(duration << 1);
		}

		// Teleport protection
		Player player = getClosestPlayer(location, 1.5);
		TeleportListener tplistener = new TeleportListener(player, items);
		
		// Schedule item deletion
		MagicSpells.scheduleDelayedTask(() -> {Arrays.stream(items).forEach(Item::remove);
												HandlerList.unregisterAll(tplistener);}, duration);
		return null;
	}
	
	private Player getClosestPlayer(Location loc, double dist) {
		Player player = null;
		double min = dist;
		for (Entity e : loc.getWorld().getNearbyEntities(loc, dist, dist, dist)) {
			if (e instanceof Player) {
				double cdist = e.getLocation().distance(loc);
				if (cdist < min) {
					player = (Player) e;
					min = cdist;
				}
				
			}
		}
		return player;
	}
	
	private static class TeleportListener implements Listener {
		private final Item[] items;
		private final Player player;
		
		public TeleportListener(Player player, Item[] items) {
			if (player != null) {
				MagicSpells.registerEvents(this);
			}
			this.items = items;
			this.player = player;
		}
		
		@EventHandler
		public void onTeleport(PlayerTeleportEvent event) {
			if (event.getPlayer().equals(player)) {
				for (Item i : items) {
					i.remove();
				}
				HandlerList.unregisterAll(this);
			}
		}
	}
	
}
