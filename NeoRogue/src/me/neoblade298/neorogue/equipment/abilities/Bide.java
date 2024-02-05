package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.EquipmentProperties;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.status.Status.GenericStatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Bide extends Equipment {
	private int shields, berserk, duration;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			bpc = new ParticleContainer(Particle.FLAME);
	
	public Bide(boolean isUpgraded) {
		super("bide", "Bide", isUpgraded, Rarity.UNCOMMON, EquipmentClass.WARRIOR, 
				EquipmentType.ABILITY, EquipmentProperties.ofUsable(0, 20, 10, 0));
		shields = 50;
		duration = 5;
		berserk = isUpgraded ? 3 : 2;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		bpc.count(20).spread(0.5, 0.5).speed(0.1);
		
		addTags(GlossaryTag.SHIELDS, GlossaryTag.BERSERK);
	}

	@Override
	public void setupItem() {
		item = createItem(Material.FLINT,
				"On cast, gain <white>" + shields + "</white> " + GlossaryTag.SHIELDS.tag(this) + " for " + duration + " seconds. During this time, "
						+ "taking damage grants you <yellow>" + berserk + "</yellow> " + GlossaryTag.BERSERK.tag(this) + " stacks.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		data.addTrigger(id, bind, new PriorityAction((fd, in) -> {
			data.addShield(p.getUniqueId(), shields, true, duration * 20, 100, 0, 1);
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new BideInstance(p));
			return TriggerResult.keep();
		}));
	}
	
	private class BideInstance extends PriorityAction {
		private long createTime;
		public BideInstance(Player p) {
			super();
			createTime = System.currentTimeMillis();
			action = (data, inputs) -> {
				if (System.currentTimeMillis() - createTime > 5000) return TriggerResult.remove();
				bpc.spawn(p);
				data.applyStatus(GenericStatusType.BASIC, "BERSERK", p.getUniqueId(), berserk, -1);
				Util.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, false);
				return TriggerResult.keep();
			};
		}
		
	}
}
