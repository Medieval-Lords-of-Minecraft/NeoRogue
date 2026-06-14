package me.neoblade298.neorogue.equipment.abilities;
import me.neoblade298.neorogue.equipment.SessionEquipment;

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
import me.neoblade298.neorogue.equipment.ActionMeta;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Power;
import me.neoblade298.neorogue.equipment.Rarity;
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
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class LightPulse extends Equipment implements Power {
	private static final String ID = "LightPulse";
	private static final SoundContainer sound = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_DEATH, 2F),
			tick = new SoundContainer(Sound.ENTITY_EVOKER_CAST_SPELL);
	private static final ParticleContainer part = new ParticleContainer(Particle.FIREWORK).count(5).spread(0.4, 0.2);
	private static final int PROJECTILE_AMOUNT = 3;
	
	private ProjectileGroup projs = new ProjectileGroup();
	private HashSet<UUID> enemiesHit = new HashSet<>();
	private int damage, cost;
	
	public LightPulse(boolean isUpgraded) {
		super(ID, "Light Pulse", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 0, 0, 6));
		damage = isUpgraded ? 200 : 135;
		cost = 6;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	private static final int ACTIVATION_THRES = 3;

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot, SessionEquipment sessionEq) {
		for (int i = 0; i < PROJECTILE_AMOUNT; i++) {
			projs.add(new LightPulseProjectile(i, PROJECTILE_AMOUNT / 2, slot, this));
		}

		ActionMeta am = new ActionMeta();
		data.addTrigger(id, Trigger.PRE_BASIC_ATTACK, (pdata, in) -> {
			if (data.getMana() < data.getMaxMana()) return TriggerResult.keep();
			am.addCount(1);
			if (am.getCount() < ACTIVATION_THRES) return TriggerResult.keep();

			if (activatePower(data, slot, es)) return TriggerResult.remove();
			return TriggerResult.keep();
		});
	}
	
	private class LightPulseInstance extends PriorityAction {
		private int count = 0;
		public LightPulseInstance(String id, PlayerFightData data) {
			super(id);
			action = (pdata, in) -> {
				if (++count >= 3 && (data.getMana() / data.getMaxMana()) > 0.5) {
					Player p = data.getPlayer();
					sound.play(p, p);
					enemiesHit.clear();
					projs.start(data, p.getLocation().add(0, 1, 0), p.getLocation().getDirection().setY(0).normalize());
					data.addMana(-cost);
					count = 0;
				}
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void onPowerActivated(PlayerFightData data, int slot, EquipSlot es) {
		data.addTask(new BukkitRunnable() {
			public void run() {
				data.addTrigger(id + "-active", Trigger.PRE_BASIC_ATTACK, new LightPulseInstance(id, data));
			}
		}.runTask(NeoRogue.inst()));
	}


	@Override
	public void setupItem() {
		item = createItem(Material.END_ROD,
				GlossaryTag.POWER.tag(this) + ". Activates after basic attacking " + DescUtil.white(ACTIVATION_THRES) + " times while above " + DescUtil.white("50%") + " max mana. When above 50% max mana, every " + DescUtil.white("third") + " basic attack fires five piercing projectiles in a cone that deal " + GlossaryTag.LIGHT.tag(this, damage, true) +
				" damage. Costs " + DescUtil.white(cost) + " mana, unaffected by mana cost reduction.");
	}
	
	private class LightPulseProjectile extends Projectile {
		private int slot;
		private Equipment eq;
		public LightPulseProjectile(int i, int center, int slot, Equipment eq) {
			super(0.5, properties.get(PropertyType.RANGE), 2);
			this.size(1, 1).pierce(-1);
			int iter = i - center;
			this.rotation(iter * 25);
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
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
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.LIGHT, DamageStatTracker.of(id + slot, eq)));
			tick.play((Player) proj.getOwner().getEntity(), proj.getLocation());
		}
	}
}
