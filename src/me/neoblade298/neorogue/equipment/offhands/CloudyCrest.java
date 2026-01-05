package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class CloudyCrest extends Equipment {
	private static final String ID = "CloudyCrest";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.LARGE_SMOKE)
			.count(50)
			.spread(5, 1)
			.speed(0.1);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	
	public CloudyCrest(boolean isUpgraded) {
		super(ID, "Cloudy Crest", isUpgraded, Rarity.RARE, EquipmentClass.THIEF, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(0, 0, 0, 0, tp.range));
		damage = isUpgraded ? 120 : 80;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.DASH, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			pc.play(p, p.getLocation());
			Sounds.attackSweep.play(p, p);
			
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightInstance.dealDamage(pdata, DamageType.DARK, damage, ent, DamageStatTracker.of(id + slot, this));
			}
			
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.DEEPSLATE_BRICK_SLAB,
				"Passive. Every time you " + GlossaryTag.DASH.tag(this) + ", deal " + 
				GlossaryTag.DARK.tag(this, damage, true) + " damage to all enemies in range.");
	}
}
