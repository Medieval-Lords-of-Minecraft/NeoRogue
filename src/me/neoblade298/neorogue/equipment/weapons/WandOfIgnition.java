package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class WandOfIgnition extends Equipment {
	private static final String ID = "wandOfIgnition";
	private static final ParticleContainer tick = new ParticleContainer(Particle.FLAME);
	private int burn, selfburn;

	public WandOfIgnition(boolean isUpgraded) {
		super(
				ID , "Wand of Ignition", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(2, 0, 40, 1, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		burn = isUpgraded ? 30 : 20;
		selfburn = 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ProjectileGroup proj = new ProjectileGroup(new WandOfIgnitionProjectile(data, this));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class WandOfIgnitionProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private WandOfIgnition eq;
		private static final SoundContainer start = Sounds.fire, hit = new SoundContainer(Sound.BLOCK_FIRE_EXTINGUISH);

		public WandOfIgnitionProjectile(PlayerFightData data, WandOfIgnition eq) {
			super(1.5, 10, 2);
			this.size(0.2, 0.2);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			WandOfIgnitionProjectile.hit.play(p, loc);
			FightInstance.applyStatus(hit.getEntity(), StatusType.BURN, data, burn, -1);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			start.play(p, proj.getLocation());
			proj.applyWeapon(data, eq);
			FightInstance.applyStatus(p, StatusType.BURN, data, selfburn, -1);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_ROD, "Applies " + GlossaryTag.BURN.tag(this, burn, true) + " on hit. Applies "
		+ GlossaryTag.BURN.tag(this, selfburn, false) + " to you when used.");
	}
}
