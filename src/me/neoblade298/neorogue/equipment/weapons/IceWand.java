package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class IceWand extends Equipment {
	private static final String ID = "iceWand";
	private static final TargetProperties props = TargetProperties.radius(0.75, true, TargetType.ENEMY);
	private static final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 60, 1, false, false, false);
	
	private static final ParticleContainer tick;
	private static final SoundContainer sc = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);

	private int shieldAmount;

	static {
		tick = new ParticleContainer(Particle.GLOW);
		tick.count(5).spread(0.1, 0.1).speed(0.01);
	}

	public IceWand(boolean isUpgraded) {
		super(
				ID, "Ice Wand", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(6, 0, isUpgraded ? 30 : 20, 0.4, DamageType.ICE, Sound.ITEM_AXE_SCRAPE)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		shieldAmount = isUpgraded ? 3 : 2;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}
	
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new IceWandProjectile(data));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}

	private class IceWandProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		
		public IceWandProjectile(PlayerFightData data) {
			super(0.4, 8, 3);
			this.size(1, 1);
			this.data = data;
			this.p = data.getPlayer();
		}
		
		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}
		
		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(hit.getEntity(), props)) {
				ent.addPotionEffect(slow);
				proj.getOwner().addSimpleShield(p.getUniqueId(), shieldAmount, 40);
			}

			Location loc = hit.getEntity().getLocation();
			sc.play(p, loc);
		}
		
		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyProperties(data, properties);
		}
	}
	
	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK,
				"Shoots an ice missile shattering on hit. All enemies hit are slightly slowed, and for each you gain <yellow>" + shieldAmount + "</yellow> "
						+ GlossaryTag.SHIELDS.tag(this) + " for <white>2</white> seconds."
		);
	}
}
