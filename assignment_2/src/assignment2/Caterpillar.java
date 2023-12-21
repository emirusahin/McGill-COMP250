package assignment2;

import java.awt.Color;
import java.util.Random;
import java.util.Stack;

import assignment2.food.*;


public class Caterpillar {
	// All the fields have been declared public for testing purposes
	public Segment head;
	public Segment tail;
	public int length;
	public EvolutionStage stage;

	public Stack<Position> positionsPreviouslyOccupied;
	public int goal;
	public int turnsNeededToDigest;


	public static Random randNumGenerator = new Random(1);


	// Creates a Caterpillar with one Segment. It is up to students to decide how to implement this.
	public Caterpillar(Position p, Color c, int goal) {
		/*
		 * TODO: ADD YOUR CODE HERE +
		 */
		this.length = 1;
		this.head = new Segment(p, c);
		this.tail = this.head;
		this.goal = goal;
		this.stage = EvolutionStage.FEEDING_STAGE;
		this.positionsPreviouslyOccupied = new Stack<>();
		this.turnsNeededToDigest = 0;
	}

	public EvolutionStage getEvolutionStage() {
		return this.stage;
	}

	public Position getHeadPosition() {
		return this.head.position;
	}

	public int getLength() {
		return this.length;
	}


	// returns the color of the segment in position p. Returns null if such segment does not exist
	public Color getSegmentColor(Position p) {
		/*
		 * TODO: ADD YOUR CODE HERE
		 */
		Segment cur = this.head;
		while (cur != null) {
			if (cur.position.equals(p)) {
				return cur.color;
			}
			cur = cur.next;
		}
		return null;
	}


	// shift all Segments to the previous Position while maintaining the old color
	public void move(Position p) {
		/*
		 * TODO: ADD YOUR CODE HERE
		 *  1. if position is unreachable raise: IllegalArgumentException
		 *  2. if the position is already occupied by catepillars body other than its head, this.state = ENTANGLED
		 *  3. if all is fine and catepillar can move: change header position to p,
		 *  and itirate through the stack (singly linked list) starting at the tail.next?, i.pos = iPrevious.pos
		 */
		// Check if the newPosition is adjacent to the head's position
		// This logic depends on your game's rules. Here's a simple example:
		if (Math.abs(p.getX() - head.position.getX()) > 1 ||
				Math.abs(p.getY() - head.position.getY()) > 1) {
			throw new IllegalArgumentException("Move to position " + p + " is illegal.");
		}

		// Check for collision with its own body
		Segment current = head;
		while (current != null) {
			if (current.position.equals(p)) {
				this.stage = EvolutionStage.ENTANGLED;
				return;
			}
			current = current.next;
		}

		// Move the caterpillar
		positionsPreviouslyOccupied.push(new Position(head.position.getX(), head.position.getY()));
		current = head;
		Position previousPosition = p;
		Position tempPosition;

		while (current != null) {
			tempPosition = current.position;
			current.position = previousPosition;
			previousPosition = tempPosition;
			current = current.next;
		}

		// Handle growth if digesting a cake
		if (this.turnsNeededToDigest > 0) {
			Color randomColor = GameColors.SEGMENT_COLORS[randNumGenerator.nextInt(GameColors.SEGMENT_COLORS.length)];
			Segment newSegment = new Segment(positionsPreviouslyOccupied.pop(), randomColor);
			this.tail.next = newSegment;
			this.tail = newSegment;
			this.length++;
			this.turnsNeededToDigest--;

			if (this.length == this.goal) {
				this.stage = EvolutionStage.BUTTERFLY;
			}
		}

		// If the caterpillar has finished digesting and is not a butterfly, resume feeding stage
		if (this.turnsNeededToDigest == 0 && this.stage != EvolutionStage.BUTTERFLY) {
			this.stage = EvolutionStage.FEEDING_STAGE;
		}
	}


	// a segment of the fruit's color is added at the end
	public void eat(Fruit f) {
		/*
		 * TODO: ADD YOUR CODE HERE +
		 */
        this.tail.next = new Segment(positionsPreviouslyOccupied.pop(), f.getColor());
		this.length += 1;
	}

	// the caterpillar moves one step backwards because of sourness
	public void eat(Pickle p) {
		/*
		 * TODO: ADD YOUR CODE HERE +
		 *  retrace its steps
		 *  starting at the head, iterate i.pos = i.next.pos, until i.pos = tail
		 *  tail.pos = positionsPreviouslyOccupied.pop()
		 */
		Segment cur = this.head;

		while (cur.next != null) {
			Segment cur2 = cur.next;
			cur.position = cur2.position;
			cur = cur.next;
		}
		tail.position = this.positionsPreviouslyOccupied.pop();
	}


	// all the caterpillar's colors shuffles around
	public void eat(Lollipop lolly) {
		/*
		 * TODO: ADD YOUR CODE HERE
		 *  create an array with length = this.length
		 *  iterate through the segment and cur.color = colorArray[i]
		 *  cur = cur.next and i += 1
		 */

		int n = this.length;
		Color[] colors = new Color[n];
		Segment cur = this.head;

		for (int i = 0; i < n; i++) {
			colors[i] = cur.color;
			cur = cur.next;
		}

		for (int i = n - 1; i > 0; i--) {
			int j = randNumGenerator.nextInt(i + 1);
			Color temp = colors[i];
			colors[i] = colors[j];
			colors[j] = temp;
		}

		cur = this.head;
		for (int i = 0; i < n; i++) {
			cur.color = colors[i];
			cur = cur.next;
		}
	}

	// brain freeze!!
	// It reverses and its (new) head turns blue
	public void eat(IceCream gelato) {
		/*
		 * TODO: ADD YOUR CODE HERE +
		 *  do a flip ie. head becomes tail tail becomes had
		 *  empty the this.previouslyPos
		 */
		Stack<Segment> segmentStack = new Stack<>();

		// Traverse the caterpillar and push each segment onto the stack
		Segment cur = this.head;
		while (cur != null) {
			segmentStack.push(cur);
			cur = cur.next;
		}

		// Pop each segment from the stack and reassign the pointers
		this.head = segmentStack.pop();
		cur = this.head;
		while (!segmentStack.isEmpty()) {
			cur.next = segmentStack.pop();
			cur = cur.next;
		}
		cur.next = null; // Set the next pointer of new tail to null
		this.tail = cur;

		// Setting the head segment's color and clearing the positions previously occupied
		this.head.color = GameColors.BLUE; // Adjusted to expected RGB values
		this.positionsPreviouslyOccupied.clear();

	}


	// the caterpillar embodies a slide of Swiss cheese loosing half of its segments.
	public void eat(SwissCheese cheese) {
		// Check if the caterpillar has less than two segments, in which case no action is needed.
		if (this.head == null || this.head.next == null) {
			return;
		}

		Segment current = this.head;
		int count = 0;

		// Loop through the caterpillar, removing every other segment starting from the second.
		while (current != null && current.next != null) {
			// Remove the next segment (every other starting from the second)
			current.next = current.next.next;

			// Update the current segment to the next one in the new sequence
			current = current.next;

			// Update the tail if necessary
			if (current != null && current.next == null) {
				this.tail = current;
			}

			// Push the position of the removed segment onto the stack
			if (current != null && count % 2 == 0) {
				this.positionsPreviouslyOccupied.push(current.position);
			}

			count++;
		}

		// Update the length of the caterpillar
		this.length = (this.length + 1) / 2;
	}



	public void eat(Cake cake) {
		/*
		 * TODO: ADD YOUR CODE HERE
		 */
		int energy = Math.min(cake.getEnergyProvided(), positionsPreviouslyOccupied.size());
		this.turnsNeededToDigest = cake.getEnergyProvided() - energy;

		for (int i = 0; i < energy; i++) {
			Position newPos = positionsPreviouslyOccupied.pop();
			int randomIndex = randNumGenerator.nextInt(GameColors.SEGMENT_COLORS.length);
			Color newColor = GameColors.SEGMENT_COLORS[randomIndex];
			Segment newSegment = new Segment(newPos, newColor);

			this.tail.next = newSegment;
			this.tail = newSegment;
			this.length++;

			if (this.length == this.goal) {
				this.stage = EvolutionStage.BUTTERFLY;
				return;
			}
		}

		if (this.turnsNeededToDigest == 0 && this.stage != EvolutionStage.BUTTERFLY) {
			this.stage = EvolutionStage.FEEDING_STAGE;
		}
	}


	// This nested class was declared public for testing purposes
	// segments are basically the same as nodes
	public class Segment {
		private Position position;
		private Color color;
		private Segment next;

		public Segment(Position p, Color c) {
			this.position = p;
			this.color = c;
		}

	}


	public String toString() {
		Segment s = this.head;
		String gus = "";
		while (s!=null) {
			String coloredPosition = GameColors.colorToANSIColor(s.color) +
					s.position.toString() + GameColors.colorToANSIColor(Color.WHITE);
			gus = coloredPosition + " " + gus;
			s = s.next;
		}
		return gus;
	}

}



