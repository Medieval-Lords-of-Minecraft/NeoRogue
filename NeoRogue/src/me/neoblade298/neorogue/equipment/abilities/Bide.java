package me.neoblade298.neorogue.equipment.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.particles.ParticleContainer;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.EquipmentClass;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Bide extends Ability {
	private int shields, berserk, duration;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD),
			bpc = new ParticleContainer(Particle.FLAME);
	
	public Bide(boolean isUpgraded) {
		super("bide", "Bide", isUpgraded, Rarity.RARE, EquipmentClass.WARRIOR);
		setBaseProperties(20, 0, 50, 0);
		shields = 50;
		duration = 5;
		berserk = isUpgraded ? 3 : 2;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		bpc.count(20).spread(0.5, 0.5).speed(0.1);
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.FLINT, null,
				"On cast, gain <yellow>" + shields + " </yellow>shields for " + duration + " seconds. During this time, "
						+ "taking damage grants you <yellow>" + berserk + "</yellow> berserk stacks.");
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, bind, (fd, in) -> {
			data.addShield(p.getUniqueId(), shields, true, duration * 20, 100, 0, 1);
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			data.addTrigger(id, Trigger.RECEIVED_DAMAGE, new BideInstance(p));
			return TriggerResult.keep();
		});
	}
	
	private class BideInstance implements TriggerAction {
		private long createTime;
		private Player p;
		public BideInstance(Player p) {
			this.p = p;
			createTime = System.currentTimeMillis();
		}
		@Override
		public TriggerResult trigger(PlayerFightData data, Object[] inputs) {
			if (System.currentTimeMillis() - createTime > 5000) return TriggerResult.remove();
			bpc.spawn(p);
			data.applyStatus("BERSERK", p.getUniqueId(), berserk);
			Util.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F, false);
			return TriggerResult.keep();
		}
		
	}
}