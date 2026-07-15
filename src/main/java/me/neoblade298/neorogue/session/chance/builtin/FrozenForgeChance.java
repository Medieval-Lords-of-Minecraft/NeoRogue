package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.PlayerSessionData.EquipmentMetadata;
import me.neoblade298.neorogue.player.inventory.EquipmentSelectInventory;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.Session;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceInstance;
import me.neoblade298.neorogue.session.chance.ChanceInventory;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import me.neoblade298.neorogue.session.settings.NotorietySetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class FrozenForgeChance extends ChanceSet {

	public FrozenForgeChance() {
		super(RegionType.FROZEN_WASTES, Material.SMITHING_TABLE, "FrozenForge", "Frozen Forge", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You come across what seems like was once a Blacksmith's forge. "
				+ "Snow is strewn about from the broken windows but somehow the forge's fire is not put out and even glows blue. "
				+ "You feel tempted to use the anvil.");

		// Choice 1: Use the forge - reforge a chosen item into a random reforge result
		ChanceChoice forge = new ChanceChoice(Material.ANVIL, "Use the Forge",
				"Lose one chosen equipment and gain one of its " + DescUtil.white("reforge") + " results at random.",
				"You have no equipment that can be reforged!",
				(s, inst, data) -> data.aggregateEquipment(FrozenForgeChance::isReforgeable).size() > 0,
				(s, inst, data) -> null); // Never runs; interactive action handles resolution
		forge.setOnInteract((prev, data) -> openForge(prev, data, stage));
		stage.addChoice(forge);

		// Choice 2: Sit and warm yourself - obtain a common reforge item
		stage.addChoice(new ChanceChoice(Material.CAMPFIRE, "Sit and Warm Yourself",
				"Obtain an upgraded common reforgeable equipment.",
				(s, inst, data) -> {
					Equipment eq = ManaPoolChance.getReforgeItem(data.getPlayerClass());
					data.giveEquipment(eq.getUpgraded());
					Util.msgRaw(data.getPlayer(), "You rest by the blue flames until your bones thaw, idly tinkering with a spare piece of gear.");
					return null;
				}));
	}

	private void openForge(ChanceInventory prev, PlayerSessionData data, ChanceStage stage) {
		Player p = data.getPlayer();
		ChanceInstance inst = prev.getInst();
		Session s = inst.getSession();
		UUID uuid = p.getUniqueId();

		new EquipmentSelectInventory(data, Component.text("Use the Forge", NamedTextColor.GOLD),
				data.aggregateEquipment(FrozenForgeChance::isReforgeable),
				(meta) -> {
					Equipment removed = data.removeEquipment(meta.getEquipSlot(), meta.getSlot()).getEquipment();
					data.setupInventory();
					ArrayList<Equipment> results = removed.getAllReforgeResults();
					Equipment result = results.get(NeoRogue.gen.nextInt(results.size()));
					SessionEquipment se = s.rollUpgrade(new SessionEquipment(result), 0);
					NotorietySetting.rollBreakable(s, se);
					data.giveEquipment(se);
					Util.msgRaw(p, Component.text("You feed your ", NamedTextColor.GRAY)
							.append(removed.getHoverable())
							.append(Component.text(" into the blue flames, reforging it anew.", NamedTextColor.GRAY)));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> used the forge!", p);
					inst.advanceStage(uuid, null);
					s.getInstance().updateBoardLines();
					p.closeInventory();
				},
				() -> new ChanceInventory(p, inst, this, stage));
	}

	// Eligible for reforge: not cursed and has at least one reforge result
	private static boolean isReforgeable(EquipmentMetadata meta) {
		Equipment eq = meta.getEquipment();
		return !eq.isCursed() && !eq.getAllReforgeResults().isEmpty();
	}
}
