package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ToRuins extends Equipment {
	private static final String ID = "ToRuins";
	private static final TargetProperties tp = TargetProperties.radius(10, false, TargetType.ENEMY);
	private int burnPerIntellect;

	public ToRuins(boolean isUpgraded) {
		super(ID, "To Ruins", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 10, tp.range));
		burnPerIntellect = isUpgraded ? 3 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new ToRuinsInstance(data, this, slot, es));
	}

	private class ToRuinsInstance extends EquipmentInstance {
		private LivingEntity target;

		public ToRuinsInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			action = (pdata, in) -> {
				if (target == null) return TriggerResult.keep();
				FightData fd = FightInstance.getFightData(target);
				if (fd != null && fd.hasStatus(StatusType.BURN)) {
					int burnStacks = fd.getStatus(StatusType.BURN).getStacks();
					int intellect = burnStacks / burnPerIntellect;
					if (intellect > 0) {
						data.applyStatus(StatusType.INTELLECT, data, intellect, -1);
						Player p = data.getPlayer();
						Sounds.enchant.play(p, p);
					}
				}
				return TriggerResult.keep();
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			if (!super.canTrigger(p, data, in)) return false;
			target = TargetHelper.getNearestInSight(p, tp);
			return target != null;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, gain " + GlossaryTag.INTELLECT.tag(this, 1, false) + " for every "
						+ DescUtil.yellow(burnPerIntellect) + " " + GlossaryTag.BURN.tag(this)
						+ " the single target you're aiming at has.");
	}
}
