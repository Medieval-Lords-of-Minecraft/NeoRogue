package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.SessionEquipment;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class BrightJavelin extends Equipment {
	private static final String ID = "BrightJavelin";
	private static final int DAMAGE = 150, SANCT_MULT = 10, RANGE = 10;
	private static final ParticleContainer projectileCore =
			new ParticleContainer(Particle.END_ROD).count(1).spread(0, 0).speed(0);
	private static final ParticleContainer projectileTrail =
			new ParticleContainer(Particle.FIREWORK).count(2).spread(0.06, 0.06).speed(0.005);
	private static final ParticleContainer hitParticles =
			new ParticleContainer(Particle.END_ROD).count(10).spread(0.1, 0.1).speed(0.01);
	private static final SoundContainer launchSound =
			new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7F, 1.35F);
	private static final SoundContainer hitSound =
			new SoundContainer(Sound.BLOCK_GLASS_BREAK, 0.8F, 1.25F);

	public BrightJavelin(boolean isUpgraded) {
		super(ID, "Bright Javelin", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 10, 8, RANGE));
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		Equipment eq = this;
		ProjectileGroup projs = new ProjectileGroup(new BrightJavelinProjectile(slot, eq));
		data.addTrigger(id, bind, new EquipmentInstance(data, sessionEq, slot, es, (pdata, in) -> {
			projs.start(pdata);
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.TRIDENT,
				"Throw a bright javelin that deals " + GlossaryTag.LIGHT.tag(this, DAMAGE) + " damage, increased by "
				+ DescUtil.val(SANCT_MULT) + " for each " + GlossaryTag.SANCTIFIED.tag(this) + " stack on the enemy.");
	}

	private class BrightJavelinProjectile extends Projectile {
		private final int slot;
		private final Equipment eq;

		public BrightJavelinProjectile(int slot, Equipment eq) {
			super(1, RANGE, 1);
			this.size(0.5, 0.5).gravity(0.02).initialY(1);
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player player = (Player) proj.getOwner().getEntity();
			projectileCore.play(player, proj.getLocation());
			projectileTrail.play(player, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			int bonusDamage = hit.getStatus(StatusType.SANCTIFIED).getStacks() * SANCT_MULT;
			meta.addDamageSlice(new DamageSlice(proj.getOwner(), DAMAGE + bonusDamage, DamageType.LIGHT,
					DamageStatTracker.of(id + slot, eq)));

			Player player = (Player) proj.getOwner().getEntity();
			hitParticles.play(player, hit.getEntity().getLocation().add(0, 1, 0));
			hitSound.play(player, hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Player player = (Player) proj.getOwner().getEntity();
			Sounds.threw.play(player, player);
			launchSound.play(player, player);
		}
	}
}