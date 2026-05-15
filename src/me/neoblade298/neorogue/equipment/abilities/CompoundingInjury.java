package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CompoundingInjury extends Equipment {
	private static final String ID = "CompoundingInjury";
	private int multStr, thres;
	private double mult;
	
	public CompoundingInjury(boolean isUpgraded) {
		super(ID, "Compounding Injury", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
				thres = isUpgraded ? 20 : 30;
				mult = isUpgraded ? 1.5 : 1;
				multStr = (int) (mult * 100);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 30;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.CONCUSSED)) return TriggerResult.keep();
			am.addCount(ev.getStacks());
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata2, in2) -> {
				DealDamageEvent ev2 = (DealDamageEvent) in2;
				FightData fd = FightInstance.getFightData(ev2.getTarget());
				if (ev2.getMeta().isSecondary())
					return TriggerResult.keep();
				if (fd.getStatus(StatusType.CONCUSSED).getStacks() >= thres && !ev2.getMeta().getPrimarySlice().getType().getCategories().contains(DamageCategory.STATUS)) {
					DamageMeta dm = ev2.getMeta().clone();
					dm.addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), Buff.multiplier(data, mult, 
							BuffStatTracker.ignored(this)));
					dm.isSecondary(true);

					for (DamageSlice slice : dm.getSlices()) {
						slice.setTracker(DamageStatTracker.of(id + slot, this));
					}
					data.addTask(new BukkitRunnable() {
						public void run() {
							FightInstance.dealDamage(dm, ev2.getTarget());
						}
					}.runTaskLater(NeoRogue.inst(), 20));
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_ORE,
				GlossaryTag.POWER.tag(this) + ". Dealing damage to an enemy with at least " + GlossaryTag.CONCUSSED.tag(this, thres, true) + " will cause the damage to happen again, but "
				+ "multiplied by " + DescUtil.yellow(multStr + "%") + ".");
	}
}
