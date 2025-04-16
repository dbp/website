import tester.Tester;
import java.awt.Color;
import javalib.worldimages.*;

class Group {
  String title;
  ILoTask tasks;

  Group(String t, ILoTask tasks) {
    this.title = t;
    this.tasks = tasks;
  }

  //rotate the list of tasks in this group
  Group rotate() {
    //stub
    return this;
  }

  //flip the first task to done or not done
  Group flip() {
    //stub
    return this;
  }
	
  //draw the the current state of this group of tasks
  WorldImage draw() {
    return this.tasks.draw();
  }
}


class Task {
  String description;
  boolean isDone;

  Task(String d, boolean isDone) {
    this.description = d;
    this.isDone = isDone;
  }

  //flip the completeness of this task
  Task flip() {
    return this;
  }
	
  //draw this task as text
  WorldImage draw() {
    WorldImage bg = new RectangleImage(300, 200, "solid", Color.cyan);
    bg = new OverlayImage(new TextImage(this.description, 20, Color.black), bg);
    bg = new OverlayOffsetImage(this.drawCheckbox(), 0, -60, bg);
    return bg;
  }
	
  //draw the check box for this task
  WorldImage drawCheckbox() {
    if (this.isDone) {
      return new RectangleImage(20,20, "solid", Color.black);
    }
    else {
      return new RectangleImage(20,20, "outline", Color.black);
    }
  }
}


interface ILoTask {
  //draw the first task's description as text or "No tasks to do" for this ILoTask
  WorldImage draw();
}

class MtLoTask implements ILoTask {

  //render that there are no tasks to do since this list is empty
  public WorldImage draw() {
    return new TextImage("No tasks to do", 30, Color.black);
  }
	
}

class ConsLoTask implements ILoTask {
  Task first;
  ILoTask rest;

  ConsLoTask(Task first, ILoTask rest) {
    this.first = first;
    this.rest = rest;
  }
	
  //draw the first task to do
  public WorldImage draw() {
    return this.first.draw();
  }

}

class Examples {
  Task clean = new Task("clean room", true);
  Task laundry = new Task("do a load of laundry", false);
  Task hw = new Task("work on fundies 2 assignment", false);
  Task lab = new Task("do lab exercises", true);
  Task sleep = new Task("sleep 8 hours", true);
  Task eat = new Task("breakfast", true);
	
  Task dd = new Task("Data Definitions", true);
  Task templ = new Task("Templates", false);
  Task examples = new Task("Examples", false);
  Task methodStubs = new Task("Method Stubs", false);
  Task checkExpects = new Task("Tests", false);
  Task code = new Task("Implement the methods", false);
	

  ILoTask mt = new MtLoTask();
  ILoTask cleaning = new ConsLoTask(this.clean, new ConsLoTask(this.laundry, this.mt));
  ILoTask cleaningRotated = new ConsLoTask(this.laundry, new ConsLoTask(this.clean, this.mt));
  ILoTask school = new ConsLoTask(this.hw, new ConsLoTask(this.lab, this.mt));
  ILoTask health = new ConsLoTask(this.sleep, new ConsLoTask(this.eat, this.mt));

  ILoTask coding = new ConsLoTask(this.checkExpects, new ConsLoTask(this.code, this.mt));
  ILoTask methods = new ConsLoTask(this.examples, new ConsLoTask(this.methodStubs, this.coding));
	
  ILoTask data = new ConsLoTask(this.dd, new ConsLoTask(this.templ, this.methods));
	
  Group houseWork = new Group("chores", this.cleaning);
  Group schoolWork = new Group("school work", this.school);
  Group living = new Group("health", this.health);
  Group design = new Group("design recipe", this.data);


  // big bang
  boolean testBigBang(Tester t) {
    TaskWorld world = new TaskWorld(this.design);
    int worldWidth = 600;
    int worldHeight = 400;
    double tickRate = .1;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }
}