package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageCategory;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.DamageBuffType;
import me.neoblade298.neorogue.session.fight.buff.StatTracker;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveDamageEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.ReceiveHealthDamageEvent;

public class GoldenCitadel extends Equipment {
	private static final String ID = "GoldenCitadel";
	private static final double LOWERED_REDUCTION = 0.1;
	private static final double STAMINA_PER_SHIELD_TICK = 2;
	private static final ParticleContainer RETALIATE = new ParticleContainer(Particle.DUST).count(8)
			.spread(0.1, 0.1).offsetY(1).speed(0.01)
			.dustOptions(new DustOptions(Color.fromRGB(218, 174, 65), 1F));
	private double raisedReduction;
	private int raisedPercent;

	public GoldenCitadel(boolean isUpgraded) {
		super(ID, "Golden Citadel", isUpgraded, Rarity.EPIC, EquipmentClass.WARRIOR, EquipmentType.OFFHAND);
		raisedReduction = isUpgraded ? 0.6 : 0.4;
		raisedPercent = (int) (raisedReduction * 100);
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta defense = new ActionMeta();
		data.addTrigger(id, Trigger.SHIELD_TICK, (pdata, in) -> {
			data.addStamina(-STAMINA_PER_SHIELD_TICK);
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, (pdata, in) -> {
			Player p = data.getPlayer();
			boolean raised = p.getHandRaised() == EquipmentSlot.OFF_HAND && p.isHandRaised();
			ReceiveDamageEvent ev = (ReceiveDamageEvent) in;
			ev.getMeta().addDefenseBuff(DamageBuffType.of(DamageCategory.DIRECT), Buff.multiplier(data,
					raised ? raisedReduction : LOWERED_REDUCTION,
					StatTracker.defenseBuffAlly(defense.getId(), this, false)));
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.RECEIVE_HEALTH_DAMAGE, (pdata, in) -> {
			ReceiveHealthDamageEvent ev = (ReceiveHealthDamageEvent) in;
			int amount = (int) Math.ceil(ev.getTotalDamage());
			if (amount <= 0) return TriggerResult.keep();
			Player p = data.getPlayer();
			data.addPermanentShield(p.getUniqueId(), amount, this);
			data.applyStatus(StatusType.THORNS, data, amount, -1, this);
			data.applyStatus(StatusType.REFLECT, data, amount, -1, this);
			RETALIATE.play(p, p);
			Sounds.block.play(p, p);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SHIELD,
				"While raised, drain " + DescUtil.white("4 stamina/s") + " and reduce damage taken by "
						+ DescUtil.yellow(raisedPercent + "%") + "; while lowered, reduce it by "
						+ DescUtil.white("10%") + ". Whenever you lose health to damage, gain permanent "
						+ GlossaryTag.SHIELDS.tag(this) + ", " + GlossaryTag.THORNS.tag(this) + ", and "
						+ GlossaryTag.REFLECT.tag(this) + " equal to the health lost.");
	}
}