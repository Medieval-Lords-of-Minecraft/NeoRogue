package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Location;
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
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class IronThrowingKnife extends Equipment {
	private static final String ID = "IronThrowingKnife";
	private int dur;
	
	private static final ParticleContainer tick = new ParticleContainer(Particle.CRIT).count(1);
	private static final SoundContainer hit = new SoundContainer(Sound.ENTITY_ITEM_BREAK);
	
	public IronThrowingKnife(boolean isUpgraded) {
		super(ID, "Iron Throwing Knife", isUpgraded, Rarity.RARE, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(isUpgraded ? 60 : 50, 1.25, 0, 5, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		properties.addUpgrades(PropertyType.DAMAGE);
		dur = isUpgraded ? 10 : 5;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupReforges() {
		addReforge(MirrorSickle.get(), PhantasmalKiller.get());
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta hitCount = new ActionMeta();
		ProjectileGroup proj = new ProjectileGroup(new IronThrowingKnifeProjectile(data, this, slot, hitCount));
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data)) return TriggerResult.keep();
			if (!data.canBasicAttack()) return TriggerResult.keep();
			weaponSwing(p, data);
			proj.start(data);
			return TriggerResult.keep();
		});
	}
	
	private class IronThrowingKnifeProjectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private IronThrowingKnife eq;
		private int slot;
		private ActionMeta hitCount;

		public IronThrowingKnifeProjectile(PlayerFightData data, IronThrowingKnife eq, int slot, ActionMeta hitCount) {
			super(0.5, 5, 1);
			this.size(0.5, 0.5);
			this.data = data;
			this.eq = eq;
			this.p = data.getPlayer();
			this.slot = slot;
			this.hitCount = hitCount;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Location loc = hit.getEntity().getLocation();
			IronThrowingKnife.hit.play(p, loc);
			
			hitCount.addCount(1);
			if (hitCount.getCount() >= 3) {
				hitCount.setCount(0);
				FightInstance.applyStatus(p, StatusType.STEALTH, data, 1, dur * 20);
			}
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.applyWeapon(data, eq, slot);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_SWORD, "Throwable. Every <white>3</white> hits grant <white>1</white> " 
				+ GlossaryTag.STEALTH.tag(this) + " " + DescUtil.yellow(dur + "s") + ".");
	}
}
