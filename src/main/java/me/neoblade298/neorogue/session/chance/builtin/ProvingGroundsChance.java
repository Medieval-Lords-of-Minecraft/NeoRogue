package me.neoblade298.neorogue.session.chance.builtin;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
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

public class ProvingGroundsChance extends ChanceSet {

	public ProvingGroundsChance() {
		super(RegionType.LOW_DISTRICT, Material.IRON_SWORD, "ProvingGrounds", "Proving Grounds", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You happen upon an encampment of fellow mercenaries. They greet you warmly,"
				+ " and after sharing stories of the road, they offer to spar so you can test and refine your skills.");

		// Choice 1: Hone your basics - upgrade one chosen piece of gear
		ChanceChoice hone = new ChanceChoice(Material.ANVIL, "Hone Your Basics",
				"Practice with the mercenaries to <yellow>upgrade</yellow> one equipment.",
				"You have no gear that can be upgraded!",
				(s, inst, data) -> data.aggregateEquipment(ProvingGroundsChance::isUpgradable).size() > 0,
				(s, inst, data) -> null); // Never runs; interactive action handles resolution
		hone.setOnInteract((prev, data) -> openHone(prev, data, stage));
		stage.addChoice(hone);

		// Choice 2: Adapt your style - transform one chosen piece of gear
		ChanceChoice adapt = new ChanceChoice(Material.SMITHING_TABLE, "Adapt Your Style",
				"Practice with the mercenaries to <yellow>transform</yellow> one equipment into a random upgraded equipment of miniboss drop rarity.",
				"You have no gear that can be transformed!",
				(s, inst, data) -> data.aggregateEquipment((meta) -> isTransformable(meta, data)).size() > 0,
				(s, inst, data) -> null); // Never runs; interactive action handles resolution
		adapt.setOnInteract((prev, data) -> openAdapt(prev, data, stage));
		stage.addChoice(adapt);
	}

	private void openHone(ChanceInventory prev, PlayerSessionData data, ChanceStage stage) {
		Player p = data.getPlayer();
		ChanceInstance inst = prev.getInst();
		Session s = inst.getSession();
		UUID uuid = p.getUniqueId();

		new EquipmentSelectInventory(data, Component.text("Hone Your Basics", NamedTextColor.GOLD),
				data.aggregateEquipment(ProvingGroundsChance::isUpgradable),
				(meta) -> {
					data.upgradeEquipment(meta.getEquipSlot(), meta.getSlot());
					Equipment upgraded = data.getSessionEquipment(meta.getEquipSlot())[meta.getSlot()].getEquipment();
					Util.msgRaw(p, Component.text("You spar with the mercenaries and hone your ", NamedTextColor.GRAY)
							.append(upgraded.getHoverable())
							.append(Component.text("!", NamedTextColor.GRAY)));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> honed their basics!", p);
					inst.advanceStage(uuid, null);
					s.getInstance().updateBoardLines();
					p.closeInventory();
				},
				() -> new ChanceInventory(p, inst, this, stage));
	}

	private void openAdapt(ChanceInventory prev, PlayerSessionData data, ChanceStage stage) {
		Player p = data.getPlayer();
		ChanceInstance inst = prev.getInst();
		Session s = inst.getSession();
		UUID uuid = p.getUniqueId();

		new EquipmentSelectInventory(data, Component.text("Adapt Your Style", NamedTextColor.GOLD),
				data.aggregateEquipment((meta) -> isTransformable(meta, data)),
				(meta) -> {
					Equipment removed = data.removeEquipment(meta.getEquipSlot(), meta.getSlot()).getEquipment();
					SessionEquipment se = new SessionEquipment(Equipment.getDrop(data.getData().getEquipmentDroptable(),
							s.getBaseDropValue() + 2, 1, data.getPlayerClass(), EquipmentClass.CLASSLESS).get(0)
							.getUpgraded());
					NotorietySetting.rollBreakable(s, se);
					Util.msgRaw(p, Component.text("You adapt your style, leaving behind your ", NamedTextColor.GRAY)
							.append(removed.getHoverable())
							.append(Component.text("...", NamedTextColor.GRAY)));
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> adapted their style!", p);
					inst.advanceStage(uuid, null);
					s.getInstance().updateBoardLines();
					p.closeInventory();
				},
				() -> new ChanceInventory(p, inst, this, stage));
	}

	// Eligible for upgrade: has an upgraded variant, not already upgraded, and not cursed
	private static boolean isUpgradable(EquipmentMetadata meta) {
		Equipment eq = meta.getEquipment();
		return !eq.isCursed() && !eq.isUpgraded() && eq.canUpgrade();
	}

	// Eligible for transform: a real (non-cursed) piece of equipment, not a consumable or artifact, and
	// not protected from removal (i.e. not the player's last weapon or last unlimited ammunition)
	private static boolean isTransformable(EquipmentMetadata meta, PlayerSessionData data) {
		Equipment eq = meta.getEquipment();
		if (eq.isCursed()) return false;
		EquipmentType type = eq.getType();
		boolean validType = type == EquipmentType.WEAPON || type == EquipmentType.ARMOR || type == EquipmentType.ACCESSORY
				|| type == EquipmentType.ABILITY || type == EquipmentType.OFFHAND;
		if (!validType) return false;
		return data.getRemovalRestriction(eq, null, true, "transform") == null;
	}
}
