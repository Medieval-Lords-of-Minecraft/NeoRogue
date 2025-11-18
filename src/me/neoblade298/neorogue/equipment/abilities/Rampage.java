package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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
import me.neoblade298.neorogue.session.fight.trigger.event.PreBasicAttackEvent;

public class Rampage extends Equipment {
	private static final String ID = "Rampage";
	private int damage, inc;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.DUST);
	
	public Rampage(boolean isUpgraded) {
		super(ID, "Rampage", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 35, 6, 0));
		damage = isUpgraded ? 300 : 200;
		inc = isUpgraded ? 75 : 50;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		ActionMeta count = new ActionMeta();
		ItemStack icon = item.clone();
		EquipmentInstance inst = new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			am.addCount(1);
			return TriggerResult.keep();
		});
		data.addTrigger(id, bind, inst);
		
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata2, in) -> {
			if (am.getCount() <= 0) return TriggerResult.keep();
			am.addCount(-1);
			PreBasicAttackEvent ev = (PreBasicAttackEvent) in;
			ev.getMeta().addDamageSlice(
					new DamageSlice(data, damage + (count.getCount() * inc), DamageType.SLASHING, DamageStatTracker.of(id + slot, this)));
			count.addCount(1);
			icon.setAmount(count.getCount());
			inst.setIcon(icon);
			hit.play(p, ev.getTarget());
			Sounds.anvil.play(p, ev.getTarget());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, your next basic attack deals an additional " + GlossaryTag.SLASHING.tag(this, damage, true) + " damage. This amount increases by " +
				DescUtil.yellow(inc) + " for every time you land this attack.");
	}
}
