class ImageDisplay
{
  PVector m_position;
  PVector m_size;
  PImage  m_image = null;
  String  m_displayText;
  color   m_outlineColor;

  ImageDisplay()
  {
  }
  ImageDisplay(int _x, int _y, int _width, int _height, String _displayText, color _outlineColor)
  {
    m_position = new PVector(_x, _y);
    m_size = new PVector(_width, _height);
    m_displayText = new String(_displayText);
    m_outlineColor = _outlineColor;
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
