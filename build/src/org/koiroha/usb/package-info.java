/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: package-info.java,v 1.3 2009/05/18 15:38:05 torao Exp $
*/
/**
 * USB for Java のコアライブラリです。このパッケージは抽象化された USB 論理構造ビューとデバ
 * イス操作を行うためのインターフェースで構成されています。プログラマが意識するオブジェクト構造は
 * 以下の図のようになります。
 * <p>
 * <img src="doc-files/class-chart.png" alt="class chart">
 * <p>
 * usb4j を使用した一般的なデバイス操作は以下の手順となります。詳細はそれぞれのクラスの API
 * リファレンスを参照してください。
 * <p>
 * <ol>
 * <li>コンテキストの構築 → {@link org.koiroha.usb.USBServiceManager#getContext()}</li>
 * <li>USB デバイスの取得 → {@link org.koiroha.usb.USBContext#lookup()}</li>
 * <li>USB デバイスのオープン → {@link org.koiroha.usb.Device#open()}</li>
 * <li>インターフェースの要求 → {@link org.koiroha.usb.Interface#claim()}</li>
 * <li>エンドポイントへの入出力操作 → {@link org.koiroha.usb.Endpoint}</li>
 * <li>インターフェースの解放 → {@link org.koiroha.usb.Interface#release()}</li>
 * <li>USB デバイスのクローズ → {@link org.koiroha.usb.Device#close()}</li>
 * <li>コンテキストの終了 → {@link org.koiroha.usb.USBContext#dispose()}</li>
 * </ol>
 * <p>
 * 例として全てのデバイスのベンダー ID / 製品 ID を出力するコードは以下のように記述することが
 * 出来ます。
 * <p>
 * <pre>USBSession session = USBServiceManager.getSession();
 * for(Device device: session.lookup()){
 *     DeviceDescriptor desc = device.getDescriptor();
 *     System.out.printf("%04X:%04X\n",
 *         desc.getVendorId(), desc.getProductId());
 * }
 * session.release();</pre>
 * <p>
 */
package org.koiroha.usb;
