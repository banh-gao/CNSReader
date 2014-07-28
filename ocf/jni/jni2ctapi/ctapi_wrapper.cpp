/*
 *  ---------
 * |.**> <**.|  CardContact
 * |*       *|  Software & System Consulting
 * |*       *|  Minden, Germany
 * |�**> <**�|  Copyright (c) 2000. All rights reserved
 *  --------- 
 *
 * See file LICENSE for details on licensing
 *
 * Abstract :       Implementation of a CTAPI Interface for Java.
 *
 * Author :         Frank Thater (FTH)
 *
 * Last modified:   06/04/2004
 *
 *****************************************************************************/

#ifdef DEBUG
#include <stdio.h>
#endif

#include <jni.h>
#include "ctapi_wrapper.h"
#include "ctapi.h"

#ifdef WIN32
#include <windows.h>
// #pragma off(unreferenced)
#else
#include <dlfcn.h>
#endif

#include <malloc.h>
#include <string.h>


/* ;;;;;; Define function definitions to avoid C++ error */


typedef signed char (LINKAGE *CT_INIT_t) (
      CTAPI_USHORT Ctn,                   /* Terminal Number */
      CTAPI_USHORT pn                     /* Port Number */
      );

typedef signed char (LINKAGE *CT_CLOSE_t) (
      CTAPI_USHORT Ctn                    /* Terminal Number */
      );                 

typedef signed char (LINKAGE *CT_DATA_t) (
       CTAPI_USHORT ctn,                  /* Terminal Number */
       unsigned char  *dad,               /* Destination */
       unsigned char  *sad,               /* Source */
       CTAPI_USHORT lc,                   /* Length of command */
       unsigned char  *cmd,               /* Command/Data Buffer */
       unsigned short *lr,                /* Length of Response */
       unsigned char  *rsp                /* Response */
       );

/* ;;;;;; Here come the local references to the function handles   */


/* ;;;;;; setReader

   - native function called by cardterminal_api constructor 
   to load specified shared lib containing the CTAPI-interface
 
   - sets global handle references
*/

JNIEXPORT void JNICALL Java_de_cardcontact_jni2ctapi_cardterminal_1api_setReader
  (JNIEnv *env, jobject obj, jstring name)
{

#ifdef WIN32
  HMODULE mod;
  HINSTANCE handle;
  LPCTSTR msg = env->GetStringUTFChars(name,0);
#else
  void *handle;
  const char* msg=env->GetStringUTFChars(name,0);
  char *error;
#endif


#ifdef DEBUG
  printf("Using Libname %s\n", msg);
#endif  


  // Get the class of the object
  jclass cls = env->GetObjectClass(obj);
  jfieldID fieldID;
 
  CT_INIT_t CT_INIT;
  CT_CLOSE_t CT_CLOSE;
  CT_DATA_t CT_DATA;

#ifdef WIN32
  /* for support of WIN32 DLLs */

if((handle = LoadLibrary(msg)) == NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, "Unable to find DLL containing CTAPI information");
    
  }

  mod = GetModuleHandle(msg);      

  CT_INIT = (CT_INIT_t) GetProcAddress(mod, "CT_init");
  if(CT_INIT == NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, "Unable to find CT_init reference");
    
  }

  CT_CLOSE = (CT_CLOSE_t) GetProcAddress(mod, "CT_close");
  if(CT_CLOSE== NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, "Unable to find CT_close reference");
  }
  
  CT_DATA = (CT_DATA_t) GetProcAddress(mod, "CT_data");
  if(CT_DATA == NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, "Unable to find CT_data reference");
    
  }  

#else
  /* assume running under the Linux OS */

  if((handle = dlopen(msg, RTLD_NOW | RTLD_GLOBAL)) == NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, dlerror());
    
  }

  CT_INIT = (CT_INIT_t) dlsym(handle, "CT_init");
  if((error = dlerror()) != NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, error);
    
  }

  CT_CLOSE = (CT_CLOSE_t) dlsym(handle, "CT_close");
  if((error = dlerror()) != NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, error);
    
  }
  
  CT_DATA = (CT_DATA_t) dlsym(handle, "CT_data");
  if((error = dlerror()) != NULL) {
    
    jclass newExcpClass = env->FindClass("java/lang/UnsatisfiedLinkError");
    
    if (newExcpClass == 0) { /* Unable to find the new exception class, give up. */
      return;
    }
    env->ThrowNew(newExcpClass, error);
    
  }  

#endif

  fieldID = env->GetFieldID(cls, "ctInitPointer", "J");
  env->SetLongField(obj, fieldID, (jlong) CT_INIT);

  fieldID = env->GetFieldID(cls, "ctClosePointer", "J");
  env->SetLongField(obj, fieldID, (jlong) CT_CLOSE); 
  
  fieldID = env->GetFieldID(cls, "ctDataPointer", "J");
  env->SetLongField(obj, fieldID, (jlong) CT_DATA);
  
  env->ReleaseStringUTFChars(name, msg);

}


JNIEXPORT jint JNICALL Java_de_cardcontact_jni2ctapi_cardterminal_1api_CT_1Init
  (JNIEnv *env, jobject obj, jchar ctn, jchar pn)
{
  int rc;

  // Get the class of the object
  jclass cls = env->GetObjectClass(obj);
  jfieldID fieldID;
  CT_INIT_t pCtInit;

  fieldID = env->GetFieldID(cls, "ctInitPointer", "J");

  pCtInit = (CT_INIT_t) env->GetLongField(obj, fieldID);
  
#ifdef DEBUG
  printf("Java CT_init(%d, %d)\n", ctn, pn);
  printf("Pointer CT_init = %p\n", pCtInit);
#endif

  rc = (*pCtInit)((CTAPI_USHORT) ctn,(CTAPI_USHORT) pn);

  return rc;

}

JNIEXPORT jint JNICALL Java_de_cardcontact_jni2ctapi_cardterminal_1api_CT_1Close
  (JNIEnv *env, jobject obj, jchar ctn)
{
  int rc;

  // Get the class of the object
  jclass cls = env->GetObjectClass(obj);
  jfieldID fieldID;
  CT_CLOSE_t pCtClose;

  fieldID = env->GetFieldID(cls, "ctClosePointer", "J");

  pCtClose = (CT_CLOSE_t) env->GetLongField(obj, fieldID);
  
#ifdef DEBUG
  printf("Java CT_close(%d)\n", ctn);
  printf("Pointer CT_close = %p\n", pCtClose);
#endif
  
  rc = (*pCtClose)((CTAPI_USHORT) ctn);

  return rc;

}


JNIEXPORT jint JNICALL Java_de_cardcontact_jni2ctapi_cardterminal_1api_CT_1Data
  (JNIEnv *env, jobject obj, jchar ctn, jbyte dad, jbyte sad, jint lc, jbyteArray jcmd, jchar lr, jbyteArray jrsp)
{
  int rc,i;

  // Get the class of the object
  jclass cls = env->GetObjectClass(obj);
  jfieldID fieldID;
  CT_DATA_t pCtData;

  fieldID = env->GetFieldID(cls, "ctDataPointer", "J");

  pCtData = (CT_DATA_t) env->GetLongField(obj, fieldID);
  
#ifdef DEBUG
  printf("Java CT_data()\n");
  printf("Pointer CT_data = %p\n", pCtData);
#endif

  jbyte *cmd=env->GetByteArrayElements(jcmd,0);
  jbyte *rsp=env->GetByteArrayElements(jrsp,0);
  
  unsigned char lsad = (unsigned char) sad;
  unsigned char ldad = (unsigned char) dad;
        
  unsigned short int lenr = lr;

  rc = (*pCtData)((CTAPI_USHORT)ctn,(unsigned char *)&ldad,(unsigned char *)&lsad,(CTAPI_USHORT) lc,(unsigned char *)cmd,&lenr,(unsigned char*)rsp);

  env->ReleaseByteArrayElements(jcmd,cmd,0);
  env->ReleaseByteArrayElements(jrsp,rsp,0);

  if(rc < 0) {
    return rc;
  }
  return lenr;
}
