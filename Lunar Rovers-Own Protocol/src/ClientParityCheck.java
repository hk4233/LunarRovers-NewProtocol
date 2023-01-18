import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
// import com.google.common.base.Splitter; 

public class ClientParityCheck {
	
	Socket socket;
	DataInputStream dis;
	String received;
	public ClientParityCheck() throws UnknownHostException, IOException {
	
	}

	public String makeString(String input) throws UnknownHostException, IOException
	{
        /**socket = new Socket("", 5000);
		System.out.println("Connected");
		
		dis=new DataInputStream(socket.getInputStream());
		
		received=dis.readUTF();
		String toReturn="";
		Iterable<String> result = Splitter.fixedLength(9) 
                .trimResults() 
                .split(input);
		int counter=0, count=0;
		ArrayList<String> res=new ArrayList<>();
		for( String str : result) res.add(str);
		for( String str : res)
		{	
			
			if(str.length()==0) break;
			String st=str.substring(0, str.length()-1);
			int asciibit=Integer.bitCount(Integer.parseInt(str, 2))%2;
			if(counter<=res.size()-2) 
			toReturn+= (char)Integer.parseInt(st, 2)+"";
			if  (asciibit==1) {
				
				int indexframe=counter+1,col_num=-1;
				for(int i=0;i<str.length();i++)
				{	int bit_count=0;
					for (int j=0;j<res.size();j++)
					{
						bit_count+=Integer.parseInt(res.get(j).charAt(i)+"");
					}
					if(bit_count%2==1)col_num=i+1; 
				}
				System.out.println(" Error at the frame number : "+indexframe+"\n Col number "+col_num);
				toReturn=null;break;}
			counter++;
		}
		
		//System.out.println(toReturn);
		return toReturn;
		**/
        return null;
	}
}