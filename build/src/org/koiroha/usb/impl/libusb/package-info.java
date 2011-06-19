/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: package-info.java,v 1.1 2009/05/09 20:51:51 torao Exp $
*/
/**
 * Java から libusb 機能を利用可能するための低レベルパッケージです。libusb の知識で利用可能な
 * 静的メソッドを用意しています。具体的な機能は該当する libusb バージョンごとのクラス API を
 * 参照してください。
 * <p>
 * このパッケージはプラットフォームにインストールされている libusb 機能を呼び出す JNI インター
 * フェースです。利用するにはプラットフォームごとに libusb がインストールされている必要があります。
 * <p>
 * <table border="1">
 * <tr><th>バージョン</th><th>JNIライブラリ名</th><th>Javaクラス名</th></tr>
 * <tr><th>libusb 0.1</th><td><code>lu04j</code></td><td>{@link org.koiroha.usb.impl.libusb.LibUSB0}</td></tr>
 * <tr><th>libusb 1.0</th><td><code>lu14j</code></td><td>N/A</td></tr>
 * </table>
 * <p>
 * このパッケージから使用する JNI ライブラリはプラットフォームごとに用意された JAR ファイルに
 * バンドルされています。
 */
package org.koiroha.usb.impl.libusb;
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
