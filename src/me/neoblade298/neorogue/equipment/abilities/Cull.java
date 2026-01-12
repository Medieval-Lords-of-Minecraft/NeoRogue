package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Cull extends Equipment {
	private static final String ID = "Cull";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public Cull(boolean isUpgraded) {
		super(ID, "Cull", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 50, 15, 0));
		damage = isUpgraded ? 300 : 200;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			am.addCount(1);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
		
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
			if (am.getCount() <= 0) return TriggerResult.keep();
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(
					new DamageSlice(data, damage, DamageType.SLASHING, DamageStatTracker.of(id + slot, this)));
			ev.getMeta().addTag(id);
			hit.play(p, ev.getTarget());
			Sounds.anvil.play(p, ev.getTarget());
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
			BasicAttackEvent ev = (BasicAttackEvent) in;
			// Remove buff if we didn't kill
			if (ev.getMeta().getTags().contains(id) && !ev.getTarget().isDead()) {
				am.addCount(-1);
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your basic attacks deals an additional " + GlossaryTag.SLASHING.tag(this, damage, true) + " damage until you don't kill an enemy with it.");
	}
}
