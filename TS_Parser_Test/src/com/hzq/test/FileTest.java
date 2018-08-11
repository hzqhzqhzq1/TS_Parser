package com.hzq.test;

import java.util.List;

import com.hzq.beans.Packet;
import com.hzq.beans.Section;
import com.hzq.utils.PacketUtil;
import com.hzq.utils.SectionUtil;
/**
 * ������
 * @author 910131
 *
 */
public class FileTest {
	private static String fileFordPath = "C:/Users/910131/Desktop/";
	private static String fileName = "������������-1.ts";

	public static void main(String[] args) {

		PacketUtil packetUtil  = PacketUtil.getPacketUtil(fileFordPath+fileName); 
		SectionUtil sectionUtil = SectionUtil.getSectionUtil();
		
		for(Packet p:packetUtil.getPidPacket(0x0011)) {
			sectionUtil.matchSection(p, 0x42);
		}
		List<Section> sectionList = sectionUtil.getSectionList();
		System.out.println("sectionList size:"+sectionList.size());
		for(Section s:sectionList) {
			System.out.println(byte2hex(s.getSectionData()));
			System.out.println(s.getSectionData().length);
		}
		System.out.println();
	}
	
	
	
	/**
	 * �ֽ�����ת16�����ַ������
	 * @param buffer
	 * @return
	 */
	private static String byte2hex(byte [] buffer){  
        String h = "";  
        for(int i = 0; i < buffer.length; i++){  
            String temp = Integer.toHexString(buffer[i] & 0xFF);  
            if(temp.length() == 1){  
                temp = "0" + temp;  
            }  
            h = h + " "+ temp;  
        }  
        return h;  
    } 
	
	
}
