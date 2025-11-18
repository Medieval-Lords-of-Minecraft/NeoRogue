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

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TreeTrunk extends Equipment {
	private static final String ID = "TreeTrunk";
	private static final int CUTOFF = 1;
	private double conc;
	private static final Circle hitShape = new Circle(4);
	private static final ParticleAnimation swing = StoneHammer.swing;
	private static final ParticleContainer hitLine = new ParticleContainer(Particle.BLOCK).blockData(Material.OAK_LOG.createBlockData()).count(10).spread(0.4, 0.1),
			circle = new ParticleContainer(Particle.DUST).count(20).spread(0.1, 0.7).offsetY(1);
	private static final TargetProperties left = TargetProperties.line(4, 2, TargetType.ENEMY),
			right = TargetProperties.radius(4, false, TargetType.ENEMY);
	
	public TreeTrunk(boolean isUpgraded) {
		super(ID, "Tree Trunk", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(isUpgraded ? 140 : 110, 0.5, 0, left.range, DamageType.BLUNT, new SoundContainer(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F)));
		properties.addUpgrades(PropertyType.DAMAGE);
		conc = isUpgraded ? 45 : 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		if (data.getSessionData().getEquipment(EquipSlot.OFFHAND)[0] != null) {
			Util.msg(p, hoverable.append(Component.text("  couldn't be equipped as you have equipment in your offhand!", NamedTextColor.RED)));
			p.getInventory().setItem(slot, null);
			return;
		}
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (pdata, inputs) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			data.runAnimation(id, p, swing, p);
			weaponSwing(p, data);
			data.addTask(new BukkitRunnable() {
				public void run() {
					leftHit(p, data, slot);
				}
			}.runTaskLater(NeoRogue.inst(), 10L));
			return TriggerResult.keep();
		});
		
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK, (pdata, inputs) -> {
			if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF) return TriggerResult.keep();
			if (!data.canBasicAttack()) return TriggerResult.keep();
			weaponSwing(p, data, 0.25);
			rightHit(p, data, slot);
			return TriggerResult.keep();
		});
	}
	
	private void leftHit(Player p, PlayerFightData data, int slot) {
		Location hit = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(left.range));
		Sounds.explode.play(p, p);
		ParticleUtil.drawLine(p, hitLine, p.getLocation(), hit, 0.5);
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
				DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType(), DamageStatTracker.of(id + slot, this));
				FightInstance.dealDamage(dm, ent);
			}
			FightInstance.knockback(ent, v);
		}
	}
	
	private void rightHit(Player p, PlayerFightData data, int slot) {
		Sounds.explode.play(p, p);
		hitShape.play(circle, p.getLocation(), LocalAxes.xz(), null);
		LinkedList<LivingEntity> enemies = TargetHelper.getEntitiesInRadius(p, right);
		if (enemies.isEmpty()) return;
		boolean first = true;
		Vector v = new Vector(0, 0.5, 0);
		for (LivingEntity ent : enemies) {
			if (first) {
				weaponDamage(p, data, ent);
				first = false;
			}
			else {
				DamageMeta dm = new DamageMeta(data, properties.get(PropertyType.DAMAGE), properties.getType(),
						DamageStatTracker.of(id + slot, this));
				FightInstance.dealDamage(dm, ent);
			}
			FightInstance.knockback(ent, v);
		}
	}

	@Override
	public void setupItem() {
		item = createItem(Material.OAK_LOG, "Can only be used without an offhand. Left click deals damage in a line,"
				+ " right click can only be used with " + GlossaryTag.BERSERK.tag(this, CUTOFF, false) + ", deals damage in a circle and "
				+ "applies " + GlossaryTag.CONCUSSED.tag(this, conc, true) + ", but at half the attack speed.");
	}
}
