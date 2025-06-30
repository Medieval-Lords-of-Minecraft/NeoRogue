package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.mechanics.Barrier;
import me.neoblade298.neorogue.equipment.mechanics.Projectile;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileGroup;
import me.neoblade298.neorogue.equipment.mechanics.ProjectileInstance;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.DealtDamageEvent;

public class LeadingKnife extends Equipment {
	private static final String ID = "leadingKnife";
	private static final ParticleContainer tick = new ParticleContainer(Particle.CRIT).count(3).speed(0.01).spread(0.1, 0.1);
	private static final SoundContainer hit = new SoundContainer(Sound.ENTITY_ITEM_BREAK);
	private int stamina;
	
	// Todo: Check if I need additional conditions for attack cooldown, add stamina mark
	public LeadingKnife(boolean isUpgraded) {
		super(ID, "Leading Knife", isUpgraded, Rarity.COMMON, EquipmentClass.THIEF,
				EquipmentType.OFFHAND, EquipmentProperties.ofRangedWeapon(10, 0.5, 0, 10, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		stamina = isUpgraded ? 4 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		LeadingKnifeInstance inst = new LeadingKnifeInstance(ID, this);
		ProjectileGroup proj = new ProjectileGroup(new LeadingKnifeProjectile(data, inst, slot));
		inst.initialize(proj);
		
		data.addTrigger(ID, Trigger.RIGHT_CLICK, inst);
		data.addTrigger(ID, Trigger.DEALT_DAMAGE, (pdata, in) -> {
			DealtDamageEvent ev = (DealtDamageEvent) in;
			if ((System.currentTimeMillis() - inst.hitTime > 2000) || inst.marked != ev.getTarget()) return TriggerResult.keep();
			
			inst.marked = null;
			data.addStamina(stamina);
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.STONE_SWORD, "Right click to throw a knife. If an enemy is hit, they are marked for <yellow>2</yellow> seconds."
				+ " Dealing damage to them grants you"
				+ " <yellow>" + stamina + "</yellow> stamina and consumes the mark.");
	}
	
	private class LeadingKnifeInstance extends PriorityAction {
		public LeadingKnife eq;
		private Entity marked;
		private long hitTime;

		public LeadingKnifeInstance(String id, LeadingKnife eq) {
			super(id);
			this.eq = eq;
		}
		
		private void initialize(ProjectileGroup proj) {
			action = (pdata, in) -> {
				if (!pdata.canBasicAttack(EquipSlot.OFFHAND)) return TriggerResult.keep();
				Player p = pdata.getPlayer();
				weaponSwing(p, pdata);
				proj.start(pdata);
				return TriggerResult.keep();
			};
		}
	}
	
	private class LeadingKnifeProjectile extends Projectile {
		private Player p;
		private PlayerFightData data;
		private LeadingKnifeInstance inst;
		private int slot;

		public LeadingKnifeProjectile(PlayerFightData data, LeadingKnifeInstance inst, int slot) {
			super(1.5, 10, 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.p = data.getPlayer();
			this.inst = inst;
			this.slot = slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			LeadingKnife.hit.play(p, hit.getEntity());
			inst.marked = hit.getEntity();
			inst.hitTime = System.currentTimeMillis();
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyProperties(data, inst.eq, slot);
		}
	}
}
