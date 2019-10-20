
int CombineChannels()
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
