
void BreakCombinedImage()
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
