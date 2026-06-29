package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import org.bukkit.Material;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ManaGuard extends Equipment implements Power {
	private static final String ID = "ManaGuard";
	private int reduc;

	public ManaGuard(boolean isUpgraded) {
		super(ID, "Mana Guard", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.none());
		reduc = isUpgraded ? 9 : 6;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana() * 0.5) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = id + slot;
		data.addManaRegen(-1);
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL),
				Buff.increase(data, reduc, StatTracker.defenseBuffAlly(buffId, this)));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD, GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after reaching " + DescUtil.white("50%") + " mana. Reduces damage taken by " + DescUtil.yellow(reduc)
				+ " but decrease mana regen by " + DescUtil.white(1) + ".");
	}
}
