//Ong Mu Sen Jeremy A0108310Y
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.io.DataOutputStream;
import java.lang.*;



public class WebProxy {
	/** Port for the proxy */
	private static int port;

	/** Socket for client connections */
	private static ServerSocket socket;
	
	public static void main(String args[]) {
		/** Read command line arguments and start proxy */

		/** Read port number as command-line argument **/
		port = Integer.parseInt(args[0]);
		/** Create a server socket, bind it to a port and start listening **/
		try{
			socket = new ServerSocket(port);
		} catch (IOException e){
		}

		/** Main loop. Listen for incoming connections **/
		Socket client = null;
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		while (true) {
			String URI = "";
			String method = "GET";
			String data = "";
			byte[] request = new byte[2048];
			final InputStream streamFromClient;
			final OutputStream streamToClient;
			int error = 404;
			try {
				client = socket.accept();
				
				System.out.println("'Received a connection from: " + client);
				/** Read client's HTTP request **/
				streamFromClient = client.getInputStream();
				BufferedReader input = new BufferedReader(new InputStreamReader(streamFromClient));
				String firstline = input.readLine();
				String[] tmp = firstline.split(" ");
				method = tmp[0];
				URI = tmp[1];
				String version = tmp[2];
				
				//Post method on stack overflow
				if(method.equals("POST")){
					String content = null;
					int length = 0;
					while(!(content = input.readLine()).equals("")){
						if(true){
							final String header = "Content-Length: ";
							if(content.startsWith(header)){
								length = Integer.parseInt(content.substring(header.length()));
							}
						}
					}
					StringBuilder body = new StringBuilder();
					if(true){
						int a = 0;
						for(int i = 0; i < length; i++){
							a = input.read();
							body.append((char) a);
						}
					}
					data = body.toString();
				}
			} 
			catch (IOException e) {
				System.out.println("Error reading request from client: " + e);
				/* Definitely cannot continue, so skip to next
				 * iteration of while loop. */
				continue;
			}
			
			/** Check cache if file exists **/
			String fileName = null;
			Long lastMod = Long.parseLong("0");
			Long mod = Long.parseLong("0");
			//Storing of filename as number and date of last modified as number_1
			if(hash.containsKey(URI)){
				fileName = hash.get(URI).toString();
			}
			else{
				int value = hash.size();
				hash.put(URI, value);
				fileName = hash.get(URI).toString();
			}
			File f = new File(fileName);
			File g = new File(fileName + "_1");
			//checking of last modified on web page
			try{
				URL url = new URL(URI);
				URLConnection conn = url.openConnection();
				lastMod = conn.getLastModified()/1000;
				if(f.exists()){
					BufferedReader check = new BufferedReader(new FileReader(g));
					mod = Long.parseLong(check.readLine());
					check.close();
				}
			}catch(IOException e){
			}
			if (f.exists() && (lastMod < mod))
			{
				/** Read the file **/
				byte[] fileArray;
				try{
					fileArray = Files.readAllBytes(f.toPath());
					streamToClient = client.getOutputStream();	
					streamToClient.write(fileArray);
					streamToClient.flush();
					streamToClient.close();
				} catch(IOException e){
				}
				/** generate appropriate respond headers and send the file contents **/
			}
			else {
				try {
					/** connect to server and relay client's request **/
					File censor = new File("censor.txt");
					streamToClient = client.getOutputStream();
					URL url = new URL(URI);
					URLConnection connect = url.openConnection();
				    Map<String, List<String>> map = connect.getHeaderFields();
				    if(map.size() == 0){
				    	error = 502;
				    }
				    //Post method from stack overflow
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					if(method.equals("POST")){
						con.setDoOutput(true);
						con.setDoInput(true);
						con.setRequestMethod("POST");
						OutputStream output = con.getOutputStream();
						byte[] d = data.getBytes();
						output.write(d);
						output.close();
					}
				    InputStream current = url.openStream();
				    FileOutputStream store = new FileOutputStream(fileName);
				    FileOutputStream date = new FileOutputStream(fileName + "_1");
				    int bytesRead;
				    String sentence = null;
			    	String web = "";
			    	String content = connect.getContentType();
			    	//Reading of text or html
				    if (content.contains("text/plain") == true || content.contains("text/html") == true){
						BufferedReader read = new BufferedReader(new InputStreamReader(current));
						/* The part that is edited out and replaced from the bottom part line 161 to 165
				    	while ((sentence = read.readLine()) != null) {		       
				    		web = web + sentence;
				    	}
						*/
						int character;
						while( (character = read.read()) != -1){
							char nextChar = (char)character;
							sentence = Character.toString(nextChar);
							web += sentence;
						}
				    }
				    else{
				    	while ((bytesRead = current.read(request)) != -1) {
				    		streamToClient.write(request, 0, bytesRead);
				    		store.write(request, 0, bytesRead);
				    		streamToClient.flush();
							store.flush();
							date.flush();
				    	}
				    }
				    //if censor.txt exists
			        if(censor.exists()){
						String word;
						BufferedReader br = new BufferedReader(new FileReader(censor));
						while((word = br.readLine()) != null){
							web = web.replaceAll("(?i)" + word, "---");
						}
							br.close();
					}
			        byte[] b = web.getBytes();
			        String time = "" + System.currentTimeMillis()/1000;
			        byte[] c = time.getBytes();
			        streamToClient.write(b);
					store.write(b);
					date.write(c);
					streamToClient.flush();
					date.flush();
					store.flush();
					/** Get response from server **/
					streamToClient.close();
					/** Cache the contents as appropriate **/
					store.close();
					date.close();
					/** Send response to client **/
					client.close();
				} catch (IOException e) {
					try{
						final DataOutputStream message = new DataOutputStream(client.getOutputStream());
						if(error == 404){
							message.writeBytes("HTTP/1.0 404 Not Found\r\n\r\n" + "404");
						}
						else{
							message.writeBytes("HTTP/1.0 502 Bad Gateway\r\n\r\n" + "502");
						}
						message.flush();
						message.close();
					}catch(IOException h){
					}
				}
			}		
		}		
	}
}
