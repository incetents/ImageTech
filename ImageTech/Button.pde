
class Button
{
  PVector m_position;
  PVector m_size;
  color m_color;
  int m_selectionID;
  boolean m_export = false;
  ImageDisplay m_imageParent = null;

  Button()
  {
    m_position = new PVector(0, 0);
    m_size = new PVector(0, 0);
    m_color = color(0);
  }
  Button(int x, int y, int w, int h, color c, int ID)
  {
    m_position = new PVector(x, y);
    m_size = new PVector(w, h);
    m_color = c;
    m_selectionID = ID;
  }

  boolean isHovered()
  {
    return (
      mouseX >= m_position.x && mouseX < (m_position.x + m_size.x) &&
      mouseY >= m_position.y && mouseY < (m_position.y + m_size.y)
      );
  }
  boolean isClicked()
  {
    return isHovered() && mouseLeftDown;
  }
  boolean isScrollClicked()
  {
     return isHovered() && mouseCenterDown;
  }

  void draw()
  {
    if (isHovered())
      stroke(255, 255, 0);
    else
      stroke(0);

    strokeWeight(2);
    fill(m_color, 170);
    rect(m_position.x, m_position.y, m_size.x, m_size.y);
  }
  void drawSelected()
  {
    stroke(0);
    strokeWeight(2);
    line(
      m_position.x, m_position.y, 
      m_position.x + m_size.x, m_position.y + m_size.y
      );
  }
}
