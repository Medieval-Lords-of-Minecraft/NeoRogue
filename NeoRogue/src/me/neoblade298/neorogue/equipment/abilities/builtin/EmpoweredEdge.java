package me.neoblade298.neorogue.equipment.abilities.builtin;

import org.bukkit.Material;
import me.neoblade298.neorogue.equipment.Ability;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.Trigger;
import me.neoblade298.neorogue.player.TriggerAction;
import me.neoblade298.neorogue.session.instance.FightData;

public class EmpoweredEdge extends Ability {
	
	public EmpoweredEdge(boolean isUpgraded) {
		super("empoweredEdge", isUpgraded, Rarity.COMMON);
		cooldown = isUpgraded ? 5 : 7;
		int damage = isUpgraded ? 100 : 75;
		item = Ability.createItem(this, Material.FLINT, "Empowered Edge",
				null, "&7On cast, your next basic attack deals &e" + damage + " &7damage.");
	}

	@Override
	public void initialize(FightData data) {
		data.addTrigger(Trigger.LEFT_CLICK_HIT, new EmpoweredEdgeAction());
	}
	
	private class EmpoweredEdgeAction implements TriggerAction {
		int count = 0;
		public boolean run(Object[] inputs) {
			((Entity) inputs[1]).damage(5);
		}
	}
}
