package me.neoblade298.neorogue.equipment.offhands;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Cone;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
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
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class TomeOfScorchedEarth extends Equipment {
	private static final String ID = "TomeOfScorchedEarth";
	private static final TargetProperties tp = TargetProperties.cone(60, 5, false, TargetType.ENEMY);
	private static final Cone cone = new Cone(tp.range, tp.arc);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).offsetY(0.3);
	private int damage, selfDmg = 3;

	public TomeOfScorchedEarth(boolean isUpgraded) {
		super(ID, "Tome of Scorched Earth", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.OFFHAND,
				EquipmentProperties.ofUsable(10, 0, 12, tp.range));
		damage = isUpgraded ? 180 : 120;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, Trigger.RIGHT_CLICK, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();
			Sounds.fire.play(p, p);
			cone.play(pc, p.getLocation(), LocalAxes.usingEyeLocation(p), pc);
			LinkedList<LivingEntity> trgs = TargetHelper.getEntitiesInCone(p, tp);
			for (LivingEntity ent : trgs) {
				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, this)), ent);
			}
			if (trgs.size() < 2) {
				FightInstance.dealDamage(new DamageMeta(data, selfDmg, DamageType.FIRE,
						DamageStatTracker.of(id + slot, this, "Self damage")).ignoreBuffs(true), p);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BOOK, "On right click, deal " + GlossaryTag.FIRE.tag(this, damage, true)
				+ " damage to all enemies in a cone in front of you. If you hit less than <white>2</white> enemies, also "
				+ "deal " + GlossaryTag.FIRE.tag(this, selfDmg, false) + " to yourself (unaffected by buffs/debuffs).");
	}
}
