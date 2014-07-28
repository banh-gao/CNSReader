/*
 *  ---------
 * |.**> <**.|  CardContact
 * |*       *|  Software & System Consulting
 * |*       *|  Minden, Germany
 * |´**> <**´|  Copyright (c) 2000. All rights reserved
 *  --------- 
 *
 * See file LICENSE for details on licensing
 *
 * Abstract :       Provide a proxy for Windows DLL
 *
 * Author :         Andreas Schwier (ASC)
 *
 * Last modified:   07/14/2000
 *
 *****************************************************************************/

#include <stdio.h>
#include <windows.h>


BOOL APIENTRY LibMain( HANDLE hinstDLL,
                       DWORD  fdwReason,
                       LPVOID lpvReserved )
{
    return 1;
}
  

