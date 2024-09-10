package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.BasicAttackEvent;

public class Finale extends Equipment {
	private static final String ID = "finale";
	private int damage, thres;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			hit = new ParticleContainer(Particle.REDSTONE);
	
	public Finale(boolean isUpgraded) {
		super(ID, "Finale", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, isUpgraded ? 30 : 40, 12, 0));
		damage = 360;
		thres = isUpgraded ? 30 : 40;
		pc.count(50).spread(0.5, 0.5).speed(0.2);
		hit.count(50).spread(0.5, 0.5);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(p, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			pc.play(p, p);
			int stacks = (int) Math.min(3, data.getStamina() / thres);
			data.addTrigger(id, Trigger.BASIC_ATTACK, (pdata2, in) -> {
				BasicAttackEvent ev = (BasicAttackEvent) in;
				FightInstance.dealDamage(data, DamageType.PIERCING, damage * stacks, ev.getTarget());
				hit.play(p, ev.getTarget());
				Sounds.anvil.play(p, ev.getTarget());
				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, for every " + DescUtil.yellow(thres) + " stamina you have up to " + DescUtil.white(thres * 3) + 
				", deal an additional " + GlossaryTag.PIERCING.tag(this, damage, false) + " damage on your next basic attack.");
	}
}
