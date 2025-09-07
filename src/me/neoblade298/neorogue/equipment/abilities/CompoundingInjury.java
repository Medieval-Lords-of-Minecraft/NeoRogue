package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class CompoundingInjury extends Equipment {
	private static final String ID = "compoundingInjury";
	private int multStr, thres;
	private double mult;
	
	public CompoundingInjury(boolean isUpgraded) {
		super(ID, "Compounding Injury", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
				thres = isUpgraded ? 100 : 150;
				mult = isUpgraded ? 1.5 : 1;
				multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			PreDealDamageEvent ev = (PreDealDamageEvent) in;
			FightData fd = FightInstance.getFightData(ev.getTarget());
			if (fd.getStatus(StatusType.CONCUSSED).getStacks() >= thres && !ev.getMeta().getPrimarySlice().getType().getCategories().contains(DamageCategory.STATUS)) {
				DamageMeta dm = ev.getMeta().clone();
				dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, mult, 
						BuffStatTracker.ignored(this)));
				data.addTask(new BukkitRunnable() {
					public void run() {
						FightInstance.dealDamage(dm, ev.getTarget());
					}
				}.runTaskLater(NeoRogue.inst(), 20));
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_ORE,
				"Passive. Dealing damage to an enemy with at least " + GlossaryTag.CONCUSSED.tag(this, thres, true) + " will cause the damage to happen again, but "
				+ "multiplied by " + DescUtil.yellow(multStr + "%") + ".");
	}
}
