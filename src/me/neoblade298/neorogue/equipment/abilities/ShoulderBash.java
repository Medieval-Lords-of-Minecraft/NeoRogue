package me.neoblade298.neorogue.equipment.abilities;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.buff.BuffType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;

public class ShoulderBash extends Equipment {
	private static final String ID = "shoulderBash";
	private int damage, inc;
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR);
	private static final ParticleContainer pc = new ParticleContainer(Particle.DUST_PLUME).count(20).spread(0.5, 0.5);
	
	public ShoulderBash(boolean isUpgraded) {
		super(ID, "Shoulder Bash", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 5, 5, 0));
		damage = 20;
		inc = isUpgraded ? 20 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.LEFT_CLICK_HIT, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) in;
			sc.play(p, p);
			pc.play(p, p);
			FightInstance.knockback(p, ev.getTarget(), 0.5);
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.BLUNT), ev.getTarget());
			FightInstance.getFightData(ev.getTarget()).addBuff(data, UUID.randomUUID().toString(), false, false, BuffType.GENERAL, -inc, 100);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POPPED_CHORUS_FRUIT,
				"Passive. Left clicking an enemy deals " + GlossaryTag.BLUNT.tag(this, damage, true) + " damage and knocks them back. They take an additional " +
					DescUtil.yellow(inc) + " damage for <white>5s</white>.");
	}
}
