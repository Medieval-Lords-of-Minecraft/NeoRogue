package me.neoblade298.neorogue.equipment.abilities;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealDamageEvent;

public class SpotWeakness extends Equipment implements Power {
	private static final String ID = "SpotWeakness";
	private static final int ACTIVATION_THRES = 2;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT).count(20).spread(0.5, 0.5);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT);
	private int strength;

	public SpotWeakness(boolean isUpgraded) {
		super(ID, "Spot Weakness", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.none());
		strength = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		// Enemies that have already counted (toward activation or a later strength grant). Each
		// enemy only ever counts once, so the enemies used to activate can't later grant strength.
		HashSet<UUID> counted = new HashSet<UUID>();
		data.addTrigger(id, Trigger.DEAL_DAMAGE, (pdata, in) -> {
			DealDamageEvent ev = (DealDamageEvent) in;
			LivingEntity target = ev.getTarget();
			if (target == null) return TriggerResult.keep();
			double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();
			if (target.getHealth() >= maxHealth * 0.5) return TriggerResult.keep();
			// Only count an enemy the first time it's damaged below 50% health
			if (!counted.add(target.getUniqueId())) return TriggerResult.keep();
			if (counted.size() < ACTIVATION_THRES) return TriggerResult.keep();
			if (!activatePower(data, slot, es)) return TriggerResult.keep();

			// Once activated, damaging a new enemy below 50% health grants strength. Enemies already
			// in `counted` (including the two used to activate) are excluded.
			data.addTrigger(id + "-active", Trigger.DEAL_DAMAGE, (pd, i) -> {
				DealDamageEvent e = (DealDamageEvent) i;
				LivingEntity t = e.getTarget();
				if (t == null) return TriggerResult.keep();
				double max = t.getAttribute(Attribute.MAX_HEALTH).getValue();
				if (t.getHealth() >= max * 0.5) return TriggerResult.keep();
				if (!counted.add(t.getUniqueId())) return TriggerResult.keep();

				Player p = data.getPlayer();
				sc.play(p, p);
				pc.play(p, p);
				data.applyStatus(StatusType.STRENGTH, data, strength, 200, this);
				return TriggerResult.keep();
			});
			return TriggerResult.remove();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		Player p = data.getPlayer();
		sc.play(p, p);
		pc.play(p, p);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				GlossaryTag.PASSIVE.tag(this) + " " + GlossaryTag.POWER.tag(this) + ". Activates after dealing damage to " +
				DescUtil.val(ACTIVATION_THRES) + " different enemies below " + DescUtil.val("50%") + " health. Afterwards, dealing damage to an enemy below " +
				DescUtil.val("50%") + " health grants you " + GlossaryTag.STRENGTH.tag(this, strength) + " " + DescUtil.duration(10) +
				". Each enemy can only grant strength once, and the enemies used to activate cannot grant strength.");
	}
}
