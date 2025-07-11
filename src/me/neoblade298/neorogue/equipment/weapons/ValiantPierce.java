package me.neoblade298.neorogue.equipment.weapons;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.ParticleUtil;
import me.neoblade298.neorogue.DescUtil;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageSlice;
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

public class ValiantPierce extends Equipment {
	private static final String ID = "valiantPierce";
	private static final ParticleContainer lancePart = new ParticleContainer(Particle.ELECTRIC_SPARK).count(3).spread(0.1, 0.1);
	private static final TargetProperties tp = TargetProperties.line(6, 1, TargetType.ENEMY);
	private int damage, bonus;

	public ValiantPierce(boolean isUpgraded) {
		super(
				ID, "Valiant Pierce", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, EquipmentType.WEAPON,
				EquipmentProperties.ofUsable(0, 35, 8, tp.range)
		);
		damage = isUpgraded ? 300 : 200;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addSlotBasedTrigger(id, slot, bind, (pdata, in) -> {
			if (!data.canBasicAttack()) return TriggerResult.keep();
			LinkedList<LivingEntity> targets = TargetHelper.getEntitiesInSight(p, tp);
			weaponSwing(p, data);
			Location start = p.getLocation().add(0, 1, 0);
			Vector v = p.getLocation().getDirection().setY(0).normalize().multiply(tp.range);
			ParticleUtil.drawLine(p, lancePart, p.getLocation().add(0, 1, 0), start.clone().add(v), 0.5);
			if (targets.isEmpty())
				return TriggerResult.keep();
			int stacks = FightInstance.getFightData(targets.getFirst()).getStatus(StatusType.SANCTIFIED).getStacks();
			DamageMeta dm = new DamageMeta(data, this, true, DamageStatTracker.of(id + slot, this)).setKnockback(-0.5);
			dm.addDamageSlice(new DamageSlice(data, stacks, DamageType.LIGHT, DamageStatTracker.of(id + slot, this)));
			FightInstance.dealDamage(dm, targets.getFirst());
			return TriggerResult.keep();
		});
	}

	@Override
	public void setupItem() {
		item = createItem(
				Material.POINTED_DRIPSTONE,
				"On cast, after " + DescUtil.charge(this, 1, 1) + ", deal " + GlossaryTag.PIERCING.tag(this, damage, true) + " to all enemies in a line, " +
				"and give each enemy " + DescUtil.yellow(multStr + "%") + " of the " + GlossaryTag.SANCTIFIED.tag(this) + " and " + GlossaryTag.CONCUSSED.tag(this) +
				" of the nearest enemy hit.");
		);
	}
}
