import java.io.*;
import java.util.*;

public class SigParser {
	/* ssh field@localhost -p 2224 cat /dev/ttyS0 | java SigParser */
	/* cat output|java SigParser*/
    public static void main(String[] args) throws IOException {
		try (BufferedInputStream bf = new BufferedInputStream(System.in)) {
			int nextByte; 
			int readLen; //# of bytes read
			int msgLen=-3; // msg length
			int cnt = 0;
			int sof = 0;
			int crc = 0;
			byte[] msg = new byte[16];
			int content = -1;
			int source = -1;
            while ((nextByte = bf.read()) != -1) {//while stream is flowing
				if (cnt <= 15){
					msg[cnt] = (byte)nextByte;
				}
				if (getLen(nextByte) != -1){//is message head
					msgLen = getLen(nextByte);
					sof = nextByte;
					cnt = 0;//reset counter
					Arrays.fill(msg, (byte)0); //empty msg array
				}else if (cnt == (msgLen + 2 -1)){//is message tail
					content = BytesToInt(Arrays.copyOfRange(msg, 0, 3));
					switch (content){		
						case 0x00800: //Absolute Time Report
							ATR atr = new ATR(msg, msgLen);
							atr.parseATR();
							if (atr.checkCRC()){
								System.out.println("ART report id" + atr.id + " ,Time Stamp:"+String.format("%.5f", atr.ts));
							}else{
								System.out.println("Checksum not matched");
							}
							break;
						case 0x4C2800://Loop Activation Report
							LAR lar = new LAR(msg, msgLen);
							lar.parseLAR();
							if (lar.checkCRC()){
								System.out.println("LAR report id:" + lar.id + " ,fps:"+ lar.fps + " ,time:" + String.format("%.5f", lar.t1) +" ,cm:"+lar.cm+" ,sm"+lar.sm);
							}else{
								System.out.println("Checksum not matched");
							}
							break;
						case 0xCBE90A:
							SSR ssr = new SSR(msg, msgLen);
							ssr.parseSSR();
							if (ssr.checkCRC()){
								System.out.println("SSR report id:" + ssr.id
								+ ", channel:" + ssr.ch
								+ ", t1:" + String.format("%.4f", ssr.t1) 
								+ ", v1:" + ssr.v1
								+ ", t2:" + String.format("%.4f", ssr.t2) 
								+ ", v2:" + ssr.v2
								+ ", t3:" + String.format("%.4f", ssr.t3) 
								+ ", v3:" + ssr.v3);								
							}else{
								System.out.println("Checksum not matched");
							}
							break;
						case 0x8B4B06:
							MinDR minDr = new MinDR(msg, msgLen);
							minDr.parseMinDR();
							if (minDr.checkCRC()){
								System.out.println("minDR report id:" + minDr.id
								+ ", channel:" + minDr.ch
								+ ", t1:" + String.format("%.5f", minDr.t1) 
								+ ", av1:" + minDr.av1);								
							}else{
								System.out.println("Checksum not matched");
							}
							break;
						case 0x8B4B05:
							MaxDR maxDr = new MaxDR(msg, msgLen);
							maxDr.parseMaxDR();
							if (maxDr.checkCRC()){
								System.out.println("maxDR report id:" + maxDr.id
								+ ", channel:" + maxDr.ch
								+ ", t1:" + String.format("%.5f", maxDr.t1) 
								+ ", av1:" + maxDr.av1);								
							}else{
								//System.out.println("Checksum not matched");
							}
							break;
						default: 
							System.out.println("Unknown SOF: " + String.format("%06x", content));
                     break;
						
					}
					//System.out.println("New message header:"+String.format("%02x", sof)+", content:"+ String.format("%06x", content) + " crc:" + msgLen + " with msg:"+ bytesToHex(msg));
				}else{//not fully received
					cnt++;
				}
			}

        }
    }
	
	/*-----------------------------------------nested classes-------------------------------*/
	private static class Message{
	//member varibles;
		byte[] msg = new byte[16];
		int id;
		int len;
		static double ts = 0; //absolute time
		String crc;
	
		public Message(byte[] newMsg, int newLen){
			id = newMsg[3];
			msg = newMsg;
			len = newLen;
			crc = checkCRC16(Arrays.copyOfRange(newMsg, 0, len));
		}
		
		public boolean checkCRC(){
			byte[] reported = {msg[len+1], msg[len]};
			return crc.equals(bytesToHex(reported));
			//return crc;
		}
		
	}	
	
	private static class ATR extends Message{//absolute time report
		public ATR(byte[] newMsg, int newLen){
			super(newMsg, newLen);
		}
	
		public void parseATR(){
			ts = BytesToInt(Arrays.copyOfRange(msg, 4, 8));
		}
	}
	
	
	private static class LAR extends Message{//loop activation report
		boolean fps;
		double t1;
		int to;
		int cm;
		int sm;
		public LAR(byte[] newMsg, int newLen){
			super(newMsg, newLen);
		}
		
		public void parseLAR(){
			fps = (((msg[4]>>7)&0xff) !=0);
			to = ((msg[4] & 0x0f)<<8 |msg[5])&0xfff; //time offset in 1/4 mS
			t1 = ts + (double) to/4000 - (fps? 1 : 0);
			cm = msg[6];
			sm = msg[7];
		}
	}
	
	private static class SSR extends Message{//signature sample report
		boolean fps;
		int ch, to, v1, v2, v3, dv2, dv3, dt2, dt3;
		double t1, t2, t3;
		public SSR(byte[] newMsg, int newLen){
			super(newMsg, newLen);
		}
		
		public void parseSSR(){
			fps = (((msg[4] >> 7) & 0xff) !=0);
			ch = (msg[4] & 0x70) >> 4;
			to = ((msg[4] & 0x0f)<<8 | msg[5])&0xfff; //time offset in 1/4 mS
			v1 = BytesToInt(Arrays.copyOfRange(msg, 6, 8));
			dt2 = (msg[8] & 0xf0) >> 4;
			dv2 = ((msg[8] & 0x0f)<<8 | msg[9]);
			dt3 = (msg[10] & 0xf0) >> 4;
			dv3 = ((msg[10] & 0x0f)<<8 | msg[11]);
			t1 = ts + (double)to/4000 - ( fps? 1 : 0);
			t2 = ts + (double)to/4000 + (double)dt2/1000 - (fps? 1 : 0);
			t3 = ts + (double)to/4000 + (double)dt2/1000 + (double)dt3/1000 - (fps? 1 : 0);
			v2 = v1 + dv2;
			v3 = v2 + dv3;
		}
	}
	
	

	private static class MinDR extends Message{//minima detection report
		boolean fps;
		int ch, to, v1, bl;
		double t1, av1;
		public MinDR(byte[] newMsg, int newLen){
			super(newMsg, newLen);
		}
		
		public void parseMinDR(){
			fps = (((msg[4] >> 7) & 0xff) !=0);
			to = ((msg[4] & 0x0f)<<8 | msg[5])&0xfff; //time offset in 1/4 mS
			ch = msg[6];
			v1 = BytesToInt(Arrays.copyOfRange(msg, 7, 9));
			bl = BytesToInt(Arrays.copyOfRange(msg, 9, 11));
			t1 = ts + (double)to/4000 - ( fps? 1 : 0);
			av1 = bl - ((double) bl * v1)/10000;
		}
	}
	
	private static class MaxDR extends Message{//maxima detection reprot
		boolean fps;
		int ch, to, v1, bl;
		double t1, av1;
		public MaxDR(byte[] newMsg, int newLen){
			super(newMsg, newLen);
		}
		
		public void parseMaxDR(){
			fps = (((msg[4] >> 7) & 0xff) !=0);
			to = ((msg[4] & 0x0f)<<8 | msg[5])&0xfff; //time offset in 1/4 mS
			ch = msg[6];
			v1 = BytesToInt(Arrays.copyOfRange(msg, 7, 9));
			bl = BytesToInt(Arrays.copyOfRange(msg, 9, 11));
			t1 = ts + (double)to/4000 - ( fps? 1 : 0);
			av1 = bl - ((double) bl * v1)/10000;
		}
	}	
	
	
	
	/*----------------------------------------Methods---------------------------------------*/
	//return message length if SOF, -1 otherwise
	public static int getLen(int val){
		int lo = val & 0x0f;
		int hi = (val >> 4) & 0x0f;
		if (hi == 0x0d){
			return lo;
		}else{
			return -1;
		}
	}
	//convert byte array to hex string, http://stackoverflow.com/a/9855338
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	
	public static int BytesToInt(byte[] bytes){
		int out = 0;
		for (int j = 0; j <bytes.length; j++){
			out <<= 8;
			out |= (int)bytes[j] & 0xff;
		}
		return out;
	}
	
	public static String checkCRC16(byte[] bytes){
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12) 

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xffff;
		return String.format("%04x", crc);
	}
}






