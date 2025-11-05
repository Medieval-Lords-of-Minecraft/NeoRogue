package me.neoblade298.neorogue.equipment.weapons;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.equipment.StandardPriorityAction;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.LeftClickHitEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MirrorSickle extends Equipment {
	private static final String ID = "mirrorSickle";
	private static final ParticleContainer pc = new ParticleContainer(Particle.PORTAL).count(50).spread(0.5, 0.5).offsetY(1);
	private static TargetProperties tp = TargetProperties.radius(3, false, TargetType.ENEMY);
	private int dash;
	
	public MirrorSickle(boolean isUpgraded) {
		super(ID, "Mirror Sickle", isUpgraded, Rarity.UNCOMMON, EquipmentClass.THIEF,
				EquipmentType.WEAPON,
				EquipmentProperties.ofRangedWeapon(60, 1, 0.2, tp.range, DamageType.PIERCING, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
		dash = isUpgraded ? 150 : 100;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		if (data.getSessionData().getEquipment(EquipSlot.OFFHAND)[0] != null) {
			Util.msg(p, hoverable.append(Component.text("  couldn't be equipped as you have equipment in your offhand!", NamedTextColor.RED)));
			p.getInventory().setItem(slot, null);
			return;
		}
		
		StandardPriorityAction inst = new StandardPriorityAction(ID);
		Equipment eq = this;
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK_HIT, (pdata, inputs) -> {
			LeftClickHitEvent ev = (LeftClickHitEvent) inputs;
			weaponSwingAndDamage(p, data, ev.getTarget());
			inst.addCount(1);
			return TriggerResult.keep();
		});
		
		inst.setAction((pdata, in) -> {
			if (inst.getCount() < 3) return TriggerResult.keep();
			inst.addCount(-3);
			Sounds.teleport.play(p, p);
			pc.play(p, p);
			Vector v = p.getEyeLocation().getDirection();
			if (p.isOnGround()) {
				p.teleport(p.getLocation().add(0, 0.2, 0));
			}
			p.setVelocity(v.setY(0).normalize().multiply(0.5).setY(0.1));
			data.addTask(new BukkitRunnable() {
				public void run() {
					LivingEntity ent = TargetHelper.getNearest(p, tp);
					if (ent == null) return;
					DamageMeta dm = new DamageMeta(data, dash, DamageType.PIERCING, DamageStatTracker.of(id + slot, eq));
					FightInstance.dealDamage(dm, ent);
				}
			}.runTaskLater(NeoRogue.inst(), 10L));
			return TriggerResult.keep();
		});
		data.addSlotBasedTrigger(id, slot, Trigger.RIGHT_CLICK, inst);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.PRISMARINE_SHARD, "Can only be used without an offhand. Basic attacks with this weapon grant "
				+ "<white>1 power</white>. Right clicking uses <white>3 power</white> to dash and deal "
				+ GlossaryTag.PIERCING.tag(this, dash, true) + " damage to the nearest enemy.");
	}
}
