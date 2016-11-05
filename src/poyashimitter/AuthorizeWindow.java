package poyashimitter;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import poyashimitter.main.Window1;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class AuthorizeWindow extends JFrame {
	
	RequestToken requestToken;
	
	Twitter twitter;
	public AuthorizeWindow(final Window1 window1,final Twitter twitter){
		this.twitter=twitter;
		
		this.setTitle("アカウント認証 Poyashimitter");
		
		try {
			requestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e2) {
			e2.printStackTrace();
		}
		
		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		this.add(mainPanel);
		
		mainPanel.add(Box.createRigidArea(new Dimension(0,30)));
		
		
		JPanel browzePanel=new JPanel();
		browzePanel.setLayout(new BoxLayout(browzePanel,BoxLayout.X_AXIS));
		mainPanel.add(browzePanel);
		
		JLabel urlLabel=new JLabel("ブラウザを起動してアカウント認証を行ってください。");
		browzePanel.add(urlLabel);
		
		
		
		JButton button=new JButton("ブラウザを起動する");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(
							new URL(requestToken.getAuthorizationURL()).toURI());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		browzePanel.add(button);
		
		mainPanel.add(Box.createRigidArea(new Dimension(0,30)));
		
		JPanel pinPanel=new JPanel();
		pinPanel.setLayout(new BoxLayout(pinPanel,BoxLayout.X_AXIS));
		mainPanel.add(pinPanel);
		
		JLabel authLabel=new JLabel("PINを入力してください:");
		pinPanel.add(authLabel);
		
		final JTextArea pinArea=new JTextArea();
		pinArea.setMaximumSize(new Dimension(150,Integer.MAX_VALUE));
		pinPanel.add(pinArea);
		
		mainPanel.add(Box.createRigidArea(new Dimension(0,30)));
		
		
		JButton authButton=new JButton("Authorize!");
		authButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				AccessToken ac = null;
				
				try {
					String pin = pinArea.getText();
					if(pin.length() > 0){
						ac = twitter.getOAuthAccessToken(requestToken, pin);
					}else{
						ac = twitter.getOAuthAccessToken();
					}
					
					Settings.setAccessToken(ac);
					Settings.save();
				} catch (TwitterException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
				
				if(ac!=null){
					window1.createWindow(ac);
					setVisible(false);
				}else{
					//エラー表示
				}
				
			}
		});
		mainPanel.add(authButton);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
}
