package me.neoblade298.neorogue.equipment.abilities;

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
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Cleave extends Equipment {
	private static final String ID = "Cleave";
	private int damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK).offsetForward(2).count(10).spread(2.5, 0.2);
	private static final TargetProperties tp = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	
	public Cleave(boolean isUpgraded) {
		super(ID, "Cleave", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 5, tp.range));
		
		damage = isUpgraded ? 100 : 70;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void setupReforges() {
		addReforge(BasicInfusionMastery.get(), Quake.get(), Smite.get(), WindSlash.get());
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Player p = data.getPlayer();
			Sounds.attackSweep.play(p, p);
			part.play(p, p);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.SLASHING, DamageStatTracker.of(id + slot, this)), ent);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLADE_POTTERY_SHERD,
				"On cast, deal <yellow>" + damage + "</yellow> " + GlossaryTag.SLASHING.tag(this) + " damage in a cone in front of you.");
	}
}
