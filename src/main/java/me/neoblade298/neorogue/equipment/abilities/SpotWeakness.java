package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class SpotWeakness extends Equipment {
	private static final String ID = "SpotWeakness";
	private static final ParticleContainer pc = new ParticleContainer(Particle.CRIT).count(20).spread(0.5, 0.5);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_CRIT);
	private int strength;

	public SpotWeakness(boolean isUpgraded) {
		super(ID, "Spot Weakness", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 15, 10, 0));
		strength = isUpgraded ? 8 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, new EquipmentInstance(data, sessionEq, slot, es,
				(pdata, in) -> {
					Player p = data.getPlayer();
					sc.play(p, p);
					pc.play(p, p);
					data.applyStatus(StatusType.STRENGTH, data, strength, 200);
					return TriggerResult.keep();
				},
				(p, pdata, in) -> {
					LeftClickHitEvent ev = (LeftClickHitEvent) in;
					LivingEntity target = ev.getTarget();
					double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getValue();
					return target.getHealth() < maxHealth * 0.5;
				}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.SPYGLASS,
				"Hitting an enemy below " + DescUtil.white("50%") + " health grants you " +
				GlossaryTag.STRENGTH.tag(this, strength, true) + " " + DescUtil.duration(10, false) + ".");
	}
}
