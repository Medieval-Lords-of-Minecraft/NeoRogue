package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
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
import me.neoblade298.neorogue.session.fight.DamageSlice;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.ApplyStatusEvent;

public class IcicleTome extends Equipment {
	private static final String ID = "IcicleTome";
	private static final ParticleContainer pc = new ParticleContainer(Particle.BLOCK).blockData(Material.BLUE_ICE.createBlockData());
	private int damage, thres;
	
	public IcicleTome(boolean isUpgraded) {
		super(ID, "Icicle Tome", isUpgraded, Rarity.COMMON, EquipmentClass.ARCHER,
				EquipmentType.OFFHAND, EquipmentProperties.ofUsable(10, 0, 2, 12));
		damage = isUpgraded ? 150 : 100;
		thres = 60;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ActionMeta am = new ActionMeta();
		EquipmentInstance eqi = new EquipmentInstance(data, this, slot, es);
		ItemStack charged = item.clone().withType(Material.ENCHANTED_BOOK);
		ProjectileGroup projs = new ProjectileGroup(new IcicleTomeProjectile(slot, this));
		eqi.setAction((pdata, in) -> {	
			if (am.getCount() >= thres) {
				Player p = data.getPlayer();
				am.addCount(-thres);
				Sounds.wind.play(p, p);
				projs.start(data);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, (pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (ev.isStatus(StatusType.FROST)) {
				am.addCount(ev.getStacks());
				if (am.getCount() >= thres) {
					eqi.setIcon(charged);
				} else {
					eqi.setIcon(item);
				}
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.LEFT_CLICK, eqi);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "Every time you apply " + GlossaryTag.FROST.tag(this, thres, false) + ", " +
				"left clicking will fire a projectile that deals " + GlossaryTag.ICE.tag(this, damage, true) + " damage.");
	}
	
	private class IcicleTomeProjectile extends Projectile {
		private int slot;
		private Equipment eq;
		public IcicleTomeProjectile(int slot, Equipment eq) {
			super(0.8, properties.get(PropertyType.RANGE), 1);
			this.slot = slot;
			this.eq = eq;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			Player p = (Player) proj.getOwner().getEntity();
			pc.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			Sounds.glass.play((Player) proj.getOwner().getEntity(), hit.getEntity());
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			proj.getMeta().addDamageSlice(new DamageSlice(proj.getOwner(), damage, DamageType.ICE, DamageStatTracker.of(id + slot, eq)));
		}
	}
}
