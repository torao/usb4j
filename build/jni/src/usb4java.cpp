/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: usb4java.cpp,v 1.6 2009/05/18 11:02:23 torao Exp $
*/

/* **************************************************************************
 * This file must save us-ascii encoding because this is used to include
 * various encoding.
 * **************************************************************************
*/
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include "usb4java.h"

using namespace usb4j;

static bool big_endian;

// get compiler name
#if defined(__GNUC__)
#define COMPILER_NAME "gcc " __VERSION__
#elif defined(_MSC_VER)
static const char* __get_msvc_version(){
	static char buf[256];
	safe_snprintf(buf, sizeof(buf), "Microsoft Visual C++ %d.%d", (_MSC_VER / 100) - 6, (_MSC_VER % 100) / 10);
	return buf;
}
#define COMPILER_NAME __get_msvc_version()
#else
#define COMPILER_NAME "unknown compiler"
#endif

// compile mode
#if defined(__DUMP)
#define COMPILE_MODE " DUMP"
#elif defined(_DEBUG)
#define COMPILE_MODE " DEBUG"
#elif defined(__NOTRACE)
#define COMPILE_MODE " silent"
#else
#define COMPILE_MODE ""
#endif

#define DUMP_FILE "usb4jdump.log"

// ==========================================================================
// Refer Version for Native Library
// ==========================================================================
/*
 * Refer the Interface Version of this library.
 *
 * Class:     org_koiroha_usb_impl_USBServiceImpl
 * Method:    nativeInterfaceVersion
 * Signature: ()J
 */
jlong usb4j::initialize(JNIEnv *env){
	JVM jvm(env);

	// byte order check
	unsigned short test = 0xCAFE;
	big_endian = (((unsigned char*)&test)[0] != 0xFE);

	// report compiler information
	jvm.log(LOG_CONFIG,
		"%s %dbit %s interface library, built on %s%s compiled with %s",
		usb4j::LIBRARY_NAME, sizeof(void*)*8, (big_endian?"BE":"LE"), __DATE__,
		COMPILE_MODE, COMPILER_NAME);

#if defined(__DUMP)
	// warning for dump compile version
	jvm.log(LOG_WARNING, "********");
	jvm.log(LOG_WARNING, "This usb4j JNI library is trace dump version. It needs to special care");
	jvm.log(LOG_WARNING, "that this cause very low performance to communicate devices. If you");
	jvm.log(LOG_WARNING, "don't want this behavior, you may recompile without __DUMP macro.");
	jvm.log(LOG_WARNING, "Please refere the file \"%s\" on current directory.", DUMP_FILE);
	jvm.log(LOG_WARNING, "********");
#endif

	// return version of environment and library
	jint jni_version = env->GetVersion();
	jint lib_version = usb4j::LIBRARY_VERSION & 0xFFFF;
	return ((jlong)jni_version << 32) | lib_version;
}

// ==========================================================================
// Create Instance
// ==========================================================================
/*
 * Create instance of specified class with arguments.
 */
jobject JVM::create(const char *className, const char *sig, ...){
	REFDEBUG("create(%s,%s,...)", className, sig);
	jclass clazz = getClass(className);
	jmethodID method = getMethod(className, "<init>", sig, false);
	va_list list;
	va_start(list, sig);
	jobject obj =env->NewObjectV(clazz, method, list);
	va_end(list);
	return obj;
}

// ==========================================================================
// Raise Exception
// ==========================================================================
/*
 * Raise exception with specified error message (can be NULL).
*/
void JVM::raise(const char *exception, jstring message){

	// refer exception class
	jclass clazz = getClass(exception);

	// if no message
	if(message == NULL){
		log(LOG_FINEST, "%s: ", exception);
		env->ThrowNew(clazz, "");
		return;
	}

	// make message string to UTF-8 binary
	const char *msgutf = env->GetStringUTFChars(message, NULL);
	env->ThrowNew(clazz, msgutf);
	env->ReleaseStringUTFChars(message, msgutf);
	return;
}

// ==========================================================================
// To Unicode
// ==========================================================================
// Convert specified native characters to jstring.
//
jstring JVM::nativeToString(const char *text){
	REFDEBUG("JVM::nativeToString(%s)", text);
	jsize len = (jsize)strlen(text);
	jbyteArray array = env->NewByteArray(len);
	env->SetByteArrayRegion(array, 0, len, (jbyte*)text);
	jstring str = (jstring)create(STRING_CLASS, "([B)V", array);
	REFDEBUG("    --> str=%p", (void*)str);
	return str;
}

// ======================================================================
// Set Instance Field
// ======================================================================
/**
 * Set the value to instance field.
 */
jclass JVM::getClass(const char* className){
	REFDEBUG("getClass(%s)", className);
	REFDEBUG("JNIEnv::FindClass(%s)", className);
	jclass clazz = env->FindClass(className);
	if(clazz == NULL || env->ExceptionCheck()){
		abort("class undefined: %s", className);
	}
	REFDEBUG("  --> class=%p", (void*)clazz);
	return clazz;
}

// ======================================================================
// Set Instance Field
// ======================================================================
/**
 * Set the value to instance field.
 */
jfieldID JVM::getField(const char* className, const char* fieldName, const char* sig, bool stat){
	REFDEBUG("getField(%s,%s,%s,%s)", className, fieldName, sig, stat? "static": "instance");
	jclass clazz = getClass(className);
	jfieldID field = NULL;
	if(stat){
		REFDEBUG("JNIEnv::GetStaticFieldID(%p,%s,%s)", (void*)clazz, fieldName, sig);
		field = env->GetStaticFieldID(clazz, fieldName, sig);
	} else {
		REFDEBUG("JNIEnv::GetFieldID(%p,%s,%s)", (void*)clazz, fieldName, sig);
		field = env->GetFieldID(clazz, fieldName, sig);
	}
	if(field == NULL || env->ExceptionCheck()){
		abort("field undefined: %s#%s", className, fieldName);
	}
	REFDEBUG("  --> field=%p", (void*)field);
	return field;
}

// ======================================================================
// Set Instance Field
// ======================================================================
/**
 * Set the value to instance field.
 */
jmethodID JVM::getMethod(const char* className, const char* methodName, const char* sig, bool stat){
	REFDEBUG("getMethod(%s,%s,%s,%s)", className, methodName, sig, stat? "static": "instance");
	jclass clazz = getClass(className);
	jmethodID method = NULL;
	if(stat){
		REFDEBUG("JNIEnv::GetStaticMethodID(%p,%s,%s)", (void*)clazz, methodName, sig);
		method = env->GetStaticMethodID(clazz, methodName, sig);
	} else {
		REFDEBUG("JNIEnv::GetMethodID(%p,%s,%s)", (void*)clazz, methodName, sig);
		method = env->GetMethodID(clazz, methodName, sig);
	}
	if(method == NULL || env->ExceptionCheck()){
		abort("method undefined: %s#%s%s", className, methodName, sig);
	}
	REFDEBUG("  --> method=%p", (void*)method);
	return method;
}

// ==========================================================================
// Logging from JNI
// ==========================================================================
/*
 * Call utility class to output specified log.
*/
void JVM::log(const char *level, const char* fmt, ...){
	va_list list;
	va_start(list, fmt);
	jstring msg = format(fmt, list);
	log(level, msg);
	va_end(list);
	return;
}

// ======================================================================
// Output Log
// ======================================================================
// Output specified log message to the Logging API of Java.
//
void JVM::log(const char *level, jstring msg){

	// refer outout level
	jobject lv = callStatic(LEVEL_CLASS, "parse", "(L" STRING_CLASS ";)L" LEVEL_CLASS ";", nativeToString(level));
	if(isError()){
		return;
	}

	// refer logging instance
	jstring logName = env->NewStringUTF(LOGGER_NAME);
	jobject logger = callStatic(LOGGER_CLASS, "getLogger", "(L" STRING_CLASS ";)L" LOGGER_CLASS ";", logName);
	if(isError()){
		return;
	}

	call(logger, LOGGER_CLASS, "log", "(L" LEVEL_CLASS ";L" STRING_CLASS ";)V", lv, msg);
	return;
}

// ==========================================================================
// Dump Binary
// ==========================================================================
/*
 * Dump specified binary on current directory.
 */
#if defined(__DUMP)
static const char *__dumpopenmode = "w";
void JVM::dump(const void *binary, size_t length, const char *format, ...){
#if defined(_MSC_VER) && _MSC_VER >= 1500
	FILE* file = NULL;
	fopen_s(&file, DUMP_FILE, __dumpopenmode);
#else
	FILE* file = fopen(DUMP_FILE, __dumpopenmode);
#endif
	__dumpopenmode = "a";

	// output current timestamp
	time_t current;
	struct tm *local;
	time(&current);
#if defined(_MSC_VER) && _MSC_VER >= 1500
	struct tm _local;
	localtime_s(&_local, &current);
	local = &_local;
#else
	local = localtime(&current);
#endif
	fprintf(file, "[%04d/%02d/%02d %02d:%02d:%02d] ",
		1900 + local->tm_year, 1 + local->tm_mon, local->tm_mday,
		local->tm_hour, local->tm_min, local->tm_sec);

	// output message
	va_list list;
	va_start(list, format);
	vfprintf(file, format, list);
	va_end(list);
	fprintf(file, "\n");

	// output binary dump
	const char *ptr = (const char*)binary;
	for(size_t y=0; y<length; y+=16){
		for(size_t x=0; x<16; x++){
			size_t i = y + x;
			if(i >= length){
				break;
			} else if((i % 8) == 0 && (i % 16) != 0){
				fprintf(file, " ");
			}
			fprintf(file, "%02X ", ptr[y + x] & 0xFF);
		}
		fprintf(file, "\n");
	}

	fclose(file);
	return;
}
#endif

// ======================================================================
// Format Message
// ======================================================================
// Format specified argument as String.format() and return Unicode string.
//
jstring JVM::format(const char* format, va_list list){
	char buffer[2 * 1024];
	safe_vsnprintf(buffer, sizeof(buffer), format, list);
	return nativeToString(buffer);
}


// ======================================================================
// Constructor
// ======================================================================
// Set JNIEnv pointer to env. If no environment attached to current thread,
// find JavaVM and attach.
//
JVMCallbackEnv::JVMCallbackEnv(){
	javavm = NULL;
	env = NULL;

	// find jvm attached to current process
	JavaVM* vm;
	jsize nVMs;
	jint ret = JNI_GetCreatedJavaVMs(&vm, 1, &nVMs);
	if(ret != JNI_OK){
		fprintf(stderr, "JNI_GetCreatedJavaVMs(): error=%d\n", (int)ret);
		return;
	}
	if(nVMs == 0){
		fprintf(stderr, "JNI_GetCreatedJavaVMs(): no jvm found in this process\n");
		return;
	}

	// get JNIEnv pointer attached to current thread
	ret = vm->GetEnv((void**)&env, JNI_VERSION_1_4);
	if(ret == JNI_OK){
		env = NULL;
		return;
	}

	// unexpected error occured
	if(ret != JNI_EDETACHED){
		env = NULL;
		fprintf(stderr, "GetEnv(): error=%d\n", (int)ret);
		return;
	}

	// attach
	ret = vm->AttachCurrentThread((void**)&env, NULL);
	if(ret != JNI_OK){
		env = NULL;
		fprintf(stderr, "AttachCurrentThread(): error=%d\n", (int)ret);
		return;
	}
	javavm = vm;
	JVM jvm(env);
	TRACE("attach java vm to current thread");
	return;
}

// ======================================================================
// Destructor
// ======================================================================
// Detach if constructor attach java environment.
//
JVMCallbackEnv::~JVMCallbackEnv(){
	if(javavm != NULL){
		javavm->DetachCurrentThread();
	}
	return;
}


namespace usb4j{

// ======================================================================
// Get JNIEnv
// ======================================================================
/*
 * Get JNIEnv pointer associate to current thread. If no Java VM is
 * attached to thread, find and attach. This is used in event callback.
 */
JNIEnv* get_jnienv(JavaVM** javavm){

	// find jvm attached to current process
	JavaVM* vm = NULL;
	jsize nVMs;
	jint ret = JNI_GetCreatedJavaVMs(&vm, 1, &nVMs);
	if(ret != JNI_OK){
		fprintf(stderr, "JNI_GetCreatedJavaVMs(): error=%d\n", (int)ret);
		return NULL;
	}
	if(nVMs == 0){
		fprintf(stderr, "JNI_GetCreatedJavaVMs(): no jvm found in this process\n");
		return NULL;
	}

	// get JNIEnv pointer attached to current thread
	JNIEnv* env = NULL;
	ret = vm->GetEnv((void**)&env, JNI_VERSION_1_4);
	if(ret == JNI_OK){
		*javavm = NULL;
		return env;
	}

	// unexpected error occured
	if(ret != JNI_EDETACHED){
		fprintf(stderr, "GetEnv(): error=%d\n", (int)ret);
		return NULL;
	}

	// attach
	ret = vm->AttachCurrentThread((void**)&env, NULL);
	if(ret != JNI_OK){
		fprintf(stderr, "AttachCurrentThread(): error=%d\n", (int)ret);
		return NULL;
	}
	JVM jvm(env);
	*javavm = vm;
	TRACE("attach java vm to current thread");
	return env;
}


// ======================================================================
// Convert Byte Order
// ======================================================================
/**
 * Convert byte order to little endian.
 */
unsigned short native_to_le16(unsigned short value){
	if(big_endian){
		value = ((value >> 8) & 0xFF) | ((value << 8) & 0xFF);
	}
	return value;
}


}
