package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Bow;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta.DamageOrigin;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class MechanicalBow extends Bow {
	private static final String ID = "mechanicalBow";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK);
	private int damage;
	
	public MechanicalBow(boolean isUpgraded) {
		super(ID, "Mechanical Bow", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.WEAPON,
				EquipmentProperties.ofBow(60, 1, 0, 12, 0, 1.4));
				damage = isUpgraded ? 60 : 40;
	}

	@Override
	public void onTick(Player p, ProjectileInstance proj, int interpolation) {
		BowProjectile.tick.play(p, proj.getLocation());
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		data.addSlotBasedTrigger(id, slot, Trigger.VANILLA_PROJECTILE, (pdata, in) -> {
			if (!canShoot(data)) return TriggerResult.keep();
			useBow(data);

			ProjectileLaunchEvent ev = (ProjectileLaunchEvent) in;
			boolean hasBonus = System.currentTimeMillis() - am.getTime() < 5000;
			BowProjectile bproj = new BowProjectile(data, ev.getEntity().getVelocity(), this, id + slot);
			bproj.setDamageBonus(hasBonus ? damage : 0);
			if (hasBonus) {
				bproj.addProjectileTickAction((p2, inst, interpolation) -> {
					pc.play(p, inst.getLocation());
				});
			}
			ProjectileGroup proj = new ProjectileGroup(bproj);
			proj.start(data);
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if (ev.getMeta().hasOrigin(DamageOrigin.TRAP)) {
				am.setTime(System.currentTimeMillis());
			}
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOW, "If you've dealt " + GlossaryTag.TRAP.tag(this) + " damage in the last <white>5s</white>, increase this bow's damage by " +
				DescUtil.yellow(damage) + ".");
	}
}
