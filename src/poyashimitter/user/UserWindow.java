package poyashimitter.user;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import poyashimitter.UserTimelinePane;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserWindow extends JFrame {
	User user;
	Twitter twitter;
	
	Dimension dim=new Dimension(450,600);
	
	JPanel userInfoPanel;//上半分、bioとか
	JPanel userTimelinePanel;//下半分、tweetとか
	
	public UserWindow(long userID,Twitter twitter) throws TwitterException{
		this(twitter.showUser(userID),twitter);
	}
	public UserWindow(User user,Twitter twitter){//twitter:accesstokenなど設定済みのもの
		super();
		
		this.user=user;
		this.twitter=twitter;
		
		setLayout(new BorderLayout());
		
		setUserInfoPanel();
		//setUserTimelinePanel();
		userTimelinePanel=new JPanel();
		userTimelinePanel.setLayout(new BoxLayout(userTimelinePanel,BoxLayout.Y_AXIS));
		userTimelinePanel.add(new UserTimelinePane(user,twitter));
		
		add(userInfoPanel,BorderLayout.NORTH);
		add(userTimelinePanel,BorderLayout.CENTER);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(dim);
		pack();
		setVisible(true);
	}
	
	private void setUserInfoPanel(){
		
		userInfoPanel=new JPanel(){
			Image bgImage;
			public JPanel setBgImage(){
				try {
					URL url=new URL(user.getProfileBannerIPadRetinaURL());
					bgImage=ImageIO.read(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return this;
			}
			@Override
			public void paintComponent(Graphics g){//出来ない
				super.paintComponent(g);
				if(bgImage==null)	return;
				
				Dimension d=this.getSize();
				double panelRatio=d.getHeight()/d.getWidth();
				double bgImageRatio=(double)bgImage.getHeight(null)/bgImage.getWidth(null);
				Image img=null;
				if(panelRatio>bgImageRatio){
					img=bgImage.getScaledInstance(-1,d.height,Image.SCALE_FAST);
				}else{
					img=bgImage.getScaledInstance(d.width,-1,Image.SCALE_FAST);
				}
				g.drawImage(img,0,0,null);
				g.setColor(new Color(255,255,255,200));
				g.fillRect(0,0,d.width,d.height);
				//System.out.println("paintComponent:"+d);
			}
			/*
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height){
				if((infoflags&ALLBITS)==0){
					//repaint();
					return true;
				}
				return false;
			}
			*/
		}.setBgImage();
		
		userInfoPanel.setLayout(new BoxLayout(userInfoPanel,BoxLayout.Y_AXIS));
		userInfoPanel.setOpaque(true);
		
		final JPanel headerPanel=new JPanel();//icon,name,screenNameが入る
		headerPanel.setLayout(new BoxLayout(headerPanel,BoxLayout.X_AXIS));
		headerPanel.setOpaque(false);
		userInfoPanel.add(headerPanel);
		
		//icon
		Image iconImg=null;
		try {
			iconImg=new ImageIcon(
					new URL(user.getBiggerProfileImageURL()))
						.getImage();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if(iconImg!=null){
			iconImg=iconImg.getScaledInstance(80,80,Image.SCALE_SMOOTH);
			JLabel iconLabel=new JLabel(new ImageIcon(iconImg));
			iconLabel.setOpaque(false);
			headerPanel.add(iconLabel);
		}
		
		
		//name,screenName
		JPanel namePanel=new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.Y_AXIS));
		namePanel.setOpaque(false);
		JTextArea name=new JTextArea(user.getName());
		name.setEditable(false);
		name.setOpaque(false);
		namePanel.add(name);
		JTextArea screenName=new JTextArea("@"+user.getScreenName());
		screenName.setEditable(false);
		screenName.setOpaque(false);
		namePanel.add(screenName);
		
		headerPanel.add(namePanel);
		
		
		
		//bio,locationとかが入る
		final JPanel footerPanel=new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		footerPanel.setOpaque(false);
		userInfoPanel.add(footerPanel);
		
		//bio
		//final JPanel bioPanel=new JPanel();
		//bioPanel.setLayout(new BorderLayout());
		final JTextPane bio=new JTextPane();
		bio.setContentType("text/html");
		bio.setText(getBioString());
		bio.setEditable(false);
		bio.setOpaque(false);
		//bioPanel.add(bio);
		footerPanel.add(bio);
		
		//location
		JTextArea location=new JTextArea("現在地 : "+user.getLocation());
		location.setEditable(false);
		location.setOpaque(false);
		footerPanel.add(location);
		
		//website
		JTextPane site=new JTextPane();
		site.setContentType("text/html");
		site.setText("<html><body>"
					+"<a href=\""+user.getURLEntity().getExpandedURL()+"\">"
					+user.getURLEntity().getDisplayURL()+"</a></body></html>");
		site.setEditable(false);
		site.setOpaque(false);
		footerPanel.add(site);
		
		
		
		
		
		
		userInfoPanel.addComponentListener(new ComponentAdapter(){
			int height=0;
			Dimension d=new Dimension();
			
			@Override
			public void componentResized(ComponentEvent e){
				int h=headerPanel.getPreferredSize().height+footerPanel.getPreferredSize().height;
				
				if(h!=height){
					d.setSize(0,h);
					userInfoPanel.setPreferredSize(d);
					height=h;
				}
				//System.out.println("userInfoPanel resized:"+headerPanel.getPreferredSize().height+"+"+footerPanel.getPreferredSize().height);
				//System.out.println(bio.getPreferredSize());
			}
		});
		
	}
	/*
	private void setUserTimelinePanel(){
		userTimelinePanel=new JPanel();
		userTimelinePanel.setLayout(new BoxLayout(userTimelinePanel,BoxLayout.Y_AXIS));
		
		JScrollPane pane=new JScrollPane();
		pane.getVerticalScrollBar().setUnitIncrement(20);
		JPanel timelinePanel=new JPanel();
		timelinePanel.setLayout(new BoxLayout(timelinePanel,BoxLayout.Y_AXIS));
		
		pane.setViewportView(timelinePanel);
		userTimelinePanel.add(pane);
		
		List<Status> list=null;
		try {
			list=twitter.getUserTimeline(user.getId());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if(list!=null){
			for(Status s:list){
				StatusPanel sp=new StatusPanel(s,twitter);
				timelinePanel.add(new StatusPanel(s,twitter));
				sp.revalidate();
			}
			timelinePanel.revalidate();
		}
		
	}
	
	private JScrollPane getUserTimelinePane(){
		JScrollPane returnPane=Window1.createTimelineScrollPane();
		JPanel panel=(JPanel)returnPane.getViewport().getView();
		
		List<Status> list=null;
		try {
			list=twitter.getUserTimeline(user.getId());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if(list!=null){
			for(Status s:list){
				StatusPanel sp=new StatusPanel(s,twitter);
				panel.add(sp);
				sp.revalidate();
			}
			panel.revalidate();
		}
		return returnPane;
	}
	*/
	
	private String getBioString(){//要改良
		return user.getDescription();
		/*
		return StringFormatter.getDisplayHTMLString(
				user.getDescription(),
				user.getDescriptionURLEntities());
		*/
	}
}
