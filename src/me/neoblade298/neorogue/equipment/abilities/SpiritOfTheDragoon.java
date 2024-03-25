package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class SpiritOfTheDragoon extends Equipment {
	private static final String ID = "spiritOfTheDragoon";
	private int strength, shield;
	private static final ParticleContainer strPart = new ParticleContainer(Particle.FLAME).count(25).spread(0.5, 0.5).offsetY(1).speed(0.1);
	
	public SpiritOfTheDragoon(boolean isUpgraded) {
		super(ID, "Spirit of the Dragoon", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 30, 15, 0));
		shield = isUpgraded ? 20 : 15;
		strength = isUpgraded ? 5 : 3;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		SpiritOfTheDragoonInstance inst = new SpiritOfTheDragoonInstance(p, this, slot, es);
		data.addTrigger(id, bind, inst);
		
		data.addTrigger(id, Trigger.FALL_DAMAGE, (pdata, in) -> {
			strPart.play(p, p);
			Sounds.fire.play(p, p);
			data.applyStatus(StatusType.STRENGTH, data, strength, -1);
			
			return TriggerResult.of(false, System.currentTimeMillis() - inst.lastCast < 5000);
		});
	}
	
	private class SpiritOfTheDragoonInstance extends EquipmentInstance {
		private long lastCast = -1;
		public SpiritOfTheDragoonInstance(Player p, Equipment eq, int slot, EquipSlot es) {
			super(p, eq, slot, es);
			action = (pdata, in) -> {
				pdata.addSimpleShield(p.getUniqueId(), shield, 100);
				p.setVelocity(p.getVelocity().add(new Vector(0, 1, 0)));
				Sounds.jump.play(p, p);
				lastCast = System.currentTimeMillis();
				return TriggerResult.keep();
			};
		}
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FEATHER,
				"Passively gain " + GlossaryTag.STRENGTH.tag(this, strength, true) + " upon taking fall damage, even if negated. " +
				"On cast, jump into the air and gain " + GlossaryTag.SHIELDS.tag(this, shield, true) + " for <white>5</white> seconds.");
	}
}
