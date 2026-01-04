package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class Dread extends Equipment {
	private static final String ID = "Dread";
	private int stealthGained, thres = 10;
	private double damageIncrease;
	
	public Dread(boolean isUpgraded) {
		super(ID, "Dread", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.none());
		stealthGained = isUpgraded ? 3 : 2;
		damageIncrease = isUpgraded ? 0.60 : 0.40;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardPriorityAction act = new StandardPriorityAction(id);
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.STEALTH)) return TriggerResult.keep();
			act.addCount(ev.getStacks());
			if (act.getCount() >= thres) {
				int triggers = act.getCount() / thres;
				for (int i = 0; i < triggers; i++) {
					// Apply permanent stealth
					data.applyStatus(StatusType.STEALTH, data, stealthGained, -1);
					
					// Apply permanent Speed 1
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0));
					
					// Apply permanent damage buff
					String buffId = UUID.randomUUID().toString();
					data.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), 
						new Buff(data, 0, damageIncrease, StatTracker.damageBuffAlly(buffId, this)));
				}
				act.setCount(act.getCount() % thres);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.WITHER_SKELETON_SKULL,
				"Passive. For every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.STEALTH.tag(this) + " you apply, " +
				"gain " + GlossaryTag.STEALTH.tag(this, stealthGained, true) + ", " + DescUtil.white("Speed 1") + ", and " +
				DescUtil.yellow((int)(damageIncrease * 100) + "%") + " " + GlossaryTag.GENERAL.tag(this) + " damage permanently.");
	}
}
