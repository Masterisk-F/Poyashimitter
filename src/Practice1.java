import javax.swing.JFrame;
import javax.swing.JPanel;

public class Practice1 extends JPanel{
	
	public Practice1(){}
	
	
	public static void main(String[] args) {
		
		JFrame app=new JFrame();
		app.add(new Practice1());
		app.setSize(400, 500);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.setVisible(true);
		
		
	}

}
