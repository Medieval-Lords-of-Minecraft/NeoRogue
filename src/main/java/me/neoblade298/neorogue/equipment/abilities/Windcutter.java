package me.neoblade298.neorogue.equipment.abilities;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Windcutter extends Equipment implements Power {
	private static final String ID = "Windcutter";
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_DEATH, 2F);
	private static final ParticleContainer part = new ParticleContainer(Particle.SWEEP_ATTACK);
	private static final int PROJECTILE_AMOUNT = 5;
	private static final int ATTACKS_REQUIRED = 12;
	private static final int MANA_COST = 15;
	private static final int ACTIVE_ATTACKS = 3;
	
	private int damage;
	
	public Windcutter(boolean isUpgraded) {
		super(ID, "Windcutter", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(MANA_COST, 0, 0, 5));
		damage = isUpgraded ? 90 : 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			am.addCount(1);
			if (am.getCount() < ATTACKS_REQUIRED) return TriggerResult.keep();
			if (data.getMana() < MANA_COST) return TriggerResult.keep();
			data.addMana(-MANA_COST);
			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				ActionMeta am = new ActionMeta();
				ProjectileGroup projs = new ProjectileGroup();
				HashSet<UUID> enemiesHit = new HashSet<>();
				for (int i = 0; i < PROJECTILE_AMOUNT; i++) {
					projs.add(new WindcutterProjectile(i, PROJECTILE_AMOUNT / 2, slot, Windcutter.this, enemiesHit));
				}
				data.addTrigger(id + "-active", Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
					am.addCount(1);
					if (am.getCount() < ACTIVE_ATTACKS) return TriggerResult.keep();
					am.setCount(0);
					Player p = data.getPlayer();
					sound.play(p, p);
					enemiesHit.clear();
					projs.start(data, p.getLocation().add(0, 1, 0), p.getLocation().getDirection().setY(0).normalize());
					return TriggerResult.keep();
				});
			}
		}.runTask(NeoRogue.inst()));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BAMBOO,
				GlossaryTag.POWER.tag(this) + ". Activates after " + DescUtil.white(ATTACKS_REQUIRED) + " basic attacks with at least " +
				DescUtil.yellow(MANA_COST) + " mana. Every " + DescUtil.white(ACTIVE_ATTACKS) + " basic attacks, fire five piercing projectiles in a cone that deal " + 
				GlossaryTag.SLASHING.tag(this, damage, true) + " damage.");
	}
	
	private class WindcutterProjectile extends Projectile {
		private int slot;
		private Equipment eq;
		private HashSet<UUID> enemiesHit;
		public WindcutterProjectile(int i, int center, int slot, Equipment eq, HashSet<UUID> enemiesHit) {
			super(0.5, properties.get(PropertyType.RANGE), 2);
			this.size(1, 1).pierce(-1);
			int iter = i - center;
			this.rotation(iter * 25);
			this.slot = slot;
			this.eq = eq;
			this.enemiesHit = enemiesHit;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			if (proj.getTick() % 3 == 0) Sounds.flap.play(p, proj.getLocation());
			part.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (!enemiesHit.add(hit.getUniqueId())) {
				meta.getSlices().clear();
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.SLASHING, DamageStatTracker.of(id + slot, eq)));
		}
	}
}
