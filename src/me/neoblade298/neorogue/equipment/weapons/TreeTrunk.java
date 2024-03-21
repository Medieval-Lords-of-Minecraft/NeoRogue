package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TreeTrunk extends Equipment {
	private static final String ID = "treeTrunk";
	private static final int CUTOFF = 20;
	private double damage, conc;
	private static final ParticleAnimation swing = StoneHammer.swing;
	private static final ParticleContainer hitLine = new ParticleContainer(Particle.CLOUD).count(25).spread(2, 0.1);
	private static final TargetProperties left = TargetProperties.line(4, 2, TargetType.ENEMY),
			right = TargetProperties.radius(4, false, TargetType.ENEMY);
	
	public TreeTrunk(boolean isUpgraded) {
		super(ID, "Tree Trunk", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(isUpgraded ? 140 : 110, 0.5, 0, left.range, DamageType.BLUNT, Sound.ENTITY_PLAYER_ATTACK_CRIT));
		properties.addUpgrades(PropertyType.DAMAGE);
		damage = properties.get(PropertyType.DAMAGE);
		conc = isUpgraded ? 14 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			data.runAnimation(id, p, swing, p);
			weaponSwing(p, data);
			data.addTask(id, new BukkitRunnable() {
				public void run() {
					leftHit(p, data);
				}
			}.runTaskLater(NeoRogue.inst(), 10L));
			return TriggerResult.keep();
		});
		
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK, (pdata, inputs) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			weaponSwing(p, data, 0.25);
			rightHit(p, data);
			return TriggerResult.keep();
		});
	}
	
	private void leftHit(Player p, PlayerFightData data) {
		Location hit = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(left.range));
		Sounds.explode.play(p, hit);
		ParticleUtil.drawLine(p, hitLine, p.getLocation(), hit, 1);
		LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInLine(p, p.getLocation(), hit, left);
		if (enemies.isEmpty()) return;
		boolean first = true;
		Vector v = new Vector(0, 0.5, 0);
		for (LivingEntity ent : enemies) {
			if (first) {
				weaponDamage(p, data, ent);
				first = false;
			}
			else {
				DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType());
				FightInstance.dealDamage(dm, ent);
			}
			FightInstance.knockback(ent, v);
		}
	}
	
	private void rightHit(Player p, PlayerFightData data) {
		Location hit = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(left.range));
		Sounds.explode.play(p, hit);
		ParticleUtil.drawLine(p, hitLine, p.getLocation(), hit, 1);
		LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInLine(p, p.getLocation(), hit, left);
		if (enemies.isEmpty()) return;
		boolean first = true;
		Vector v = new Vector(0, 0.5, 0);
		for (LivingEntity ent : enemies) {
			if (first) {
				weaponDamage(p, data, ent);
				first = false;
			}
			else {
				DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType());
				FightInstance.dealDamage(dm, ent);
			}
			FightInstance.knockback(ent, v);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_LOG, "Can only be used without an offhand. Left click deals damage in a line,"
				+ " right click can only be used with " + GlossaryTag.BERSERK.tag(this, CUTOFF, false) + ", deals damage in a circle and "
				+ " applies " + GlossaryTag.CONCUSSED.tag(this, conc, true) + ", but at half the attack speed.");
	}
}
