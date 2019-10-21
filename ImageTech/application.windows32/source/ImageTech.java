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
boolean mouseLeftDown = false;
boolean mouseRightDown = false;
boolean mouseCenterDown = false;
boolean HelpMode = true;

// Mouse Start
PVector mouseLeftClickStart = new PVector(0, 0);
PVector mouseRightClickStart = new PVector(0, 0);
PVector mouseLeftDragDelta = new PVector(0, 0);
PVector mouseRightDragDelta = new PVector(0, 0);

// Button
Button buttons[] = new Button[4];
public Button GetPressedGlobalButton()
{
  // Global Buttons
  for (int i = 0; i < 4; i++)
    if (buttons[i] != null && buttons[i].isClicked())
      return buttons[i];

  // no buttons found
  return null;
}
public Button GetPressedChannelButton()
{
  // Channel Buttons
  for (int i = 1; i < 5; i++)
  {
    for (int j = 0; j < 5; j++)
    {
      if (displays[i].buttons[j].isClicked())
      {
        return displays[i].buttons[j];
      }
    }
  }

  // no buttons found
  return null;
}

Button btn_exportOption1 = new Button(565, 492, 15, 15, color(255), 0); // tga
Button btn_exportOption2 = new Button(565, 492 - 20, 15, 15, color(255), 1); // tiff
Button btn_exportOption3 = new Button(565, 492 - 40, 15, 15, color(255), 2); // jpg
Button btn_exportOption4 = new Button(565, 492 - 60, 15, 15, color(255), 3); // png

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
public boolean CheckSizeMismatch()
{
  boolean first = true;
  int _width = 0;
  int _height = 0;

  for (int i = 1; i < 5; i++)
  {
    if (displays[i].m_image != null)
    {
      if (first)
      {
        _width = displays[i].m_image.width;
        _height = displays[i].m_image.height;
        first = false;
      } else
      {
        if (_width != displays[i].m_image.width)
          return true;
        if (_height != displays[i].m_image.height)
          return true;
      }
    }
  }

  return false;
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
// Export Display reference
ImageDisplay displayExport = display0;

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
  textLeading(15);

  drop = new SDrop(this);

  // Init data
  display1.btn_export.m_selectionID = 1;
  display2.btn_export.m_selectionID = 2;
  display3.btn_export.m_selectionID = 3;
  display4.btn_export.m_selectionID = 4;

  display1.btn_export.m_export = true;
  display2.btn_export.m_export = true;
  display3.btn_export.m_export = true;
  display4.btn_export.m_export = true;

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
  if (mouseLeftDown)
  {
    mouseLeftDragDelta.x = mouseX - mouseLeftClickStart.x;
    mouseLeftDragDelta.y = mouseY - mouseLeftClickStart.y;
  } else
  {
    mouseLeftDragDelta.x = 0;
    mouseLeftDragDelta.y = 0;
  }
  if (mouseRightDown)
  {
    mouseRightDragDelta.x = mouseX - mouseRightClickStart.x;
    mouseRightDragDelta.y = mouseY - mouseRightClickStart.y;
  } else
  {
    mouseRightDragDelta.x = 0;
    mouseRightDragDelta.y = 0;
  }

  // Check button presses
  Button clickedExportButton = GetPressedGlobalButton();
  Button clickedChannelButton = GetPressedChannelButton();
  boolean ButtonPressed = (clickedExportButton != null) || (clickedChannelButton != null);

  // scroll click to remove image
  if (mouseCenterDown && !ButtonPressed)
  {
    displayHighlighted = GetHoveredDisplay();
    displayHighlighted.m_image = null;
  }
  // Display image
  if (mouseLeftDown && !ButtonPressed)
    displayHighlighted = GetHoveredDisplay();

  // selecting and dragging images
  if (!ButtonPressed)
  {
    // Acquire dragged display
    if (mouseRightDown && displayDragged == null)
    {
      displayDragged = GetHoveredDisplay();
      displayDragOriginalPosition.set(displayDragged.m_position);
      // Cannot drag first display
      if (displayDragged == display0)
        displayDragged = null;
    }
    // Drop dragged display
    else if (!mouseRightDown && displayDragged != null)
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
      displayDragged.m_position.add(mouseRightDragDelta);
    }
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
  int _text_x = image_size_small + 3;
  int _text_y_base = image_size_large - 4;

  // Helpmode text
  if (HelpMode)
  {
    final String Help_Message =
      "[Left Click] Select an image field\n" +
      "* Drag and drop images to load them in your selected field\n" +
      "[Right Click] You can re-order channels by dragging and dropping\n" +
      "[Scroll Click] Remove image\n" +
      "[C] Combine Channels\n" +
      "[X] Export Combined Image\n" +
      "[B] Break Combined Image into Channels\n" +
      "[H] Hide text";

    DropshadowText(Help_Message, _text_x, _text_y_base - 105, color(0), color(255)); // -15 per extra line
  }
  //
  else
  {
    final String Help_Message = "[H] Show text";

    DropshadowText(Help_Message, _text_x, _text_y_base - 0, color(0), color(255, 55, 55)); // -15 per extra line
  }

  // Size Mismatch Text
  if (CheckSizeMismatch())
    DropshadowText("Sizes do not match, smaller images will be scaled up when combining", _text_x, 26, color(0), color(255, 55, 55)); // -15 per extra line

  // Draw Buttons (if no dragging is occurring)
  if (displayDragged == null)
  {
    // Export Text
    DropshadowText("Export Format:", 550, 503 - 80, color(0), color(255));
    DropshadowText("(PNG)", 589, 503 - 60, color(0), color(255));
    DropshadowText("(JPG)", 589, 503 - 40, color(0), color(255));
    DropshadowText("(TIFF)", 589, 503 - 20, color(0), color(255));
    DropshadowText("(TGA)", 589, 503, color(0), color(255));

    // Draw Global Buttons
    for (int i = 0; i < 4; i++)
    {
      buttons[i].draw();
      if (buttons[i] == selected_exportOption)
        buttons[i].drawSelected();
    }
    // Draw Display Buttons
    for (int i = 1; i < 5; i++)
    {
      if (displays[i] != null)
        displays[i].drawButtons();
    }
  }
}

public void keyPressed()
{
  // Help mode
  if (key == 'h' || key == 'H')
    HelpMode = !HelpMode;

  // Combine Channels
  if (key == 'c' || key == 'C')
  {
    int result = CombineChannels();
    switch(result)
    {
    case 1:
      println("Combine Error: No available channels");
      break;
    }
  }

  // Export
  if (key == 'x' || key == 'X')
  {
    if (display0 != null && display0.m_image != null)
    {
      displayExport = display0;
      selectOutput("Select a file to write to:", "fileSelected");
    } else
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
  if (displayExport == null || displayExport.m_image == null)
  {
    println("Export Image is Null");
    return;
  }

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
      displayExport.m_image.save(selection.getAbsolutePath());
    }
    // Append type from export selection
    else
    {
      if (selected_exportOption == btn_exportOption4)
        displayExport.m_image.save(path + ".png");
      else if (selected_exportOption == btn_exportOption3)
        displayExport.m_image.save(path + ".jpg");
      else if (selected_exportOption == btn_exportOption2)
        displayExport.m_image.save(path + ".tiff");
      else if (selected_exportOption == btn_exportOption1)
        displayExport.m_image.save(path + ".tga");
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
    mouseLeftClickStart = new PVector(mouseX, mouseY);
    mouseLeftDown = true;

    // Check button presses
    Button clickedExportButton = GetPressedGlobalButton();
    Button clickedChannelButton = GetPressedChannelButton();

    // Click type export settings
    if (clickedExportButton != null && clickedChannelButton == null)
    {
      selected_exportOption = clickedExportButton;
    }

    // Click channel export
    if (clickedExportButton == null && clickedChannelButton != null)
    {
      // Export button pressed
      if (clickedChannelButton.m_export)
      {
        displayExport = displays[clickedChannelButton.m_selectionID];
        if (displayExport != null && displayExport.m_image != null)
        {
          selectOutput("Select a file to write to:", "fileSelected");
        }
      }
      // Change channel
      else
      {
        switch(clickedChannelButton.m_selectionID)
        {
          // change channel
        case 0:
          clickedChannelButton.m_imageParent.selected_channel = clickedChannelButton.m_imageParent.btn_channel_r;
          break;
        case 1:
          clickedChannelButton.m_imageParent.selected_channel = clickedChannelButton.m_imageParent.btn_channel_g;
          break;
        case 2:
          clickedChannelButton.m_imageParent.selected_channel = clickedChannelButton.m_imageParent.btn_channel_b;
          break;
        case 3:
          clickedChannelButton.m_imageParent.selected_channel = clickedChannelButton.m_imageParent.btn_channel_a;
          break;
        }
      }
    }
  } else if (mouseButton == CENTER)
    mouseCenterDown = true;
  else if (mouseButton == RIGHT)
  {
    mouseRightClickStart = new PVector(mouseX, mouseY);
    mouseRightDown = true;
  }
}
public void mouseReleased()
{
  if (mouseButton == LEFT)
  {
    mouseLeftDown = false;
  } else if (mouseButton == CENTER)
    mouseCenterDown = false;
  else if (mouseButton == RIGHT)
    mouseRightDown = false;
}

class Button
{
  PVector m_position;
  PVector m_size;
  int m_color;
  int m_selectionID;
  boolean m_export = false;
  ImageDisplay m_imageParent = null;

  Button()
  {
    m_position = new PVector(0, 0);
    m_size = new PVector(0, 0);
    m_color = color(0);
  }
  Button(int x, int y, int w, int h, int c, int ID)
  {
    m_position = new PVector(x, y);
    m_size = new PVector(w, h);
    m_color = c;
    m_selectionID = ID;
  }

  public boolean isHovered()
  {
    return (
      mouseX >= m_position.x && mouseX < (m_position.x + m_size.x) &&
      mouseY >= m_position.y && mouseY < (m_position.y + m_size.y)
      );
  }
  public boolean isClicked()
  {
    return isHovered() && mouseLeftDown;
  }
  public boolean isScrollClicked()
  {
     return isHovered() && mouseCenterDown;
  }

  public void draw()
  {
    if (isHovered())
      stroke(255, 255, 0);
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
enum ChannelType
{
  RED, 
    GREEN, 
    BLUE, 
    ALPHA
}

class ImageDisplay
{
  PVector m_position;
  PVector m_size;
  PImage  m_image = null;
  String  m_displayText;
  int   m_outlineColor;
  ChannelType m_channelSelection = ChannelType.RED;

  final float buttonSize = 12;
  final float buttonPadding = 3;
  Button buttons[] = new Button[5];
  Button btn_channel_r = new Button(0, 0, 12, 12, color(200, 0, 0), 0);
  Button btn_channel_g = new Button(0, 0, 12, 12, color(0, 200, 0), 1);
  Button btn_channel_b = new Button(0, 0, 12, 12, color(0, 0, 200), 2);
  Button btn_channel_a = new Button(0, 0, 12, 12, color(200, 200, 200), 3);
  Button selected_channel = btn_channel_r;

  Button btn_export = new Button(0, 0, 12, 12, color(100, 100, 100), 4);

  ImageDisplay()
  {
    buttons[0] = btn_channel_r;
    buttons[1] = btn_channel_g;
    buttons[2] = btn_channel_b;
    buttons[3] = btn_channel_a;
    buttons[4] = btn_export;

    buttons[0].m_imageParent = this;
    buttons[1].m_imageParent = this;
    buttons[2].m_imageParent = this;
    buttons[3].m_imageParent = this;
    buttons[4].m_imageParent = this;
  }
  ImageDisplay(int _x, int _y, int _width, int _height, String _displayText, int _outlineColor)
  {
    m_position = new PVector(_x, _y);
    m_size = new PVector(_width, _height);
    m_displayText = new String(_displayText);
    m_outlineColor = _outlineColor;

    buttons[0] = btn_channel_r;
    buttons[1] = btn_channel_g;
    buttons[2] = btn_channel_b;
    buttons[3] = btn_channel_a;
    buttons[4] = btn_export;

    buttons[0].m_imageParent = this;
    buttons[1].m_imageParent = this;
    buttons[2].m_imageParent = this;
    buttons[3].m_imageParent = this;
    buttons[4].m_imageParent = this;
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

  public float GetPixelColorFromSelectedChannel(int i, int _targetWidth, int _targetHeight)
  {
    // Pixel Fetch
    if (_targetWidth == m_image.width && _targetHeight == m_image.height)
    {
      switch (selected_channel.m_selectionID)
      {
      case 0:
        return red(m_image.pixels[i]);
      case 1:
        return green(m_image.pixels[i]);
      case 2:
        return blue(m_image.pixels[i]);
      case 3:
        return alpha(m_image.pixels[i]);
      }
    }
    // Pixel Sample
    else
    {
      int target_x = i % _targetWidth;
      int target_y = i / _targetHeight;
      float resized_x = constrain(PApplet.parseFloat(target_x) / PApplet.parseFloat(_targetWidth), 0.0f, 1.0f);
      float resized_y = constrain(PApplet.parseFloat(target_y) / PApplet.parseFloat(_targetHeight), 0.0f, 1.0f);
      int new_x = PApplet.parseInt(resized_x * PApplet.parseFloat(m_image.width));
      int new_y = PApplet.parseInt(resized_y * PApplet.parseFloat(m_image.height));

      int c = m_image.get(new_x, new_y);

      switch (selected_channel.m_selectionID)
      {
      case 0:
        return red(c);
      case 1:
        return green(c);
      case 2:
        return blue(c);
      case 3:
        return alpha(c);
      }
    }

    // ERROR ID
    println("Selected ID ERROR");
    return 0.0f;
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
    // Name
    if (this == displayHighlighted)
      DropshadowText(m_displayText, PApplet.parseInt(m_position.x + 2), PApplet.parseInt(m_position.y + text_size + 1), color(0), color(255,255,0));
    else
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

  public void drawButtons()
  {
    btn_channel_r.m_position.set(m_position.x + buttonPadding +  0, m_position.y + 128 - buttonSize - buttonPadding);
    btn_channel_g.m_position.set(m_position.x + buttonPadding + 12, m_position.y + 128 - buttonSize - buttonPadding);
    btn_channel_b.m_position.set(m_position.x + buttonPadding + 24, m_position.y + 128 - buttonSize - buttonPadding);
    btn_channel_a.m_position.set(m_position.x + buttonPadding + 36, m_position.y + 128 - buttonSize - buttonPadding);
    btn_export.m_position.set(m_position.x + 128 - 18, m_position.y + 128 - buttonSize - buttonPadding);

    btn_channel_r.draw();
    btn_channel_g.draw();
    btn_channel_b.draw();
    btn_channel_a.draw();
    btn_export.draw();

    // Selected Button
    for (int i = 0; i < 4; i++)
      if (buttons[i] == selected_channel)
        buttons[i].drawSelected();

    // Export Text
    DropshadowText("Export:", PApplet.parseInt(m_position.x + 128 - 61), PApplet.parseInt(m_position.y + 128 - buttonSize - buttonPadding + 10), color(0), color(255));
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

    display1.m_image.pixels[i] = color(_r, _r, _r);
    display2.m_image.pixels[i] = color(_g, _g, _g);
    display3.m_image.pixels[i] = color(_b, _b, _b);
    display4.m_image.pixels[i] = color(_a, _a, _a);
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

  // Acquire size from largest available channel
  int _width = 0;
  int _height = 0;
  for (int i = 0; i < 4; i++)
  {
    if (Channels[i])
    {
      _width = max(_height, displays[i+1].m_image.width);
      _height = max(_height, displays[i+1].m_image.height);
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
        Channels[0] ? display1.GetPixelColorFromSelectedChannel(i, _width, _height) : 0, 
        Channels[1] ? display2.GetPixelColorFromSelectedChannel(i, _width, _height) : 0, 
        Channels[2] ? display3.GetPixelColorFromSelectedChannel(i, _width, _height) : 0, 
        Channels[3] ? display4.GetPixelColorFromSelectedChannel(i, _width, _height) : 0
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
        Channels[0] ? display1.GetPixelColorFromSelectedChannel(i, _width, _height) : 0, 
        Channels[1] ? display2.GetPixelColorFromSelectedChannel(i, _width, _height) : 0, 
        Channels[2] ? display3.GetPixelColorFromSelectedChannel(i, _width, _height) : 0
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
