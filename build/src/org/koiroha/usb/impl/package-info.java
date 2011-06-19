/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: package-info.java,v 1.1 2009/05/18 15:38:06 torao Exp $
*/
/**
 * プラットフォームごとの USB 実装を usb4j へポーティングするためのパッケージです。ポーティング
 * 作業を簡略化するために usb4j のインターフェースに対する実装クラスが用意されています。開発者は
 * {@link org.koiroha.usb.impl.USBBridge} を実装し、{@link org.koiroha.usb.impl.USBServiceImpl}
 * のサブクラスでそのオブジェクトを指定する事でプラットフォームの USB 実装を usb4j へポーティ
 * ングすることが出来ます。
 * <p>
 */
package org.koiroha.usb.impl;
