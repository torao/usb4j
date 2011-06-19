/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: GarminGPSTest.java,v 1.8 2009/05/18 15:38:07 torao Exp $
*/
package org.koiroha.usb;

import java.nio.*;
import java.util.*;

import org.junit.Test;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// GarminGPSTest:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version usb4j 1.0 $Revision: 1.8 $ $Date: 2009/05/18 15:38:07 $
 * @author torao
 * @since 2009/04/25 Java2 SE 5.0
 */
public class GarminGPSTest extends TestObject{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GarminGPSTest.class.getName());

	// ======================================================================
	// Garmin GPS デバイスのテスト
	// ======================================================================
	/**
	 * Garmin GPS デバイスを検出してテストを行います。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void garmin() throws Exception{
		int idVendor = 0x091E;
		int idProduct = 0x0003;

		USBContext session = USBServiceManager.getContext();
		List<Device> devices = session.lookup(idVendor, idProduct);
		if(devices.size() == 0){
			logger.warning("Garmin GPS デバイスが検出されませんでした. テストをスキップします");
			session.dispose();
			return;
		}

		for(int i=0; i<devices.size(); i++){
			Device device = devices.get(i);
			logger.info("Garming GPS[" + i + "] " + device);
			device.open();
			Interface intf = device.getConfigurations().get(i).getInterfaces().get(i);
			intf.claim();
			List<Endpoint> endpoints = intf.getAltSettings().get(0).getEndpoints();
			scenario(endpoints.get(2), endpoints.get(1), endpoints.get(3));
//			for(int j=1; j<endpoints.size(); j++){
//				Endpoint e = endpoints.get(j);
//				logger.info("endpoint: " + e);
//				if(e.getDirection() == Endpoint.Direction.OUT){
//					e.reset();
//					e.write(new byte[0], 0, 0);
//				} else if(e.getDirection() == Endpoint.Direction.IN){
//					e.read(new byte[0], 0, 0);
//				}
//			}
			intf.release();
		}
		session.dispose();
		return;
	}

	// ======================================================================
	// シナリオの実行
	// ======================================================================
	/**
	 * シナリオを実行します。
	 * <p>
	 * @param out 出力エンドポイント
	 * @param in 入力エンドポイント
	 * @param bulk バルク入力エンドポイント
	 * @throws Exception テストに失敗した場合
	 */
	private void scenario(Endpoint out, Endpoint in, Endpoint bulk) throws Exception{
		final byte USB_PROTOCOL = 0;
		final byte APP_PROTOCOL = 2;
		final short PID_START_SESSION = 5;
		final short PID_PRODUCT_RQST = 254;
//		final short PID_PRODUCT_DATA = 255;

		out.clearHalt();
		in.clearHalt();
		bulk.clearHalt();

		// コンテキストの開始
		ByteBuffer buffer = make(USB_PROTOCOL, PID_START_SESSION, 0);
		byte[] outbuf = buffer.array();
		out.write(outbuf, 0, buffer.position(), 1000);
		logger.info(Arrays.toString(outbuf));

		// コンテキスト開始の応答
		byte[] inbuf = new byte[in.getDescriptor().getMaxPacketSize()];
		int length = in.read(inbuf, 0, inbuf.length, 1000);
		buffer = ByteBuffer.wrap(inbuf, 0, length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		logger.info(Arrays.toString(inbuf));
		logger.info(String.format("%02X:%04X:%04X",
				buffer.get(0), buffer.getShort(4) & 0xFFFF, buffer.getInt(12)));

		// 製品情報取得
		buffer = make(APP_PROTOCOL, PID_PRODUCT_RQST, 0);
		outbuf = buffer.array();
		out.write(outbuf, 0, buffer.position(), 1000);
		logger.info(Arrays.toString(outbuf));

		// コンテキスト開始の応答
		length = in.read(inbuf, 0, inbuf.length, 1000);
		buffer = ByteBuffer.wrap(inbuf, 0, length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		logger.info(Arrays.toString(inbuf));
		logger.info(String.format("%02X:%04X",
				buffer.get(0), buffer.getShort(4) & 0xFFFF));

		return;
	}

	// ======================================================================
	// パケットの生成
	// ======================================================================
	/**
	 * パケット用のバイトバッファを作成します。
	 * <p>
	 * @param packetType パケット型
	 * @param packetId パケット ID
	 * @param length データの長さ
	 * @return バイトバッファ
	 */
	private ByteBuffer make(byte packetType, short packetId, int length){
		ByteBuffer buffer = ByteBuffer.allocate(12 + length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(packetType);
		buffer.put((byte)0x00);
		buffer.put((byte)0x00);
		buffer.put((byte)0x00);
		buffer.putShort(packetId);
		buffer.put((byte)0x00);
		buffer.put((byte)0x00);
		buffer.putInt(length);
		return buffer;
	}

}
