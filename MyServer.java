package ftpserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;


public class MyServer {
	public static void main(String[] args) {
		ServerSocket serversocket = null;
		   try {
		      serversocket=new ServerSocket(21);              //����serversocket����21�˿�
		      while(true) {
			   Socket socket = serversocket.accept();         //�������ӵĶ˿�
			   SocketThread s=new SocketThread(socket);
			   s.start();
		      }
		   }catch(IOException e){
			   e.printStackTrace();
				}
		   try {
			   serversocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	
	   }
}


class SocketThread extends Thread{
	private Socket socket;
	private PrintWriter pw;              //�ֽڴ�ӡ���������ͻ��˽���ͨѶ
	private BufferedReader br;           //����������
	private String tmp=null;
	private String username="";	        //�˺�������
	private String password="";
	private Socket dataSocket;          //�������ӵ�socket
	int  IsLogin = 0;                   //��¼��־
	int  IsConnect=0;
	boolean  Online=false;              //���߱�־
	String filerecvpath1="C:\\Users\\L\\Desktop\\FTPtest\\rec";
	String filepath1="C:\\Users\\L\\Desktop\\FTPtest";
	String oldname=null;
	
	public  SocketThread(Socket socket) throws IOException {
		this.socket=socket;
		pw=new PrintWriter(socket.getOutputStream());
		br=new BufferedReader(new InputStreamReader(socket.getInputStream()));

	}
	public void run()  {

		pw.print("220 (vsFTPd 3.0.2)\r\n");              //���û�����׼������
		pw.flush();	
		try {		
		tmp=br.readLine();
		} catch (IOException e) {
					e.printStackTrace();
				} 
		pw.print("200 Always in UTF8 mode.\r\n");       //����ɹ���ʹ��UTF8ģʽ
		pw.flush();
		Online=true;
		while(Online) {
			try {
				tmp=br.readLine();
				System.out.println(tmp);
				switch(tmp.split(" ")[0]) {
			    case "USER":
					if(tmp.split(" ").length>1)
					username =tmp.split(" ")[1];
				    pw.print("331 Please specify the password.\r\n");  //�û�����ȷ����Ҫ����
				    pw.flush();
				break;
			    case "PASS":
					password =tmp.split(" ")[1];
					if(username.equals("1")&&password.equals("1")) {
					IsLogin=1;
					pw.print("230 Login successful.\r\n");   //�û���¼
					pw.flush();
				    }
				    else {
					pw.print("530 Login incorrect.\r\n");   //�û���¼ʧ��
					pw.flush();
				    }
				break;
			    case "QUIT":
					pw.print("221 Goodbye.\r\n");           //����رտ������ӣ������˳���¼ 
					pw.flush();
					pw.close();
					br.close();
					Online=false;
				break;
			    case "PORT":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");    //δ��¼Ҫ���¼
						pw.flush();
			    		break;
			    		}
			    	String[] addrs=tmp.split(" ")[1].split(",");
					dataSocket=new Socket(addrs[0]+"."+addrs[1]+"."+addrs[2]+"."+addrs[3],Integer.parseInt(addrs[4])*256+Integer.parseInt(addrs[5]),InetAddress.getByName("127.0.0.1"),20);
					pw.print("200 PORT command successful.Consider using PASV.\r\n");    //����ģʽ������ճɹ�
					pw.flush();
			    break;
			    case "LIST":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
			    	if(tmp.split(" ").length>1) {
						tmp=tmp.split(" ")[1];
			    	    filepath1=filepath1+"\\"+tmp;
			    	    }
			    	System.out.println(filepath1);
					pw.print("150 Here comes the directory listing\r\n");               //����������
					pw.flush();
					File file = new File(filepath1);
					PrintWriter dataWriter = null;
					try {
						dataWriter = new PrintWriter(new OutputStreamWriter(dataSocket.getOutputStream()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (!file.exists()) {
					     System.out.println(tmp + " not exists");
					     dataWriter.write(tmp + " not exists\r\n"); 
					     return;
					   }
					   File files[] = file.listFiles();
					   for (File i: files) {
					     if (i.isDirectory()) {
					       System.out.println(i.getName() + " [menu]");
		                   dataWriter.print(i.getName() + " [filefolk]\r\n");
						   dataWriter.flush();
					     }else {
					       System.out.println(i.getName());
						   dataWriter.print(i.getName()+"\r\n");
					       dataWriter.flush();  
					    }
			            }
					     try {
							dataSocket.close();
						} catch (IOException e) {
				
							e.printStackTrace();
						}  
					     dataWriter.close();
					     pw.print("226 Directory send OK.\r\n");     //�ر��������ӣ�������ļ������ɹ�
		                 pw.flush(); 
		                 filepath1="C:\\Users\\L\\Desktop\\FTPtest";
			    break;
			    case"RETR":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
					  if(tmp.split(" ").length>1) {
							tmp=tmp.split(" ")[1];
						}
						else {
							tmp="";
						}
					  BufferedOutputStream  dataout= null;
					  InputStream datain=null;
					  File file2=new File(filepath1+"\\"+tmp);
					  if(!file2.exists()) {
						 System.out.println("�Ҳ���ָ���ļ�"); 
						 pw.print("550 can not find file\r\n");    //�ļ�����δִ��
						 pw.flush();
					    }
					  pw.print("150 get file from C:\\Users\\L\\Desktop\\FTPtest\r\n");
					  pw.flush();
					  try {
						  
						   dataout = new  BufferedOutputStream(dataSocket.getOutputStream());
					       datain=new FileInputStream(file2);
					       byte[] buf = new byte[1024];
					        while(-1!=datain.read(buf)) {  
							     dataout.write(buf);  
							 }
					       dataout.flush();
		                   dataSocket.close();  
						   } catch (IOException e) {
							e.printStackTrace();
						   }
					   dataout.close();
					   datain.close();
					   pw.print("220 transfer complete\r\n");  
					   pw.flush();
					   filepath1="C:\\Users\\L\\Desktop\\FTPtest";
				break;
			    case "STOR":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
			    	
			    	if(tmp.split(" ").length>1) {
						tmp=tmp.split(" ")[1];
					}
					else {
						tmp="";
					}
			    	   pw.print("150 Ok to send data\r\n");  
					   pw.flush();
			    	File file3=new File(filerecvpath1+"\\"+tmp);
			    	BufferedInputStream  datain2= null;
					OutputStream dataout2=null;
					try {
						datain2 = new  BufferedInputStream(dataSocket.getInputStream());
						dataout2=new FileOutputStream(file3);
						byte[] buf = new byte[1024];
						while(-1!=datain2.read(buf)) {  
						     dataout2.write(buf);  
						 }
						dataout2.flush();
						datain2.close();
						dataout2.close();
		                dataSocket.close(); 
		                pw.print("226 Tranfer completed\r\n");     
						pw.flush();
					}catch(IOException e) {
						e.printStackTrace();
					}
				   filerecvpath1="C:\\Users\\L\\Desktop\\FTPtest\\rec";
			    break;
			    case "DELE":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
			    	if(tmp.split(" ").length>1) {
						tmp=tmp.split(" ")[1];
					}
					else {
						tmp="";
					}
			    	File file4=new File(filepath1+"\\"+tmp);
			    	if(file4.exists())file4.delete();
			    	 pw.print("226 Delete completed\r\n");  
					 pw.flush();
					 filepath1="C:\\Users\\L\\Desktop\\FTPtest";
			    break;
			    case "RNFR":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
			        
			    	if(tmp.split(" ").length>1) {
						tmp=tmp.split(" ")[1];
					}
					else {
						tmp="";
					}
			        oldname=tmp;
			    	pw.print("350 Ready for RNTO.\r\n");    //��һ������
					pw.flush();
			    break;
			    case "RNTO":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
			    	if(tmp.split(" ").length>1) {
						tmp=tmp.split(" ")[1];
					}
					else {
						tmp="";
					}
			    	File file5=new File(filepath1+"\\"+oldname);
			    	if(file5.exists()) file5.renameTo(new File(filepath1+"\\"+tmp));
			    	pw.print("226 Rename completed\r\n");  
					pw.flush();
					filepath1="C:\\Users\\L\\Desktop\\FTPtest";
			    break;
			    case "APPE":
			    	if(IsLogin==0) {
			    		pw.print("530 Please login with username and password.\r\n");
						pw.flush();
			    		break;
			    		}
			    	if(tmp.split(" ").length>1) {
						tmp=tmp.split(" ")[1];
					}
					else {
						tmp="";
					}
			    	pw.print("150 Ok to send data\r\n");  
					pw.flush();
					File file6=new File(filerecvpath1+"\\"+tmp);
			    	BufferedInputStream  datain3= null;
					OutputStream dataout3=null;
					try {
						datain3 = new  BufferedInputStream(dataSocket.getInputStream());
						dataout3=new FileOutputStream(file6,true);
						byte[] buf = new byte[1024];
						while(-1!=datain3.read(buf)) {  
						     dataout3.write(buf);  
						 }
						dataout3.flush();
						dataout3.close();
						datain3.close();
		                dataSocket.close(); 
		                pw.print("226 Append completed\r\n");  
						pw.flush();
					}catch(IOException e) {
						e.printStackTrace();
					}
				   filerecvpath1="C:\\Users\\L\\Desktop\\FTPtest\\rec";
			    break;
			    	
				}
			}catch(IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	}
 }
    

