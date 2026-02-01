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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Contaminate extends Equipment {
	private static final String ID = "Contaminate";
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL),
			hit = new ParticleContainer(Particle.DUST).count(50).spread(0.5, 0.5);
	private int damage = 150;
	private double mult;
	
	public Contaminate(boolean isUpgraded) {
		super(ID, "Contaminate", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 30, 18, 0));
		pc.count(50).spread(0.5, 0.5).offsetY(1);
		mult = isUpgraded ? 1.6 : 1.4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POISONOUS_POTATO,
				"On cast, Grant speed <white>1</white> and " + GlossaryTag.STEALTH.tag(this) +
				" [<white>5s</white>]. "
				+ "Your next <white>3</white> basic attacks deal an additional " + GlossaryTag.PIERCING.tag(this, damage, false) + " damage and multiply existing stacks of "
				+ GlossaryTag.POISON.tag(this) + " on the enemy hit by <yellow>" + mult + "</yellow>, rounded down.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		inst.setAction((pdata, in) -> {		Player p = data.getPlayer();			Sounds.teleport.play(p, p);
			pc.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			data.applyStatus(StatusType.STEALTH, data, 1, 60);
			inst.addCount(3);
			return TriggerResult.keep();
		});
		
		data.addTrigger(ID, bind, inst);
		data.addTrigger(ID, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {		Player p = data.getPlayer();			if (inst.getCount() > 0) {
				inst.addCount(-1);
				PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
				hit.play(p, p);
				Sounds.anvil.play(p, p);
				ev.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(ID + slot, this)));
				FightData fd = FightInstance.getFightData(ev.getTarget());
				int toAdd = (int) (fd.getStatus(StatusType.POISON).getStacks() * (mult - 1));
				if (toAdd <= 0) return TriggerResult.keep();
				fd.applyStatus(StatusType.POISON, data, toAdd, -1);
			}
			return TriggerResult.keep();
		});
	}
}
