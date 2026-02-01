package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.bukkit.effects.ParticleAnimation;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.NeoRogue;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.buff.Buff;
import me.neoblade298.neorogue.session.fight.buff.BuffStatTracker;
import me.neoblade298.neorogue.session.fight.status.Status;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;
import me.neoblade298.neorogue.session.fight.trigger.event.PreApplyStatusEvent;

public class BeamStaff extends Equipment {
	private static final String ID = "BeamStaff";
	private static final TargetProperties tp = TargetProperties.radius(12, false),
		aoe = TargetProperties.radius(1.5, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FIREWORK).count(10).spread(0.25, 0.2).speed(0.01);
	private double mult;
	private int multstr;
	private static final ParticleAnimation anim;
	
	static {
		anim = new ParticleAnimation(pc, (loc, tick) -> {
			LinkedList<Location> partLocs = new LinkedList<Location>();
			partLocs.add(loc.clone().add(0, 4 * ((10 - tick) * 0.4), 0));
			return partLocs;
		}, 10);
	}

	public BeamStaff(boolean isUpgraded) {
		super(
				ID , "Beam Staff", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.WEAPON,
				EquipmentProperties.ofWeapon(3, 0, 60, 0.75, DamageType.EARTHEN, Sound.ENTITY_PLAYER_ATTACK_SWEEP)
				.add(PropertyType.RANGE, tp.range).add(PropertyType.AREA_OF_EFFECT, aoe.range)
		);
		mult = isUpgraded ? 0.3 : 0.2;
		multstr = (int) (mult * 100);
		canDrop = false;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		Player p = data.getPlayer();
		String statName = p.getName() + "beamStaff";
		String buffId = UUID.randomUUID().toString();
		data.addSlotBasedTrigger(id, slot, Trigger.LEFT_CLICK, (d, inputs) -> {
			if (!canUseWeapon(data) || !data.canBasicAttack(EquipSlot.HOTBAR))
				return TriggerResult.keep();
			Block block = p.getTargetBlockExact((int) properties.get(PropertyType.RANGE));
			if (block == null) {
				return TriggerResult.keep();
			}
			Location loc = block.getLocation();
			weaponSwing(p, data);
			anim.play(p, loc);
			data.addTask(new BukkitRunnable() {
				public void run() {
					Sounds.explode.play(p, loc);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, loc, aoe)) {
						weaponDamage(p, data, ent);
						Status s = Status.createByGenericType(GenericStatusType.BASIC, statName,
								FightInstance.getFightData(ent), true);
						FightInstance.applyStatus(ent, s, 1, 100, data);
					}
				}
			}.runTaskLater(NeoRogue.inst(), 10));
			return TriggerResult.keep();
		});

		data.addTrigger(id, Trigger.PRE_APPLY_STATUS, (pdata, in) -> {
			PreApplyStatusEvent ev = (PreApplyStatusEvent) in;
			FightData fd = ev.getTarget();
			if (!fd.hasStatus(statName)) return TriggerResult.keep();
			ev.getStacksBuffList().add(Buff.multiplier(data, mult, BuffStatTracker.of(buffId, this, "Statuses increased")));
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(Material.END_ROD, "Fires a beam down onto the block you aim at after a brief delay, dealing " + 
		GlossaryTag.LIGHT.tag(this, properties.get(PropertyType.DAMAGE), false) + " damage to all enemies in a small radius. Enemies hit receive an additional " + 
		DescUtil.yellow(multstr + "%") + " of all statuses from you.");
	}
}
