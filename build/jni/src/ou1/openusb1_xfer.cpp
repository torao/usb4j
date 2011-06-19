/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: openusb1_xfer.cpp,v 1.2 2009/05/15 02:41:16 torao Exp $
*/
#define CONFDESC_MAXPOWER bMaxPower
#include <string.h>
#include <openusb.h>
#include "usb4java.h"
#include "org_koiroha_usb_impl_openusb_OpenUSB.h"

using namespace usb4j;

// Class name definition
#define CLS_OPENUSB "org/koiroha/usb/impl/openusb/OpenUSB"
#define CLS_REF            CLS_OPENUSB "$Ref"
#define CLS_DEVDATA        CLS_OPENUSB "$dev_data"
#define CLS_REQUEST_RESULT CLS_OPENUSB "$request_result_t"
#define CLS_CTRL_REQUEST   CLS_OPENUSB "$ctrl_request_t"
#define CLS_INTR_REQUEST   CLS_OPENUSB "$intr_request_t"
#define CLS_BULK_REQUEST   CLS_OPENUSB "$bulk_request_t"
#define CLS_ISOC_REQUEST   CLS_OPENUSB "$isoc_request_t"
#define CLS_REQUEST_HANDLE CLS_OPENUSB "$request_handle_t"
#define CLS_REQUEST_HANDLE_CALLBACK CLS_OPENUSB "$request_handle_callback"

// Prototype declaration for local functions
static int32_t request_xfer_sync(JVM& jvm, jobject request);
static int32_t request_xfer_async(JVM& jvm, jobject request);
static int32_t request_ctrl_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request);
static int32_t request_intr_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request);
static int32_t request_bulk_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request);
static int32_t request_isoc_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request);
static openusb_request_handle_t build_request_handle(JVM& jvm, jobject request_handle, bool sharedbuf);
static void commit_request_handle(JVM& jvm, openusb_request_handle_t handle, jobject request, bool sharedbuf);
static void dispose_request_handle(JVM& jvm, openusb_request_handle_t handle, jobject request, bool sharedbuf);
static openusb_ctrl_request_t* build_ctrl_request(JVM& jvm, jobjectArray request, bool sharedbuf);
static void commit_ctrl_request(JVM& jvm, openusb_ctrl_request_t* ctrl, jobjectArray request, bool sharedbuf);
static void dispose_ctrl_request(openusb_ctrl_request_t* ctrl, bool sharedbuf);
static int32_t xfer_request_callback(struct openusb_request_handle *handle);

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _xfer
 * Signature: (Lorg/koiroha/usb/impl/openusb/OpenUSB$request_handle_t;Z)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1xfer__Lorg_koiroha_usb_impl_openusb_OpenUSB_00024request_1handle_1t_2Z
  (JNIEnv *env, jclass, jobject request_handle, jboolean async)
{
	JVM jvm(env);

	int32_t ret = OPENUSB_SUCCESS;
	if(async){
		ret = request_xfer_async(jvm, request_handle);
	} else {
		ret = request_xfer_sync(jvm, request_handle);
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _xfer
 * Signature: (IJBB[Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1xfer__IJBB_3Ljava_lang_Object_2
  (JNIEnv *env, jclass, jint type, jlong dev, jbyte ifc, jbyte ept, jobjectArray request)
{
	JVM jvm(env);

	int32_t ret = OPENUSB_SUCCESS;
	switch(type){
	case USB_TYPE_CONTROL:
		ret = request_ctrl_xfer(jvm, (openusb_dev_handle_t)dev, (uint8_t)ifc, (uint8_t)ept, request);
		break;
	case USB_TYPE_INTERRUPT:
		ret = request_intr_xfer(jvm, (openusb_dev_handle_t)dev, (uint8_t)ifc, (uint8_t)ept, request);
		break;
	case USB_TYPE_BULK:
		ret = request_bulk_xfer(jvm, (openusb_dev_handle_t)dev, (uint8_t)ifc, (uint8_t)ept, request);
		break;
	case USB_TYPE_ISOCHRONOUS:
		ret = request_isoc_xfer(jvm, (openusb_dev_handle_t)dev, (uint8_t)ifc, (uint8_t)ept, request);
		break;
	default:
		jvm.abort("unsupported transfer type: %d", type);
		break;
	}
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _abort
 * Signature: (Lorg/koiroha/usb/impl/openusb/OpenUSB$request_handle_t;)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1abort
  (JNIEnv *env, jclass, jobject request)
{
	JVM jvm(env);
	openusb_request_handle_t handle = (openusb_request_handle_t)jvm.getLong(request, CLS_REQUEST_HANDLE, "ref");
	TRACE("abort(%p)", (void*)handle);

	if(handle == NULL){
		jvm.log(LOG_FINE, "async request already completed of not started");
		return OPENUSB_SUCCESS;
	}

	int32_t ret = openusb_abort(handle);
	return ret;
}

/*
 * Class:     org_koiroha_usb_impl_openusb_OpenUSB
 * Method:    _wait
 * Signature: ([Lorg/koiroha/usb/impl/openusb/OpenUSB$request_handle_t;[IZ)I
 */
JNIEXPORT jint JNICALL Java_org_koiroha_usb_impl_openusb_OpenUSB__1wait
  (JNIEnv *env, jclass, jobjectArray requests, jintArray buf, jboolean wait)
{
	JVM jvm(env);

	// create and initialize request_handler array
	uint32_t len = (uint32_t)jvm.env->GetArrayLength(requests);
	openusb_request_handle_t* handles = (openusb_request_handle_t*)calloc(len, sizeof(openusb_request_handle_t));
	for(uint32_t i=0; i<len; i++){

		// set request_handle to array
		jobject request = jvm.env->GetObjectArrayElement(requests, i);
		handles[i] = (openusb_request_handle_t)jvm.getLong(request, CLS_REQUEST_HANDLE, "ref");

		// exception if request_handler has async callback (spec of openusb)
		if(handles[i]->cb != NULL){
			jvm.raisef("cannot wait/poll for the request that has async callback: %d", i);
			free(handles);
			return 0;
		}
	}

	// execute wait()/poll()
	openusb_request_handle_t handle = NULL;
	int32_t ret = OPENUSB_SUCCESS;
	if(wait){
		TRACE("wait(%d)", (int)len);
		ret = openusb_wait(len, handles, &handle);
	} else {
		TRACE("poll(%d)", (int)len);
		ret = openusb_poll(len, handles, &handle);
	}

	// find index of request_handle and set return buffer
	// if poll() called without complete request, return negative index
	jint index = -1;
	for(uint32_t i=0; i<len; i++){
		if(handles[i] == handle){
			index = i;
			break;
		}
	}
	set_int_to_buffer(jvm, buf, index);

	// release handles array
	free(handles);
	return ret;
}

/*
 * Request synchronous transfer.
 */
static int32_t request_xfer_sync(JVM& jvm, jobject request){
	TRACE("xfer_wait(req)");

	// build handle for specified request
	openusb_request_handle_t handle = build_request_handle(jvm, request, true);
	if(handle == NULL){
		return 0;
	}

	// notify if async callback specified
	ASSERT(handle->cb == NULL);

	// execute and wait for request complete
	int32_t ret = openusb_xfer_wait(handle);
	if(ret == OPENUSB_SUCCESS){
		commit_request_handle(jvm, handle, request, true);
	}

	// dispose request handler
	dispose_request_handle(jvm, handle, request, true);
	return ret;
}

/*
 * Request asynchronous transfer.
 */
static int32_t request_xfer_async(JVM& jvm, jobject request){
	TRACE("xfer_aio(req)");

	// build handle for specified request
	openusb_request_handle_t handle = build_request_handle(jvm, request, false);
	if(handle == NULL){
		return 0;
	}

	// execute and exit without to wait complete
	int32_t ret = openusb_xfer_aio(handle);
	return ret;
}

/*
 * execute control transfer request.
 */
static int32_t request_ctrl_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request){
	TRACE("ctrl_xfer(%p,%d,%d,ctrl)", (void*)dev, (int)ifc & 0xFF, (int)ept & 0xFF);
	openusb_ctrl_request_t* ctrl = build_ctrl_request(jvm, request, true);
	if(ctrl == NULL){
		return OPENUSB_NO_RESOURCES;
	}
	int32_t ret = openusb_ctrl_xfer(dev, ifc, ept, ctrl);
	if(ret == OPENUSB_SUCCESS){
		commit_ctrl_request(jvm, ctrl, request, true);
	}
	dispose_ctrl_request(ctrl, true);
	return ret;
}

/*
 * execute control transfer request.
 */
static int32_t request_intr_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request){
	TRACE("intr_xfer(%p,%d,%d,intr)", (void*)dev, (int)ifc & 0xFF, (int)ept & 0xFF);
	jvm.raise(UNSUPPORTED_OPERATION_EXCEPTION, "request_intr_xfer()");
	return 0;
}

/*
 * execute control transfer request.
 */
static int32_t request_bulk_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request){
	TRACE("bulk_xfer(%p,%d,%d,bulk)", (void*)dev, (int)ifc & 0xFF, (int)ept & 0xFF);
	jvm.raise(UNSUPPORTED_OPERATION_EXCEPTION, "request_bulk_xfer()");
	return 0;
}

/*
 * execute control transfer request.
 */
static int32_t request_isoc_xfer(JVM& jvm, openusb_dev_handle_t dev, uint8_t ifc, uint8_t ept, jobjectArray request){
	TRACE("isoc_xfer(%p,%d,%d,isoc)", (void*)dev, (int)ifc & 0xFF, (int)ept & 0xFF);
	jvm.raise(UNSUPPORTED_OPERATION_EXCEPTION, "request_isoc_xfer()");
	return 0;
}

/*
 * Build request_handle structure. Return NULL if error.
 */
static openusb_request_handle_t build_request_handle(JVM& jvm, jobject request_handle, bool sharedbuf){

	// allocate memory for request_handle
	openusb_request_handle* handle = (openusb_request_handle*)calloc(1, sizeof(openusb_request_handle));
	if(handle == NULL){
		jvm.raise(OUT_OF_MEMORY_ERROR_CLASS, "");
		return NULL;
	}
	TRACE("build_request_handle(%p)", handle);

	//
	handle->dev = (openusb_dev_handle_t)jvm.getLong(request_handle, CLS_REQUEST_HANDLE, "dev");
	handle->interface = (uint8_t)jvm.getByte(request_handle, CLS_REQUEST_HANDLE, "intf");
	handle->endpoint = (uint8_t)jvm.getByte(request_handle, CLS_REQUEST_HANDLE, "edpt");
	handle->type = (openusb_transfer_type_t)jvm.getInt(request_handle, CLS_REQUEST_HANDLE, "type");

	jobjectArray req = NULL;
	switch(handle->type){
	case USB_TYPE_CONTROL:
		req = (jobjectArray)jvm.getObject(request_handle, CLS_REQUEST_HANDLE, "[L" CLS_CTRL_REQUEST ";", "ctrl");
		handle->req.ctrl = build_ctrl_request(jvm, req, sharedbuf);
		break;
	case USB_TYPE_INTERRUPT:
		// FIXME
		break;
	case USB_TYPE_BULK:
		// FIXME
		break;
	case USB_TYPE_ISOCHRONOUS:
		// FIXME
		break;
	default:
		jvm.raisef(ILLEGAL_ARGUMENT_EXCEPTION_CLASS, "unsupported transfer type: %d", handle->type);
		free(handle);
		return NULL;
	}

	// set async callback if async request
	if(! sharedbuf){
		handle->cb = xfer_request_callback;
		handle->arg = jvm.env->NewGlobalRef(request_handle);
	}

	// store request_handle pointer to java class
	jvm.setLong(request_handle, CLS_REQUEST_HANDLE, "ref", (jlong)handle);
	return handle;
}

/*
 * Build request_handle structure. Return NULL if error.
 */
static void commit_request_handle(JVM& jvm, openusb_request_handle_t handle, jobject request, bool sharedbuf){
	jobjectArray req = NULL;
	switch(handle->type){
	case USB_TYPE_CONTROL:
		req = (jobjectArray)jvm.getObject(request, CLS_REQUEST_HANDLE, "[L" CLS_CTRL_REQUEST ";", "ctrl");
		commit_ctrl_request(jvm, handle->req.ctrl, req, sharedbuf);
		break;
	case USB_TYPE_INTERRUPT:
		// FIXME
		break;
	case USB_TYPE_BULK:
		// FIXME
		break;
	case USB_TYPE_ISOCHRONOUS:
		// FIXME
		break;
	default:
		jvm.raisef(ILLEGAL_ARGUMENT_EXCEPTION_CLASS, "unsupported transfer type: %d", handle->type);
		free(handle);
		return;
	}
	return;
}

/*
 * Release request_handle structure.
 */
static void dispose_request_handle(JVM& jvm, openusb_request_handle_t handle, jobject request, bool sharedbuf){

	if(handle->req.ctrl != NULL){
		dispose_ctrl_request(handle->req.ctrl, sharedbuf);
	}
	// FIXME
	/*
	if(handle->req.intr != NULL){
		dispose_intr_request(handle->req.intr, sharedbuf);
	}
	if(handle->req.bulk != NULL){
		dispose_bulk_request(handle->req.bulk, sharedbuf);
	}
	if(handle->req.isoc != NULL){
		dispose_isoc_request(handle->req.isoc, sharedbuf);
	}
	*/

	// arg field set to global reference of java request_handle_t object
	if(handle->arg != NULL){
		jvm.env->DeleteGlobalRef((jobject)handle->arg);
	}

	// release handle
	free(handle);

	jvm.setLong(request, CLS_REQUEST_HANDLE, "ref", (jlong)NULL);
	return;
}

/*
 * Build control request structure from specified java ctrl_request_t object.
 * If sharedbuf is true, the payload buffers are shared to java array. It's useful
 * for synchronous request.
 */
static openusb_ctrl_request_t* build_ctrl_request(JVM& jvm, jobjectArray request, bool sharedbuf){

	// allocate control requests as array
	jsize len = jvm.env->GetArrayLength(request);
	openusb_ctrl_request_t* ctrl = (openusb_ctrl_request_t*)calloc(len, sizeof(openusb_ctrl_request_t));

	for(jsize i=0; i<len; i++){
		jobject elem = jvm.env->GetObjectArrayElement(request, i);

		// initialize primitive fields
		ctrl[i].setup.bmRequestType = (uint8_t)jvm.getByte(elem, CLS_CTRL_REQUEST, "bmRequestType");
		ctrl[i].setup.bRequest = (uint8_t)jvm.getByte(elem, CLS_CTRL_REQUEST, "bRequest");
		ctrl[i].setup.wValue = (uint16_t)jvm.getShort(elem, CLS_CTRL_REQUEST, "wValue");
		ctrl[i].setup.wIndex = (uint16_t)jvm.getShort(elem, CLS_CTRL_REQUEST, "wIndex");
		ctrl[i].timeout = (uint32_t)jvm.getInt(elem, CLS_CTRL_REQUEST, "timeout");
		ctrl[i].flags = (uint32_t)jvm.getInt(elem, CLS_CTRL_REQUEST, "flags");

		// payload buffer
		jbyteArray payload = jvm.getByteArray(elem, CLS_CTRL_REQUEST, "payload");
		jsize buflen = jvm.env->GetArrayLength(payload);
		jbyte* buffer = jvm.env->GetByteArrayElements(payload, NULL);
		if(sharedbuf){
			ctrl[i].payload = (uint8_t*)buffer;
		} else {
			ctrl[i].payload = (uint8_t*)malloc(buflen);
			if(ctrl[i].payload == NULL){
				dispose_ctrl_request(ctrl, sharedbuf);
				return NULL;
			}
			memcpy(ctrl[i].payload, buffer, buflen);
		}
		ctrl[i].length = (uint32_t)buflen;

		// result structure
		jobject result = jvm.getObject(elem, CLS_CTRL_REQUEST, "L" CLS_REQUEST_RESULT ";", "result");
		ctrl[i].result.status = (int32_t)jvm.getInt(result, CLS_REQUEST_RESULT, "status");
		ctrl[i].result.transferred_bytes = (int32_t)jvm.getInt(result, CLS_REQUEST_RESULT, "transferred_bytes");

		// list structure
		if(i+1<len){
			ctrl[i].next = &ctrl[i+1];
		} else {
			ctrl[i].next = NULL;
		}
		TRACE("bmRequestType=0x%02X,bRequest=0x%02X,wValue=0x%04X,wIndex=%d,timeout=%d,flags=0x%02X,length=%d,next=%p",
			(int)ctrl[i].setup.bmRequestType, (int)ctrl[i].setup.bRequest,
			(int)ctrl[i].setup.wValue, (int)ctrl[i].setup.wIndex,
			(int)ctrl[i].timeout, (int)ctrl[i].flags, (int)ctrl[i].length,
			ctrl[i].next);
	}
	return ctrl;
}

/*
 * Commit control request to specified java ctrl_request_t object array.
 */
static void commit_ctrl_request(JVM& jvm, openusb_ctrl_request_t* ctrl, jobjectArray request, bool sharedbuf){

	// allocate control requests as array
	jsize len = jvm.env->GetArrayLength(request);

	for(jsize i=0; i<len; i++){
		jobject elem = jvm.env->GetObjectArrayElement(request, i);

		jvm.setByte(elem, CLS_CTRL_REQUEST, "bmRequestType", (jbyte)ctrl[i].setup.bmRequestType);
		jvm.setByte(elem, CLS_CTRL_REQUEST, "bRequest", (jbyte)ctrl[i].setup.bRequest);
		jvm.setShort(elem, CLS_CTRL_REQUEST, "wValue", (jshort)ctrl[i].setup.wValue);
		jvm.setShort(elem, CLS_CTRL_REQUEST, "wIndex", (jshort)ctrl[i].setup.wIndex);
		jvm.setInt(elem, CLS_CTRL_REQUEST, "timeout", (jint)ctrl[i].timeout);
		jvm.setInt(elem, CLS_CTRL_REQUEST, "flags", (jint)ctrl[i].flags);

		// payload buffer
		jbyteArray payload = jvm.getByteArray(elem, CLS_CTRL_REQUEST, "payload");
		jsize buflen = jvm.env->GetArrayLength(payload);
		ASSERT((size_t)buflen == ctrl[i].length);
		if(sharedbuf){
			jvm.env->ReleaseByteArrayElements(payload, (jbyte*)ctrl[i].payload, 0);
			ctrl[i].payload = NULL;
		} else {
			jbyte* buffer = jvm.env->GetByteArrayElements(payload, NULL);
			memcpy(buffer, ctrl[i].payload, buflen);
			jvm.env->ReleaseByteArrayElements(payload, buffer, 0);
		}

		jobject result = jvm.getObject(elem, CLS_CTRL_REQUEST, "L" CLS_REQUEST_RESULT ";", "result");
		jvm.setInt(result, CLS_REQUEST_RESULT, "status", (jint)ctrl[i].result.status);
		jvm.setInt(result, CLS_REQUEST_RESULT, "transferred_bytes", (jint)ctrl[i].result.transferred_bytes);

		ASSERT((ctrl[i].next == NULL && (i+1)==len) || (ctrl[i].next != NULL && (i+1)<len));
	}
	return;
}

/*
 * Dispose control request.
 */
static void dispose_ctrl_request(openusb_ctrl_request_t* ctrl, bool sharedbuf){
	openusb_ctrl_request_t* mover = ctrl;
	do{
		if(! sharedbuf){
			free(mover->payload);
		}
		mover = mover->next;
	} while(mover != NULL);
	free(ctrl);
	return;
}

static int32_t xfer_request_callback(struct openusb_request_handle* handle){

	// find JNIEnv attached to current thread
	JVMCallbackEnv ce;
	if(ce.env == NULL){
		return OPENUSB_CB_TERMINATE;
	}

	JVM jvm(ce.env);
	TRACE("xfer_request_callback()");
	jobject request = (jobject)handle->arg;
	commit_request_handle(jvm, handle, request, false);

	// execute callback
	int32_t ret = jvm.callInt(request, CLS_REQUEST_HANDLE, "async_callback", "()V");

	// dispose request handle
	dispose_request_handle(jvm, handle, request, false);

	return ret;
}
