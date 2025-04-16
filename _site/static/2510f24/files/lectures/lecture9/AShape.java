import tester.*;

/**
 * HtDC Lectures
 * Lecture 7: Abstract classes
 * 
 * Copyright 2013 Viera K. Proulx
 * This program is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 * 
 * @since 14 September 2013
 */

/*
                    +----------------------------+                    
                    | IShape                     |                    
                    +----------------------------+                    
                    | double area()              |                    
                    | boolean distTo0()          |                    
                    | IShape grow(int)           |                    
                    | boolean biggerThan(IShape) |                    
                    | boolean contains(CartPt)   |                    
                    +----------------------------+                    
                                   |                                    
                                  / \                                   
                                  ---                                   
                                   |                                    
                    +-----------------------------------+                    
                    | abstract AShape                   |                    
                    +-----------------------------------+ 
+-------------------| CartPt loc                        |
|                   | String color                      |                   
|                   +-----------------------------------+                    
|                   | abstract double area()            |                    
|                   | boolean distTo0()                 |                    
|                   | abstract IShape grow(int)         |                    
|                   | boolean biggerThan(IShape)        |                    
|                   | abstract boolean contains(CartPt) |                    
|                   +-----------------------------------+                    
|                                  |                                    
|                                 / \                                   
|                                 ---                                   
|                                  |                                    
|                ---------------------------------------------
|                |                              |            |
| +--------------------------+  +--------------------------+ | 
| | Circle                   |  | Square                   | | 
| +--------------------------+  +--------------------------+ | 
| | int radius               |  | int size                 | | 
| +--------------------------+  +--------------------------+ | 
| | double area()            |  | double area()            | | 
| | boolean distTo0()        |  | IShape grow(int)         | |  
| | IShape grow(int)         |  | boolean contains(CartPt) | | 
| | boolean contains(CartPt) |  +--------------------------+ | 
| +--------------------------+                               | 
|                                                            |
|                        +-----------------------------------+
|                        |
|          +--------------------------+ 
|          | Rect                     | 
|          +--------------------------+ 
|          | int width                | 
|          | int height               | 
|          +--------------------------+  
|          | double area()            |
|          | IShape grow(int)         |
|          | boolean contains(CartPt) |  
|          +--------------------------+       
| 
+-------+
        |
        v                               
  +-----------------------+
  | CartPt                |
  +-----------------------+
  | int x                 |
  | int y                 |
  +-----------------------+
  | double distTo0()      |
  | double distTo(CartPt) |
  +-----------------------+
*/


interface IShape {
    
    // to compute the area of this shape
    public double area();
    
    // to compute the distance form this shape to the origin
    public double distTo0();
    
    // to increase the size of this shape by the given increment
    public IShape grow(int inc);
    
    // is the area of this shape is bigger than the area of the given shape?
    public boolean biggerThan(IShape that);
    
    // does this shape (including the boundary) contain the given point?
    public boolean contains(CartPt pt);
}

// to represent a geometric shape
abstract class AShape implements IShape {
    CartPt loc;
    String color;
    
    AShape(CartPt loc, String color) {
        this.loc = loc;
        this.color = color;
    }
    
    // to compute the area of this shape
    public abstract double area();
    
    // to compute the distance form this shape to the origin
    public double distTo0(){
        return this.loc.distTo0();
    }
    
    // to increase the size of this shape by the given increment
    public abstract IShape grow(int inc);
    
    // is the area of this shape is bigger than the area of the given shape?
    public boolean biggerThan(IShape that){
        return this.area() >= that.area();
    }
    
    // does this shape (including the boundary) contain the given point?
    public abstract boolean contains(CartPt pt);
}

// to represent a circle
class Circle extends AShape {
    int radius;
    
    Circle(CartPt center, int radius, String color) {
        super(center, color);
        this.radius = radius;
    }
    
    /*  TEMPLATE 
     Fields:
     ... this.loc ...             -- CartPt
     ... this.rad ...             -- int
     ... this.color ...           -- String
     Methods:
     ... this.area() ...                  -- double 
     ... this.distTo0() ...               -- double
     ... this.grow(int) ...               -- IShape
     ... this.biggerThan(IShape) ...      -- boolean
     ... this.contains(CartPt) ...        -- boolean 
     Methods for fields:
     ... this.loc.distTo0() ...           -- double 
     ... this.loc.distTo(CartPt) ...      -- double 
     */
    
    // to compute the area of this shape
    public double area(){
        return Math.PI * this.radius * this.radius;
    }
    
    // to compute the distance form this shape to the origin
    public double distTo0(){
        return this.loc.distTo0() - this.radius;
    }
    
    // to increase the size of this shape by the given increment
    public IShape grow(int inc){
        return new Circle(this.loc, this.radius + inc, this.color);
    }
    
    // does this shape (including the boundary) contain the given point?
    public boolean contains(CartPt pt){
        return this.loc.distTo(pt) <= this.radius;
    }
    
}

// to represent a square
class Square extends AShape {
    int size;
    
    Square(CartPt nw, int size, String color) {
        super(nw, color);
        this.size = size;
    }
    
    /*  TEMPLATE 
     Fields:
     ... this.loc ...             -- CartPt
     ... this.size ...            -- int
     ... this.color ...           -- String
     Methods:
     ... this.area() ...                  -- double 
     ... this.distTo0() ...               -- double
     ... this.grow(int) ...               -- IShape
     ... this.biggerThan(IShape) ...      -- boolean
     ... this.contains(CartPt) ...        -- boolean 
     Methods for fields:
     ... this.loc.distTo0() ...           -- double 
     ... this.loc.distTo(CartPt) ...      -- double 
     */
    
    // to compute the area of this shape
    public double area(){
        return this.size * this.size;
    }
    
    // to increase the size of this shape by the given increment
    public IShape grow(int inc){
        return new Square(this.loc, this.size + inc, this.color);
    }
    
    // does this shape (including the boundary) contain the given point?
    public boolean contains(CartPt pt){
        return (this.loc.x <= pt.x) && (pt.x <= this.loc.x + this.size) &&
        (this.loc.y <= pt.y) && (pt.y <= this.loc.y + this.size);            
    }
    
    
}

// to represent a rectangle
class Rect extends AShape {
    int width;
    int height;
    
    Rect(CartPt nw, int width, int height, String color) {
        super(nw, color);
        this.width = width;
        this.height = height;
    }
    
    /* TEMPLATE
     FIELDS
     ... this.loc ...             -- CartPt
     ... this.width ...           -- int
     ... this.height ...          -- int
     ... this.color ...           -- String
     METHODS
     ... this.area() ...                  -- double 
     ... this.distTo0() ...               -- double 
     ... this.grow(int) ...               -- IShape
     ... this.biggerThan(IShape) ...      -- boolean
     ... this.contains(CartPt) ...        -- boolean 
     METHODS FOR FIELDS:
     ... this.loc.distTo0() ...           -- double 
     ... this.loc.distTo(CartPt) ...      -- double 
     */
    
    // to compute the area of this shape
    public double area(){
        return this.width * this.height;
    }
    
    // to increase the size of this shape by the given increment
    public IShape grow(int inc){
        return new Rect(this.loc, this.width + inc, this.height + inc, 
                        this.color);
    }
    
    // does this shape (including the boundary) contain the given point?
    public boolean contains(CartPt pt){
        return (this.loc.x <= pt.x) && (pt.x <= this.loc.x + this.width) &&
        (this.loc.y <= pt.y) && (pt.y <= this.loc.y + this.height);            
    }
    
}


// to represent a Cartesian point
class CartPt {
    int x;
    int y;
    
    CartPt(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /* TEMPLATE
     FIELDS
     ... this.x ...          -- int
     ... this.y ...          -- int
     METHODS
     ... this.distTo0() ...        -- double
     ... this.distTo(CartPt) ...   -- double
     */
    
    // to compute the distance form this point to the origin
    public double distTo0(){
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }
    
    // to compute the distance form this point to the given point
    public double distTo(CartPt pt){
        return Math.sqrt((this.x - pt.x) * (this.x - pt.x) + 
                         (this.y - pt.y) * (this.y - pt.y));
    }
}

class ExamplesShapes {
    ExamplesShapes() {}
    
    CartPt pt1 = new CartPt(0, 0);
    CartPt pt2 = new CartPt(3, 4);
    CartPt pt3 = new CartPt(7, 1);
    
    IShape c1 = new Circle(new CartPt(50, 50), 10, "red");
    IShape c2 = new Circle(new CartPt(50, 50), 30, "red");
    IShape c3 = new Circle(new CartPt(30, 100), 30, "blue");
    
    IShape s1 = new Square(new CartPt(50, 50), 30, "red");
    IShape s2 = new Square(new CartPt(50, 50), 50, "red");
    IShape s3 = new Square(new CartPt(20, 40), 10, "green");
    
    IShape r1 = new Rect(new CartPt(50, 50), 30, 20, "red");
    IShape r2 = new Rect(new CartPt(50, 50), 50, 40, "red");
    IShape r3 = new Rect(new CartPt(20, 40), 10, 20, "green");
    
    // test the method distTo0 in the class CartPt
    boolean testDistTo0(Tester t) { 
        return
        t.checkInexact(this.pt1.distTo0(), 0.0, 0.001) &&
        t.checkInexact(this.pt2.distTo0(), 5.0, 0.001);
    }
    
    // test the method distTo in the class CartPt
    boolean testDistTo(Tester t) { 
        return
        t.checkInexact(this.pt1.distTo(this.pt2), 5.0, 0.001) &&
        t.checkInexact(this.pt2.distTo(this.pt3), 5.0, 0.001);
    }
    
    // test the method area in the class Circle
    boolean testCircleArea(Tester t) { 
        return
        t.checkInexact(this.c1.area(), 314.15, 0.01);
    }
    
    // test the method area in the class Square
    boolean testSquareArea(Tester t) { 
        return
        t.checkInexact(this.s1.area(), 900.0, 0.01);
    }
    
    // test the method area in the class Rect
    boolean testRectArea(Tester t) { 
        return
        t.checkInexact(this.r1.area(), 600.0, 0.01);
    }
    
    // test the method distTo0 in the class Circle
    boolean testCircleDistTo0(Tester t) { 
        return
        t.checkInexact(this.c1.distTo0(), 60.71, 0.01) &&
        t.checkInexact(this.c3.distTo0(), 74.40, 0.01);
    }
    
    // test the method distTo0 in the class Square
    boolean testSquareDistTo0(Tester t) { 
        return
        t.checkInexact(this.s1.distTo0(), 70.71, 0.01) &&
        t.checkInexact(this.s3.distTo0(), 44.72, 0.01);
    }  
    
    // test the method distTo0 in the class Rect
    boolean testRectDistTo0(Tester t) { 
        return
        t.checkInexact(this.r1.distTo0(), 70.71, 0.01) &&
        t.checkInexact(this.r3.distTo0(), 44.72, 0.01);
    }
    
    // test the method grow in the class Circle
    boolean testCircleGrow(Tester t) { 
        return
        t.checkExpect(this.c1.grow(20), this.c2);
    }
    
    // test the method grow in the class Square
    boolean testSquareGrow(Tester t) { 
        return
        t.checkExpect(this.s1.grow(20), this.s2);
    }
    
    // test the method grow in the class Rect
    boolean testRectGrow(Tester t) { 
        return
        t.checkExpect(this.r1.grow(20), this.r2);
    }
    
    // test the method biggerThan in the class Circle
    boolean testCircleBiggerThan(Tester t) { 
        return
        t.checkExpect(this.c1.biggerThan(this.c2), false) && 
        t.checkExpect(this.c2.biggerThan(this.c1), true) && 
        t.checkExpect(this.c1.biggerThan(this.s1), false) && 
        t.checkExpect(this.c1.biggerThan(this.s3), true);
    }
    
    // test the method biggerThan in the class Square
    boolean testSquareBiggerThan(Tester t) { 
        return
        t.checkExpect(this.s1.biggerThan(this.s2), false) && 
        t.checkExpect(this.s2.biggerThan(this.s1), true) && 
        t.checkExpect(this.s1.biggerThan(this.c1), true) && 
        t.checkExpect(this.s3.biggerThan(this.c1), false);
    }
    
    // test the method biggerThan in the class Rect
    boolean testRectBiggerThan(Tester t) { 
        return
        t.checkExpect(this.r1.biggerThan(this.r2), false) && 
        t.checkExpect(this.r2.biggerThan(this.r1), true) && 
        t.checkExpect(this.r1.biggerThan(this.c1), true) && 
        t.checkExpect(this.r3.biggerThan(this.s1), false);
    }
    
    // test the method contains in the class Circle
    boolean testCircleContains(Tester t) { 
        return
        t.checkExpect(this.c1.contains(new CartPt(100, 100)), false) && 
        t.checkExpect(this.c2.contains(new CartPt(40, 60)), true);
    }
    
    // test the method contains in the class Square
    boolean testSquareContains(Tester t) { 
        return
        t.checkExpect(this.s1.contains(new CartPt(100, 100)), false) && 
        t.checkExpect(this.s2.contains(new CartPt(55, 60)), true);
    }
    
    // test the method contains in the class Rect
    boolean testRectContains(Tester t) { 
        return
        t.checkExpect(this.r1.contains(new CartPt(100, 100)), false) && 
        t.checkExpect(this.r2.contains(new CartPt(55, 60)), true);
    }
}
