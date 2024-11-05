package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LaunchProjectileGroupEvent;

public class ShatteringShot extends Equipment {
	private static final String ID = "shatteringShot";
	private int damage;
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK).blockData(Material.ICE.createBlockData()).spread(0.2, 0.2).count(5);
	
	public ShatteringShot(boolean isUpgraded) {
		super(ID, "Shattering Shot", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(30, 10, 8, 0));
		damage = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.equip.play(p, p);
			data.addTrigger(ID, Trigger.LAUNCH_PROJECTILE_GROUP, (pdata2, in) -> {
				LaunchProjectileGroupEvent ev = (LaunchProjectileGroupEvent) in;
				if (!ev.isBowProjectile()) return TriggerResult.keep();
				ProjectileInstance inst = (ProjectileInstance) ev.getInstances().getFirst();
				inst.getMeta().addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING));
				BowProjectile settings = (BowProjectile) inst.getParent();
				settings.pierce(2);
				settings.addProjectileTickAction((p2, proj, interpolation) -> {
					pc.play(p2, proj.getLocation());
					Sounds.ice.play(p2, proj.getLocation());
				});
				inst.addHitAction((hit, hitBarrier, meta, proj) -> {
					if (hit.hasStatus(StatusType.FROST)) {
						proj.addPierce(1);
						proj.addMaxRange(2);
					}
				});

				return TriggerResult.remove();
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POWDER_SNOW_BUCKET,
				"On cast, your next basic attack deals an additional " + GlossaryTag.ICE.tag(this, damage, true) + " and pierces " +
				DescUtil.white(1) + " enemy. Targets hit with this projectile that have " + GlossaryTag.FROST.tag(this) + " will always be pierced, increase the projectile's range by " +
				DescUtil.white(2) + ", and take an additional " + GlossaryTag.ICE.tag(this, 0.2, true) + " per stack of " + GlossaryTag.FROST.tag(this) + ".");
	}
}
