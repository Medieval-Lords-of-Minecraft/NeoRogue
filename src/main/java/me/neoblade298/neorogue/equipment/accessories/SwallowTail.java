package me.neoblade298.neorogue.equipment.accessories;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class SwallowTail extends Equipment {
	private static final String ID = "SwallowTail";
	private static final int DAMAGE_INSTANCES = 6;
	private static final int SHIELD_DURATION_TICKS = 80;
	private int shields;

	public SwallowTail(boolean isUpgraded) {
		super(ID, "Swallow Tail", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER, EquipmentType.ACCESSORY);
		shields = isUpgraded ? 3 : 2;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta instances = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent event = (DealDamageEvent) in;
			if (!event.getMeta().hasOrigin(DamageOrigin.PROJECTILE)) return TriggerResult.keep();
			if (instances.addCount(1) < DAMAGE_INSTANCES) return TriggerResult.keep();

			instances.setCount(0);
			Player player = data.getPlayer();
			data.addSimpleShield(player.getUniqueId(), shields, SHIELD_DURATION_TICKS, this);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER, "Every " + DescUtil.white(DAMAGE_INSTANCES)
				+ " instances of dealing projectile damage, gain " + GlossaryTag.SHIELDS.tag(this, shields)
				+ " [" + DescUtil.white("4s") + "].");
	}
}