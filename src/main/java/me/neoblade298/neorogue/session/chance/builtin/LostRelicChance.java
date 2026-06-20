package me.neoblade298.neorogue.session.chance.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Equipment.EquipmentType;
import me.neoblade298.neorogue.equipment.cursed.DullDagger;
import me.neoblade298.neorogue.equipment.cursed.GnarledStaff;
import me.neoblade298.neorogue.equipment.cursed.MangledBow;
import me.neoblade298.neorogue.equipment.cursed.RustySword;
import me.neoblade298.neorogue.equipment.weapons.BeamStaff;
import me.neoblade298.neorogue.equipment.weapons.Nightmare;
import me.neoblade298.neorogue.equipment.weapons.RedBaron;
import me.neoblade298.neorogue.equipment.weapons.SilverFang;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.region.RegionType;
import me.neoblade298.neorogue.session.chance.ChanceChoice;
import me.neoblade298.neorogue.session.chance.ChanceSet;
import me.neoblade298.neorogue.session.chance.ChanceStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

public class LostRelicChance extends ChanceSet {
	private static final HashMap<EquipmentClass, Equipment> items = new HashMap<EquipmentClass, Equipment>();
	private static final HashMap<EquipmentClass, Equipment> purified = new HashMap<EquipmentClass, Equipment>();
	
	static {
		items.put(EquipmentClass.ARCHER, MangledBow.get());
		items.put(EquipmentClass.THIEF, DullDagger.get());
		items.put(EquipmentClass.WARRIOR, RustySword.get());
		items.put(EquipmentClass.MAGE, GnarledStaff.get());
		purified.put(EquipmentClass.ARCHER, RedBaron.get());
		purified.put(EquipmentClass.THIEF, Nightmare.get());
		purified.put(EquipmentClass.WARRIOR, SilverFang.get());
		purified.put(EquipmentClass.MAGE, BeamStaff.get());
	}

	public LostRelicChance() {
		super(RegionType.LOW_DISTRICT, Material.GRAVEL, "LostRelic", "Lost Relic", true);
		ChanceStage stage = new ChanceStage(this, INIT_ID, "You spot an old weapon on the ground. At first glance it seems worthless, "
				+ "but something makes you think it’s worth keeping around.");

		ChanceChoice ch = new ChanceChoice(Material.COPPER_INGOT, "Take the old weapon",
				(s, inst, pdata) -> {
					return getDescription(pdata);
				},
				"You don't have an accessory slot available",
				(s, inst, pdata) -> {
					int numCurses = pdata.aggregateEquipment((meta) -> { return meta.getEquipment().getType() == EquipmentType.ACCESSORY && meta.getEquipment().isCursed(); }).size();
					if (numCurses >= PlayerSessionData.ACCESSORY_SIZE) return false;
					return true;
				},
				(s, inst, data) -> {
					Player p = data.getPlayer();
					data.unequip(EquipmentType.ACCESSORY);
					data.giveEquipment(items.get(data.getPlayerClass()));
					Util.msgRaw(p, "You pick up the old weapon and go on your way. It's a little heavy.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to take the old weapon!", p);
					return null;
				});
		stage.addChoice(ch);
		
		stage.addChoice(new ChanceChoice(Material.LEATHER_BOOTS, "Leave it",
				"Doesn't look worth the extra weight.",
				(s, inst, data) -> {
					Player p = data.getPlayer();
					Util.msgRaw(data.getPlayer(), "Hard pass. It looks ugly too.");
					s.broadcastOthers("<yellow>" + p.getName() + "</yellow> decided to skip the old weapon!", p);
					return null;
				}));
	}
	
	private List<TextComponent> getDescription(PlayerSessionData pdata) {
		ArrayList<TextComponent> lore = new ArrayList<TextComponent>();
		lore.addAll(SharedUtil.addLineBreaks((TextComponent) NeoCore.miniMessage()
				.deserialize("Acquire a <red>cursed accessory</red> that cannot be unequipped or used, but becomes a strong weapon when purified at a shop."), 250));
		Equipment eq = purified.get(pdata.getPlayerClass());
		ItemStack eqItem = eq.getItem();
		ItemMeta eqMeta = eqItem.getItemMeta();
		lore.add((TextComponent) Component.empty());
		lore.add((TextComponent) eqMeta.displayName().decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
		if (eqMeta.lore() != null) {
			for (Component line : eqMeta.lore()) {
				lore.add((TextComponent) line.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE));
			}
		}
		return lore;
	}
}
