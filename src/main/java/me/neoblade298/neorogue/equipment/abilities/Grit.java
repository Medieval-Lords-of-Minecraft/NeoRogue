package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.KillEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreDealDamageEvent;

public class Grit extends Equipment implements Power {
	private static final String ID = "Grit";
	private int shields, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	private static final SoundContainer equip = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN);
	
	public Grit(boolean isUpgraded) {
		super(ID, "Grit", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 0));
		shields = isUpgraded ? 9 : 6;
		inc = isUpgraded ? 35 : 25;
		pc.count(30).spread(0.5, 0.5).speed(0.2).offsetY(1);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			if (data.getPlayer().getLocation().distance(ev.getTarget().getLocation()) > 5) return TriggerResult.keep();
			if (am.addCount(1) < 5) return TriggerResult.keep();

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
			double dist = ev2.getTarget().getLocation().distanceSquared(p2.getLocation());
			if (dist <= 25) {
				ev2.getMeta().addDamageBuff(DamageBuffType.of(DamageCategory.GENERAL), new Buff(data, inc, 0, StatTracker.damageBuffAlly(buffId, this)));
			}
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.KILL, (pdata3, in3) -> {
			Player p3 = data.getPlayer();
			KillEvent ev3 = (KillEvent) in3;
			double dist = ev3.getTarget().getLocation().distanceSquared(p3.getLocation());
			if (dist <= 25) {
				pc.play(p3, p3);
				data.addSimpleShield(p3.getUniqueId(), shields, 160);
				equip.play(p3, p3);
			}
			return TriggerResult.keep();
		});
	}


	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				GlossaryTag.POWER.tag(this) + ". Activates after dealing close-range damage " + DescUtil.white(5) + " times. Dealing damage from at most " + DescUtil.white(5) + " blocks away increases " + GlossaryTag.GENERAL.tag(this) + " damage by " +
				DescUtil.yellow(inc) + ". Killing an enemy within " + DescUtil.white(5) + " blocks grants " + GlossaryTag.SHIELDS.tag(this, shields, true) + " [" + DescUtil.white("8s") + "].");
	}
}
