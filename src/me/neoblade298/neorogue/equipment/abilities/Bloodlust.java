package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neorogue.DescUtil;
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

public class Bloodlust extends Equipment {
	private static final String ID = "Bloodlust";
	private int strength;
	private static final int CUTOFF = 15;
	private static final ParticleContainer pc = new ParticleContainer(Particle.FLAME).count(20).speed(0.01).offsetY(1);
	
	public Bloodlust(boolean isUpgraded) {
		super(ID, "Bloodlust", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR,
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(10, 20, 0, 0));
		strength = isUpgraded ? 15 : 10;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (pdata, in) -> {
			Sounds.equip.play(data.getPlayer(), data.getPlayer());

			data.addTrigger(id, Trigger.KILL, (pdata2, in2) -> {
				Player p = data.getPlayer();
				if (data.getStatus(StatusType.BERSERK).getStacks() < CUTOFF) {
					data.applyStatus(StatusType.BERSERK, data, 1, -1);
				}
				else {
					pc.play(p, p);
					Sounds.fire.play(p, p);
					data.applyStatus(StatusType.STRENGTH, data, strength, -1);
				}
				return TriggerResult.keep();
			});

			return TriggerResult.remove();
		}));
	}

	@Override
	public void setupItem() {
		item = createItem(Material.REDSTONE_ORE,
				GlossaryTag.POWER.tag(this) + ". On kill, if below " + DescUtil.white(CUTOFF) + " stacks of " + GlossaryTag.BERSERK.tag(this) + ", gain " + GlossaryTag.BERSERK.tag(this, 1, false)
				+ ". Otherwise, gain " + GlossaryTag.STRENGTH.tag(this, strength, true) + ".");
	}
}
