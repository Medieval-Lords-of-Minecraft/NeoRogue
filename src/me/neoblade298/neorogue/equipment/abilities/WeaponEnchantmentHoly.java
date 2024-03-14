package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WeaponEnchantmentHoly extends Equipment {
	private static final String ID = "weaponEnchantmentHoly";
	private ProjectileGroup projs = new ProjectileGroup(new WeaponEnchantmentHolyProjectile());
	private int damage, sanct;
	private static final int RANGE = 8;
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK),
			scHit = new SoundContainer(Sound.BLOCK_GLASS_BREAK);
	private static final ParticleContainer tick = new ParticleContainer(Particle.FIREWORKS_SPARK).count(5).speed(0.02);
	
	public WeaponEnchantmentHoly(boolean isUpgraded) {
		super(ID, "Weapon Enchantment: Holy", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 3, RANGE));
		damage = isUpgraded ? 60 : 40;
		sanct = isUpgraded ? 6 : 4;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.BASIC_ATTACK, new WeaponEnchantmentHolyInstance(id));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.NETHER_STAR,
				"Passive. Your basic attacks fire a projectile after <white>0.5</white> seconds that deals " + GlossaryTag.LIGHT.tag(this, damage, true)
				+ " damage and applies " + GlossaryTag.SANCTIFIED.tag(this, sanct, true) + ".");
	}
	
	private class WeaponEnchantmentHolyInstance extends PriorityAction {
		private long nextCastTime = 0L;
		public WeaponEnchantmentHolyInstance(String id) {
			super(id);
			action = (pdata, in) -> {
				if (System.currentTimeMillis() > nextCastTime) {
					nextCastTime = System.currentTimeMillis() + 3000L;
					pdata.addTask(new BukkitRunnable() {
						public void run() {
							projs.start(pdata);
						}
					}.runTaskLater(NeoRogue.inst(), 10L));
				}
				return TriggerResult.keep();
			};
		}
	}
	
	private class WeaponEnchantmentHolyProjectile extends Projectile {
		public WeaponEnchantmentHolyProjectile() {
			super(0.5, RANGE, 2);
		}

		@Override
		public void onTick(ProjectileInstance proj, boolean interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, ProjectileInstance proj) {
			damageProjectile(hit.getEntity(), proj, new DamageMeta(proj.getOwner(), damage, DamageType.LIGHT), hitBarrier);
			scHit.play((Player) proj.getOwner().getEntity(), hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			sc.play((Player) proj.getOwner().getEntity(), proj.getOwner().getEntity());
		}
	}
}
