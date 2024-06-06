package name.panitz.game2d;



import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
public class SwingScreen extends JPanel{
  private static final long serialVersionUID = 1403492898373497054L;
  Game logic;
  Timer t;

  public SwingScreen(Game gl) {
    this.logic = gl;


    t = new Timer(13, (ev)->{
        logic.move();
        logic.doChecks();
        repaint();
        getToolkit().sync();
        requestFocus();
      });
      t.start();

		
    addKeyListener(new KeyAdapter() {	
        @Override public void keyPressed(KeyEvent e) {
          logic.keyPressedReaction(e);
        }
      });
    setFocusable(true);
    requestFocus();
    }

	
  @Override public Dimension getPreferredSize() {
    return new Dimension((int)logic.width(),(int)logic.height());
  }

	
  @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      logic.paintTo(g);
      g.setColor(Color.BLACK);
      g.setFont(new Font("Arial", Font.BOLD, 16));
      g.drawString("Score: " + logic.getmyScore(), 10, 20);
      g.drawString("Lives: " + logic.getmyLives(), 10, 40);

  }
}

