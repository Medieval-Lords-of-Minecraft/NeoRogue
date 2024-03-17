package me.neoblade298.neorogue.session.fight.status;

import java.util.Comparator;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.neoblade298.neocore.shared.util.SharedUtil;
import me.neoblade298.neorogue.session.fight.FightData;
import me.neoblade298.neorogue.session.fight.TickAction;
import net.kyori.adventure.text.Component;

public abstract class Status {
	public static final Status EMPTY = new BasicStatus("EMPTY", null);
	protected String id;
	protected int stacks;
	protected TickAction action;
	protected FightData data;
	protected StatusSliceHolder slices = new StatusSliceHolder();
	protected int seconds;
	
	public static final Comparator<Status> comp = new Comparator<Status>() {
		@Override
		public int compare(Status o1, Status o2) {
			int comp = Integer.compare(o1.stacks, o2.stacks);
			if (comp != 0) return comp;
			return o1.getId().compareTo(o2.getId());
		}
	};
	
	public Status(String id, FightData data) {
		this.id = id;
		this.data = data;
	}
	
	// Setting stacks or status to 0 means they will be untouched
	public abstract void apply(UUID applier, int stacks, int seconds);
	
	public static Status createByType(StatusType id, UUID applier, FightData target) {
		switch (id) {
		case POISON: return new PoisonStatus(target);
		case BLEED: return new BleedStatus(target);
		case BURN: return new DecrementStackStatus(id.name(), target);
		case FROST: return new FrostStatus(target);
		case ELECTRIFIED: return new DecrementStackStatus(id.name(), target);
		case CONCUSSED: return new ConcussedStatus(target);
		case INSANITY: return new DecrementStackStatus(id.name(), target);
		case SANCTIFIED: return new DecrementStackStatus(id.name(), target);
		case THORNS: return new BasicStatus(id.name(), target);
		case REFLECT: return new BasicStatus(id.name(), target);
		case BERSERK: return new BasicStatus(id.name(), target);
		case STRENGTH: return new StrengthStatus(target);
		case INTELLECT: return new IntellectStatus(target);
		}
		Bukkit.getLogger().warning("[NeoRogue] Failed to create status type " + id);
		return new BasicStatus(id.name(), target);
	}
	
	public static Status createByGenericType(GenericStatusType type, String id, UUID applier, FightData target) {
		switch (type) {
		case DECREMENT_STACK: return new DecrementStackStatus(id, target);
		case BASIC: return new BasicStatus(id, target);
		case DURATION: return new DurationStatus(id, target);
		default: return null;
		}
	}
	
	public String getBoardDisplay() {
		try {
			return StatusType.valueOf(id).boardLine + "§7: §f" + stacks;
		}
		catch (IllegalArgumentException ex) {
			return "§f" + id + "§7: §f" + stacks;
		}
	}
	
	public String getHologramLine() {
		return "&e" + id + "&f: " + stacks + (seconds > 0 ? ", " + seconds + "s" : "");
	}
	
	public void cleanup() {
		if (action != null) action.setCancelled(true);
	}
	
	public String getId() {
		return id;
	}
	
	public StatusSliceHolder getSlices() {
		return slices;
	}
	
	public int getStacks() {
		return stacks;
	}
	
	public enum StatusType {
		POISON("<dark_green>Poison</dark_green>", "&2Poison"),
		BLEED("<red>Bleed</red>", "&cBleed"),
		BURN("<gold>Burn</gold>", "&6Burn"),
		FROST("<blue>Frost</blue>", "&9Frost"),
		ELECTRIFIED("<yellow>Electrified</yellow>", "&eElectrified"),
		CONCUSSED("<dark_green>Concussed</dark_green>", "&2Concussed"),
		INSANITY("<dark_purple>Insanity</dark_purple>", "&5Insanity"),
		SANCTIFIED("<white>Sanctified</white>", "&fSanctified"),
		THORNS("<gold>Thorns</gold>", "&6Thorns"),
		REFLECT("<purple>Reflect</purple>", "&dReflect"),
		BERSERK("<dark_red>Berserk</dark_red>", "&4Berserk"),
		STRENGTH("<red>Strength</red>", "&cStrength"),
		INTELLECT("<blue>Intellect</blue>", "&9Intellect");
		public String tag;
		public Component ctag;
		public String boardLine;
		private StatusType(String tag, String boardLine) {
			this.tag = tag;
			this.ctag = SharedUtil.color(tag);
			this.boardLine = boardLine.replaceAll("&", "§");
		}
	}
	public enum GenericStatusType {
		DECREMENT_STACK, BASIC, DURATION;
	}
}
