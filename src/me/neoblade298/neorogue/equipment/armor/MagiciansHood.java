package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MagiciansHood extends Equipment {
	private static final String ID = "magiciansHood";
	private static final int MAX_STACKS = 3;
	private int thres, def;

	public MagiciansHood(boolean isUpgraded) {
		super(ID, "Magician's Hood", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ARMOR);
		thres = isUpgraded ? 25 : 35;
		def = 1;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			double mana = data.getMaxMana();
			int stacks = Math.min((int) Math.floor(mana / thres), MAX_STACKS);
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, def * stacks, StatTracker.defenseBuffAlly(buffId, this)), 40);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_HELMET,
				"Increase your " + GlossaryTag.GENERAL.tag(this) + " defense by " + DescUtil.white(def) + " for every "
						+ DescUtil.yellow(thres) + " max mana you have, up to " + DescUtil.white(MAX_STACKS + "x") + ".");

		LeatherArmorMeta dye = (LeatherArmorMeta) item.getItemMeta();
		dye.setColor(Color.BLUE);
		item.setItemMeta(dye);
	}
}
