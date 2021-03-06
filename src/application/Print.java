package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Print {
	public void print(boolean html, List<String> info, String gettime, String getdate, List<String[]> netInfo) {
		String base = " Internet Report ";
		String dir = "InternetInfo/";
		File directory = new File(dir);
		if(!directory.exists())
			directory.mkdirs();
		File styles = new File(dir+"print.css");
		if(!styles.exists())
			makeStyles(styles.getPath());
		if(html) {
			String time = gettime.replaceAll(":", ".");
			Path path = Paths.get(dir+getdate+base+time+".html");
			Path netPath = Paths.get(dir+getdate+base+"NetStat "+time+".html");
			boolean ping = false;
			boolean net = false;
			if(info.size() != 0)
				ping = true;
			if(netInfo != null)
				net = true;
			for(int i=0; i<2; i++) {
				if(ping && i==0) {
					try (BufferedWriter out = Files.newBufferedWriter(path,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);) {
						out.write("<!DOCTYPE html>");
						out.write("<html lang=\"en\">");
						out.write("<head>");
						out.write("<title>"+getdate+base+gettime+"</title>");
						out.write("<link href=\"print.css\" rel=\"stylesheet\">");
						out.write("</head>");
						out.write("<body>");
						out.write("<div>");
						if(net) {
							out.write("<div id=\"links\"><a href=\""+getdate+base+time+".html"+"\">Ping Log</a><span class=\"spacer\"></span><a href=\""+getdate+base+"NetStat "+time+".html"+"\">NetStat Log</a>");
						}
						for(String s : convertToHTML(info))
							out.write(s);
						out.write("</div>");
						out.write("</body>");
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
				if(net && i==1) {
					try (BufferedWriter out = Files.newBufferedWriter(netPath,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);) {
						out.write("<!DOCTYPE html>");
						out.write("<html lang=\"en\">");
						out.write("<head>");
						out.write("<title>"+getdate+base+"NetStat "+gettime+"</title>");
						out.write("<link href=\"print.css\" rel=\"stylesheet\">");
						out.write("</head>");
						out.write("<body>");
						out.write("<div>");
						if(ping) {
							out.write("<div id=\"links\"><a href=\""+getdate+base+time+".html"+"\">Ping Log</a><span class=\"spacer\"></span><a href=\""+getdate+base+"NetStat "+time+".html"+"\">NetStat Log</a>");
						}
						for(String s : netConvertToHTML(netInfo))
							out.write(s);
						out.write("</div>");
						out.write("</body>");
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			System.out.println("print plain");
			String time = gettime.replaceAll(":", ".");
			Path path = Paths.get(dir+getdate+base+time+".txt");
			try {
				Files.write(path, info, Charset.forName("UTF-8"));
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			path = Paths.get(dir+getdate+base+"NetStat "+time+".txt");
			if(netInfo.size() > 0) {
				try {
					List<String> newNetInfo = new ArrayList<String>();
					int spacer = 1;
					for(String[] array : netInfo) {
						for(String s : array) {
							newNetInfo.add(s);
							if((spacer%4) == 0) {
								newNetInfo.add("\n");
							}
							spacer++;
						}
					}
					Files.write(path, newNetInfo, Charset.forName("UTF-8"));
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private List<String> netConvertToHTML(List<String[]> info) {
		List<String> html = new ArrayList<String>();
		String str = "<div>";
		for(String[] array : info) {
			for(String s : array) {
				if(s.contains("Connection: ")) {
					str += "<div class=\"section\"><span class=\"netBlue bold\">"+s+"</span>";
				}
				else if(s.contains("Status: ")) {
					str += "<span class=\"netBlue bold\">"+s+"</span>";
				}
				else {
					if(s.contains(":"))
						str += "<span><a href=\"http://www.google.com/search?q="+s+"\">"+s+"</a></span><br/>";
					else
						str += "<span>"+s+"</span><br/>";
				}
			}
			str += "<br/>";
		}
		str += "</div>";
		html.add(str);
		return html;
	}
	
	private List<String> convertToHTML(List<String> info) {
		List<String> html = new ArrayList<String>();
		for(String str : info) {
			if(str.startsWith("START")) {
				str = str.replace("START", "");
				str = "<div><span class=\"bold pingGreen\">START</span>" + str;
			} 
			if(str.startsWith("STOP")) {
				str = str.replace("STOP", "");
				str = "<div><span class=\"bold pingYellow\">STOP</span>" + str;
			}
			if(str.startsWith("UPDATE")) {
				str = str.replace("UPDATE", "");
				str = "<div><span class=\"bold pingGreen\">UPDATE</span>" + str;
			} 
			if(str.startsWith("UP ")) {
				str = str.replace("UP", "");
				str = "<div><span class=\"bold pingGreen\">UP</span>" + str;
			} 
			if(str.startsWith("DOWN")) {
				str = str.replace("DOWN", "");
				str = "<div><span class=\"bold pingRed\">DOWN</span>" + str;
			} 
			if(str.startsWith("END")) {
				str = str.replace("END", "");
				str = "<div><span class=\"bold pingGreen\">END</span>" + str;
			} 
			if(str.contains("down"))
				str = "<div class=\"downtime\">" + str;
			if(str.startsWith("Ping")) {
				str = "<div class=\"ping\">" + str;
				String arr[] = str.split("(?=\\%)");
				String loss = "null";
				String ms = "null";
				for(String s : arr) {
					Matcher matcher = Pattern.compile("\\d+").matcher(s);
					matcher.find();
					String number = matcher.group();
					if(s.equals(arr[0]))
						loss = number;
					if(s.equals(arr[1]))
						ms = number;
				}
				if(Integer.parseInt(loss) < 10)
					arr[0] = arr[0].replace(loss, "<span class=\"pingGreen\">" + loss + "% Loss</span>");
				if(Integer.parseInt(loss) >= 10 && Integer.parseInt(loss) < 25)
					arr[0] = arr[0].replace(loss, "<span class=\"pingYellow\">" + loss + "% Loss</span>");
				if(Integer.parseInt(loss) >= 25)
					arr[0] = arr[0].replace(loss, "<span class=\"pingRed\">" + loss + "% Loss</span>");
				
				arr[1] = arr[1].replace("ms.", "");
				arr[1] = arr[1].replaceAll("% Loss", "");
				if(Integer.parseInt(ms) < 50)
					arr[1] = arr[1].replace(ms, "<span class=\"pingGreen\">" + ms + "ms.</span>");
				if(Integer.parseInt(ms) >= 50 && Integer.parseInt(ms) < 150)
					arr[1] = arr[1].replace(ms, "<span class=\"pingYellow\">" + ms + "ms.</span>");
				if(Integer.parseInt(ms) >= 150)
					arr[1] = arr[1].replace(ms, "<span class=\"pingRed\">" + ms + "ms.</span>");
				
				str = "";
				for(String s : arr) {
					str += s;
				}
			}
			str = str + "</div>";
			html.add(str);
		}
		return html;
	}
	
	private void makeStyles(String str){
		Path path = Paths.get(str);
		try (BufferedWriter out = Files.newBufferedWriter(path,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW)){
			out.write(".bold{ font-weight: 700; }\n");
			out.write(".pingGreen{ background-color: rgba(0,200,0,0.2); }\n");
			out.write(".pingYellow{ background-color: rgba(200,200,0,0.2); }\n");
			out.write(".pingRed{ background-color: rgba(200,0,0,0.2); }\n");
			out.write(".ping{ text-indent: 30px; }\n");
			out.write(".downtime{ text-indent: 30px; }\n");
			out.write(".netBlue{ background-color: rgba(0,0,200,0.2); }\n");
			out.write("a { margin: 0px 40px 20px 0px; }\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
