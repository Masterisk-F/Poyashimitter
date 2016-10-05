package poyashimitter.main;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetSendPanel extends JPanel {
	Twitter twitter;
	JTextArea tweetTextArea;
	JButton sendButton;
	
	JPanel mediaPanel;//ツイートに含める画像とか
	JPanel mentionStatusPanel;//リプライ先のツイート
	
	List<File> mediaFiles;
	
	public TweetSendPanel(Twitter twitter){
		super();
		
		this.twitter=twitter;
		mediaFiles=new ArrayList<File>(4);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		tweetTextArea=getTweetTextArea();
		sendButton=getSendButton();
		
		JPanel headerPanel=new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel,BoxLayout.X_AXIS));
		headerPanel.add(tweetTextArea);
		headerPanel.add(sendButton);
		add(headerPanel);
		
		mediaPanel=getMediaPanel();
		add(mediaPanel);
		
		mentionStatusPanel=getMentionStatusPanel();
		add(mentionStatusPanel);
		
		/*
		addComponentListener(new ComponentAdapter(){
			Dimension d=new Dimension();
			@Override
			public void componentResized(ComponentEvent e){
				//if(tweetTextArea==null || sendButton==null)	return;
				
				int h=(int)Math.max(
						tweetTextArea.getPreferredSize().getHeight(),
						sendButton.getPreferredSize().getHeight());
				
				h+=mediaPanel.getPreferredSize().getHeight()+mentionStatusPanel.getHeight();
				d.setSize(0,h);
				setPreferredSize(d);
				
			}
		});
		*/
	}
	
	JTextArea getTweetTextArea(){
		//ツイート書き込むところ
		final JTextArea returnArea=new JTextArea();
		returnArea.setLineWrap(true);
		returnArea.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		returnArea.setPreferredSize(new Dimension(0,0));
		returnArea.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){//shift+Enterでtweet
				if(twitter!=null
						&& e.getKeyCode()==KeyEvent.VK_ENTER
						&& (e.getModifiersEx()&InputEvent.SHIFT_DOWN_MASK)!=0){
					tweet();
				}
			}
		});
		returnArea.setTransferHandler(new TransferHandler(){
			@Override
			public boolean canImport(TransferSupport support){
				return support.isDrop() 
						&& support.getTransferable()
									.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
						
				/*
				if(!support.isDrop())
					return false;
				
				Transferable t=support.getTransferable();
				if(!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					return false;
				
				List<?> list=null;
				try {
					Object object=t.getTransferData(DataFlavor.javaFileListFlavor);//bug
					
					if(!(object instanceof List<?>))
						return false;
					
					list=(List<?>)object;
					
					for(Object obj:list){
						if(!(obj instanceof File))
							continue;
						
						File file=(File)obj;
						if(file.getName().endsWith(".JPG"))
							return true;
					}
				} catch (UnsupportedFlavorException e) {
					// TODO 自動生成された catch ブロック
					//e.printStackTrace();
					return false;
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					//e.printStackTrace();
					return false;
				}
				*/
			}
			
			@Override
			public boolean importData(TransferSupport support){
				//要る？
				if(!canImport(support))	return false;
				
				Transferable t=support.getTransferable();
				
				List<?> list;
				try {
					list = (List<?>)t.getTransferData(DataFlavor.javaFileListFlavor);
					for(Object obj:list){
						if(!(obj instanceof File))	continue;
						
						final File file=(File)obj;
						if(!file.getName().matches("(?i).+\\.(jpg|jpeg|png|gif)$"))	continue;
						
						System.out.println(file.getName());
						
						addMedia(file);
					}
				} catch (UnsupportedFlavorException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					return false;
				}
				
				return true;
			}
		});
		
		return returnArea;
	}
	JButton getSendButton(){
		//ツイート送信ボタン
		final JButton returnButton=new JButton("tweet!");
		returnButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(twitter==null || tweetTextArea==null)	return;
				tweet();
			}
		});
		return returnButton;
	}
	
	
	JPanel getMediaPanel(){
		final JPanel returnPanel=new JPanel();
		returnPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		return returnPanel;
	}
	void addMedia(final File file){
		if(
				mediaPanel==null 
				|| !file.getName().matches("(?i).+\\.(jpg|jpeg|png|gif)$")
				|| mediaFiles.size()>=4)
			return;
		
		try {
			Image tmp=ImageIO.read(file);
			Image image=null;
			if(tmp.getHeight(null)>tmp.getWidth(null)){
				image=tmp.getScaledInstance(-1,100,Image.SCALE_SMOOTH);
			}else{
				image=tmp.getScaledInstance(100,-1,Image.SCALE_SMOOTH);
			}
			final JLabel label=new JLabel(new ImageIcon(image));
			label.setPreferredSize(new Dimension(100,100));
			
			JButton button=new JButton("×");
			button.setBounds(label.getPreferredSize().width-20,0,20,20);
			button.setMargin(new Insets(0,0,0,0));
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					removeMedia(file,label);
				}
			});
			
			label.setLayout(null);
			label.add(button);
			
			mediaPanel.add(label);
			mediaFiles.add(file);
			label.revalidate();
			
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		
	}
	void removeMedia(File file,JLabel label){//mediaを削除
		mediaFiles.remove(file);
		mediaPanel.remove(label);
		mediaPanel.revalidate();
	}
	
	JPanel getMentionStatusPanel(){
		JPanel returnPanel=new JPanel();
		
		
		
		return returnPanel;
	}
	
	
	void tweet(){
		try {
			StatusUpdate statusUpdate=new StatusUpdate(tweetTextArea.getText());
			
			//mediaツイート
			if(mediaFiles.size()!=0){
				long[] idList=new long[mediaFiles.size()];
				for(int i=0;i<idList.length;i++){
					idList[i]=twitter.uploadMedia(mediaFiles.get(i)).getMediaId();
				}
				statusUpdate.setMediaIds(idList);
			}
			
			twitter.updateStatus(statusUpdate);
			tweetTextArea.setText("");
			
			mediaFiles=new ArrayList<File>();
			remove(mediaPanel);
			mediaPanel=getMediaPanel();
			add(mediaPanel);
			mediaPanel.revalidate();
		} catch (TwitterException e1) {
			e1.printStackTrace();
		}
	}
	
}
