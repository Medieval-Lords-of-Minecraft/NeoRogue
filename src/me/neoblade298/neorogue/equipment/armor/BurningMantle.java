package me.neoblade298.neorogue.equipment.armor;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceivedDamageEvent;

public class BurningMantle extends Equipment {
	private static final String ID = "burningMantle";
	private int reduc, damage = 50, thres = 500, inc;
	
	public BurningMantle(boolean isUpgraded) {
		super(ID, "Burning Mantle", isUpgraded, Rarity.COMMON, EquipmentClass.CLASSLESS,
				EquipmentType.ARMOR);
				reduc = isUpgraded ? 3 : 2;
			inc = isUpgraded ? 50 : 25;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta tracker = new ActionMeta(), damageCount = new ActionMeta();
		data.addDefenseBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.increase(data, reduc, StatTracker.defenseBuffAlly(UUID.randomUUID().toString(), this)));
		data.addTrigger(id, Trigger.RECEIVED_DAMAGE, (pdata, in) -> {
			ReceivedDamageEvent ev = (ReceivedDamageEvent) in;
			DamageMeta dm = ev.getMeta().getReturnDamage();
			dm.addDamageSlice(new DamageSlice(data, damage + (damageCount.getCount() * inc), DamageType.FIRE));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			tracker.addCount((int) ev.getTotalDamage());
			if (tracker.getCount() >= thres) {
				tracker.addCount(-thres);
				damageCount.addCount(1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER, "Decrease all " + GlossaryTag.GENERAL.tag(this) + " damage taken by <yellow>" + reduc + "</yellow> and deal "
		+ GlossaryTag.FIRE.tag(this, damage, true) + " damage in return. For every " +
		GlossaryTag.FIRE.tag(this, thres, true) + " damage you deal, increase return damage by " + DescUtil.yellow(inc) + ".");
	}
}
