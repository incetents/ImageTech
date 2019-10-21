// Library
import drop.*;
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
Button GetPressedGlobalButton()
{
  // Global Buttons
  for (int i = 0; i < 4; i++)
    if (buttons[i] != null && buttons[i].isClicked())
      return buttons[i];

  // no buttons found
  return null;
}
Button GetPressedChannelButton()
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
boolean CheckSizeMismatch()
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

void update()
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

  // Draw Display Text
  for (int i = 0; i < 5; i++)
  {
    if (displays[i] != null)
      displays[i].drawText();
  }

  // Draw Base Outline
  stroke(0);
  strokeWeight(1);
  for (int i = 0; i < 5; i++)
  {
    if (displays[i] != null)
      displays[i].drawOutline();
  }

  // Draw Colored Outline
  stroke(GetHoveredDisplay().m_outlineColor);
  strokeWeight(3);
  GetHoveredDisplay().drawOutline();
  noStroke();

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

void keyPressed()
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

void fileSelected(File selection)
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

void mousePressed()
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
void mouseReleased()
{
  if (mouseButton == LEFT)
  {
    mouseLeftDown = false;
  } else if (mouseButton == CENTER)
    mouseCenterDown = false;
  else if (mouseButton == RIGHT)
    mouseRightDown = false;
}
