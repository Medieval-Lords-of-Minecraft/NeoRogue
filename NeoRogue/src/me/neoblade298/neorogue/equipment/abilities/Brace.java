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
import me.neoblade298.neorogue.equipment.Usable;
import me.neoblade298.neorogue.equipment.UsableInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class Brace extends Ability {
	private int shields;
	private ParticleContainer pc = new ParticleContainer(Particle.CLOUD);
	
	public Brace(boolean isUpgraded) {
		super("brace", "Brace", isUpgraded, Rarity.COMMON, EquipmentClass.WARRIOR);
		setBaseProperties(20, 0, 50); //50
		shields = isUpgraded ? 30 : 20;
		pc.count(10).spread(0.5, 0.5).speed(0.2);
		addReforgeOption("brace", new String[] {"brace2", "parry", "bide"});
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, int slot) {
		data.addTrigger(id, bind, new BraceInstance(this, p));
	}
	
	private class BraceInstance extends UsableInstance {
		private Player p;
		public BraceInstance(Usable u, Player p) {
			super(u);
			this.p = p;
		}

		@Override
		public TriggerResult run(PlayerFightData data, Object[] inputs) {
			pc.spawn(p);
			data.addShield(p.getUniqueId(), shields, true, 100, 100, 0, 1);
			Util.playSound(p, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F, false);
			return TriggerResult.keep();
		}
	}

	@Override
	public void setupItem() {
		item = createItem(this, Material.FLINT, null,
				"On cast, gain <yellow>" + shields + " </yellow>shields for 5 seconds.");
	}
}