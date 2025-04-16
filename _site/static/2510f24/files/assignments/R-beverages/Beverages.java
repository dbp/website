import tester.Tester;


// Represents a drink you might get at a coffee shop
interface IBeverage {

  boolean isDecaf();

  boolean containsIngredient(String ingredient);

  String format();
}

// Represents a bubble-tea drink, with various mixins
class BubbleTea implements IBeverage {

  String variety; // Black tea, Oolong, Green tea, etc.

  int size; // in ounces

  ILoString mixins; // boba, extra sugar, milk, etc

  BubbleTea(String variety, int size, ILoString mixins) {
    this.variety = variety;
    this.size = size;
    this.mixins = mixins;
  }

  public boolean isDecaf() {
    return false;
  }

  public boolean containsIngredient(String ingredient) {
    return false;
  }

  public String format() {
    return "";
  }
}


// Represents any coffee-based drink
class Coffee implements IBeverage {

  String variety; // Arabica, Robusta, Excelsa or Liberica

  String style; // americano, demitasse, espresso, etc.

  boolean isIced; // whether it's cold or hot

  ILoString mixins; // cream, sugar, flavored syrup, etc.

  Coffee(String variety, String style, boolean isIced, ILoString mixins) {
    this.variety = variety;
    this.style = style;
    this.isIced = isIced;
    this.mixins = mixins;
  }

  public boolean isDecaf() {
    return false;
  }

  public boolean containsIngredient(String ingredient) {
    return false;
  }

  public String format() {
    return "";
  }
}

// Represents an ice-cream-based blended drink
class Milkshake implements IBeverage {
  
  String flavor;  // vanilla, mint-chip, strawberry, etc.

  String brandName; // Ben&Jerrys, JPLicks, etc.

  int size; // in ounces

  ILoString toppings; // whipped cream, sprinkles, cookie crumbles, etc.

  Milkshake(String flavor, String brandName, int size, ILoString toppings) {
    this.flavor = flavor;
    this.brandName = brandName;
    this.size = size;
    this.toppings = toppings;
  }

  public boolean isDecaf() {
    return false;
  }

  public boolean containsIngredient(String ingredient) {
    return false;
  }

  public String format() {
    return "";
  }
}


// lists of strings
interface ILoString {}

// an empty list of strings
class MtLoString implements ILoString {}

// a non-empty list of strings
class ConsLoString implements ILoString {
  String first;
  ILoString rest;
  
  ConsLoString(String first, ILoString rest) {
    this.first = first;
    this.rest = rest;
  }
}

class ExamplesBeverages {}