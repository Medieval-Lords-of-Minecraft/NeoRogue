package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
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
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Riftmaker extends Equipment {
	private static final String ID = "Riftmaker";
	private static final int RANGE = 14;
	private static final int RIFT_DURATION = 200;
	private static final ParticleContainer tick;
	private static final SoundContainer start = new SoundContainer(Sound.ITEM_AXE_SCRAPE),
			sc = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);

	private int manaGain, intellectFreq;

	static {
		tick = new ParticleContainer(Particle.ENCHANTED_HIT);
		tick.count(4).spread(0.15, 0.15).speed(0);
	}

	public Riftmaker(boolean isUpgraded) {
		super(
				ID, "Riftmaker", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(isUpgraded ? 45 : 35, 0.9, 0, 1, RANGE, DamageType.DARK, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		manaGain = isUpgraded ? 6 : 4;
		intellectFreq = isUpgraded ? 3 : 5;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta attackCounter = new ActionMeta();
		ProjectileGroup proj = new ProjectileGroup(new RiftmakerProjectile(data, this, slot, attackCounter));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			Player p = data.getPlayer();
			weaponSwing(p, data);
			data.chargeSecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> {
				Player current = data.getPlayer();
				proj.start(data);

				Rift nearest = null;
				double nearestDistSq = Double.MAX_VALUE;
				Location pLoc = current.getLocation();
				for (Rift rift : data.getRifts().values()) {
					if (rift.getLocation().getWorld() != pLoc.getWorld())
						continue;
					double distSq = rift.getLocation().distanceSquared(pLoc);
					if (distSq < nearestDistSq) {
						nearestDistSq = distSq;
						nearest = rift;
					}
				}

				if (nearest != null) {
					Location target = current.getEyeLocation().add(current.getLocation().getDirection().normalize().multiply(7));
					Vector direction = target.toVector().subtract(nearest.getLocation().toVector());
					if (direction.lengthSquared() > 0) {
						proj.start(data, nearest.getLocation(), direction);
					}
				}
			});
			return TriggerResult.keep();
		});
	}

	private class RiftmakerProjectile extends Projectile {
		private final PlayerFightData data;
		private final Riftmaker eq;
		private final int slot;
		private final ActionMeta attackCounter;

		public RiftmakerProjectile(PlayerFightData data, Riftmaker eq, int slot, ActionMeta attackCounter) {
			super(2.0, RANGE, 1);
			this.size(0.6, 0.6);
			this.data = data;
			this.eq = eq;
			this.slot = slot;
			this.attackCounter = attackCounter;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			data.addMana(manaGain);

			int count = attackCounter.getInt() + 1;
			attackCounter.setInt(count);

			if (count % intellectFreq == 0) {
				data.applyStatus(StatusType.INTELLECT, data, 1, -1);
				data.addRift(new Rift(data, data.getPlayer().getLocation(), RIFT_DURATION));
			}

			sc.play(data.getPlayer(), hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			start.play(data.getPlayer(), proj.getLocation());
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.STICK,
				"Grants " + GlossaryTag.INTELLECT.tag(this, 1, false) + " every " + DescUtil.yellow(intellectFreq)
						+ " basic attacks. Each hit grants " + DescUtil.yellow(manaGain) + " mana. Basic attacks also fire from your nearest "
						+ GlossaryTag.RIFT.tag(this) + " toward <white>7</white> blocks in front of you. When this weapon grants "
						+ GlossaryTag.INTELLECT.tag(this) + ", create a " + GlossaryTag.RIFT.tag(this) + " [<white>10s</white>]."
		);
	}
}