package assignment2;

import assignment2.Caterpillar.Segment;
import assignment2.food.*;

import org.junit.jupiter.api.*;


import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

import java.awt.Color;
import java.util.Random;


public class CaterpillarMinitester {
 private Caterpillar gus;
 private Position initialPosition;
 private Color initialColor;
 private int goalLength;

 @BeforeEach
 public void setUp() {
  initialPosition = new Position(1,1);
  initialColor = GameColors.GREEN;
  goalLength = 10;
  gus = new Caterpillar(initialPosition, initialColor, goalLength);
 }

 public void addSegment(Segment tail, Position p, Color c) {
  // adds a segment to the caterpillar
  try {
   Field tailNext = tail.getClass().getDeclaredField("next");
   tailNext.setAccessible(true);
   Segment toAdd = gus.new Segment(p, c);
   tailNext.set(tail, toAdd);
   gus.tail = toAdd;
   gus.length++;
  } catch (Exception e) {

   e.printStackTrace();

  }
 }

 @Test
 @Tag("score:6")
 void testConstructorAndFeedingStage() {
  assertEquals(initialPosition, gus.getHeadPosition(), "Caterpillar should start at the given position");
  assertEquals(initialColor, gus.getSegmentColor(new Position(1, 1)), "Head segment should be green");
  assertEquals(goalLength, gus.goal, "Goal should match");
  assertEquals(EvolutionStage.FEEDING_STAGE, gus.getEvolutionStage(), "Caterpillar should start in the feeding stage");
  assertEquals(1, gus.getLength(), "Caterpillar should start with one segment");
  assertEquals(gus.head, gus.tail, "The tail should reference the same segment as the head.");
 }


 @Test
 @Tag("score:3")
 void testEatFruit() {
  gus.positionsPreviouslyOccupied.push(new Position(1, 0));
  Fruit fruit = new Fruit(GameColors.RED);
  gus.eat(fruit);

  assertEquals(GameColors.RED, gus.getSegmentColor(new Position(1, 0)), "The color of the new segment should match the fruit");
 }

 @Test
 @Tag("score:3")
 public void testEatPickleRetract() {
  // add a segment to the caterpillar
  addSegment(gus.tail, new Position(1,0), GameColors.RED);
  gus.positionsPreviouslyOccupied.push(new Position(0, 0));

  Pickle pickle = new Pickle();
  gus.eat(pickle);

  assertEquals(new Position(1, 0), gus.getHeadPosition(), "Caterpillar head is not at the given position");
  assertEquals(2, gus.getLength(), "Eating a pickle does not change the length of the caterpillar");
 }

 @Test
 @Tag("score:2")
 void testEatPickleWithOneSegment() {
  gus.positionsPreviouslyOccupied.push(new Position(1, 0));
  Pickle pickle = new Pickle();
  gus.eat(pickle);

  assertEquals(new Position(1,0), gus.getHeadPosition(), "The caterpillar should retrace its steps when tasting a pickle.");
 }

 // Eat a lollipop and verify all segment colors are shuffled.
 @Test
 @Tag("score:6")
 void testEatLollipopWithMultipleSegments() {
  // add two segments
  addSegment(gus.tail, new Position(1,0), GameColors.BLUE);
  addSegment(gus.tail, new Position(0,0), GameColors.RED);

  Caterpillar.randNumGenerator = new Random(12);
  gus.eat(new Lollipop());

  assertEquals(GameColors.BLUE, gus.getSegmentColor(initialPosition), "head segment should be blue");
  assertEquals(GameColors.RED, gus.getSegmentColor(new Position(1,0)), "middle segment should be red");
  assertEquals(GameColors.GREEN, gus.getSegmentColor(new Position(0,0)), "tail segment should be green");
 }

 @Test
 @Tag("score:2")
 public void testEatLollipopWithOneSegment() {

  // Save the initial state
  Color headColorBefore = gus.getSegmentColor(initialPosition);

  // Eat a lollipop
  Lollipop lolly = new Lollipop();
  gus.eat(lolly);

  // Get the state after eating the lollipop
  Color headColorAfter = gus.getSegmentColor(initialPosition);

  // Assert that the caterpillar's color did not change
  assertEquals(headColorBefore, headColorAfter, "Caterpillar color should not change after eating a lollipop with one segment");
 }


 @Test
 @Tag("score:2")
 void testEatIceCreamAndReverse() {
  // add a segment
  addSegment(gus.tail, new Position(1,0), GameColors.RED);

  gus.eat(new IceCream());

  Position headPosition = gus.getHeadPosition();

  assertEquals(GameColors.BLUE, gus.getSegmentColor(headPosition), "The new head segment should turn blue after eating ice cream");
  assertEquals(new Position(1, 0), gus.getHeadPosition(), "Caterpillar head is not at the given position");
 }


 // Eat Swiss cheese and verify every other segment is removed, and there is no gap in between.
 @Test
 @Tag("score:5")
 void testSwissCheese() {
  // add four segments
  addSegment(gus.tail, new Position(1,0), GameColors.RED);
  addSegment(gus.tail, new Position(0,0), GameColors.BLUE);
  addSegment(gus.tail, new Position(0,1), GameColors.ORANGE);
  addSegment(gus.tail, new Position(0,2), GameColors.YELLOW);

  gus.eat(new SwissCheese());


  assertEquals(GameColors.GREEN, gus.getSegmentColor(initialPosition), "head segment should be at x=1, y=1 and be green");
  assertEquals(GameColors.BLUE, gus.getSegmentColor(new Position(1,0)), "middle segment should be at x=1, y=0 and be blue");
  assertEquals(GameColors.YELLOW, gus.getSegmentColor(new Position(0,0)), "tail segment should be at x=0, y=0 and be yellow");
  assertEquals(null, gus.getSegmentColor(new Position(0,1)), "there should be no segment at x=0, y=1");
  assertEquals(null, gus.getSegmentColor(new Position(0,2)), "there should be no segment at x=0, y=2");
 }

 @Test
 @Tag("score:4")
 public void testEatCakeWithSufficientEnergy() {
  // add positions to the stack
  gus.positionsPreviouslyOccupied.push(new Position(0,0));
  gus.positionsPreviouslyOccupied.push(new Position(1,0));



  Cake cake = new Cake(1);
  gus.eat(cake);


  assertEquals(2, gus.getLength(), "Caterpillar should have grown by one segment after eating cake");
  assertNotNull(gus.getSegmentColor(new Position(1,0)), "There should be a segment in position (1,0)");

 }




 @Test
 @Tag("score:4")
 void testMoveAndGrow() {
  gus.move(new Position(1, 2)); // Caterpillar moves to a new position

  assertEquals(new Position(1, 2), gus.getHeadPosition(), "Caterpillar head should be at the new position after moving");
  assertEquals(1, gus.positionsPreviouslyOccupied.size(), "The stack should now contain one position");
  assertEquals(new Position(1,1), gus.positionsPreviouslyOccupied.peek(), "Position (1,1) should be the most recent previously occupied position by the caterpillar");
  assertEquals(1, gus.getLength(), "The length of the caterpillar should not change");
 }

 // When eating cake, verify that the colors of the newly added segments are randomly selected
 @Test
 @Tag("score:3")
 void testEatCakeSegmentColors() {
  // add positions to the stack
  gus.positionsPreviouslyOccupied.push(new Position(1,0));
  gus.positionsPreviouslyOccupied.push(new Position(0,0));
  gus.positionsPreviouslyOccupied.push(new Position(0,1));

  Caterpillar.randNumGenerator = new Random(21);
  gus.eat(new Cake(3));


  assertEquals(GameColors.GREEN, gus.getSegmentColor(new Position(1,1)), "the head color should still be green");
  assertEquals(GameColors.YELLOW, gus.getSegmentColor(new Position(0,1)), "the 2nd segment should be yellow");
  assertEquals(GameColors.ORANGE, gus.getSegmentColor(new Position(0,0)), "the 3rd segment should be orange");
  assertEquals(GameColors.RED, gus.getSegmentColor(new Position(1,0)), "the last segment should be red");
 }
}