package me.neoblade298.neorogue.equipment.abilities;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.Circle;
import me.neoblade298.neocore.bukkit.effects.LocalAxes;
import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.Rift;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Dematerialize extends Equipment {
	private static final String ID = "Dematerialize";
	private static final TargetProperties tp = TargetProperties.radius(5, true, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);
	private static final ParticleContainer pc = new ParticleContainer(Particle.END_ROD).count(40).spread(0.6, 0.6)
			.offsetY(0.4);
	private static final SoundContainer sc = new SoundContainer(Sound.ENTITY_ELDER_GUARDIAN_HURT);
	private int damage;

	public Dematerialize(boolean isUpgraded) {
		super(ID, "Dematerialize", isUpgraded, Rarity.RARE, EquipmentClass.MAGE,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(20, 0, 15, 0, tp.range));
		damage = isUpgraded ? 300 : 200;
	}

	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Player p = data.getPlayer();

			for (Rift rift : new ArrayList<>(data.getRifts().values())) {
				circ.play(pc, rift.getLocation(), LocalAxes.xz(), null);
				sc.play(p, rift.getLocation());
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, rift.getLocation(), tp)) {
					FightInstance.dealDamage(data, DamageType.DARK, damage, ent, DamageStatTracker.of(id + slot, this));
				}
			}

			data.addRift(new Rift(data, p.getLocation(), 300));
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.CHORUS_FLOWER,
				"On cast, explode all " + GlossaryTag.RIFT.tag(this) + ", dealing "
						+ GlossaryTag.DARK.tag(this, damage, true)
						+ " to all enemies near them. Then create a " + GlossaryTag.RIFT.tag(this)
						+ " [<white>15s</white>] at your location.");
	}
}