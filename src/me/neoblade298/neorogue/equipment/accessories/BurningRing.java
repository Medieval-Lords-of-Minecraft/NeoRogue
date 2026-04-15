package me.neoblade298.neorogue.equipment.accessories;

import java.util.UUID;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;

public class BurningRing extends Equipment {
	private static final String ID = "BurningRing";
	private int inc, corruption;
	public BurningRing(boolean isUpgraded) {
		super(ID, "Burning Ring", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ACCESSORY);
		inc = isUpgraded ? 90 : 60;
		corruption = 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addDamageBuff(DamageBuffType.of(DamageCategory.FIRE), Buff.multiplier(data, inc * 0.01, StatTracker.damageBuffAlly(UUID.randomUUID().toString(), this)));
		FightInstance.applyStatus(data.getEntity(), StatusType.CORRUPTION, data, corruption, -1);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_NUGGET, GlossaryTag.FIRE.tag(this) + " damage is increased by " + DescUtil.yellow(inc + "%") + ", but you start fights with " +
		GlossaryTag.CORRUPTION.tag(this, corruption, false) + ".");
	}
}
