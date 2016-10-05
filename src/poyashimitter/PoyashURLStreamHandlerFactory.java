package poyashimitter;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;



/*



～仕様～

poyash://(ホスト名)/(パス名)

ホスト名
//・media	(メディア)
・user	(ユーザー)
・hashtag	(ハッシュタグ)

パス名
・media
	(同ツイートで何番目のメディアか).(メディアID)

・user
	(ユーザーID).(ユーザーのscreenName)

・hashtag	(ハッシュタグ)
	(ハッシュタグ文字列、#無し)

*/
public class PoyashURLStreamHandlerFactory implements URLStreamHandlerFactory {
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if(protocol.equals("poyash"))
			return new PoyashURLStreamHandler();
		else
			return null;
	}
	
}

class PoyashURLStreamHandler extends URLStreamHandler{
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		return new PoyashURLConnection(u);
	}
	
}

class PoyashURLConnection extends URLConnection{
	protected PoyashURLConnection(URL url) {
		super(url);
	}
	
	@Override
	public void connect() throws IOException {
		
	}
	/*
	public InputStream getInputStream(){
		
	}
	*/
}