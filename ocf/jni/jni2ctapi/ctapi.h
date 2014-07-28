/*****************************************************************
/
/ File   :   ctapi.h
/ Author :   David Corcoran
/ Date   :   September 2, 1998
/ Purpose:   Defines CT-API functions and returns
/ License:   See file LICENSE
/
******************************************************************/

#ifndef _ctapi_h_
#define _ctapi_h_

#ifdef __cplusplus
extern "C" {
#endif

#define MAX_APDULEN     1040

#ifdef WIN32
#define LINKAGE __stdcall
#define CTAPI_USHORT		unsigned int    // Make sure, that DLL which expect an int rather than a short
                                            // will work on Windows (Gemplus driver bug)
#else
#define LINKAGE
#define CTAPI_USHORT		unsigned short  // Behaviour as specified
#endif


char LINKAGE CT_init (
      CTAPI_USHORT Ctn,                  /* Terminal Number */
      CTAPI_USHORT pn                    /* Port Number */
      );

char LINKAGE CT_close(
      CTAPI_USHORT Ctn                  /* Terminal Number */
      );                 

char LINKAGE CT_data( 
       CTAPI_USHORT ctn,                /* Terminal Number */
       unsigned char  *dad,               /* Destination */
       unsigned char  *sad,               /* Source */
       CTAPI_USHORT lc,                 /* Length of command */
       unsigned char  *cmd,               /* Command/Data Buffer */
       unsigned short *lr,                /* Length of Response */
       unsigned char  *rsp                /* Response */
       );


  /* CTAPI - response codes  */

#define OK               0               /* Success */
#define ERR_INVALID     -1               /* Invalid Data */
#define ERR_CT          -8               /* CT Error */
#define ERR_TRANS       -10              /* Transmission Error */
#define ERR_MEMORY      -11              /* Memory Allocate Error */
#define ERR_HTSI        -128             /* HTSI Error */

#define PORT_COM1          0             /* COM 1 */
#define PORT_COM2          1             /* COM 2 */
#define PORT_COM3          2             /* COM 3 */
#define PORT_COM4          3             /* COM 4 */
#define PORT_Printer       4             /* Printer Port (MAC) */
#define PORT_Modem         5             /* Modem Port (MAC)   */
#define PORT_LPT1          6             /* LPT 1 */
#define PORT_LPT2          7             /* LPT 2 */


  /* CTAPI / CTBCS SW1/2 states   */

#define SMARTCARD_SUCCESS           0x9000
#define SMARTCARD_SUCCESS_ASYNC     0X9001
#define NOT_SUCCESSFUL              0x6400

#define W_NO_CARD_PRESENTED         0x6200
#define W_ICC_ALREADY_PRESENT       0x6201

#define DATA_CORRUPTED              0x6281
#define NO_CARD_PRESENT             0x64A1
#define CARD_NOT_ACTIVATED          0x64A2
#define WRONG_LENGTH                0x6700
#define COMMAND_NOT_ALLOWED         0x6900
#define VERIFICATION_METHOD_BLOCK   0x6983
#define VERIFICATION_UNSUCCESSFUL   0x63C0
#define WRONG_PARAMETERS_P1_P2      0x6A00
#define FILE_NOT_FOUND              0x6A82
#define OUT_OF_RANGE                0x6B00
#define WRONG_LENGTH_LE             0x6C00
#define WRONG_INSTRUCTION           0x6D00
#define CLASS_NOT_SUPPORTED         0x6E00
#define COMMUNICATION_NOT_POSSIBLE  0x6F00

#ifndef HIGH
#define HIGH(x)   ((x >> 8))
#define LOW(x)    ((x & 0xff))
#endif


/* #define DAD */
#define CT              1
#define HOST            2
#ifdef __cplusplus
}
#endif

#endif



