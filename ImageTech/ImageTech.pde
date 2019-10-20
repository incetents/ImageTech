// Library
import drop.*;
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
Button GetHoveredButton()
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
ImageDisplay GetHoveredDisplay()
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
color lightGrey = color(85, 255);
color darkGrey = color(55, 255);

// Draw dropshadowed text
void DropshadowText(String text, int x, int y, color bg, color fg)
{
  // text drop shadow
  fill(bg);
  text(text, x + 1, y + 1);
  // text
  fill(fg);
  text(text, x, y);
}

void setup()
{
  size(640, 512);//512+128
  strokeWeight(3);
  noSmooth();
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

void update()
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

void draw()
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

void keyPressed()
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

void fileSelected(File selection)
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

void mousePressed()
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
void mouseReleased()
{
  mouseDown = false;
}
