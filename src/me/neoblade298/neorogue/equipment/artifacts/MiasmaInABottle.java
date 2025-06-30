package me.neoblade298.neorogue.equipment.artifacts;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;

import me.neoblade298.neorogue.equipment.Artifact;
import me.neoblade298.neorogue.equipment.ArtifactInstance;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.player.PlayerSessionData;
import me.neoblade298.neorogue.player.inventory.GlossaryTag;
import me.neoblade298.neorogue.session.fight.DamageStatTracker;
import me.neoblade298.neorogue.session.fight.DamageType;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.FightInstance;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.TargetHelper;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetProperties;
import me.neoblade298.neorogue.session.fight.TargetHelper.TargetType;
import me.neoblade298.neorogue.session.fight.status.Status.StatusType;
import me.neoblade298.neorogue.session.fight.trigger.PriorityAction;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class MiasmaInABottle extends Artifact {
	private static final String ID = "miasmaInABottle";
	private static final TargetProperties tp = TargetProperties.radius(5, false, TargetType.ENEMY);
	private double damage = 0.1;

	public MiasmaInABottle() {
		super(ID, "Miasma in a Bottle", Rarity.UNCOMMON, EquipmentClass.THIEF);
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, ArtifactInstance ai) {
		data.addTrigger(ID, Trigger.PLAYER_TICK, new MiasmaInABottleInstance(ID, this));
	}
	
	private class MiasmaInABottleInstance extends PriorityAction {
		private int timer = 5;
		
		public MiasmaInABottleInstance(String id, Equipment eq) {
			super(id);
			action = (pdata, in) -> {
				if (--timer > 0) return TriggerResult.keep();
				for (LivingEntity ent : TargetHelper.getEntitiesInRadius(pdata.getPlayer(), tp)) {
					FightData fd = FightInstance.getFightData(ent);
					if (!fd.hasStatus(StatusType.INSANITY)) continue;
					double add = fd.getStatus(StatusType.INSANITY).getStacks() * 0.1;
					FightInstance.dealDamage(pdata, DamageType.DARK, add, ent, DamageStatTracker.of(id, eq));
				}
				return TriggerResult.keep();
			};
		}
	}

	@Override
	public void onAcquire(PlayerSessionData data, int amount) {
		
	}

	@Override
	public void onInitializeSession(PlayerSessionData data) {
		
	}

	@Override
	public void setupItem() {
		item = createItem(Material.POTION, 
				"Every <white>5</white> seconds, enemies within <white>5</white> blocks of you take " + GlossaryTag.DARK.tag(this, damage, false)
				+ " damage for every stack of " + GlossaryTag.INSANITY.tag(this) + " they have.");
		PotionMeta pm = (PotionMeta) item.getItemMeta();
		pm.setColor(Color.fromRGB(139, 69, 19));
		item.setItemMeta(pm);
	}
}
