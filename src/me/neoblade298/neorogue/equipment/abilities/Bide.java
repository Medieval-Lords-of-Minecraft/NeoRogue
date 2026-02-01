package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentInstance;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Bide extends Equipment {
	private static final String ID = "Bide";
	private int shields, berserk, duration;
	private static final ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			bpc = new ParticleContainer(Particle.FLAME);
	private static final SoundContainer sc = new SoundContainer(Sound.ITEM_ARMOR_EQUIP_CHAIN),
			strengthGain = new SoundContainer(Sound.ENTITY_BLAZE_SHOOT);
	
	public Bide(boolean isUpgraded) {
		super(ID, "Bide", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, 
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 25, 10, 0));
		shields = 50;
		duration = 5;
		berserk = isUpgraded ? 3 : 2;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		bpc.count(20).spread(0.5, 0.5).speed(0.1);
		
		addTags(GlossaryTag.SHIELDS, GlossaryTag.BERSERK);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.BLAZE_POWDER,
				"On cast, gain <white>" + shields + "</white> " + GlossaryTag.SHIELDS.tag(this) + " for " + duration + " seconds. During this time, "
						+ "taking damage grants you <yellow>" + berserk + "</yellow> " + GlossaryTag.BERSERK.tag(this) + " stacks.");
	}

	@Override
	public void initialize(PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new EquipmentInstance(data, this, slot, es, (fd, in) -> {
			Player p = data.getPlayer();
			data.addSimpleShield(p.getUniqueId(), shields, duration * 20);
			sc.play(p, p);
			data.addTrigger(id, Trigger.PRE_RECEIVE_DAMAGE, new BideInstance(p, id));
			return TriggerResult.keep();
		}));
	}
	
	private class BideInstance extends PriorityAction {
		private long createTime;
		public BideInstance(Player p, String id) {
			super(id);
			createTime = System.currentTimeMillis();
			action = (data, inputs) -> {
				if (System.currentTimeMillis() - createTime > 5000) return TriggerResult.remove();
				bpc.play(p, p);
				data.applyStatus(StatusType.BERSERK, data, berserk, -1);
				strengthGain.play(p, p);
				return TriggerResult.keep();
			};
		}
		
	}
}
