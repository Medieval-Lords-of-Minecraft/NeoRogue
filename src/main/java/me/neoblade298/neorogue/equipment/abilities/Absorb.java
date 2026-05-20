package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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

public class Absorb extends Equipment {
	private static final String ID = "Absorb";
	private static final TargetProperties tp = TargetProperties.radius(8, false, TargetType.ENEMY);
	private int damage, mana;
	private static final ParticleContainer pc = new ParticleContainer(Particle.SMOKE).count(50).spread(0.3, 0.3).offsetY(1);
	
	public Absorb(boolean isUpgraded) {
		super(ID, "Absorb", isUpgraded, Rarity.COMMON, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(15, 0, 12, tp.range));
		damage = isUpgraded ? 90 : 60;
		mana = isUpgraded ? 30 : 20;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pd, in) -> {
			Player p = data.getPlayer();
			LivingEntity trg = TargetHelper.getNearest(p, tp);
			if (trg == null) return TriggerResult.keep();
			pc.play(p, trg);
			Sounds.wither.play(p, trg);
			FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.DARK, DamageStatTracker.of(id + slot, this)), trg);
			if (trg.isDead()) {
				data.addMana(mana);
				Sounds.levelup.play(p, p);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.HOPPER,
				"On cast, deal " + GlossaryTag.DARK.tag(this, damage, true) + " to the enemy you're looking at. If they're killed, " +
				"gain " + DescUtil.yellow(mana) + " mana.");
	}
}
