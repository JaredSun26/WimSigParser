import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;


public class WimParser {
	public static void main(String[] args) throws IOException {
		String cCode, lrc; //content code, lrc
		int l, ld, mo, dd, yy, hh, mm, ss, hs, vehNum, na, cl, gros, leng, sped,
		sp1, sp2, sp3, sp4, sp5, sp6, sp7, sp8, sp9, sp10, sp11, sp12, wt1, wt2, wt3, wt4, wt5, wt6, wt7, wt8 ,wt9 ,wt10 ,wt11 ,wt12 ,wt13;
		
		int unknown1, unknown2;
		Pattern start = Pattern.compile("\\x01\\d\\x02");
		Pattern end = Pattern.compile("\\x03.*");
		
		Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter(",|\\x04|<|>"); //delimiters = ",", EOT, "<", ">"
        while(scanner.hasNext()){
			if (scanner.hasNext(start)){//message start
				cCode = scanner.next();
				l = scanner.nextInt();
				ld = scanner.nextInt();
				mo = scanner.nextInt();
				dd = scanner.nextInt();
				yy = scanner.nextInt();
				hh = scanner.nextInt();
				mm = scanner.nextInt();
				ss = scanner.nextInt();
				hs = scanner.nextInt();
				vehNum = scanner.nextInt();
				na = scanner.nextInt();
				cl = scanner.nextInt();
				gros = scanner.nextInt();
				leng = scanner.nextInt();
				sped = scanner.nextInt();
				sp1 = scanner.nextInt();
				sp2 = scanner.nextInt();
				sp3 = scanner.nextInt();
				sp4 = scanner.nextInt();
				sp5 = scanner.nextInt();
				sp6 = scanner.nextInt();
				sp7 = scanner.nextInt();
				sp8 = scanner.nextInt();
				sp9 = scanner.nextInt();
				sp10 = scanner.nextInt();
				sp11 = scanner.nextInt();
				sp12 = scanner.nextInt();
				wt1 = scanner.nextInt();
				wt2 = scanner.nextInt();
				wt3 = scanner.nextInt();
				wt4 = scanner.nextInt();
				wt5 = scanner.nextInt();
				wt6 = scanner.nextInt();
				wt7 = scanner.nextInt();
				wt8 = scanner.nextInt();
				wt9 = scanner.nextInt();
				wt10 = scanner.nextInt();
				wt11 = scanner.nextInt();
				wt12 = scanner.nextInt();
				wt13 = scanner.nextInt();
				
				unknown1 = scanner.nextInt();
				unknown2 = scanner.nextInt();
				
				System.out.println("New Message, content:" + checkContent(cCode) + ", Lane Number:" + l + ", Lane Direction:"+ ld);
				System.out.println("Time: " + mo+"/" + dd + "/" + yy + " " + hh + ":" + mm + ":" +ss + ":" +hs);
				System.out.println("Veh Num:" + vehNum + ", No. Axles:" + na + ", Class:"+ cl +  ", Gross Weight(lb):" + gros*100 + ", Overall Length(ft):" + (double)leng/10 + ", Speed(mph):" +  (double)sped/10 );
				System.out.println("Axle spacing(ft):" 
				+ (double)sp1/10 + ","
				+ (double)sp2/10 + ","
				+ (double)sp3/10 + ","
				+ (double)sp4/10 + ","
				+ (double)sp5/10 + ","
				+ (double)sp6/10 + ","
				+ (double)sp7/10 + ","
				+ (double)sp8/10 + ","
				+ (double)sp9/10 + ","
				+ (double)sp10/10 + ","
				+ (double)sp11/10 + ","
				+ (double)sp12/10);
				
				System.out.println("Axle weight(lb):" 
				+ wt1*100 + ","
				+ wt2*100 + ","
				+ wt3*100 + ","
				+ wt4*100 + ","
				+ wt5*100 + ","
				+ wt6*100 + ","
				+ wt7*100 + ","
				+ wt8*100 + ","
				+ wt9*100 + ","
				+ wt10*100 + ","
				+ wt11*100 + ","
				+ wt12*100 + ","
				+ wt13*100 + ","
				);
				
			}
			
			if (scanner.hasNext(end)){
				lrc = getLRC(scanner.next());
				System.out.println("END, LRC:"+ lrc);
				continue;
			}			
			
            System.out.print(scanner.next()+"|");
        }
        scanner.close();
    }
	
	
	
	
	public static String checkContent(String cCode){
		String code = "";
		cCode = cCode.substring(cCode.length() - 2, cCode.length()-1);
		switch (cCode){
			case "0": code = "WIM";
				break;
			case "1": code = "RC"; //remote control
				break;
			case "2": code = "WIM2";
				break;
			case "3": code = "SDO"; // Sort decision override
				break;				
		}
		return code;
	}
	
	public static String getLRC(String end){
		String lrc = "";
		lrc = end.substring(end.length() - 1, end.length());
		return lrc;
	}
	

}

