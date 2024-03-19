package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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
	private static final String ID = "cripplingFencingSword";
	private int shields, concussed;
	
	public CripplingFencingSword(boolean isUpgraded) {
		super(
				ID, "Crippling Fencing Sword", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(45, 1, 0.3, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_CRIT)
		);
		shields = isUpgraded ? 4 : 3;
		concussed = isUpgraded ? 6 : 4;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			LivingEntity target = ev.getTarget();
			weaponSwingAndDamage(p, data, target);
			FightInstance.getFightData(target.getUniqueId())
					.applyStatus(StatusType.CONCUSSED, data, concussed, 0);
			data.addSimpleShield(p.getUniqueId(), shields, 40);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STONE_SWORD,
				"On hit, grant yourself <yellow>" + shields + "</yellow> " + GlossaryTag.SHIELDS.tag(this)
						+ " for <white>2</white> seconds. Apply " + GlossaryTag.CONCUSSED.tag(this, concussed, true)
						+ " every 2 hits."
		);
	}
}
