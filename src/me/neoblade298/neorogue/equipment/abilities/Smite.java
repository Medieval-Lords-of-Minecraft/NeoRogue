package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Smite extends Equipment {
	private static final String ID = "Smite";
	private int sanctified, damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK).offsetForward(2).count(30).spread(4, 0.2);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE);
	private static final TargetProperties tp = TargetProperties.cone(90, 5, false, TargetType.ENEMY);
	
	public Smite(boolean isUpgraded) {
		super(ID, "Smite", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 20, 8, 7));
		
		sanctified = isUpgraded ? 45 : 30;
		damage = isUpgraded ? 130 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Sounds.attackSweep.play(p, p);
			sc.play(p, p);
			part.play(p, p);
			for (LivingEntity ent : TargetHelper.getEntitiesInCone(p, tp)) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.SLASHING, DamageStatTracker.of(id + slot, this)), ent);
				FightInstance.applyStatus(ent, StatusType.SANCTIFIED, p, sanctified, -1);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"On cast, deal <yellow>" + damage + "</yellow> " + GlossaryTag.SLASHING.tag(this) + " damage in a cone in front of you and "
						+ "apply <yellow>" + sanctified + "</yellow> " + GlossaryTag.SANCTIFIED.tag(this) + ".");
	}
}
