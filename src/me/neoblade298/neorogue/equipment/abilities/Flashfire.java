package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.EquipmentProperties.PropertyType;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Flashfire extends Equipment {
	private static final String ID = "flashfire";
	private static final TargetProperties tp = TargetProperties.radius(6, false);
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private static final Circle circ = new Circle(tp.range);
	private int damage, burn;

	public Flashfire(boolean isUpgraded) {
		super(ID, "Flashfire", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(20, 5, 18, 0, tp.range));
		damage = isUpgraded ? 150 : 100;
		burn = isUpgraded ? 120 : 80;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			double manaBefore = data.getMana() + properties.get(PropertyType.MANA_COST);
			boolean hasBonus = manaBefore > data.getMaxMana() * 0.5;
			Sounds.fire.play(p, p);
			circ.play(pc, p.getLocation(), LocalAxes.xz(), null);
			for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
				FightData fd = FightInstance.getFightData(ent);
				if (fd.hasStatus(StatusType.BURN)) {
					fd.applyStatus(StatusType.BURN, data,
							fd.getStatus(StatusType.BURN).getStacks() * (hasBonus ? 2 : 1), slot);
				}

				FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE, DamageStatTracker.of(id + slot, this)), ent);
				fd.applyStatus(StatusType.BURN, data, burn, -1);
			}
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, double (triple if above <white>50%</white> mana) the " + GlossaryTag.BURN.tag(this)
						+ " of all nearby enemies, then deal " + GlossaryTag.FIRE.tag(this, damage, true)
						+ " damage and apply " + GlossaryTag.BURN.tag(this, burn, true)
						+ " to all enemies nearby enemies.");
	}
}
