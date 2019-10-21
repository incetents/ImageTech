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
  color   m_outlineColor;
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
  ImageDisplay(int _x, int _y, int _width, int _height, String _displayText, color _outlineColor)
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
  void copy(ImageDisplay other)
  {
    this.m_position = other.m_position;
    this.m_size = other.m_size;
    this.m_image = other.m_image;
    this.m_displayText = other.m_displayText;
    this.m_outlineColor = other.m_outlineColor;
  }

  void load(PImage _image)
  {
    m_image = _image;//loadImage(path);
  }
  void unload()
  {
    m_image = null;
  }

  boolean isHovered()
  {
    return (
      mouseX >= m_position.x && mouseX < (m_position.x + m_size.x) &&
      mouseY >= m_position.y && mouseY < (m_position.y + m_size.y)
      );
  }

  float GetPixelColorFromSelectedChannel(int i, int _targetWidth, int _targetHeight)
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
      float resized_x = constrain(float(target_x) / float(_targetWidth), 0.0f, 1.0f);
      float resized_y = constrain(float(target_y) / float(_targetHeight), 0.0f, 1.0f);
      int new_x = int(resized_x * float(m_image.width));
      int new_y = int(resized_y * float(m_image.height));

      color c = m_image.get(new_x, new_y);

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
    return 0.0;
  }

  void drawOutline()
  {
    // outline

    line(m_position.x, m_position.y, m_position.x + m_size.x, m_position.y);
    line(m_position.x, m_position.y, m_position.x, m_position.y + m_size.y);
    line(m_position.x + m_size.x, m_position.y + m_size.y, m_position.x + m_size.x, m_position.y);
    line(m_position.x + m_size.x, m_position.y + m_size.y, m_position.x, m_position.y + m_size.y);
  }
  void drawText()
  {
    // Bg for text
    noStroke();
    fill(0,120);
    rect(m_position.x, m_position.y, m_size.x, 15);
    
    // Name
    if (this == displayHighlighted)
      DropshadowText(m_displayText, int(m_position.x + 2), int(m_position.y + text_size + 1), color(0), color(255,255,0));
    else
      DropshadowText(m_displayText, int(m_position.x + 2), int(m_position.y + text_size + 1), color(0), color(255));
  }

  void drawImage()
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

  void drawHover()
  {
    fill(155, 155, 0, 55);
    rect(m_position.x, m_position.y, m_size.x, m_size.y);
  }

  void drawButtons()
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
    DropshadowText("Export:", int(m_position.x + 128 - 61), int(m_position.y + 128 - buttonSize - buttonPadding + 10), color(0), color(255));
  }
}

void dropEvent(DropEvent theDropEvent)
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
