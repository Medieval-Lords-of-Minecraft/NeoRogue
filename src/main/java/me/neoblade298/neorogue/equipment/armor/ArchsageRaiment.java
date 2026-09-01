package me.neoblade298.neorogue.equipment.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class ArchsageRaiment extends Equipment {
	private static final String ID = "ArchsageRaiment";
	private static final int MAGICAL_REDUCTION = 4, SHIELD_DURATION = 3;
	private int shields;

	public ArchsageRaiment(boolean isUpgraded) {
		super(ID, "Archsage Raiment", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE,
				EquipmentType.ARMOR, EquipmentProperties.none());
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() { return Equipment.get(ID, false); }

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.MAGICAL), Buff.increase(data, MAGICAL_REDUCTION,
				StatTracker.defenseBuffAlly(id + slot, this)));
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			if (event.getMeta().isBasicAttack()) return TriggerResult.keep();
			Player player = data.getPlayer();
			data.addSimpleShield(player.getUniqueId(), shields, SHIELD_DURATION * 20, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.LEATHER_CHESTPLATE, "Reduce " + GlossaryTag.MAGICAL.tag(this)
				+ " damage taken by " + DescUtil.val(MAGICAL_REDUCTION) + ". Whenever you deal non-basic attack damage, gain "
				+ GlossaryTag.SHIELDS.tag(this, shields) + " " + DescUtil.duration(SHIELD_DURATION) + ".");
	}
}