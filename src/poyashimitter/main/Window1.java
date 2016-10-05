package poyashimitter.main;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import poyashimitter.PoyashURLStreamHandlerFactory;
import poyashimitter.Settings;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class Window1 extends JFrame {
	
	public static final String CONSUMER_KEY="52UiiTOApaJOpEMS5dBPF3lU6";
	public static final String CONSUMER_SECRET="PXei5Mz1b0RwcLbUhJxKbZ63CbmT2dYaRP1K7AgyyqrWZDmhMn";
	
	JPanel mainPanel;//全体のパネル
	
	JTabbedPane tabbedPane;
	
	HomeTimelinePane timelinePane;//TLが入る。スクロール領域
	MentionTimelinePane mentionPane;
	
	Twitter twitter;
	TwitterStream twitterStream;
	
	JPanel tweetSendPanel;
	
	
	public Window1() throws TwitterException{
		super();
		
		//OAuth
		twitter =new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(CONSUMER_KEY,CONSUMER_SECRET);
		
		AccessToken accessToken=null;
		if((accessToken=this.getAccessToken())==null)
			return;
		twitter.setOAuthAccessToken(accessToken);
		
		
		
		
		mainPanel=new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		//mainPanel.setLayout(new BorderLayout());
		this.add(mainPanel);
		
		setJMenuBar(getMenu());
		
		
		tabbedPane=new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
		mainPanel.add(tabbedPane);
		
		//home timeline
		timelinePane=new HomeTimelinePane(twitter);
		timelinePane.setCount(Settings.getTimelineOption(Settings.timeline.loadStatusNumber));
		tabbedPane.addTab("HOME",timelinePane);
		
		//mention
		mentionPane=new MentionTimelinePane(twitter);
		mentionPane.setCount(Settings.getTimelineOption(Settings.timeline.loadStatusNumber));
		tabbedPane.addTab("Reply",mentionPane);
		
		//search
		JPanel search=new SearchPanel(twitter,tabbedPane);
		tabbedPane.addTab("Search",search);
		
		
		tweetSendPanel=new TweetSendPanel(twitter);
		mainPanel.add(tweetSendPanel,BorderLayout.SOUTH);
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
				/*
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						timelinePane.setStatuses();
						mentionPane.setStatuses();
						new Thread(new Runnable(){
							@Override
							public void run() {
								try {
									Thread.sleep(300);
								} catch (InterruptedException e) {
									//e.printStackTrace();
								}
								timelinePane.getPanel().revalidate();
								//timelinePane.getVerticalScrollBar().setValue(timelinePane.getVerticalScrollBar().getMinimum());
							}
						}).start();
					}
				});
				*/
				timelinePane.setStatuses();
				mentionPane.setStatuses();
				
				
			}
		}).start();
		
		//twitterStream
		twitterStream=TwitterStreamFactory.getSingleton();
		twitterStream.setOAuthConsumer(CONSUMER_KEY,CONSUMER_SECRET);
		twitterStream.setOAuthAccessToken(accessToken);
		twitterStream.addListener(timelinePane.getStatusListener());
		twitterStream.addListener(mentionPane.getStatusListener());
		twitterStream.user();
		
	}
	/*
	public void setStatuses(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				timelinePane.setStatuses();
				mentionPane.setStatuses();
			}
		});
	}
	*/
	JMenuBar getMenu(){
		JMenuBar bar=new JMenuBar();//メニューバー
		//setJMenuBar(bar);
		
		JMenu[] menu={
				new JMenu("メニュー"),
				//else
		};
		final JMenuItem[][] menuItem={
				{//メニュー
					new JMenuItem("設定保存"),
					new JMenuItem("再起動"),
					new JMenuItem("終了"),
				},
				{
					//else
				},
		};
		ActionListener listener=new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!(e.getSource() instanceof JMenuItem))	return;
				
				String str=((JMenuItem)e.getSource()).getText();
				if(str.equals("再起動")){
					
				}else if(str.equals("終了")){
					System.exit(0);
				}else if(str.equals("設定保存")){
					Settings.save();
				}
			}
		};
		for(int i=0;i<menu.length;i++){
			bar.add(menu[i]);
			
			for(int j=0;j<menuItem[i].length;j++){
				menu[i].add(menuItem[i][j]);
				menuItem[i][j].addActionListener(listener);
			}
			
		}
		
		return bar;
	}
	
	private AccessToken getAccessToken() throws TwitterException{
		AccessToken ac=Settings.getAccessToken();
		if(ac==null){
			RequestToken requestToken = twitter.getOAuthRequestToken();
			System.out.println("Open the following URL and grant access to your account:");
			System.out.println(requestToken.getAuthorizationURL());
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
			try{
				String pin = br.readLine();
				if(pin.length() > 0){
					ac = twitter.getOAuthAccessToken(requestToken, pin);
				}else{
					ac = twitter.getOAuthAccessToken();
				}
				
				Settings.setAccessToken(ac);
				Settings.save();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (TwitterException te) {
				if(401 == te.getStatusCode()){
					System.out.println("Unable to get the access token.");
				}else{
					te.printStackTrace();
				}
			}
		}
		
		return ac;
	}
	
	
	public static void main(String[] args) throws TwitterException {
		//System.setProperty("awt.useSystemAAFontSettings", "on");//アンチエイリアス
		
		URL.setURLStreamHandlerFactory(new PoyashURLStreamHandlerFactory());
		
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				Window1 app=null;
				try {
					app = new Window1();
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}
				if(app==null)	return;
				
				app.setDefaultCloseOperation(EXIT_ON_CLOSE);
				app.setPreferredSize(Settings.getSize(Settings.position.size.mainWindow));
				app.setBounds(
					new Rectangle(
							Settings.getPosition(Settings.position.mainWindow),
							Settings.getSize(Settings.position.size.mainWindow)
					)
				);
				app.setTitle("ぽやしみった～");
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					SwingUtilities.updateComponentTreeUI(app);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					//e.printStackTrace();
				}
				app.pack();
				app.setVisible(true);
				
				//app.setStatuses();
			}
		});
		
		
		/*
		UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();
		for(UIManager.LookAndFeelInfo in:infos){
			System.out.println(in);
		}
		*/
	}

}

