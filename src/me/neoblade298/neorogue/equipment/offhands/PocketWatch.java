package me.neoblade298.neorogue.equipment.offhands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neorogue.Sounds;
import me.neoblade298.neorogue.equipment.Equipment;
import me.neoblade298.neorogue.equipment.Rarity;
import me.neoblade298.neorogue.session.fight.PlayerFightData;
import me.neoblade298.neorogue.session.fight.trigger.Trigger;
import me.neoblade298.neorogue.session.fight.trigger.TriggerAction;
import me.neoblade298.neorogue.session.fight.trigger.TriggerResult;

public class PocketWatch extends Equipment {
	private static final String ID = "pocketWatch";
	private static SoundContainer sc = new SoundContainer(Sound.BLOCK_NOTE_BLOCK_CHIME);
	private static ParticleContainer pc = new ParticleContainer(Particle.PORTAL).count(50).spread(1, 2).offsetY(1);
	private int uses;
	
	public PocketWatch(boolean isUpgraded) {
		super(ID, "Pocket Watch", isUpgraded, Rarity.UNCOMMON, EquipmentClass.CLASSLESS,
				EquipmentType.OFFHAND);
		uses = isUpgraded ? 2 : 1;
	}
	
	public static Equipment get() {
		return Equipment.get(ID, false);
	}

	@Override
	public void initialize(Player p, PlayerFightData data, Trigger bind, EquipSlot es, int slot) {
		PocketWatchInstance inst = new PocketWatchInstance();
		data.addTrigger(ID, Trigger.PLAYER_TICK, inst);
		data.addTrigger(ID, data.getSessionData().getPlayerClass() == EquipmentClass.ARCHER ? Trigger.LEFT_CLICK : Trigger.RIGHT_CLICK, (pdata, in) -> {
			return inst.useWatch(data);
		});
	}
	
	private class PocketWatchInstance implements TriggerAction {
		private Snapshot[] snapshots = new Snapshot[3];
		private boolean active = true;
		private int numUses = 0;

		@Override
		public TriggerResult trigger(PlayerFightData data, Object in) {
			if (!active) return TriggerResult.remove();
			snapshots[2] = snapshots[1];
			snapshots[1] = snapshots[0];
			snapshots[0] = new Snapshot(data);
			return TriggerResult.keep();
		}
		
		public TriggerResult useWatch(PlayerFightData data) {
			Player p = data.getPlayer();
			if (snapshots[2] == null) return TriggerResult.keep();
			snapshots[2].load(data);
			if (++numUses >= uses) {
				Sounds.breaks.play(p, p);
				p.getInventory().setItem(EquipmentSlot.OFF_HAND, null);
				active = false;
				return TriggerResult.remove();
			}
			return TriggerResult.keep();
		}
	}
	
	private class Snapshot {
		private Location loc;
		private double health, mana, stamina;
		
		public Snapshot(PlayerFightData data) {
			save(data);
		}
		
		public void save(PlayerFightData data) {
			Player p = data.getPlayer();
			loc = p.getLocation();
			health = p.getHealth();
			mana = data.getMana();
			stamina = data.getStamina();
		}
		
		public void load(PlayerFightData data) {
			Player p = data.getPlayer();
			p.teleport(loc);
			sc.play(p, p);
			pc.play(p, p);
			Sounds.teleport.play(p, p);
			p.setHealth(health);
			data.setMana(mana);
			data.setStamina(stamina);
		}
	}

	@Override
	public void setupItem() {
		String usesString = isUpgraded ? "Twice" : "Once";
		item = createItem(Material.CLOCK, "<yellow>" + usesString + "</yellow> per fight, right (left for <gold>Archer<gold>) click to" +
				" set your health, mana, stamina, and location to what it was <white>2</white> seconds ago.");
	}
}
