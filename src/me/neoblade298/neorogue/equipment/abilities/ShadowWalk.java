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
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class ShadowWalk extends Equipment {
	private static final String ID = "shadowWalk";
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL),
			hit = new ParticleContainer(Particle.DUST).count(50).spread(0.5, 0.5);
	private int damage = 80;
	
	public ShadowWalk(boolean isUpgraded) {
		super(ID, "Shadow Walk", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 25, isUpgraded ? 17 : 21, 0));
		properties.addUpgrades(PropertyType.COOLDOWN);
		pc.count(50).spread(0.5, 0.5).offsetY(1);
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
				"On cast, Grant speed <white>1</white> and " + GlossaryTag.STEALTH.tag(this) +
				" [<white>5s</white>]. "
				+ "Your next <white>3</white> basic attacks deal an additional " + GlossaryTag.PIERCING.tag(this, damage, false) + " damage. "
				+ "The cooldown of this ability is reduced by your " + GlossaryTag.STEALTH.tag(this)
				+ " stacks every second.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {
			Sounds.teleport.play(p, p);
			pc.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
			data.applyStatus(StatusType.STEALTH, data, 1, 100);
			inst.addCount(3);
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
			if (!data.hasStatus(StatusType.STEALTH)) return TriggerResult.keep();
			inst.reduceCooldown(data.getStatus(StatusType.STEALTH).getStacks());
			return TriggerResult.keep();
		});
	}
}
