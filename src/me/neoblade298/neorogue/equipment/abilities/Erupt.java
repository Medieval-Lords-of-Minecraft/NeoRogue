package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
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
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Erupt extends Equipment {
	private static final String ID = "Erupt";
	private static TargetProperties tp = TargetProperties.line(8, 2, TargetType.ENEMY),
		aoe = TargetProperties.radius(3, false);
	private static ParticleContainer fire = new ParticleContainer(Particle.FLAME).count(50).spread(0.5, 2).offsetY(1).speed(0.1),
			cloud = new ParticleContainer(Particle.CLOUD).count(3).spread(0.1, 0.1).offsetY(0.2),
			expl = new ParticleContainer(Particle.EXPLOSION).count(15).spread(aoe.range / 2, 0.5);
	private static final Circle circ = new Circle(aoe.range);
	private int damage;
	
	public Erupt(boolean isUpgraded) {
		super(ID, "Erupt", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 14, tp.range, aoe.range));
				damage = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EruptInstance(data, this, slot, es));
	}

	private class EruptInstance extends EquipmentInstance	{
		private LivingEntity trg;
		public EruptInstance(PlayerFightData data, Equipment eq, int slot, EquipSlot es) {
			super(data, eq, slot, es);
			Player p = data.getPlayer();
			action = (pdata, in) -> {
				if (trg == null) return TriggerResult.keep();
				FightData fd = FightInstance.getFightData(trg);
				if (!fd.hasStatus(StatusType.BURN)) {
					FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, eq)), trg);
					Sounds.fire.play(p, trg);
					fire.play(p, trg);
				}
				else {
					Sounds.explode.play(p, trg);
					circ.play(cloud, trg.getLocation(), LocalAxes.xz(), null);
					expl.play(p, trg.getLocation());
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, trg.getLocation(), aoe)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE,
								DamageStatTracker.of(id + slot, eq)), ent);
					}
				}
				return TriggerResult.keep();
			};
		}

		@Override
		public boolean canTrigger(Player p, PlayerFightData data, Object in) {
			if (!super.canTrigger(p, data, in)) return false;
			trg = TargetHelper.getNearestInSight(p, tp);
			return trg != null;
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TORCH,
				"On cast, deal " + GlossaryTag.FIRE.tag(this, damage, true) + " damage to the target you're aiming at. If they have " + GlossaryTag.BURN.tag(this) + ", " +
				"additionally deal damage to all enemies near the target.");
	}
}
