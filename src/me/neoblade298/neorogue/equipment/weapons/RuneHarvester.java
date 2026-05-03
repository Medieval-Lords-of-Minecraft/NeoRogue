package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.CastUsableEvent;

public class RuneHarvester extends Equipment {
	private static final String ID = "RuneHarvester";
	private static final int RANGE = 14;
	private static final ParticleContainer tick = new ParticleContainer(Particle.ENCHANT);
	private static final SoundContainer cast = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_BREAK),
			hit = new SoundContainer(Sound.BLOCK_ENCHANTMENT_TABLE_USE);

	private final int cooldownReduction;

	static {
		tick.count(5).spread(0.2, 0.2).speed(0);
	}

	public RuneHarvester(boolean isUpgraded) {
		super(
				ID, "Rune Harvester", isUpgraded, Rarity.RARE, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWand(isUpgraded ? 55 : 40, 0.9, 0, 1, RANGE, DamageType.DARK, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
		);
		properties.addUpgrades(PropertyType.DAMAGE);
		cooldownReduction = isUpgraded ? 6 : 4;
	}

	@Override
	public void setupReforges() {
		addSelfReforge(Maelstrom.get());
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta empowerment = new ActionMeta();

		data.addTrigger(id, Trigger.CAST_USABLE, (pdata, in) -> {
			CastUsableEvent ev = (CastUsableEvent) in;
			if (ev.getInstance().getEquipment().getType() != EquipmentType.ABILITY) {
				return TriggerResult.keep();
			}

			empowerment.setBool(true);
			empowerment.setObject(ev.getInstance());
			return TriggerResult.keep();
		});

		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, in) -> {
			if (!empowerment.getBool()) {
				return TriggerResult.keep();
			}
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR)) {
				return TriggerResult.keep();
			}

			EquipmentInstance source = (EquipmentInstance) empowerment.getObject();
			if (source == null) {
				empowerment.setBool(false);
				return TriggerResult.keep();
			}

			empowerment.setBool(false);
			empowerment.setObject(null);
			weaponSwing(data.getPlayer(), data);

			ProjectileGroup proj = new ProjectileGroup(new RuneHarvesterProjectile(data, this, slot, source));
			data.chargeSecs(properties.get(PropertyType.CHARGE_TIME)).then(() -> proj.start(data));
			return TriggerResult.keep();
		});
	}

	private class RuneHarvesterProjectile extends Projectile {
		private final PlayerFightData data;
		private final RuneHarvester eq;
		private final int slot;
		private final EquipmentInstance source;

		public RuneHarvesterProjectile(PlayerFightData data, RuneHarvester eq, int slot, EquipmentInstance source) {
			super(1.8, RANGE, 1);
			this.size(0.45, 0.45);
			this.data = data;
			this.eq = eq;
			this.slot = slot;
			this.source = source;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(data.getPlayer(), proj.getLocation());
		}

		@Override
		public void onHit(FightData hitTarget, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			source.reduceCooldown(cooldownReduction);
			hit.play(data.getPlayer(), hitTarget.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			cast.play(data.getPlayer(), proj.getLocation());
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.AMETHYST_SHARD,
				"Can only be used while empowered. Casting an ability empowers this weapon. Firing it removes the empowerment. When fired, "
						+ GlossaryTag.CHARGE.tag(this) + " " + DescUtil.white("1s") + " before launching a projectile that deals "
						+ GlossaryTag.DARK.tag(this, (int) properties.get(PropertyType.DAMAGE), true) + " damage. If the projectile hits an enemy, reduce the cooldown of the ability that empowered it by "
						+ DescUtil.yellow(cooldownReduction + "s") + ".");
	}
}