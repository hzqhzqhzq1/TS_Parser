package com.hzq.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hzq.beans.Packet;

/**
 * Packet������
 * 
 * @author 910131
 *
 */
public class PacketUtil {
	private static final int PACKET_HEADER_SYNC_BYTE = 0x47;
	private static final int PACKET_LENTH_188 = 188;
	private static final int PACKET_LENTH_204 = 204;
//	private static final int PAT_PID = 0x0000;
	private static final int CYCLE_TEN_TIMES = 10;

	private String inputFilePath;
	private int packetStartPosition = -1;
	private int packetLength = -1;

	private PacketUtil(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	public static PacketUtil getPacketUtil(String inputFilePath) {
		PacketUtil packetUtil = new PacketUtil(inputFilePath);
		packetUtil.getPacketLenth();
		return packetUtil;
	}

	private int getPacketStartPosition() {
		return packetStartPosition;
	}

	public int getPacketLenth() {
		File file = new File(inputFilePath);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			int tmp;
			boolean packetLengthConfirm;
			while ((tmp = fis.read()) != -1) {
				
				packetStartPosition++;
				packetLengthConfirm = false;
				System.out.println("====��ǰλ��Ϊ��===��" + packetStartPosition + "0x" + Integer.toHexString(tmp&0xff));
				if (tmp == PACKET_HEADER_SYNC_BYTE) {
					/**
					 * ѭ��10����188����Ƿ�Ϊ0x47
					 */
					for (int i = 0; i < CYCLE_TEN_TIMES; i++) {
						long lg = fis.skip(PACKET_LENTH_188 - 1);
						// ���Ȳ������
						if (lg != PACKET_LENTH_188 - 1) {
							System.out.println("====��ת187�ֽ�ʧ��=====");
							return -1;
						}
						// ��ȡ����187�ֽں���ֽ�
						tmp = fis.read();
						System.out.println("====��ǰ�Ѿ�����" + PACKET_LENTH_188 * (i + 1) + "byte");
						// ���ַ�0x47
						if (tmp != PACKET_HEADER_SYNC_BYTE) {
							// �ص�ѭ����ʼλ��
							lg = fis.skip((-1) * PACKET_LENTH_188 * (i + 1));
							// ��Ҫ��������Ƿ�Ϊ204�ֽ�
							break;
						}
						if(i==CYCLE_TEN_TIMES-1) {
							packetLengthConfirm = true;
						}
					}
					
					if (packetLengthConfirm) {
						packetLength = PACKET_LENTH_188;
						break;
					}
					/**
					 * ѭ��10����204����Ƿ�Ϊ204
					 */
					for (int i = 0; i < CYCLE_TEN_TIMES; i++) {
						long lg = fis.skip(PACKET_LENTH_204 - 1);
						if (lg != PACKET_LENTH_204 - 1) {
							System.out.println("====��ת203�ֽ�ʧ��=====");
							return -1;
						}

						tmp = fis.read();
						System.out.println("====�Ѿ�����" + PACKET_LENTH_204 * (i + 1) + "byte");
						if (tmp != PACKET_HEADER_SYNC_BYTE) {
							lg = fis.skip((-1) * PACKET_LENTH_204 * (i + 1));
							break;
						}
						if(i==CYCLE_TEN_TIMES-1) {
							packetLengthConfirm = true;
						}
					}
					if (packetLengthConfirm) {
						packetLength = PACKET_LENTH_204;
						break;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		return packetLength;
	}

	public List<Packet> getPidPacket(int PID) {
		inputFilePath = getInputFilePath();
		List<Packet> packetList = new ArrayList<Packet>();
		FileInputStream fis = null;
		if (packetLength == -1) {
			System.out.println("����getPidPacket��ʱpacketLenth:" + packetLength);
			packetLength = getPacketLenth();
		}
		try {
 			fis = new FileInputStream(inputFilePath);
			long lg = fis.skip(getPacketStartPosition());
			System.out.println(packetStartPosition);
			if (lg != packetStartPosition) {
				System.out.println("��תʧ��");
				return null;
			}
			int tmp;
			int packetNum = 0;
			do {
				byte[] buff = new byte[packetLength];
				tmp = fis.read(buff);
				if (tmp == packetLength) {
					if (buff[0] == PACKET_HEADER_SYNC_BYTE) {
						packetNum++;
						Packet packet = new Packet(buff);
						if (packet.getPid() == PID) {
							packetList.add(packet);
						}
					}
				}
			} while (tmp != -1);
			System.out.println("packetNum:" + packetNum);
			System.out.println("List���ȣ�" + packetList.size());
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return packetList;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

}
