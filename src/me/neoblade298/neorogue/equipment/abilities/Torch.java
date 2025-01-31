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
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageMeta;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Torch extends Equipment {
	private static final String ID = "torch";
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME);
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private static final Circle circ = new Circle(tp.range);

	private int damage, burn;
	
	public Torch(boolean isUpgraded) {
		super(
				ID , "Torch", isUpgraded, Rarity.UNCOMMON, EquipmentClass.MAGE, EquipmentType.ABILITY,
				EquipmentProperties.ofUsable(40, 0, 12, 0, tp.range));
		damage = isUpgraded ? 240 : 160;
		burn = 30;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata ,in) -> {
			data.channel(20).then(new Runnable() {
				public void run() {
					data.applyStatus(StatusType.BURN, data, burn, -1);
					circ.play(pc, p.getLocation(), LocalAxes.xz(), pc);
					Sounds.infect.play(p, p);
					for (LivingEntity ent : TargetHelper.getEntitiesInRadius(p, tp)) {
						FightInstance.dealDamage(new DamageMeta(data, damage, DamageType.FIRE), ent);
					}
				}
			});
			return TriggerResult.keep();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
			GlossaryTag.CHANNEL.tag(this) + " for <white>1s</white> before dealing " + GlossaryTag.FIRE.tag(this, damage, true) + " damage to nearby enemies, but apply " +
			GlossaryTag.BURN.tag(this, burn, false) + " to yourself.");
	}
}
