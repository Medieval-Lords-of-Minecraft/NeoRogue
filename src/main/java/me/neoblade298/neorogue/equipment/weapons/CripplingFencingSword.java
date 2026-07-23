package me.neoblade298.neorogue.equipment.weapons;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
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
		shields = 5;
		concussed = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			LivingEntity target = ev.getTarget();
			Player p = data.getPlayer();
			weaponSwingAndDamage(p, data, target);
			if (am.addCount(1) < 3) return TriggerResult.keep();
			am.setCount(0);
			FightInstance.getFightData(target.getUniqueId())
					.applyStatus(StatusType.CONCUSSED, data, concussed, -1, this);
			data.addSimpleShield(p.getUniqueId(), shields, 60, this);
			return TriggerResult.keep();
		});
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STONE_SWORD,
				"Every " + DescUtil.val("3rd") + " hit, grant yourself " + GlossaryTag.SHIELDS.tag(this, shields)
						+ " [<white>3s</white>] and apply " + GlossaryTag.CONCUSSED.tag(this, concussed) + "."
		);
	}
}
