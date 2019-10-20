import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import drop.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ImageTech extends PApplet {

// Library

SDrop drop;

// Constant
final int image_size_small = 128;
final int image_size_large = image_size_small * 4;
final int text_size = 12;

// Data
boolean mouseDown = false;

// Mouse Start
PVector mouseClickStart = new PVector(0, 0);
PVector mouseDragDelta = new PVector(0, 0);

// Error time
int error_time = 0;

// Button
Button buttons[] = new Button[4];
public Button GetHoveredButton()
{
  for (int i = 0; i < 4; i++)
    if (buttons[i] != null && buttons[i].isHovered())
      return buttons[i];

  // no buttons found
  return null;
}

Button btn_exportOption1 = new Button(565, 492, 15, 15, color(255)); // tga
Button btn_exportOption2 = new Button(565, 492 - 20, 15, 15, color(255)); // tiff
Button btn_exportOption3 = new Button(565, 492 - 40, 15, 15, color(255)); // jpg
Button btn_exportOption4 = new Button(565, 492 - 60, 15, 15, color(255)); // png

Button selected_exportOption = btn_exportOption4;

// Global Display images
ImageDisplay displays[] = new ImageDisplay[5];
public ImageDisplay GetHoveredDisplay()
{
  for (int i = 0; i < 5; i++)
  {
    if (displays[i].isHovered() && displays[i] != displayDragged)
      return displays[i];
  }

  // safety
  return display0;
}

// Display images
ImageDisplay display0 = new ImageDisplay(image_size_small, 0, image_size_large, image_size_large, "Combined Image", color(255, 255, 0));
ImageDisplay display1 = new ImageDisplay(0, 0, image_size_small, image_size_small, "Red Channel", color(255, 0, 0));
ImageDisplay display2 = new ImageDisplay(0, image_size_small, image_size_small, image_size_small, "Green Channel", color(0, 255, 0));
ImageDisplay display3 = new ImageDisplay(0, image_size_small*2, image_size_small, image_size_small, "Blue Channel", color(0, 0, 255));
ImageDisplay display4 = new ImageDisplay(0, image_size_small*3, image_size_small, image_size_small, "Alpha Channel", color(255, 255, 255));

// Highlighted Display reference
ImageDisplay displayHighlighted = display1;
// Dragged Display reference
PVector displayDragOriginalPosition = new PVector(0, 0);
ImageDisplay displayDragged = null;

// Checkerboard image
PImage checkerboard;
int lightGrey = color(85, 255);
int darkGrey = color(55, 255);

// Draw dropshadowed text
public void DropshadowText(String text, int x, int y, int bg, int fg)
{
  // text drop shadow
  fill(bg);
  text(text, x + 1, y + 1);
  // text
  fill(fg);
  text(text, x, y);
}

public void setup()
{
  //512+128
  strokeWeight(3);
  
  textSize(text_size);

  drop = new SDrop(this);

  // Button list
  buttons[0] = btn_exportOption1;
  buttons[1] = btn_exportOption2;
  buttons[2] = btn_exportOption3;
  buttons[3] = btn_exportOption4;
  // Display list
  displays[0] = display0;
  displays[1] = display1;
  displays[2] = display2;
  displays[3] = display3;
  displays[4] = display4;

  int _width = 4;
  int _height = 4;
  checkerboard = createImage(_width, _height, ARGB);

  for (int i = 0; i < checkerboard.pixels.length; i++)
  {
    int x = i % _width;
    int y = i / _height;

    if (x % 2 == y % 2)
      checkerboard.pixels[i] = lightGrey;
    else
      checkerboard.pixels[i] = darkGrey;
  }
}

public void update()
{
  // Update drag value
  if (mouseDown)
  {
    mouseDragDelta.x = mouseX - mouseClickStart.x;
    mouseDragDelta.y = mouseY - mouseClickStart.y;
  } else
  {
    mouseDragDelta.x = 0;
    mouseDragDelta.y = 0;
  }

  // Acquire dragged display
  if (mouseDown && displayDragged == null)
  {
    displayDragged = GetHoveredDisplay();
    displayDragOriginalPosition.set(displayDragged.m_position);
    // Cannot drag first display
    if (displayDragged == display0)
      displayDragged = null;
  }
  // Drop dragged display
  else if (!mouseDown && displayDragged != null)
  {
    for (int i = 1; i < 5; i++)
    {
      // Check if dropped on another display
      if (displays[i] != null && displays[i].isHovered() && displays[i] != displayDragged)
      {
        // Swap images
        PImage temp = displayDragged.m_image;
        displayDragged.m_image = displays[i].m_image;
        displays[i].m_image = temp;
      }
    }
    // Revert drag position and remove dragging
    displayDragged.m_position.set(displayDragOriginalPosition);  
    displayDragged = null;
  }

  // Move dragged display
  if (displayDragged != null)
  {
    // Start at original position
    displayDragged.m_position.set(displayDragOriginalPosition);
    // Move based on drag delta
    displayDragged.m_position.add(mouseDragDelta);
  }
}

public void draw()
{
  update();

  // default
  background(0);
  fill(255);
  stroke(0);
  tint(255, 255);

  // Draw Base
  for (int i = 0; i < 5; i++)
  {
    if (displays[i] != null)
      displays[i].drawImage();
  }

  // Hover effect
  if (displayHighlighted != null)
    displayHighlighted.drawHover();

  // Draw Outline
  stroke(0);
  strokeWeight(1);
  for (int i = 0; i < 5; i++)
  {
    if (displays[i] != null)
      displays[i].drawOutline();
  }
  
  stroke(GetHoveredDisplay().m_outlineColor);
  strokeWeight(3);
  GetHoveredDisplay().drawOutline();
  noStroke();

  // Draw Display Text
  for (int i = 0; i < 5; i++)
  {
    if (displays[i] != null)
      displays[i].drawText();
  }

  // Settings Text
  int _text_x = image_size_small + 2;
  int _text_y_base = image_size_large - 4;
  DropshadowText("[C] Combine Channels", _text_x, _text_y_base, color(0), color(255));
  DropshadowText("[X] Export Combined Image", _text_x, _text_y_base - 15, color(0), color(255));
  DropshadowText("[B] Break Combined Image into Channels", _text_x, _text_y_base - 30, color(0), color(255));
  DropshadowText("[Scroll Click] Delete image", _text_x, _text_y_base - 45, color(0), color(255));
  DropshadowText("You can re-order channels by dragging and dropping", _text_x, _text_y_base - 60, color(0), color(255));

  // Export Text
  DropshadowText("Export Format:", 550, 503 - 80, color(0), color(255));
  DropshadowText("(PNG)", 589, 503 - 60, color(0), color(255));
  DropshadowText("(JPG)", 589, 503 - 40, color(0), color(255));
  DropshadowText("(TIFF)", 589, 503 - 20, color(0), color(255));
  DropshadowText("(TGA)", 589, 503, color(0), color(255));
  
  // Error Message
  if(error_time > millis())
    DropshadowText("ERROR: FILE SIZES MUST MATCH WIDTH/HEIGHT", 320 - 80, 256 - 14, color(0), color(255, 50, 50, 255));

  // Draw Buttons
  for (int i = 0; i < 4; i++)
  {
    buttons[i].draw();
    if (buttons[i] == selected_exportOption)
      buttons[i].drawSelected();
  }
}

public void keyPressed()
{
  // Combine Channels
  if (key == 'c' || key == 'C')
  {
    int result = CombineChannels();
    switch(result)
    {
    case 1:
      println("Combine Error: No available channels");
      break;

    case 2:
      error_time = millis() + 3000;
      println("Combine Error: Channel Width Mismatch");
      break;

    case 3:
      error_time = millis() + 3000;
      println("Combine Error: Channel Height Mismatch");
      break;
    }
  }

  // Export
  if (key == 'x' || key == 'X')
  {
    if (display0 != null && display0.m_image != null)
      selectOutput("Select a file to write to:", "fileSelected");
    else
      println("Export Error: image empty");
  }

  // Break Combined Image
  if (key == 'b' || key == 'B')
  {
    if (display0 != null && display0.m_image != null)
      BreakCombinedImage();
    else
      println("Break Error: image empty");
  }
}

public void fileSelected(File selection)
{
  if (selection != null)
  {
    println("User selected " + selection.getAbsolutePath());
    String path = selection.getAbsolutePath();
    // check if it ends with an existing format
    String end_4 = path.substring(path.length() - 4, path.length());
    String end_5 = path.substring(path.length() - 5, path.length());
    if (
      end_4.compareTo(".png") == 0 ||
      end_4.compareTo(".jpg") == 0 ||
      end_5.compareTo(".tiff") == 0 ||
      end_4.compareTo(".tga") == 0
      )
    {
      display0.m_image.save(selection.getAbsolutePath());
    }
    // Append type from export selection
    else
    {
      if (selected_exportOption == btn_exportOption4)
        display0.m_image.save(path + ".png");
      else if (selected_exportOption == btn_exportOption3)
        display0.m_image.save(path + ".jpg");
      else if (selected_exportOption == btn_exportOption2)
        display0.m_image.save(path + ".tiff");
      else if (selected_exportOption == btn_exportOption1)
        display0.m_image.save(path + ".tga");
      else
        println("Export Type Error");
    }
  }
}

public void mousePressed()
{
  // Left Click
  if (mouseButton == LEFT)
  {
    mouseClickStart = new PVector(mouseX, mouseY);
    mouseDown = true;

    // Check if button was clicked
    Button clickedButton = GetHoveredButton();

    // If no button is hovered, check if selection was clicked
    if (clickedButton == null)
      displayHighlighted = GetHoveredDisplay();
    // Update clicked button
    else
      selected_exportOption = clickedButton;
  }
  // Middle click
  else if (mouseButton == CENTER)
  {
    // Check if button was clicked
    Button clickedButton = GetHoveredButton();

    // If no button is hovered, check if selection was clicked
    if (clickedButton == null)
    {
      // Remove image
      displayHighlighted = GetHoveredDisplay();
      displayHighlighted.m_image = null;
    }
  }
}
public void mouseReleased()
{
  mouseDown = false;
}

class Button
{
  PVector m_position;
  PVector m_size;
  int m_color;

  Button()
  {
    m_position = new PVector(0, 0);
    m_size = new PVector(0, 0);
    m_color = color(0);
  }
  Button(int x, int y, int w, int h, int c)
  {
    m_position = new PVector(x, y);
    m_size = new PVector(w, h);
    m_color = c;
  }

  public boolean isHovered()
  {
    return (
      mouseX >= m_position.x && mouseX < (m_position.x + m_size.x) &&
      mouseY >= m_position.y && mouseY < (m_position.y + m_size.y)
      );
  }

  public void draw()
  {
    if (isHovered())
      stroke(255,255,0);
    else
      stroke(0);
      
    strokeWeight(2);
    fill(m_color, 170);
    rect(m_position.x, m_position.y, m_size.x, m_size.y);
  }
  public void drawSelected()
  {
    stroke(0);
    strokeWeight(2);
    line(
      m_position.x, m_position.y, 
      m_position.x + m_size.x, m_position.y + m_size.y
      );
  }
}
class ImageDisplay
{
  PVector m_position;
  PVector m_size;
  PImage  m_image = null;
  String  m_displayText;
  int   m_outlineColor;

  ImageDisplay()
  {
  }
  ImageDisplay(int _x, int _y, int _width, int _height, String _displayText, int _outlineColor)
  {
    m_position = new PVector(_x, _y);
    m_size = new PVector(_width, _height);
    m_displayText = new String(_displayText);
    m_outlineColor = _outlineColor;
  }
  public void copy(ImageDisplay other)
  {
    this.m_position = other.m_position;
    this.m_size = other.m_size;
    this.m_image = other.m_image;
    this.m_displayText = other.m_displayText;
    this.m_outlineColor = other.m_outlineColor;
  }

  public void load(PImage _image)
  {
    m_image = _image;//loadImage(path);
  }
  public void unload()
  {
    m_image = null;
  }

  public boolean isHovered()
  {
    return (
      mouseX >= m_position.x && mouseX < (m_position.x + m_size.x) &&
      mouseY >= m_position.y && mouseY < (m_position.y + m_size.y)
      );
  }

  public void drawOutline()
  {
    // outline

    line(m_position.x, m_position.y, m_position.x + m_size.x, m_position.y);
    line(m_position.x, m_position.y, m_position.x, m_position.y + m_size.y);
    line(m_position.x + m_size.x, m_position.y + m_size.y, m_position.x + m_size.x, m_position.y);
    line(m_position.x + m_size.x, m_position.y + m_size.y, m_position.x, m_position.y + m_size.y);
  }
  public void drawText()
  {
    DropshadowText(m_displayText, PApplet.parseInt(m_position.x + 2), PApplet.parseInt(m_position.y + text_size + 1), color(0), color(255));
  }

  public void drawImage()
  {
    // Draw checkerbox
    if (m_image == null || m_image != null && isHovered())
    {
      tint(255, 255);
      image(checkerboard, m_position.x, m_position.y, m_size.x, m_size.y);
    }

    // Draw Image
    if (m_image != null)
    {
      if (isHovered())
        tint(255, 170);
      else
        tint(255, 255);

      image(m_image, m_position.x, m_position.y, m_size.x, m_size.y);
    }
  }

  public void drawHover()
  {
    fill(155, 155, 0, 55);
    rect(m_position.x, m_position.y, m_size.x, m_size.y);
  }
}

public void dropEvent(DropEvent theDropEvent)
{
  if (theDropEvent.isImage())
  {
    if (displayHighlighted != null)
    {
      PImage loadedImage = theDropEvent.loadImage();
      displayHighlighted.load(loadedImage);
    }
  }
}

public void BreakCombinedImage()
{
  display1.m_image = null;
  display2.m_image = null;
  display3.m_image = null;
  display4.m_image = null;
  
  int _width = display0.m_image.width;
  int _height = display0.m_image.height;
  
  display1.m_image = createImage(_width, _height, RGB);
   display2.m_image = createImage(_width, _height, RGB);
    display3.m_image = createImage(_width, _height, RGB);
     display4.m_image = createImage(_width, _height, RGB);
  
  for (int i = 0; i < display0.m_image.pixels.length; i++)
  {
    //int x = i % _width;
    //int y = i / _height;
    float _r = red(display0.m_image.pixels[i]);
    float _g = green(display0.m_image.pixels[i]);
    float _b = blue(display0.m_image.pixels[i]);
    float _a = alpha(display0.m_image.pixels[i]);

    display1.m_image.pixels[i] = color(_r,_r,_r);
    display2.m_image.pixels[i] = color(_g,_g,_g);
    display3.m_image.pixels[i] = color(_b,_b,_b);
    display4.m_image.pixels[i] = color(_a,_a,_a);
  }

}

public int CombineChannels()
{
  boolean Channels[] = new boolean[4]; 
  Channels[0] = display1.m_image != null;
  Channels[1] = display2.m_image != null;
  Channels[2] = display3.m_image != null;
  Channels[3] = display4.m_image != null;

  int ChannelCount = 0;
  for (int i = 0; i < 4; i ++)
  {
    if (Channels[i])
      ChannelCount++;
  }

  // Requires at least one channel
  if (ChannelCount == 0)
    return 1;

  // Acquire size from first available channel
  int _width = 0;
  int _height = 0;
  for (int i = 0; i < 4; i++)
  {
    if (Channels[i])
    {
      _width = displays[i+1].m_image.width;
      _height = displays[i+1].m_image.height;
    }
  }

  // Check if all other available channels have the same size
  for (int i = 0; i < 4; i++)
  {
    if (Channels[i])
    {
      if (_width != displays[i+1].m_image.width)
        return 2;

      if (_height != displays[i+1].m_image.height)
        return 3;
    }
  }

  // Create image (channel type depends on alpha channel)
  PImage result;
  if (Channels[3])
  {
    result = createImage(_width, _height, ARGB);

    // Iterate through all the pixels
    int pixelCount = _width * _height;
    for (int i = 0; i < pixelCount; i++)
    {
      result.pixels[i] = color(
        Channels[0] ? red(display1.m_image.pixels[i]) : 0, 
        Channels[1] ? red(display2.m_image.pixels[i]) : 0, 
        Channels[2] ? red(display3.m_image.pixels[i]) : 0, 
        red(display4.m_image.pixels[i])
        );
    }
    //
  }
  //
  else
  {
    result = createImage(_width, _height, RGB);
    
    // Iterate through all the pixels
    int pixelCount = _width * _height;
    for (int i = 0; i < pixelCount; i++)
    {
      result.pixels[i] = color(
        Channels[0] ? red(display1.m_image.pixels[i]) : 0, 
        Channels[1] ? red(display2.m_image.pixels[i]) : 0, 
        Channels[2] ? red(display3.m_image.pixels[i]) : 0
        );
    }
    //
  }

  // Update result
  result.updatePixels();
  display0.m_image = result;

  return 0; // no errors
}
  public void settings() {  size(640, 512);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ImageTech" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
