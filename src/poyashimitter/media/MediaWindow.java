package poyashimitter.media;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import poyashimitter.Settings;
import twitter4j.MediaEntity;

public class MediaWindow extends JFrame {
	MediaEntity[] media;
	int i;
	
	MediaMainPanel mainPanel;
	JPanel listPanel;
	
	public MediaWindow(MediaEntity[] media,int i){
		this.media=media;
		this.i=i;
		
		setLayout(new BorderLayout());
		mainPanel=new MediaMainPanel(this,media,i);
		add(mainPanel,BorderLayout.CENTER);
		
		listPanel=new MediaListPanel(media,(MediaMainPanel)mainPanel);
		add(listPanel,BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
}


class MediaMainPanel extends JComponent{
	int i;
	
	Dimension size=new Dimension(500,500);
	MediaEntity[] media;
	Image[] img;//一度DLしたらここに保存、以降はここから読み込み
	
	Image dispImg;//画面に表示する、スケーリングしたimage
	
	
	Point offset=new Point(0,0);//表示する画像の左端の座標
	double ratio=1;//画像の倍率
	
	MediaMainPanel(final JFrame frame,MediaEntity[] media,int index){
		this.i=index;
		this.media=media;
		img=new Image[media.length];
		
		
		setPreferredSize(size);
		
		setDisplayImage(i);
		
		MouseAdapter ma=new MouseAdapter(){
			Point p;	//マウス位置
			
			@Override
			public void mouseClicked(MouseEvent e){	//ダブルクリックでwindow閉じる
				if(e.getClickCount()>=2)
					frame.dispose();
			}
			
			@Override
			public void mouseDragged(MouseEvent e){
				offset.translate(e.getX()-p.x,e.getY()-p.y);
				p=e.getPoint();
				repaint();
			}
			@Override
			public void mouseMoved(MouseEvent e){
				p=e.getPoint();
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e){
				//System.out.println(e.getWheelRotation());
				if(e.getWheelRotation()==-1 && ratio<8){
					ratio*=1.5;
					offset.x=(int)((offset.x-p.x)*1.5+p.x);
					offset.y=(int)((offset.y-p.y)*1.5+p.y);
				}else if(ratio>0.5){
					ratio/=1.5;
					offset.x=(int)((offset.x-p.x)/1.5+p.x);
					offset.y=(int)((offset.y-p.y)/1.5+p.y);
				}
				new Thread(new Runnable(){
					@Override
					public void run() {
						scaling();
						repaint();
				}}).start();
			}
		};
		
		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
		this.addMouseWheelListener(ma);
	}
	
	void scaling(){
		if(img[i].getWidth(null)>img[i].getHeight(null)){
			if(500*ratio<img[i].getWidth(null))
				dispImg=img[i].getScaledInstance((int)(500*ratio),-1, Image.SCALE_AREA_AVERAGING);
			else
				dispImg=img[i].getScaledInstance((int)(500*ratio),-1, Image.SCALE_FAST);
		}else{
			if(500*ratio<img[i].getHeight(null))
				dispImg=img[i].getScaledInstance(-1,(int)(500*ratio), Image.SCALE_AREA_AVERAGING);
			else
				dispImg=img[i].getScaledInstance(-1,(int)(500*ratio), Image.SCALE_FAST);
		}
	}
	
	@Override
	public void paintComponent(Graphics g){	//小さく表示するときは品質の良いscalingアルゴリズムを使う
		super.paintComponent(g);
		
		if(dispImg==null)	return;
		
		
		/*
		if(img[i].getWidth(null)>img[i].getHeight(null)){
			if(500*ratio<img[i].getWidth(null))
				dispImg=img[i].getScaledInstance((int)(500*ratio),-1, Image.SCALE_AREA_AVERAGING);
			else
				dispImg=img[i].getScaledInstance((int)(500*ratio),-1, Image.SCALE_FAST);
		}else{
			if(500*ratio<img[i].getHeight(null))
				dispImg=img[i].getScaledInstance(-1,(int)(500*ratio), Image.SCALE_AREA_AVERAGING);
			else
				dispImg=img[i].getScaledInstance(-1,(int)(500*ratio), Image.SCALE_FAST);
		}
		*/
		
		g.drawImage(dispImg, offset.x, offset.y, null);
		
		//g.drawImage(img[i], offset.x, offset.y,(int)(500*ratio),(int)(500*ratio),Color.white, null);
	}
	/*
	@Override
	public boolean imageUpdate(Image img,int infoflags,int x,int y,int width,int height){
		if((infoflags&ImageObserver.ALLBITS)!=0){
			dispImg=img;
			repaint();
			return false;
		}
		System.out.println(((infoflags&HEIGHT)!=0 || (infoflags&WIDTH)!=0) && (infoflags&ALLBITS)==0);
		System.out.println("width="+width+", height="+height);
		return true;
	}
	*/
	
	void setDisplayImage(final int index){//最初の表示、画像切替時
		/*
		i=index;
		ratio=1;
		offset.setLocation(0,0);
		*/
		if(img[index]==null){
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					URL url = null;
					try {
						url=new URL(media[index].getMediaURLHttps()+":orig");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					if(url!=null){
						try {
							img[index]=ImageIO.read(url);
						} catch (IOException e) {
							// TODO 自動生成された catch ブロック
							e.printStackTrace();
						}
						i=index;
						ratio=1;
						offset.setLocation(0,0);
						scaling();
						System.out.println("getPicture");
						repaint();
					}
				}
			}).start();
			
		}else{
			System.out.println("exist");
			this.i=index;
			ratio=1;
			offset.setLocation(0,0);
			scaling();
			repaint();
		}
		
	}
}




class MediaListPanel extends JPanel{
	
	public MediaListPanel(final MediaEntity[] media,final MediaMainPanel p){
		
		setLayout(new FlowLayout());
		
		for(int i=0;i<media.length;i++){
			final JLabel l = new JLabel("loading...");
			int size=Settings.getTimelineOption(Settings.timeline.mediaSize);
			l.setPreferredSize(new Dimension(size,size));
			this.add(l);
			
			new SwingWorker<Image,Object>(){
				int i;
				
				public SwingWorker<Image,Object> setI(int i){
					this.i=i;
					return this;
				}
				@Override
				protected Image doInBackground() throws Exception {
					URL url=null;
					try {
						url=new URL(media[i].getMediaURLHttps()+":small");
					} catch (MalformedURLException e) {
						e.printStackTrace();
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() {
								l.setText("failed");
							}
						});
					}
					if(url!=null){
						Image img=ImageIO.read(url);
						if(img.getWidth(null)>img.getHeight(null)){
							img=img.getScaledInstance(100,-1, Image.SCALE_SMOOTH);
						}else{
							img=img.getScaledInstance(-1,100, Image.SCALE_SMOOTH);
						}
						MediaTracker tracker=new MediaTracker(l);
						tracker.addImage(img,0);
						tracker.waitForAll();
						
						return img;
						
					}
					return null;
				}
				@Override
				protected void done(){
					try {
						Image img=get();
						l.setIcon(new ImageIcon(img));
						
						l.addMouseListener(new MouseAdapter(){
							int i;
							
							public MouseListener setI(int i){
								this.i=i;
								return this;
							}
							
							@Override
							public void mousePressed(MouseEvent e){
								p.setDisplayImage(i);
								p.repaint();
							}
							
						}.setI(i));
						l.setText("");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					
				}
				
			}.setI(i).execute();
			
			
			/*
			URL url=null;
			try {
				url=new URL(media[i].getMediaURLHttps()+":small");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
			if(url!=null){
				JLabel l = new JLabel();
				
				this.add(l);
				
				ImageIcon icon=new ImageIcon(url);
				if(icon.getIconWidth()>icon.getIconHeight()){
					icon=new ImageIcon(icon.getImage().getScaledInstance(100,-1, Image.SCALE_SMOOTH));
				}else{
					icon=new ImageIcon(icon.getImage().getScaledInstance(-1,100, Image.SCALE_SMOOTH));
				}
				l.setIcon(icon);;
				
				l.addMouseListener(new MouseAdapter(){
					int i;
					
					public MouseListener setI(int i){
						this.i=i;
						return this;
					}
					
					@Override
					public void mousePressed(MouseEvent e){
						p.setDisplayImage(i);
						p.repaint();
					}
					
				}.setI(i));
				
			}
			
			*/
		}
	}
	
}