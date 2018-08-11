package com.hzq.utils;

import java.util.ArrayList;
import java.util.List;

import com.hzq.beans.Packet;
import com.hzq.beans.Section;

public class SectionUtil {
	public static final int PACKET_HEADER_LENGTH = 4;
	public static final int SECTION_START_POSITION_1 = 5;
	public static final int SECTION_START_POSITION_2 = 4;
	public static final int PACKET_LENTH_188 = 188;
	public static final int PACKET_LENTH_204 = 204;
	public static final int SKIP_ONE = 1;
	
//	��ʼ��VersionNumer
	private int mVersionNumber = -1;
	
	private byte[][] mList;
	private int[] mCursor;
	private int[] mNextContinuityCounter;

	private List<Section> mSectionList = new ArrayList<Section>();
	
	
	public int sectionRNum=0;
	
	public SectionUtil() {

	}
	
	public static SectionUtil getSectionUtil() {
		return new SectionUtil();
	}

	public int matchSection(Packet p, int inputTableId) {

		byte[] packet = p.getPacket();

		int packetLength = packet.length;
		int thisPacketEffectiveLength = 0;


//		int syncByte = p.getSyncByte();
		int transportErrorIndicator = p.getTransportErrorIndicator();
		int payloadUnitStartIndicator = p.getPayloadUnitStartIndicator();
//		int pid = p.getPid();
		int coutinuityCounter = p.getContinuityCounter();

//		transportErrorIndicator�жϵ�ǰ���Ƿ�Ϊ��Ч��
		if (transportErrorIndicator == 0x1) {
			System.out.println("The packet is Error");
			return -1;
		}
		


//		�ж�Packet�Ƿ�Ϊ�װ�
//		Ϊ�װ�
		if (payloadUnitStartIndicator == 0x1) {
//			������ͷ������
//			��payloadUnitStartIndicatorΪ0x1ʱ�����±�Ϊ5���ֽڿ�ʼΪ��Ч����
//			int tableId = packet[SECTION_START_POSITION_1] & 0xFF;
			int sectionLength = (((packet[SECTION_START_POSITION_1 + 1] & 0xF) << 8)
					| (packet[SECTION_START_POSITION_1 + 2] & 0xFF)) & 0xFFF;
			int versionNumber = (packet[SECTION_START_POSITION_1 + 5] >> 1) & 0x1F;
			int sectionNumber = packet[SECTION_START_POSITION_1 + 6] & 0xFF;
			int lastSectionNumber = packet[SECTION_START_POSITION_1 + 7] & 0xFF;
			
			
			thisPacketEffectiveLength = getEffectiveLenth(packetLength, SKIP_ONE);
			
//			sectionLengthΪsectionLength�ֶκ󳤶�
			int sectionSize = sectionLength+3;
			
			/*
			 *�ж�VersionNumber 
			 */
			if(mVersionNumber == -1) {
				initData(versionNumber, lastSectionNumber);
			}
			if(mVersionNumber!=versionNumber) {
				System.out.println("versionNumber��һ��");
				initData(versionNumber, lastSectionNumber);
			}
			
//			����sectionNum�ж��Ƿ�����ӵ�mList��
			System.out.println(sectionNumber);
			int num = mCursor[sectionNumber];
			
			if(num == 0 ) {
//				System.out.println("sectionSize:"+sectionSize);
				mList[sectionNumber]  = new byte[sectionSize];
			}else {
				sectionRNum++;
				System.out.println("�Ѿ����ڣ�Ϊ�ظ���");
				System.out.println(sectionRNum);
				return -1;
			}
			

			if(sectionSize <= thisPacketEffectiveLength) {
//				Packet������ת��
//				����Ч���ݳ��ȴ��ڶ�
//				���γ��ȶ�ȡ
				for(int i =0;i<sectionSize;i++) {
//					System.out.println(i);
					mList[sectionNumber][i] = packet[SECTION_START_POSITION_1+i];
//					��¼��ǰsectionNumber�Ѿ������˶��ٸ��ֽ�
					mCursor[sectionNumber]++;
				}
				
				Section section = new Section(mList[sectionNumber]);
				System.out.println("����SectionList");
				mSectionList.add(section);
			}else {
//				Packet������ת��δ������������
				for(int i =0;i<thisPacketEffectiveLength;i++) {
//					System.out.println(i);
					mList[sectionNumber][i] = packet[SECTION_START_POSITION_1+i];
//					��¼��ǰsectionNumber�Ѿ������˶��ٸ��ֽ�
					mCursor[sectionNumber]++;
				}
				
//				��¼��һ������ContinuityCouter
				if(coutinuityCounter ==15) {
					coutinuityCounter = -1;
				}
				mNextContinuityCounter[sectionNumber]  = coutinuityCounter +1;
			}
		
//			return 1;
		}
//		��Section�װ�
		else {
			if(mVersionNumber == -1) {
				System.out.println("no versionNumber");
				return -1;
			}
			
			thisPacketEffectiveLength = getEffectiveLenth(packetLength, 0);
			
			int unFinishSectionNumber = -1;
//			Ѱ��δ������Section
			for(int i=0;i<mNextContinuityCounter.length;i++) {
				if(mNextContinuityCounter[i] == coutinuityCounter) {
					unFinishSectionNumber = i;
				}
			}
			if(unFinishSectionNumber == -1) {
				System.out.println("û��δ�����װ��");
				return -1;
			}
			
			int sectionSize = mList[unFinishSectionNumber].length;
			int afterSize = sectionSize - mCursor[unFinishSectionNumber];
			

			
			if(afterSize <= thisPacketEffectiveLength) {
				
//				Packet��Ч���ȱ�ʣ��Section����Ҫ��
				for(int i =0;i<afterSize;i++) {
//					System.out.println("��װ����ԭ��Section����װ���Section�����"+thisPacketEffectiveLength);
					mList[unFinishSectionNumber][mCursor[unFinishSectionNumber]]=packet[SECTION_START_POSITION_2+i];
					mCursor[unFinishSectionNumber]++;
				}
				
				Section section = new Section(mList[unFinishSectionNumber]);
				mSectionList.add(section);
				System.out.println("���װ�����SectionList");

			}else {
				
//				Packet��Ч���ȱ�ʣ��Section����ҪС
				for(int i =0;i<thisPacketEffectiveLength;i++) {
//					System.out.println("��װ����ԭ��Section δ�������"+thisPacketEffectiveLength);
					mList[unFinishSectionNumber][mCursor[unFinishSectionNumber]]=packet[SECTION_START_POSITION_2+i];
					mCursor[unFinishSectionNumber]++;
				}
				
				if(coutinuityCounter ==15) {
					coutinuityCounter = -1;
				}
				mNextContinuityCounter[unFinishSectionNumber]  = coutinuityCounter +1;
			}
		}
		return 0;
	}
	
	private int getEffectiveLenth(int packetLength,int skip) {
		int effectiveLength = 0;
//		�жϵ�ǰPacket����Ч���ݳ���
		if (packetLength == PACKET_LENTH_188) {
			effectiveLength = packetLength - PACKET_HEADER_LENGTH - skip;
		}else if(packetLength == PACKET_LENTH_204){
			effectiveLength = packetLength -PACKET_HEADER_LENGTH -skip - 16;
		}
		return effectiveLength; 
	}
	
	private void initData(int versionNumber,int lastSectionNumber) {
		int size = lastSectionNumber + 1;
		
		mVersionNumber = versionNumber;
		mList = new byte[size][];
		mCursor = new int[size];
		mNextContinuityCounter = new int[size];
		for(int i =0 ; i<size;i++) {
			mCursor[i] = 0;
			mNextContinuityCounter[i] = -1;
		}
		mSectionList.clear();
		System.out.println("cursor and  mlist size:"+size);
		System.out.println("initData");
	
	}
	
	public  List<Section> getSectionList() {
		if(mSectionList.size()==0) {
			System.out.println("No Section");
			return null;
		}
		return mSectionList;
	}
}
