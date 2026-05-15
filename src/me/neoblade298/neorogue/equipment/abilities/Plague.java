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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Plague extends Equipment {
	private static final String ID = "Plague";
	private int damage, thres, maxThres;
	
	public Plague(boolean isUpgraded) {
		super(ID, "Plague", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = 10;
		thres = 5;
		maxThres = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();

		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.POISON)) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < 3) return TriggerResult.keep();
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			Util.msg(p, hoverable.append(Component.text(" was activated", NamedTextColor.GRAY)));

			PlagueInstance inst = new PlagueInstance(ID, slot, this);
			data.addTask(new BukkitRunnable() {
				public void run() {
					data.addTrigger(id + "-active", Trigger.APPLY_STATUS, (pdata2, in2) -> {
						ApplyStatusEvent ev2 = (ApplyStatusEvent) in2;
						if (!ev2.isStatus(StatusType.POISON)) return TriggerResult.keep();
						return inst.calculateStacks(ev2.getStacks());
					});
				}
			}.runTask(NeoRogue.inst()));
			data.addTrigger(id + "-attack", Trigger.PRE_BASIC_ATTACK, inst);

			return TriggerResult.remove();
		});
	}

	private class PlagueInstance extends PriorityAction	{
		private int damageStacks, stacksApplied;
		public PlagueInstance(String id, int slot, Equipment eq) {
			super(id);
			action = (pdata, in) -> {
				if (damageStacks > 0) {
					PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
					ev.getMeta().addDamageSlice(new DamageSlice(pdata, damage * damageStacks, DamageType.POISON, DamageStatTracker.of(ID + slot, eq)));
				}
				return TriggerResult.keep();
			};
		}

		public TriggerResult calculateStacks(int added) {
			stacksApplied += added;
			damageStacks = Math.min(maxThres, stacksApplied / thres);
			if (damageStacks >= maxThres) return TriggerResult.remove();
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CACTUS,
				GlossaryTag.POWER.tag(this) + ". Activates after applying " + GlossaryTag.POISON.tag(this) + " " + DescUtil.white(3) + " times. Your basic attacks deal an additional " + GlossaryTag.POISON.tag(this, damage, false) +
				" damage for every " + DescUtil.white(thres) + " stacks of " + GlossaryTag.POISON.tag(this) + " you've applied this fight, up to " + DescUtil.yellow(maxThres * thres) + ".");
	}
}
