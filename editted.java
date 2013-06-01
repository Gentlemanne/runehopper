import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import org.powerbot.core.event.events.MessageEvent;
import org.powerbot.core.event.listeners.MessageListener;
import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.map.TilePath;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;

@Manifest(authors = { "GentleManne" }, description = "Mining and banking and coming back", name = "edited Miner/Banker")
public class editted extends ActiveScript implements PaintListener,
		MessageListener {

	private static final Tile[] BANK_TILES_1 = new Tile[] { 
		new Tile(2871, 10240, 0), new Tile(2870,10237, 0),
			new Tile(2867, 10234, 0), new Tile(2864, 10232, 0),
			new Tile(2861, 10231, 0), new Tile(2857, 10230, 0),
			new Tile(2855, 10226, 0), new Tile(2854, 10221, 0),
			new Tile(2850, 10218, 0), new Tile(2846, 10215, 0),
			new Tile(2839, 10209, 0) };
	private static final Tile[] BANK_TILES_2 = new Tile[] {
			 new Tile(2869, 10248, 0),
			new Tile(2865, 10232, 0), new Tile(2855, 10230, 0),
			new Tile(2854, 10222, 0), new Tile(2848, 10216, 0),
			new Tile(2843, 10211, 0), new Tile(2838, 10208, 0) };
	private static final Tile[] BANK_TILES_3 = new Tile[] {
			 new Tile(2869, 10248, 0),
			new Tile(2868, 10232, 0), new Tile(2866, 10224, 0),
			new Tile(2863, 10220, 0), new Tile(2857, 10218, 0),
			new Tile(2856, 10214, 0), new Tile(2852, 10212, 0),
			new Tile(2846, 10210, 0), new Tile(2838, 10208, 0) };

	private static final Tile BANK_TILE = new Tile(2839, 10209, 0);
	private static final Tile MINE_TILE = new Tile(2871, 10240, 0);
	
	private static final TilePath BANK_TILE_PATH_1 = new TilePath(BANK_TILES_1);
	private static final TilePath BANK_TILE_PATH_2 = new TilePath(BANK_TILES_2);
	private static final TilePath BANK_TILE_PATH_3 = new TilePath(BANK_TILES_3);

	private static final TilePath MINE_TILE_PATH_1 = BANK_TILE_PATH_1.reverse();
	private static final TilePath MINE_TILE_PATH_2 = BANK_TILE_PATH_2.reverse();
	private static final TilePath MINE_TILE_PATH_3 = BANK_TILE_PATH_3.reverse();

	private static final Area BANK_AREA = new Area(new Tile(2841, 10212, 0),
			new Tile(2836, 10212, 0), new Tile(2836, 10207, 0), new Tile(2841, 10207, 0));
	private static final Area MINE_AREA = new Area(new Tile(2873, 10239, 0),
			new Tile(2873, 10262, 0), new Tile(2866, 10262, 0), new Tile(2866,
					10239, 0));

	private static final int[] GOLD_ROCKS = { 45067, 45068 };
	private static final int[] ADAM_ROCKS = { 29233, 29235 };
	public int gold;
	public int adam;
	public int teleport;
	static boolean banked = false;

	public static final Timer RUN_TIME = new Timer(0);
	public static int START_LVL;
	public static int START_XP;
	private static final Timer timer = new Timer(0);

	public void onStart() {
		START_LVL = Skills.getRealLevel(Skills.MINING);
		START_XP = Skills.getExperience(Skills.MINING);
	}

	class Banking extends Node {

		@Override
		public boolean activate() {
			return BANK_AREA.contains(Players.getLocal()) && Inventory.isFull();
		}

		@Override
		public void execute() {
			Bank.open();
			Bank.depositInventory();
			if(Bank.getItem(20407) != null) {
				Bank.withdraw(20407, 1);
			}
			else if(Bank.getItem(20407) == null) {
				Bank.withdraw(20406, 1);
		}
		}
	}

	class Mining extends Node {

		@Override
		public boolean activate() {
			return !Inventory.isFull()
					&& Players.getLocal().getAnimation() == -1
					&& !Players.getLocal().isMoving()
					&& MINE_AREA.contains(Players.getLocal());
		}

		@Override
		public void execute() {
			SceneObject rock = SceneEntities.getNearest(ADAM_ROCKS);
			if (rock == null) {
				rock = SceneEntities.getNearest(GOLD_ROCKS);
			}
			if (rock != null) {
				if (rock.isOnScreen()) {
					if (rock.interact("Mine")) {
						sleep(1000);
					}
				} 
			}
			if((rock != null) && !rock.isOnScreen()) {
				Camera.turnTo(rock);
			}
		}
	}

	class walkToBank extends Node {

		@Override
		public boolean activate() {
			return Inventory.isFull()
					&& !BANK_AREA.contains(Players.getLocal());
		}

		@Override
		public void execute() {
			
			if(Walking.walk(BANK_TILE)) {
			    final Timer timer = new Timer(Random.nextInt(3500, 4500));
			    while(timer.isRunning() && Calculations.distanceTo(Walking.getDestination()) > 6) {
			        Task.sleep(80, 120);
			    }
			}
//			Walking.walk(BANK_TILE);
//			int o = 1;
//			switch (o) {
//			case 1:
//				BANK_TILE_PATH_1.traverse();
//				break;
//			case 2:
//				BANK_TILE_PATH_2.traverse();
//				break;
//			case 3:
//				BANK_TILE_PATH_3.traverse();
//				break;
//			}
		}
	}

	class walkToMine extends Node {

		@Override
		public boolean activate() {
			return !Inventory.isFull()
					&& !MINE_AREA.contains(Players.getLocal()) && (Inventory.contains(20406) || Inventory.contains(20407));
		}

		@Override
		public void execute() {
			
			if(Walking.walk(MINE_TILE)) {
			    final Timer timer = new Timer(Random.nextInt(3500, 4500));
			    while(timer.isRunning() && Calculations.distanceTo(Walking.getDestination()) > 6) {
			        Task.sleep(80, 120);
			    }
			}
//			Walking.walk(MINE_TILE);
//			int o = 1;
//			switch (o) {
//			case 1:
//				MINE_TILE_PATH_1.traverse();
//				break;
//			case 2:
//				MINE_TILE_PATH_2.traverse();
//				break;
//			case 3:
//				MINE_TILE_PATH_3.traverse();
//				break;
//			}
		}
	}

	class Dourn extends Node {

		@Override
		public boolean activate() {
			return Inventory.contains(20408);
		}

		@Override
		public void execute() {
			Item full = Inventory.getItem(20408);
			full.getWidgetChild().interact("Teleport urn");
		}
	}
	
	class Geturn extends Node {

		@Override
		public boolean activate() {
			return !Inventory.contains(20406) && !Inventory.contains(20407) && !Inventory.contains(20408);
		}

		@Override
		public void execute() {
			
			if(!BANK_AREA.contains(Players.getLocal())) {
				if(Walking.walk(BANK_TILE)) {
				    final Timer timer = new Timer(Random.nextInt(3500, 4500));
				    while(timer.isRunning() && Calculations.distanceTo(Walking.getDestination()) > 6) {
				        Task.sleep(80, 120);
				    }
				}
			}
//			Walking.walk(BANK_TILE);
			Bank.open();
			Item urny = Bank.getItem(20407);
			Bank.depositInventory();
			if(urny != null) {
				Bank.withdraw(20407, 1);
			}
			else if(urny == null) {
				Bank.withdraw(20406, 1);
			}
			Bank.close();
		}
	}
	
	
	Node[] nodes = { new Geturn(), new Dourn(), new Mining(), new walkToBank(), new Banking(), new walkToMine() };

	@Override
	public int loop() {
		for (Node node : nodes) {
			if (node.activate()) {
				node.execute();
				return 50;
			}
		}
		return 50;
	}

	public void messageReceived(MessageEvent m) {
		String s = m.getMessage();
		if (s.contains("some gold"))
			gold++;
		if (s.contains("some adamantite"))
			adam++;
		if(s.contains("teleported"))
			teleport++;
	}

	@Override
	public void onRepaint(Graphics G1) {

		final Point P = Mouse.getLocation();
		final Graphics2D G = (Graphics2D) G1;
		final int CurXp = Skills.getExperience(Skills.MINING);
		final int CurXpGain = CurXp - START_XP;
		final int CurLvl = Skills.getRealLevel(Skills.MINING);
		final long TTL = (long) ((RUN_TIME.getElapsed() * (Skills.XP_TABLE[CurLvl + 1] - CurXp)) / CurXpGain);

		G.setColor(Color.RED);
		G.drawString(String.format("By GentleManne"), 547, 470);
		G.drawString(String.format("XP gained: " + (gold * 65 + adam * 95 + teleport * 625)),
				547, 485);
		G.drawString(Time.format(TTL), 547, 500);
		G.drawString(
				String.format("Time running: %s", timer.toElapsedString()),
				547, 515);

		G.setColor(Mouse.isPressed() ? Color.YELLOW : Color.GREEN);
		G.drawOval(P.x - 3, P.y - 3, 6, 6);
	}
}