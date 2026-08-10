package me.neoblade298.neorogue.equipment.abilities;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
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

public class UmbralVolley extends Equipment {
	private static final String ID = "UmbralVolley";
	private static final int COOLDOWN = 60; // 3 seconds in ticks
	private int damage;
	private static final ParticleContainer part = new ParticleContainer(Particle.SQUID_INK)
			.count(1).spread(0.1, 0.1);
	
	public UmbralVolley(boolean isUpgraded) {
		super(ID, "Umbral Volley", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 4));
		
		damage = isUpgraded ? 65 : 50;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		DamageStatTracker tracker = DamageStatTracker.of(ID + slot, this);
		
		ActionMeta cooldown = new ActionMeta();
		
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (cooldown.getTime() >= System.currentTimeMillis()) return TriggerResult.keep();
			
			cooldown.setTime(System.currentTimeMillis() + COOLDOWN * 50); // Convert ticks to ms
			
			// Fire projectiles after half a second delay
			data.addTask(new BukkitRunnable() {
				public void run() {
					Player p = data.getPlayer();
					ProjectileGroup projs = new ProjectileGroup();
					HashSet<UUID> enemiesHit = new HashSet<>();
					for (int i = 0; i < 5; i++) {
						projs.add(new UmbralVolleyProjectile(i, tracker, enemiesHit));
					}
					Sounds.attackSweep.play(p, p);
					projs.start(data);
				}
			}.runTaskLater(NeoRogue.inst(), 10L)); // 10 ticks = 0.5 seconds
			
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PHANTOM_MEMBRANE,
				GlossaryTag.PASSIVE.tag(this) + ". On a " + DescUtil.val("3s") + " cooldown, on basic attack, fire " + DescUtil.val(5) + " dark needles in a cone in front of you "
						+ DescUtil.val("0.5s") + " later that deal "
						+ GlossaryTag.DARK.tag(this, damage) + " damage. Needles can't hit the same enemy twice.");
	}
	
	private class UmbralVolleyProjectile extends Projectile {
		private DamageStatTracker tracker;
		private HashSet<UUID> enemiesHit;
		public UmbralVolleyProjectile(int i, DamageStatTracker tracker, HashSet<UUID> enemiesHit) {
			super(1, properties.get(PropertyType.RANGE), 1);
			int iter = i - 2;
			this.rotation(iter * 15);
			this.size(1, 1);
			this.tracker = tracker;
			this.enemiesHit = enemiesHit;
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			if (!enemiesHit.add(hit.getUniqueId())) {
				meta.getSlices().clear();
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.DARK, tracker));
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			part.play(p, proj.getLocation());
		}
	}
}
