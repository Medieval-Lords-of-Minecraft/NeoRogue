package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class ShadowWalk extends Equipment {
	private static final String ID = "shadowWalk";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL),
			hit = new ParticleContainer(Particle.REDSTONE).count(50).spread(0.5, 0.5);
	private int shields, damage = 50, cdr;
	
	public ShadowWalk(boolean isUpgraded) {
		super(ID, "Shadow Walk", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(5, 10, 10, 0));
		pc.count(50).spread(0.5, 0.5).offsetY(1);
		shields = isUpgraded ? 3 : 2;
		cdr = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addSelfReforge(NightShade.get(), Sidestep.get(), Contaminate.get());
	}

	@Override
	public void setupItem() {
		item = createItem(Material.RABBIT_FOOT,
				"On cast, Grant speed <white>1</white>, " + GlossaryTag.INVISIBLE.tag(this) + ", and " + GlossaryTag.SHIELDS.tag(this, shields, true) +
				" for <white>3</white> seconds. "
				+ "Your next basic attack deals an additional " + GlossaryTag.PIERCING.tag(this, damage, false) + " damage. Not being within "
						+ "<white>5m</white> of an enemy decreases the cooldown"
						+ " of this ability by <yellow>" + cdr + "</yellow> second(s).");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(p, this, slot, es);
		inst.setAction((pdata, in) -> {
			Sounds.teleport.play(p, p);
			pc.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			data.applyStatus(StatusType.INVISIBLE, data, 1, 60);
			data.addSimpleShield(p.getUniqueId(), shields, 60);
			inst.addCount(1);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
		data.addTrigger(ID, Trigger.BASIC_ATTACK, (pdata, in) -> {
			if (inst.getCount() > 0) {
				inst.addCount(-1);
				BasicAttackEvent ev = (BasicAttackEvent) in;
				hit.play(p, p);
				Sounds.anvil.play(p, p);
				ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING));
			}
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, Trigger.PLAYER_TICK, (pdata, in) -> {
			if (!TargetHelper.getEntitiesInRadius(p, tp).isEmpty()) return TriggerResult.keep();
			inst.reduceCooldown(cdr);
			return TriggerResult.keep();
		});
	}
}
