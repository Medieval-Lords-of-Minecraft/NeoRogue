package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class ConsumingFlame extends Equipment {
	private static final String ID = "ConsumingFlame";
	private static final int RANGE = 14;
	private static final ParticleContainer tick;
	private static final SoundContainer start = new SoundContainer(Sound.ITEM_AXE_SCRAPE),
			sc = new SoundContainer(Sound.BLOCK_CHAIN_PLACE);

	private int manaGain, intellectFreq;

	static {
		tick = new ParticleContainer(Particle.FLAME);
		tick.count(4).spread(0.15, 0.15).speed(0);
	}

	public ConsumingFlame(boolean isUpgraded) {
		super(
				ID, "Consuming Flame", isUpgraded, Rarity.EPIC, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(isUpgraded ? 50 : 40, 0.9, 0, 1, RANGE, DamageType.FIRE, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
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
		ProjectileGroup proj = new ProjectileGroup(new ConsumingFlameProjectile(data, this, slot, attackCounter));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			Player p = data.getPlayer();
			weaponSwing(p, data);
			data.chargeSecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> proj.start(data));
			return TriggerResult.keep();
		});
	}

	private class ConsumingFlameProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private ConsumingFlame eq;
		private int slot;
		private ActionMeta attackCounter;

		public ConsumingFlameProjectile(PlayerFightData data, ConsumingFlame eq, int slot, ActionMeta attackCounter) {
			super(2.0, RANGE, 1);
			this.size(0.6, 0.6);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.slot = slot;
			this.attackCounter = attackCounter;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			// Grant mana to caster
			data.addMana(manaGain);

			// Increment attack counter
			int count = attackCounter.getInt();
			count++;
			attackCounter.setInt(count);

			// Grant intellect equal to current corruption stacks every Nth attack
			if (count % intellectFreq == 0) {
				int corruptionStacks = 0;
				if (data.hasStatus(StatusType.CORRUPTION)) {
					corruptionStacks = data.getStatus(StatusType.CORRUPTION).getStacks();
				}
				if (corruptionStacks > 0) {
					data.applyStatus(StatusType.INTELLECT, data, corruptionStacks, -1);
				}
			}

			sc.play(p, hit.getEntity());
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
				Material.BLAZE_ROD,
				"Grants " + GlossaryTag.INTELLECT.tag(this) + " equal to your current "
						+ GlossaryTag.CORRUPTION.tag(this) + " every " + DescUtil.yellow(intellectFreq)
						+ " basic attacks. Each hit grants " + DescUtil.yellow(manaGain) + " mana."
		);
	}
}
