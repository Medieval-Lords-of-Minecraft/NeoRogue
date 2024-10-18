package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.PotionProjectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Firebomb extends Equipment {
	private static final String ID = "firebomb";
	private static final TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).spread(1.5, 0.1).count(100);
	private int burn, damage;
	
	public Firebomb(boolean isUpgraded) {
		super(ID, "Firebomb", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0));
		burn = isUpgraded ? 15 : 10;
		damage = 120;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		PotionProjectile pot = new PotionProjectile((loc, hit) -> {
			for (LivingEntity ent : hit) {
				if (ent instanceof Player || !(ent instanceof LivingEntity)) continue;
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
			}

			data.addTask(new BukkitRunnable() {
				int tick = 0;
				public void run() {
					pc.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, tp)) {
						FightInstance.applyStatus(ent, StatusType.BURN, data, burn, -1);
					}
					if (++tick >= 3) cancel();
				}
			}.runTaskTimer(NeoRogue.inst(), 20L, 20L));
		});
		ProjectileGroup grp = new ProjectileGroup(pot);
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, inputs) -> {
			Sounds.threw.play(p, p);
			grp.start(data);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION,
				"On cast, throw a bomb that deals " + GlossaryTag.FIRE.tag(this, damage, false) + " damage, coating the area in flames and applying " +
				GlossaryTag.BURN.tag(this, burn, true) + " to enemies who walk through it over the next <white>3s</white>.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.RED);
		item.setItemMeta(pm);
	}
}
