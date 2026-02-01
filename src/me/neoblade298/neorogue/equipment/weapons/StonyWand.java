package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class StonyWand extends Equipment {
	private static final String ID = "StonyWand";
	private static final ParticleContainer tick = new ParticleContainer(Particle.BLOCK).blockData(Material.STONE.createBlockData());
	private static final SoundContainer start = new SoundContainer(Sound.BLOCK_STONE_BREAK),
			hit = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);
	private int conc;

	public StonyWand(boolean isUpgraded) {
		super(
				ID , "Stony Wand", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(2, 0, isUpgraded ? 45 : 35, 1, DamageType.EARTHEN, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		conc = isUpgraded ? 20 : 15;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new StonyWandProjectile(data, this, slot));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(d.getPlayer(), data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class StonyWandProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private StonyWand eq;
		private int slot;

		public StonyWandProjectile(PlayerFightData data, StonyWand eq, int slot) {
			super(1.5, 10, 2);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			StonyWand.hit.play(p, loc);
			FightInstance.applyStatus(hit.getEntity(), StatusType.CONCUSSED, data, conc, -1);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			start.play(p, proj.getLocation());
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_HOE, "Applies " + GlossaryTag.CONCUSSED.tag(this, conc, true) + " on hit.");
	}
}
