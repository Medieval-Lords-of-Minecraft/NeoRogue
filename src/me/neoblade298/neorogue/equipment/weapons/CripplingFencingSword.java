package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class CripplingFencingSword extends Equipment {
	private static final String ID = "CripplingFencingSword";
	private int shields, concussed;
	
	public CripplingFencingSword(boolean isUpgraded) {
		super(
				ID, "Crippling Fencing Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(45, 1, 0.3, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		shields = 6;
		concussed = isUpgraded ? 50 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			if (am.addCount(1) < 3) return TriggerResult.keep();
			am.setCount(0);
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			LivingEntity target = ev.getTarget();
			weaponSwingAndDamage(p, data, target);
			FightInstance.getFightData(target.getUniqueId())
					.applyStatus(StatusType.CONCUSSED, data, concussed, -1);
			data.addSimpleShield(p.getUniqueId(), shields, 60);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STONE_SWORD,
				"Every <white>3rd</white> hit, grant yourself " + GlossaryTag.SHIELDS.tag(this, shields, false)
						+ " [<white>3s</white>] and apply " + GlossaryTag.CONCUSSED.tag(this, concussed, true) + "."
		);
	}
}
