package me.neoblade298.neorogue.equipment.offhands;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.Mob;
import me.neoblade298.neorogue.session.fight.Mob.MobType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class WristBlade extends Equipment {
	private static final String ID = "WristBlade";
	private static final double NORMAL_THRESHOLD = 0.3, BOSS_THRESHOLD = 0.06;
	private final int stealthDuration, shields;

	public WristBlade(boolean isUpgraded) {
		super(ID, "Wrist Blade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.OFFHAND);
		stealthDuration = isUpgraded ? 160 : 100;
		shields = isUpgraded ? 5 : 3;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			LivingEntity target = ev.getTarget();
			if (target == null || target.getAttribute(Attribute.MAX_HEALTH) == null) return TriggerResult.keep();

			double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();
			double threshold = NORMAL_THRESHOLD;
			FightData targetFD = FightInstance.getFightData(target);
			if (targetFD != null) {
				Mob mob = targetFD.getMob();
				if (mob != null && (mob.getType() == MobType.MINIBOSS || mob.getType() == MobType.BOSS)) {
					threshold = BOSS_THRESHOLD;
				}
			}

			if (ev.getTotalDamage() > maxHealth * threshold) {
				data.applyStatus(StatusType.STEALTH, data, 1, stealthDuration, this);
				data.addSimpleShield(data.getPlayer().getUniqueId(), shields, stealthDuration, this);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_SHARD,
				"Dealing over " + DescUtil.val("30%") + " of an enemy's health (" + DescUtil.val("6%")
				+ " for a miniboss or boss) in one hit grants " + GlossaryTag.STEALTH.tag(this, 1) + " "
				+ DescUtil.duration(stealthDuration / 20) + " and " + GlossaryTag.SHIELDS.tag(this, shields) + ".");
	}
}
