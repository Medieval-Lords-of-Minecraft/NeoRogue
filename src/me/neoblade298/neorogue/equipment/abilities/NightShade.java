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
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class NightShade extends Equipment {
	private static final String ID = "NightShade";
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL),
			hit = new ParticleContainer(Particle.DUST).count(50).spread(0.5, 0.5);
	private int damage, insanity;
	
	public NightShade(boolean isUpgraded) {
		super(ID, "Night Shade", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 35, 12, 0));
		pc.count(50).spread(0.5, 0.5).offsetY(1);
		damage = isUpgraded ? 200 : 150;
		insanity = isUpgraded ? 150 : 90;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OBSIDIAN,
				"On cast, Grant speed <white>1</white> and " + GlossaryTag.STEALTH.tag(this) +
				" [<white>5s</white>]. "
				+ "Your next <white>3</white> basic attacks deals an additional " + GlossaryTag.DARK.tag(this, damage, true) + " damage and applies " +
				GlossaryTag.INSANITY.tag(this, insanity, true) + ". "
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
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (inst.getCount() > 0) {
				inst.addCount(-1);
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				hit.play(p, p);
				Sounds.anvil.play(p, p);
				ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.DARK, DamageStatTracker.of(ID + slot, this)));
				FightInstance.applyStatus(ev.getTarget(), StatusType.INSANITY, p, insanity, -1);
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
