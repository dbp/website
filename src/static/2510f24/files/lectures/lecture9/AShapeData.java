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
              +--------+                    
              | IShape |                    
              +--------+
                  |                                    
                 / \                                   
                 ---                                   
                  |                                    
            +--------------+                    
            | AShape       |                    
            +--------------+
+-----------| CartPt loc   | 
|           | String color | 
|           +--------------+
|                  |                                    
|                 / \                                   
|                 ---                                   
|                  |                                    
|      --------------------------------
|      |                |             |
| +------------+  +----------+  +------------+
| | Circle     |  | Square   |  | Rect       |
| +------------+  +----------+  +------------+ 
| | int radius |  | int size |  | int width  | 
| +------------+  +----------+  | int height |
|                               +------------+   
+----+
     | 
     v                                
  +--------+
  | CartPt |
  +--------+
  | int x  |
  | int y  |
  +--------+
 */

interface IShape {
    
}

// to represent a geometric shape
abstract class AShape implements IShape {
    CartPt loc;
    String color;
    
    AShape(CartPt loc, String color) {
        this.loc = loc;
        this.color = color;
    }
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
     ... this.methodName() ...                  -- ??? 
     Methods for fields:
     ... this.loc.distTo0() ...           -- double 
     ... this.loc.distTo(CartPt) ...      -- double 
     */
    
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
     ... this.loc ...              -- CartPt
     ... this.size ...             -- int
     ... this.color ...            -- String
     Methods:
     ... this.methodName() ...             -- ?? 
     Methods for fields:
     ... this.loc.distTo0() ...            -- double 
     ... this.loc.distTo(CartPt) ...       -- double 
     */
    
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
     ... this.methodName() ...         -- ?? 
     METHODS FOR FIELDS:
     ... this.loc.distTo0() ...        -- double
     ... this.loc.distTo(CartPt) ...   -- double
     */
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
    
}
