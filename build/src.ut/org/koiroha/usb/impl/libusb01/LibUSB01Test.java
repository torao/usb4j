/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: LibUSB01Test.java,v 1.6 2009/05/16 11:05:49 torao Exp $
*/
package org.koiroha.usb.impl.libusb01;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.koiroha.usb.TestObject;
import org.koiroha.usb.impl.libusb.LibUSB0;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LibUSB01Test:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version usb4j 1.0 $Revision: 1.6 $ $Date: 2009/05/16 11:05:49 $
 * @author torao
 * @since 2009/05/01 Java2 SE 5.0
 */
public class LibUSB01Test extends TestObject{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LibUSB01Test.class.getName());

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#init()} のためのテスト・メソッド。
	 */
	@Test
	public void testInit(){
		logger.info("init()");
		LibUSB0.init();
		logger.info(LibUSB0.strerror());
		return;
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#set_debug(int)} のためのテスト・メソッド。
	 */
	@Test
	public void testSet_debug() {
		logger.info("set_debug(1)");
		LibUSB0.set_debug(1);
		logger.info(LibUSB0.strerror());
		return;
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#find_busses()} のためのテスト・メソッド。
	 */
	@Test
	public void testFind_busses() {
		logger.info("find_busses()");
		LibUSB0.find_busses();
		logger.info(LibUSB0.strerror());
		return;
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#find_devices()} のためのテスト・メソッド。
	 */
	@Test
	public void testFind_devices() {
		logger.info("find_devices()");
		LibUSB0.find_devices();
		logger.info(LibUSB0.strerror());
		return;
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#get_busses()} のためのテスト・メソッド。
	 */
	@Test
	public void testGet_busses() {
		logger.info("get_busses()");
		LibUSB0.Bus bus = LibUSB0.get_busses();
		while(bus != null){
			logger.info("Bus: [" + bus.location + "] " + bus.dirname + ": " + bus.root_dev);
			LibUSB0.Device dev = bus.devices;
			while(dev != null){
				logger.info("  Device: " + dev.filename);
				dev = dev.next;
			}
			bus = bus.next;
		}
		logger.info(LibUSB0.strerror());
		return;
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#strerror()} のためのテスト・メソッド。
	 */
	@Test
	public void testStrerror() {
		logger.info("strerror()");
		logger.info(LibUSB0.strerror());
		return;
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#open(org.koiroha.usb.impl.libusb.LibUSB0.Device)} のためのテスト・メソッド。
	 */
	@Test
	public void testOpen() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#close(long)} のためのテスト・メソッド。
	 */
	@Test
	public void testClose() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#get_string(long, int, int, byte[])} のためのテスト・メソッド。
	 */
	@Test
	public void testGet_string() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#get_string_simple(long, int, byte[])} のためのテスト・メソッド。
	 */
	@Test
	public void testGet_string_simple() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#get_descriptor_by_endpoint(long, int, byte, byte, byte[])} のためのテスト・メソッド。
	 */
	@Test
	public void testGet_descriptor_by_endpoint() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#get_descriptor(long, byte, byte, byte[])} のためのテスト・メソッド。
	 */
	@Test
	public void testGet_descriptor() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#bulk_write(long, int, byte[], int, int, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testBulk_write() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#bulk_read(long, int, byte[], int, int, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testBulk_read() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#interrupt_write(long, int, byte[], int, int, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testInterrupt_write() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#interrupt_read(long, int, byte[], int, int, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testInterrupt_read() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#control_msg(long, int, int, int, int, byte[], int)} のためのテスト・メソッド。
	 */
	@Test
	public void testControl_msg() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#set_configuration(long, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testSet_configuration() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#claim_interface(long, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testClaim_interface() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#release_interface(long, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testRelease_interface() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#set_altinterface(long, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testSet_altinterface() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#clear_halt(long, int)} のためのテスト・メソッド。
	 */
	@Test
	public void testClear_halt() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link org.koiroha.usb.impl.libusb.LibUSB0#reset(long)} のためのテスト・メソッド。
	 */
	@Test
	public void testReset() {
		fail("まだ実装されていません");
	}

}
