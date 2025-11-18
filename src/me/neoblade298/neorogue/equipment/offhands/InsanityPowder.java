package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class InsanityPowder extends Equipment {
	private static final String ID = "InsanityPowder";
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private int stacks;
	
	public InsanityPowder(boolean isUpgraded) {
		super(ID, "Insanity Powder", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(0, 0, 0, tp.range));
		stacks = isUpgraded ? 25 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RECEIVE_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightInstance.applyStatus(ent, StatusType.INSANITY, p, stacks, -1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_SHARD, "Gaining " + GlossaryTag.STEALTH.tag(this) + " applies "
				+ GlossaryTag.INSANITY.tag(this, stacks, true) + " to enemies around you.");
	}
}
