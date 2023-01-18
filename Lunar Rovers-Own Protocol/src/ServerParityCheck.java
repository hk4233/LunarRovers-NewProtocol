import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerParityCheck {
	ServerSocket ss;
	Socket socket;
	DataOutputStream dos;
	Scanner sc;
    int port;
	public ServerParityCheck(int port) throws IOException {
		
		this.port =port;
	}
	private String converttobinary(String str) throws IOException {
		ss=new ServerSocket(this.port);
		socket=ss.accept();
		dos=new DataOutputStream(socket.getOutputStream());
		
		while (true) {
			
			System.out.println("Enter data to be deliver to client");
			String strin=sc.nextLine();
			if(strin.equals("exit")) break;
			
			String converted=converttobinary(strin);
			System.out.println(converted);
			dos.writeUTF(converted);
			
		}
		StringBuilder sb = new StringBuilder();
		String s = null;
		ArrayList<String> bindigit=new ArrayList<>();
		String toReturn="";
		for (char c : str.toCharArray())
		{
			s = String.format("%8s", Integer.toBinaryString((int)c & 0xFF)).replace(' ', '0');
			if(Integer.bitCount(((int)c))%2==0) s=s+"0";
			else s+="1";
			bindigit.add(s);
			toReturn+=s;
		}
		String st="";
		for(int i=0;i<bindigit.get(0).length();i++)
		{	
			int ct=0;
		
			for (String str1 : bindigit)
			{
				ct+=Integer.parseInt(str1.charAt(i)+"");
			}
			if(ct%2==0) st+="0";
			else st+="1";
			}
		toReturn+=st;

        socket.close();
		ss.close();
		return toReturn;
	}
	
}