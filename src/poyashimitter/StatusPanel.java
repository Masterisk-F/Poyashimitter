package poyashimitter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import poyashimitter.media.MediaWindow;
import poyashimitter.user.UserWindow;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;



/*

やること
・RTした人の情報を見やすく・・・ok
・時刻の表示をより多機能に・・・ok
・引用statusの下に元ツイートのbottomPanelが表示されるの直す・・・ok

・リプライのTL表示
・画像が3枚以上のとき2段で表示・・・ok
・画像クリックで画像window開く・・・ok
・画像サムネイル取得は別スレッドで・・・ok

・アイコン・名前クリックで垢情報window開く(RT垢含め)

・windowの幅を狭くしたとき、アカウント名・RTした人のアカウント名が改行されない
・
・
・
・


*/
public class StatusPanel extends JPanel{
	Status s;
	Status RTStatus;//RT"した人"に関してのStatus
	Status quotedStatus;//リプライ元・引用元Status
	
	Twitter twitter;
	
	JPanel iconPanel;
	
	JPanel tweetPanel;//名前とツイート内容
	
	JPanel namePanel;//名前
	
	//JPanel textPanel;//ツイート本文とRTした人
	JEditorPane textPane;//test
	
	JPanel RTPanel;//test
	
	JPanel mediaPanel;
	
	int mediaPosition;
	static int MEDIA_POSITION_RIGHT=0;
	static int MEDIA_POSITION_BOTTOM=1;
	
	
	JPanel bottomPanel;//infoPanel,buttonPanelをいれる
	
	ArrayList<StatusPanel> quotedList;//リプライ・引用のリスト
	boolean isInStatusPanel;
	
	ComponentListener componentListener=new ComponentAdapter(){
		int height=0;
		Dimension d=new Dimension();
		
		public void componentResized(ComponentEvent e){
			for(StatusPanel sp:quotedList){
				sp.revalidate();
			}
			int h=0;
			if(mediaPosition==MEDIA_POSITION_RIGHT){
				h+=Math.max(
						textPane.getPreferredSize().height,
						mediaPanel.getPreferredSize().height);
			}else{
				h+=textPane.getPreferredSize().height
						+mediaPanel.getPreferredSize().height;
			}
			h+=RTPanel.getPreferredSize().height;
			//System.out.println("contentsPanel height : "+contentsPanel.getPreferredSize().height);
			
			//System.out.println("textPanel height:"+textPanel.getPreferredSize().height+" "+s.getText());
			for(StatusPanel sp:quotedList){
				h+=sp.getPreferredSize().height;
			}
			
			h+=namePanel.getPreferredSize().height
					+bottomPanel.getPreferredSize().height;
			if(height!=h){
				d.setSize(0,h);
				setPreferredSize(d);
				height=h;
			}
			
		}
		
	};
	
	private ActionListener buttonListener=new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!(e.getSource() instanceof AbstractButton))	return;
			
			String str=((AbstractButton)e.getSource()).getText();
			
			if(str.equals("リプライ")){
				
			}else if(str.equals("非公式RT")){
				
			}else if(str.equals("fav&RT")){
				try {
					twitter.retweetStatus(s.getId());
					twitter.createFavorite(s.getId());
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}
			}else if(str.equals("パクる")){
				try {
					twitter.updateStatus(s.getText());
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}
			}else if(str.equals("削除")){
				
			}else if(str.equals("RT")){
				try {
					twitter.retweetStatus(s.getId());
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}
			}else if(str.equals("Fav")){
				try {
					twitter.createFavorite(s.getId());
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}
			}
		}
	};
	
	public StatusPanel(Status status,Twitter t){
		this(status,t,false);
	}
	
	public StatusPanel(Status status,Twitter t,boolean isInStatusPanel){//twitter:accesstokenなど設定済みのもの
		super();
		twitter=t;
		this.isInStatusPanel=isInStatusPanel;
		if(status.isRetweet()){
			s=status.getRetweetedStatus();
			RTStatus=status;
		}else{
			s=status;
		}
		quotedStatus=s.getQuotedStatus();	//無ければnullっぽい
		
		String pos=Settings.getMediaPosition();
		if(pos.equals("right")){
			mediaPosition=MEDIA_POSITION_RIGHT;
		}else{
			mediaPosition=MEDIA_POSITION_BOTTOM;
		}
		
		setLayout(new BorderLayout());
		
		iconPanel=getIconPanel();
		tweetPanel=getTweetPanel();
		
		add(iconPanel,BorderLayout.WEST);
		add(tweetPanel,BorderLayout.CENTER);
		
		
		this.addComponentListener(componentListener);
		
	}
	/*
	@Override
	public void doLayout(){//テスト
		System.out.println("doLayout()");
		for(StatusPanel sp:quotedList){
			sp.revalidate();
		}
		int h=0;
		if(mediaPosition==MEDIA_POSITION_RIGHT){
			h+=Math.max(
					textPanel.getPreferredSize().height,
					mediaPanel.getPreferredSize().height);
		}else{
			h+=textPanel.getPreferredSize().height
					+mediaPanel.getPreferredSize().height;
		}
		
		//System.out.println(quotedList.size());
		for(StatusPanel sp:quotedList){
			h+=sp.getPreferredSize().height;
		}
		
		h+=namePanel.getPreferredSize().height
				+bottomPanel.getPreferredSize().height;
		Dimension d=new Dimension(0,h);
		setPreferredSize(d);
		
		super.doLayout();
		
	}
	*/
	private JPanel getIconPanel(){
		
		JPanel returnPanel=new JPanel();//iconPanel
		//returnPanel.setLayout(new BoxLayout(returnPanel,BoxLayout.Y_AXIS));
		returnPanel.setLayout(new BorderLayout());
		returnPanel.setOpaque(true);
		returnPanel.setBackground(this.getFrameBackgroundColor());
		
		URL url=null;
		try {
			url=new URL(s.getUser().getBiggerProfileImageURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if(url!=null){
			ImageIcon icon=new ImageIcon(url);
			Image img=icon.getImage().getScaledInstance(40, -1, Image.SCALE_SMOOTH);	//アイコンサイズ変更
			JLabel iconLabel=new JLabel(new ImageIcon(img));
			//returnPanel.add(iconLabel);
			returnPanel.add(iconLabel,BorderLayout.NORTH);
			
			iconLabel.addMouseListener(new MouseAdapter(){
				@Override
				public void mousePressed(MouseEvent e){
					if(e.getClickCount()!=1)	return;
					
					new UserWindow(s.getUser(),twitter);
				}
			});
		}
		
		return returnPanel;
	}
	/*
	private JPanel getContentsPanel(){
		JPanel returnPanel=new JPanel();
		SpringLayout layout=new SpringLayout();
		returnPanel.setLayout(layout);
		
		JEditorPane text;//ツイート本文
		text=new JEditorPane();
		text.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		text.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		text.setContentType("text/html");
		text.setText(getDisplayString());
		
		JLabel[] media=getMediaLabels();
		
		returnPanel.add(text);
		for(JLabel m:media){
			returnPanel.add(m);
		}
		
		if(mediaPosition==MEDIA_POSITION_RIGHT){
			layout.putConstraint(SpringLayout.NORTH,text,0,SpringLayout.NORTH,returnPanel);
			layout.putConstraint(SpringLayout.SOUTH,text,0,SpringLayout.SOUTH,returnPanel);
			layout.putConstraint(SpringLayout.WEST,text,0,SpringLayout.WEST,returnPanel);
			//layout.putConstraint(SpringLayout.EAST,text,0,SpringLayout.WEST,media[0]);
			
			switch(media.length){
			case 0:
				layout.putConstraint(SpringLayout.EAST,text,0,SpringLayout.EAST,returnPanel);
				break;
			case 1:
				layout.putConstraint(SpringLayout.EAST,text,0,SpringLayout.WEST,media[0]);
				
				layout.putConstraint(SpringLayout.EAST,media[0],0,SpringLayout.WEST,returnPanel);
				layout.putConstraint(SpringLayout.NORTH,media[0],0,SpringLayout.NORTH,returnPanel);
				break;
			
			
			case 4:
				layout.putConstraint(SpringLayout.NORTH,media[3],0,SpringLayout.SOUTH,media[1]);
				layout.putConstraint(SpringLayout.EAST,media[3],0,SpringLayout.EAST,media[1]);
			case 3:
				layout.putConstraint(SpringLayout.NORTH,media[2],0,SpringLayout.SOUTH,media[0]);
				layout.putConstraint(SpringLayout.EAST,media[2],0,SpringLayout.EAST,media[0]);
			case 2:
				layout.putConstraint(SpringLayout.EAST,media[0],0,SpringLayout.WEST,media[1]);
				layout.putConstraint(SpringLayout.EAST,media[1],0,SpringLayout.WEST,returnPanel);
				
				layout.putConstraint(SpringLayout.NORTH,media[0],0,SpringLayout.NORTH,returnPanel);
				layout.putConstraint(SpringLayout.NORTH,media[1],0,SpringLayout.NORTH,returnPanel);
				break;
			}
			
		}else{
			
		}
		
		return returnPanel;
	}
	*/
	private JPanel getTweetPanel(){
		
		JPanel contentsPanel=new JPanel();//本文とメディア
		//SpringLayout layout=new SpringLayout();
		//contentsPanel.setLayout(layout);
		contentsPanel.setLayout(new BorderLayout());
		contentsPanel.setOpaque(false);
		
		textPane=getTextPane();
		//textPanel=getTextPanel();//要検討
		mediaPanel=getMediaPanel();//要検討
		//contentsPanel.add(textPanel, BorderLayout.CENTER);
		contentsPanel.add(textPane, BorderLayout.CENTER);
		//contentsPanel.add(textPane);
		if(mediaPosition==MEDIA_POSITION_RIGHT){
			contentsPanel.add(mediaPanel, BorderLayout.EAST);
			/*
			contentsPanel.add(mediaPanel);
			
			layout.putConstraint(SpringLayout.NORTH,textPane,0,SpringLayout.NORTH,contentsPanel);
			layout.putConstraint(SpringLayout.WEST,textPane,0,SpringLayout.WEST,contentsPanel);
			//layout.putConstraint(SpringLayout.SOUTH,textPane,0,SpringLayout.SOUTH,contentsPanel);
			layout.putConstraint(SpringLayout.EAST,textPane,0,SpringLayout.WEST,mediaPanel);
			
			
			//layout.putConstraint(SpringLayout.WEST,mediaPanel,0,SpringLayout.EAST,textPane);
			layout.putConstraint(SpringLayout.NORTH,mediaPanel,0,SpringLayout.NORTH,contentsPanel);
			layout.putConstraint(SpringLayout.EAST,mediaPanel,0,SpringLayout.EAST,contentsPanel);
			layout.putConstraint(SpringLayout.SOUTH,mediaPanel,0,SpringLayout.SOUTH,contentsPanel);
			*/
			
		}else{
			contentsPanel.add(mediaPanel, BorderLayout.SOUTH);
		}
		
		//contentsPanel=getContentsPanel();
		namePanel=getNamePanel();//要検討
		bottomPanel=getBottomPanel();//要検討
		
		
		JPanel returnPanel=new JPanel();//tweetPanel
		returnPanel.setLayout(new BoxLayout(returnPanel,BoxLayout.Y_AXIS));
		returnPanel.setOpaque(true);
		returnPanel.setBackground(this.getBackgroundColor());
		
		
		
		final JPanel qtPanel=new JPanel();
		qtPanel.setLayout(new BoxLayout(qtPanel,BoxLayout.Y_AXIS));
		
		/*
		JPanel returnPanel=new JPanel();//tweetPanel
		SpringLayout layout=new SpringLayout();
		returnPanel.setLayout(layout);
		returnPanel.setOpaque(true);
		returnPanel.setBackground(this.getBackgroundColor());
		layout.putConstraint(SpringLayout.NORTH,namePanel,0,SpringLayout.NORTH,returnPanel);
		layout.putConstraint(SpringLayout.WEST,namePanel,0,SpringLayout.WEST,returnPanel);
		layout.putConstraint(SpringLayout.EAST,namePanel,0,SpringLayout.EAST,returnPanel);
		layout.putConstraint(SpringLayout.SOUTH,namePanel,0,SpringLayout.NORTH,contentsPanel);
		
		layout.putConstraint(SpringLayout.NORTH,contentsPanel,namePanel.getPreferredSize().height,SpringLayout.NORTH,returnPanel);
		//layout.putConstraint(SpringLayout.NORTH,contentsPanel,0,SpringLayout.SOUTH,namePanel);
		layout.putConstraint(SpringLayout.WEST,contentsPanel,0,SpringLayout.WEST,returnPanel);
		layout.putConstraint(SpringLayout.EAST,contentsPanel,0,SpringLayout.EAST,returnPanel);
		//layout.putConstraint(SpringLayout.SOUTH,contentsPanel,0,SpringLayout.NORTH,bottomPanel);
		
		layout.putConstraint(SpringLayout.NORTH,bottomPanel,0,SpringLayout.SOUTH,contentsPanel);
		layout.putConstraint(SpringLayout.EAST,bottomPanel,0,SpringLayout.EAST,returnPanel);
		layout.putConstraint(SpringLayout.WEST,bottomPanel,0,SpringLayout.WEST,returnPanel);
		layout.putConstraint(SpringLayout.SOUTH,bottomPanel,0,SpringLayout.SOUTH,qtPanel);
		
		//layout.putConstraint(SpringLayout.NORTH,qtPanel,0,SpringLayout.SOUTH,bottomPanel);
		layout.putConstraint(SpringLayout.EAST,qtPanel,0,SpringLayout.EAST,returnPanel);
		layout.putConstraint(SpringLayout.WEST,qtPanel,0,SpringLayout.WEST,returnPanel);
		layout.putConstraint(SpringLayout.SOUTH,qtPanel,0,SpringLayout.SOUTH,returnPanel);
		*/
		
		
		
		returnPanel.add(namePanel);
		returnPanel.add(contentsPanel);
		
		RTPanel=getRTPanel();
		returnPanel.add(RTPanel);
		
		returnPanel.add(bottomPanel);
		
		returnPanel.add(qtPanel);
		
		//qt,reply
		quotedList=new ArrayList<StatusPanel>();
		if(!isInStatusPanel){
			/*
			Status qts=quotedStatus;
			while(qts!=null){
				StatusPanel sp=new StatusPanel(qts,twitter,true);
				quotedList.add(sp);
				returnPanel.add(sp);
				sp.revalidate();
				qts=qts.getQuotedStatus();
			}
			
			try {
				Status rep=s;
				while(rep.getInReplyToStatusId()>=0){
					rep=twitter.showStatus(rep.getInReplyToStatusId());//よくexception発生する
					StatusPanel sp=new StatusPanel(rep,twitter,true);
					quotedList.add(sp);
					returnPanel.add(sp);
					sp.revalidate();
				}
			} catch (TwitterException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			*/
			
			//第2案
			Status status=null;
			if(s.getInReplyToStatusId()>=0){
				try {
					status=twitter.showStatus(s.getInReplyToStatusId());
				} catch (TwitterException e) {
					//e.printStackTrace();
				}
			}
			
			//System.out.println(s.getInReplyToStatusId());
			
			if(quotedStatus!=null && status==null)
				status=quotedStatus;
			
			if(status!=null){//reply,qtあれば始めは1つだけ表示
				StatusPanel sp=new StatusPanel(status,twitter,true);
				quotedList.add(sp);
				//returnPanel.add(sp);
				qtPanel.add(sp);
				if(status.getInReplyToStatusId()>=0 || status.getQuotedStatus()!=null){
					//さらにreply,qtあればボタン表示、ボタンを押すとすべて表示する
					final JButton button=new JButton("▼");
					button.setMargin(new Insets(0,0,0,0));
					sp.iconPanel.add(button,BorderLayout.SOUTH);
					button.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							button.setEnabled(false);
							
							//close
							if(button.getText().equals("▲")){
								for(int i=1;i<quotedList.size();i++){
									//returnPanel.remove(quotedList.get(i));
									qtPanel.remove(quotedList.get(i));
								}
								for(int i=1;i<quotedList.size();i++){
									quotedList.remove(1);
								}
								quotedList.get(0).revalidate();
								
								button.setText("▼");
								button.setEnabled(true);
								
								componentListener.componentResized(null);
								
								return;
							}
							
							//open
							new SwingWorker<Object,Status>(){
								@Override
								protected Object doInBackground() throws Exception {
									System.out.println("doInBackground()");
									
									Status status=quotedList.get(0).s;
									//System.out.println(status.getText());
									while(status.getInReplyToStatusId()>=0 || status.getQuotedStatus()!=null){
										if(status.getInReplyToStatusId()>=0){
											try {
												status=twitter.showStatus(status.getInReplyToStatusId());
											} catch (TwitterException e1) {
												//e1.printStackTrace();
											}
										}
										else if(status.getQuotedStatus()!=null)
											status=status.getQuotedStatus();
										
										if(status==null)	break;
										
										publish(status);
									}
									return null;
								}
								
								@Override
								protected void done(){
									button.setEnabled(true);
									button.setText("▲");
									
								}
								@Override
								protected void process(List<Status> chunks){
									for(Status status:chunks){
										System.out.println(status.getText());
										StatusPanel sp=new StatusPanel(status,twitter,true);
										quotedList.add(sp);
										//returnPanel.add(sp);
										qtPanel.add(sp);
										
										sp.revalidate();
										//returnPanel.revalidate();
										
										componentListener.componentResized(null);
									}
									
								}
								
							}.execute();
						}
						/*
						synchronized void open(){
							Status status=quotedList.get(0).s;
							//System.out.println(status.getText());
							while(status.getInReplyToStatusId()>=0 || status.getQuotedStatus()!=null){
								if(status.getInReplyToStatusId()>=0){
									try {
										status=twitter.showStatus(status.getInReplyToStatusId());
									} catch (TwitterException e1) {
										//e1.printStackTrace();
									}
								}
								else if(status.getQuotedStatus()!=null)
									status=status.getQuotedStatus();
								
								if(status==null)	break;
								
								System.out.println(status.getText());
								StatusPanel sp=new StatusPanel(status,twitter,true);
								quotedList.add(sp);
								returnPanel.add(sp);
								sp.revalidate();
								//returnPanel.revalidate();
							}
						}
						
						synchronized void close(){
							for(int i=1;i<quotedList.size();i++){
								returnPanel.remove(quotedList.get(i));
								
							}
							for(int i=1;i<quotedList.size();i++){
								quotedList.remove(1);
							}
							quotedList.get(0).revalidate();
						}
						*/
					});
					
				}
				
			}
			
		}
		return returnPanel;
	}
	
	private JEditorPane getTextPane(){
		JEditorPane text;//ツイート本文
		text=new JEditorPane();
		text.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		text.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		text.setContentType("text/html");
		text.setText(this.getDisplayString());
		//text.setText(StringFormatter.getDisplayHTMLString2(s.getText(),s.getURLEntities(),s.getExtendedMediaEntities()));
		text.setEditable(false);
		text.setOpaque(false);
		
		text.addHyperlinkListener(new HyperlinkListener(){
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED){
					URL url=e.getURL();
					
					if(url.getProtocol().equals("poyash")){
						if(url.getHost().equals("media")){
							int i=Integer.valueOf(url.getPath().split("[/.]")[1]);
							new MediaWindow(s.getExtendedMediaEntities(),i);
						}else if(url.getHost().equals("user")){
							long id=Long.valueOf(url.getPath().split("[/.]")[1]);
							try {
								new UserWindow(id,twitter);
							} catch (TwitterException e1) {
								e1.printStackTrace();
							}
						}
						else if(url.getHost().equals("hashtag")){
							//まだ
						}
						return;
					}
					
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		return text;
	}
	/*
	private JPanel getTextPanel(){
		JPanel returnPanel=new JPanel();//textPanel
		returnPanel.setLayout(new BorderLayout());
		//contentsPanel.add(returnPanel, BorderLayout.CENTER);
		
		returnPanel.add(getRTPanel(), BorderLayout.SOUTH);
		
		returnPanel.setOpaque(false);
		JEditorPane text;//ツイート本文
		text=new JEditorPane();
		text.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		text.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		text.setContentType("text/html");
		text.setText(this.getDisplayString());
		//text.setText(StringFormatter.getDisplayHTMLString2(s.getText(),s.getURLEntities(),s.getExtendedMediaEntities()));
		text.setEditable(false);
		text.setOpaque(false);
		
		returnPanel.add(text,BorderLayout.NORTH);
		
		text.addHyperlinkListener(new HyperlinkListener(){
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED){
					URL url=e.getURL();
					
					if(url.getProtocol().equals("poyash")){
						if(url.getHost().equals("media")){
							int i=Integer.valueOf(url.getPath().split("[/.]")[1]);
							new MediaWindow(s.getExtendedMediaEntities(),i);
						}else if(url.getHost().equals("user")){
							long id=Long.valueOf(url.getPath().split("[/.]")[1]);
							try {
								new UserWindow(id,twitter);
							} catch (TwitterException e1) {
								e1.printStackTrace();
							}
						}
						else if(url.getHost().equals("hashtag")){
							//まだ
						}
						return;
					}
					
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		return returnPanel;
	}
	*/
	/*
	private JLabel[] getMediaLabels(){
		final MediaEntity[] m=s.getExtendedMediaEntities();
		final JLabel[] media=new JLabel[m.length];
		final int mediaSize=Settings.getTimelineOption(Settings.timeline.mediaSize);
		Dimension d=new Dimension(mediaSize,mediaSize);
		for(int i=0;i<media.length;i++){
			media[i]=new JLabel("Now loading...");
			media[i].setOpaque(false);
			media[i].setPreferredSize(d);
			media[i].setSize(d);
			media[i].setHorizontalAlignment(JLabel.CENTER);
			
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
						url=new URL(m[i].getMediaURL()+":small");
					} catch (MalformedURLException e) {
						e.printStackTrace();
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() {
								media[i].setText("failed");
							}
						});
					}
					
					if(url!=null){
						
						Image img=ImageIO.read(url);
						if(img.getWidth(null)>img.getHeight(null)){
							img=img.getScaledInstance(mediaSize,-1, Image.SCALE_SMOOTH);
						}else{
							img=img.getScaledInstance(-1,mediaSize, Image.SCALE_SMOOTH);
						}
						MediaTracker tracker=new MediaTracker(media[i]);
						tracker.addImage(img, i);
						tracker.waitForAll();
						return img;
					}
					return null;
					
				}
				
				@Override
				protected void done(){
					Image img = null;
					try {
						img = get();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} catch (ExecutionException e1) {
						e1.printStackTrace();
					}
					if(img==null)	return;
					
					media[i].setIcon(new ImageIcon(img));
					media[i].setText("");
					media[i].addMouseListener(new MouseAdapter(){
						int i;
						MouseListener setI(int i){
							this.i=i;
							return this;
						}
						@Override
						public void mousePressed(MouseEvent e){
							if(e.getClickCount()!=1)	return;
							
							new MediaWindow(m,i);
							
						}
					}.setI(i));
				}
				
			}.setI(i).execute();
		}
		
		return media;
		
	}
	*/
	
	private JPanel getMediaPanel(){
		//メディア
		JPanel returnPanel=new JPanel(null);
		returnPanel.setOpaque(false);
		final MediaEntity[] m=s.getExtendedMediaEntities();
		/*
		GridBagLayout gbl=new GridBagLayout();
		if(mediaPosition==MEDIA_POSITION_RIGHT){
			if(m.length<=1){
				returnPanel.setLayout(gbl);
			}else{
				returnPanel.setLayout(gbl);
			}
		}else{
			returnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		}
		*/
		final JLabel[] media=new JLabel[m.length];
		final int mediaSize=Settings.getTimelineOption(Settings.timeline.mediaSize);
		Dimension d=new Dimension(mediaSize,mediaSize);
		if(m.length==0){
			Dimension dim=new Dimension(0,0);
			returnPanel.setSize(dim);
			returnPanel.setPreferredSize(dim);
		}else{
			Dimension dim=new Dimension(100+100*Math.min(1,m.length-1),100+100*(m.length/3));
			returnPanel.setSize(dim);
			returnPanel.setPreferredSize(dim);
			returnPanel.setMinimumSize(dim);
			returnPanel.setMaximumSize(dim);
			//System.out.println(m.length+" : "+dim);
		}
		
		for(int i=0;i<media.length;i++){
			media[i]=new JLabel("Now loading...");
			media[i].setOpaque(false);
			//media[i].setPreferredSize(d);
			//media[i].setSize(d);
			media[i].setHorizontalAlignment(JLabel.CENTER);
			
			if(mediaPosition==MEDIA_POSITION_RIGHT){
				media[i].setBounds(100*(i%2),100*(i/2), mediaSize, mediaSize);
				//System.out.println("pos : "+i+" "+media[i].getBounds());
			}else{}
			
			returnPanel.add(media[i]);
			/*
			if(mediaPosition==MEDIA_POSITION_RIGHT){
				GridBagConstraints gbc=new GridBagConstraints();
				gbc.gridx=i%2;
				gbc.gridy=i/2;
				gbl.setConstraints(media[i],gbc);
			}
			*/
			
			
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
						url=new URL(m[i].getMediaURL()+":small");
					} catch (MalformedURLException e) {
						e.printStackTrace();
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() {
								media[i].setText("failed");
							}
						});
					}
					
					if(url!=null){
						
						Image img=ImageIO.read(url);
						if(img.getWidth(null)>img.getHeight(null)){
							img=img.getScaledInstance(mediaSize,-1, Image.SCALE_SMOOTH);
						}else{
							img=img.getScaledInstance(-1,mediaSize, Image.SCALE_SMOOTH);
						}
						MediaTracker tracker=new MediaTracker(media[i]);
						tracker.addImage(img, i);
						tracker.waitForAll();
						return img;
					}
					return null;
					
				}
				
				@Override
				protected void done(){
					Image img = null;
					try {
						img = get();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} catch (ExecutionException e1) {
						e1.printStackTrace();
					}
					if(img==null)	return;
					
					media[i].setIcon(new ImageIcon(img));
					media[i].setText("");
					media[i].addMouseListener(new MouseAdapter(){
						int i;
						MouseListener setI(int i){
							this.i=i;
							return this;
						}
						@Override
						public void mousePressed(MouseEvent e){
							if(e.getClickCount()!=1)	return;
							
							new MediaWindow(m,i);
							
						}
					}.setI(i));
				}
				
			}.setI(i).execute();
		}
		
		returnPanel.setMinimumSize(returnPanel.getPreferredSize());
		//returnPanel.setSize(returnPanel.getPreferredSize());
		
		/*
		//contentsPanel.add(returnPanel, BorderLayout.EAST);
		new Thread(new Runnable(){
			@Override
			public void run() {
				for(int i=0;i<m.length;i++){
					URL url=null;
					try {
						url=new URL(m[i].getMediaURL()+":small");
					} catch (MalformedURLException e) {
						e.printStackTrace();
						media[i].setText("failed");
					}
					if(url!=null){
						ImageIcon icon=new ImageIcon(url);
						Image img;
						if(icon.getIconWidth()>icon.getIconHeight()){
							img=icon.getImage().getScaledInstance(mediaSize,-1, Image.SCALE_SMOOTH);
						}else{
							img=icon.getImage().getScaledInstance(-1,mediaSize, Image.SCALE_SMOOTH);
						}
						media[i].setIcon(new ImageIcon(img));
						media[i].setText("");
						media[i].addMouseListener(new MouseAdapter(){
							int i;
							MouseListener setI(int i){
								this.i=i;
								return this;
							}
							@Override
							public void mousePressed(MouseEvent e){
								if(e.getClickCount()!=1)	return;
								
								new Thread(new Runnable(){
									@Override
									public void run(){
										new MediaWindow(m,i);
									}
								}).start();
								
							}
						}.setI(i));
						
					}
				}
				
			}
		}).start();
		
		*/
		
		return returnPanel;
	}
	
	private JPanel getRTPanel(){
		//RTした人のicon,name
		JPanel returnPanel=new JPanel();
		if(RTStatus!=null){
			int size=Settings.getTimelineOption(Settings.timeline.RTIconSize);
			returnPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
			returnPanel.setOpaque(false);
			returnPanel.setPreferredSize(new Dimension(0,size));
			MouseListener listener=new MouseAdapter(){
				@Override
				public void mousePressed(MouseEvent e){
					if(e.getClickCount()!=1)	return;
					
					new UserWindow(RTStatus.getUser(),twitter);
				}
			};
			//icon
			URL url=null;
			try {
				url=new URL(RTStatus.getUser().getBiggerProfileImageURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if(url!=null){
				ImageIcon icon=new ImageIcon(url);
				Image img=icon.getImage().getScaledInstance(
						size, -1, Image.SCALE_SMOOTH);	//アイコンサイズ変更
				JLabel RTIconLabel=new JLabel(new ImageIcon(img));
				RTIconLabel.addMouseListener(listener);
				returnPanel.add(RTIconLabel);
			}
			
			//name
			JLabel RTName=new JLabel("(RT by "+RTStatus.getUser().getName()
					+" @"+RTStatus.getUser().getScreenName()+") ");
			RTName.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));
			returnPanel.add(RTName);
			RTName.addMouseListener(listener);
			//textPanel.add(RTPanel, BorderLayout.SOUTH);
		}else{
			returnPanel.setPreferredSize(new Dimension(0,0));
		}
		
		return returnPanel;
	}
	
	private JPanel getNamePanel(){
		
		JTextArea name=new JTextArea(s.getUser().getName());
		name.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,15));
		name.setEnabled(false);
		name.setDisabledTextColor(Color.black);
		name.setOpaque(false);
		
		JTextArea screenName=new JTextArea(" @"+s.getUser().getScreenName()+" ");
		screenName.setEnabled(false);
		screenName.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14));
		screenName.setDisabledTextColor(Color.black);
		screenName.setOpaque(false);
		
		String rtFavStr = null;
		if(s.getRetweetCount()!=0 || s.getFavoriteCount()!=0){
			rtFavStr="(";
			if(s.getRetweetCount()!=0){
				rtFavStr+=" RT "+s.getRetweetCount()+" ";
			}
			if(s.getFavoriteCount()!=0){
				rtFavStr+=" Fav "+s.getFavoriteCount()+" ";
			}
			rtFavStr+=")";
		}
		JTextArea RTFavState=new JTextArea(rtFavStr);
		RTFavState.setEditable(false);
		RTFavState.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		RTFavState.setEnabled(false);
		RTFavState.setDisabledTextColor(Color.black);
		RTFavState.setOpaque(false);
		
		JPanel returnPanel=new JPanel();
		returnPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		returnPanel.setOpaque(true);
		returnPanel.setBackground(this.getFrameBackgroundColor());
		returnPanel.add(name);
		returnPanel.add(screenName);
		returnPanel.add(RTFavState);
		returnPanel.setPreferredSize(new Dimension(0,returnPanel.getPreferredSize().height));
		returnPanel.setMinimumSize(new Dimension(0,returnPanel.getPreferredSize().height));
		
		return returnPanel;
	}
	
	private JPanel getBottomPanel(){
		
		JPanel infoPanel=new JPanel();//via,timeを入れる
		infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		infoPanel.setOpaque(false);
		
		JLabel via=new JLabel("<html><body>"+s.getSource()+"</html></body>");//クライアント名
		via.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13));
		via.setOpaque(false);
		infoPanel.add(via);
		
		JLabel time=new JLabel(getTimeString());//時刻・・・RTはRT元のツイート時刻 
		time.setOpaque(false);
		infoPanel.add(time);
		
		
		JPanel buttonPanel=new JPanel();//RT,favするボタンを入れる
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		buttonPanel.setOpaque(false);
		
		Insets insets=new Insets(0,3,0,3);//ボタンの空白
		
		final JButton menuButton=new JButton("...");
		//menuButton.setPreferredSize(new Dimension(40,15));
		menuButton.addActionListener(new ActionListener(){
			JPopupMenu popup;
			
			public ActionListener init(){
				popup= new JPopupMenu();
				JMenuItem[] menuItem={
						new JMenuItem("リプライ"),
						new JMenuItem("非公式RT"),
						new JMenuItem("fav&RT"),
						new JMenuItem("パクる"),
						new JMenuItem("削除"),
						};
				for(JMenuItem item:menuItem){
					item.addActionListener(buttonListener);
					popup.add(item);
				}
				return this;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				popup.show(menuButton,0,menuButton.getSize().height);
			}
		}.init());
		menuButton.setMargin(insets);
		buttonPanel.add(menuButton);
		
		JButton RTButton=new JButton("RT");//RTボタン
		//RTButton.setPreferredSize(new Dimension(50,15));//とりあえずのサイズ設定
		RTButton.addActionListener(buttonListener);
		RTButton.setMargin(insets);
		buttonPanel.add(RTButton);
		
		JButton favButton=new JButton("Fav");//favボタン
		//favButton.setPreferredSize(new Dimension(50,15));
		favButton.addActionListener(buttonListener);
		favButton.setMargin(insets);
		buttonPanel.add(favButton);
		
		JPanel returnPanel=new JPanel();
		returnPanel.setOpaque(false);
		returnPanel.setLayout(new BorderLayout());
		returnPanel.add(infoPanel,BorderLayout.WEST);
		returnPanel.add(buttonPanel,BorderLayout.EAST);
		returnPanel.setPreferredSize(new Dimension(0,returnPanel.getPreferredSize().height));
		
		return returnPanel;
	}
	
	private String getDisplayString(){
		ArrayList<URLEntity> list=new ArrayList<URLEntity>();
		list.addAll(Arrays.asList(s.getURLEntities()));
		list.addAll(Arrays.asList(s.getExtendedMediaEntities()));
		
		return StringFormatter.getDisplayHTMLString(s);
	}
	
	private String getTimeString(){
		return StringFormatter.getTimeString(s.getCreatedAt());
	}
	
	
	private Color getFrameBackgroundColor(){
		try {
			if(s.getUser().getId()==twitter.getId()){
				return Settings.getColor(Settings.color.frameBackground.myself);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if(RTStatus!=null){
			return Settings.getColor(Settings.color.frameBackground.RT);
		}
		if(s.getInReplyToStatusId()>=0){
			return Settings.getColor(Settings.color.frameBackground.mention);
		}
		if(quotedStatus!=null){
			return Settings.getColor(Settings.color.frameBackground.quoted);
		}
		return Settings.getColor(Settings.color.frameBackground.main);
		
	}
	private Color getBackgroundColor(){
		
		try {
			if(s.getUser().getId()==twitter.getId()){
				return Settings.getColor(Settings.color.background.myself);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if(RTStatus!=null){
			return Settings.getColor(Settings.color.background.RT);
		}
		if(s.getInReplyToStatusId()>=0){
			return Settings.getColor(Settings.color.background.mention);
		}
		if(quotedStatus!=null){
			return Settings.getColor(Settings.color.background.quoted);
		}
		return Settings.getColor(Settings.color.background.main);
		
		
	}
	
}
