package me.neoblade298.neorogue.equipment.abilities;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Initiator extends Equipment implements Power {
	private static final String ID = "Initiator";
	private int damage;

	public Initiator(boolean isUpgraded) {
		super(ID, "Initiator", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(0, 0, 0, 0));
		damage = isUpgraded ? 50 : 30;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			am.addCount((int) ev.getTotalDamage());
			if (am.getCount() < 250) return TriggerResult.keep();
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		String buffId = UUID.randomUUID().toString();
		data.addTrigger(id, Trigger.PRE_DEAL_DAMAGE, (pdata2, in2) -> {
			Player p2 = data.getPlayer();
			PreDealDamageEvent ev2 = (PreDealDamageEvent) in2;
			FightData fd = FightInstance.getFightData(ev2.getTarget());
			String statusId = p2.getName() + "-INITIATOR";
			if (fd.hasStatus(statusId))
				return TriggerResult.keep();
			ev2.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL),
					new Buff(data, 0, damage * 0.01, StatTracker.damageBuffAlly(buffId, this)));
			data.addTask(new BukkitRunnable() {
				public void run() {
					fd.applyStatus(Status.createByGenericType(GenericStatusType.BASIC, statusId, fd, true),
							data, 1, -1, ev2.getMeta(), false);
				}
			}.runTaskLater(NeoRogue.inst(), 40L));
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				GlossaryTag.POWER.tag(this) + ". Activates after dealing " + DescUtil.white(250) + " damage. For the first " + DescUtil.white("2s") + " after hitting a unique enemy, increase damage by " + DescUtil.yellow(
						damage + "%") + ".");
	}
}
