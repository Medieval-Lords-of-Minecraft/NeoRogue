package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.BowProjectile;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardEquipmentInstance;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
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

public class Backstep2 extends Equipment {
	private static final String ID = "Backstep2";
	private static final int[] ROTATIONS = { 0, -15, 15 };
	private int thres, damage, rend, shields;
	
	public Backstep2(boolean isUpgraded) {
		super(ID, "Backstep II", isUpgraded, Rarity.UNCOMMON, EquipmentClass.ARCHER,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 10, 1, 12));
		damage = isUpgraded ? 20 : 10;
		rend = 10;
		thres = isUpgraded ? 60 : 45;
		shields = 6;
	}

	@Override
	public void setupItem() {
		item = createItem(Material.IRON_BOOTS,
				"This ability can be stored and cast once for every " + GlossaryTag.REND.tag(this, thres, true) + " stacks you apply. " +
				"On cast, jump backwards, gain " + DescUtil.potion("Speed", 0, 3) + ", gain " + GlossaryTag.SHIELDS.tag(this, shields, false) + " [<white>5s</white>], " +
				"fire <white>3</white> projectiles that deal " +
				GlossaryTag.PIERCING.tag(this, damage, true) + " damage, and apply " + GlossaryTag.REND.tag(this, rend, false) +".");
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		ItemStack icon = item.clone();
		StandardEquipmentInstance inst = new StandardEquipmentInstance(data, this, slot, es);
		updateIcon(inst, icon);
		ProjectileGroup projs = new ProjectileGroup();
		for (int i : ROTATIONS) {;
			projs.add(new Backstep2Projectile(data, i, slot, this));
		}

		inst.setAction((pdata, in) -> {
			Player p = data.getPlayer();
			inst.addCount(-1);
			updateIcon(inst, icon);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).setX(-v.getX()).setZ(-v.getZ()).normalize().multiply(0.7).setY(0.3));
			Sounds.jump.play(p, p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));
			data.addSimpleShield(p.getUniqueId(), shields, 100);
			projs.start(data);
			return TriggerResult.keep();
		});
		inst.setCondition((pl, pdata, in) -> {
			return inst.getCount() > 0;
		});
		data.addTrigger(id, bind, inst);

		StandardPriorityAction counter = new StandardPriorityAction(id);
		counter.setAction((pdata, in) -> {
			ApplyStatusEvent ev = (ApplyStatusEvent) in;
			if (!ev.isStatus(StatusType.REND)) return TriggerResult.keep();
			counter.addCount(ev.getStacks());
			if (counter.getCount() >= thres) {
				inst.addCount(counter.getCount() / thres);
				updateIcon(inst, icon);
				counter.setCount(counter.getCount() % thres);
			}
			return TriggerResult.keep();
		});
		data.addTrigger(id, Trigger.APPLY_STATUS, counter);
	}

	private void updateIcon(StandardEquipmentInstance inst, ItemStack icon) {
		int count = inst.getCount();
		if (count == 0) {
			icon = icon.withType(Material.BARRIER);
			icon.setAmount(1);
		}
		else {
			icon = icon.withType(Material.IRON_BOOTS);
			icon.setAmount(count);
		}
		inst.setIcon(icon);
	}
	
	private class Backstep2Projectile extends Projectile {
		private PlayerFightData data;
		private Player p;
		private Backstep2 eq;
		private String id;

		// Vector is non-normalized velocity of the vanilla projectile being fired
		public Backstep2Projectile(PlayerFightData data, int rotation, int slot, Backstep2 eq) {
			super(properties.get(PropertyType.RANGE), 1);
			this.blocksPerTick(3);
			this.rotation(rotation);
			this.data = data;
			this.p = data.getPlayer();
			this.eq = eq;
			this.id = ID + slot;
		}

		@Override
		public void onTick(ProjectileInstance proj, int interpolation) {
			BowProjectile.tick.play(p, proj.getLocation());
		}

		@Override
		public void onHit(FightData hit, Barrier hitBarrier, DamageMeta meta, ProjectileInstance proj) {
			hit.applyStatus(StatusType.REND, data, rend, -1);
		}

		@Override
		public void onStart(ProjectileInstance proj) {
			Sounds.shoot.play(p, p);
			DamageMeta dm = proj.getMeta();
			dm.addDamageSlice(new DamageSlice(data, damage, DamageType.PIERCING, DamageStatTracker.of(id, eq)));
		}
	}
}
