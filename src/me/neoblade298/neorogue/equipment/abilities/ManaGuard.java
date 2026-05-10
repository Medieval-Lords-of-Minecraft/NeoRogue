package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaGuard extends Equipment {
	private static final String ID = "ManaGuard";
	private int reduc;

	public ManaGuard(boolean isUpgraded) {
		super(ID, "Mana Guard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(10, 5, 0, 0));
		reduc = isUpgraded ? 9 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			String buffId = UUID.randomUUID().toString();
			data.addManaRegen(-1);
			data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
					Buff.increase(data, reduc, StatTracker.defenseBuffAlly(buffId, this)));

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, GlossaryTag.POWER.tag(this) + ". Reduces damage taken by " + DescUtil.yellow(reduc)
				+ " but decrease mana regen by " + DescUtil.white(1) + ".");
	}
}
