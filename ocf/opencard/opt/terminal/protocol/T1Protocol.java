/*
 * Copyright © 1997 - 1999 IBM Corporation.
 *
 * Redistribution and use in source (source code) and binary (object code)
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributed source code must retain the above copyright notice, this
 * list of conditions and the disclaimer below.
 * 2. Redistributed object code must reproduce the above copyright notice,
 * this list of conditions and the disclaimer below in the documentation
 * and/or other materials provided with the distribution.
 * 3. The name of IBM may not be used to endorse or promote products derived
 * from this software or in any other form without specific prior written
 * permission from IBM.
 * 4. Redistribution of any modified code must be labeled "Code derived from
 * the original OpenCard Framework".
 *
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. IBM SHALL NOT BE
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE.  ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IBM DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS
 * SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL
 * BE UNINTERRUPTED OR ERROR-FREE.  IN NO EVENT, UNLESS REQUIRED BY APPLICABLE
 * LAW, SHALL IBM BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  ALSO, IBM IS UNDER NO OBLIGATION
 * TO MAINTAIN, CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS
 * SOFTWARE.
 */

package opencard.opt.terminal.protocol;

import opencard.core.util.Tracer;

/** <tt>T1Protocol</tt> is a small subset of the T1 block protocol.
 *
 *  NOTE: T1Protocol IS NOT IN FINAL STATE!!!!!!!!!!!!!!!
 *
 *  - chaining is not supported
 *  - EDC-byte calculation only with LDR (XORed), CRC is not provided.
 *
 *  see     ISO7816_3P9
 *
 *  @author  Stephan Breideneich (sbreiden@de.ibm.com)
 *  @version $Id: T1Protocol.java,v 1.3 1999/11/03 12:37:19 damke Exp $
 */
public abstract class T1Protocol {

  private static Tracer ctracer = new Tracer(T1Protocol.class);

  /**
   * Constructor setting host- and remoteaddress to 0
   */
  T1Protocol() {
    setBlockWaitingTime(0);
  }

  /**
   * Constructor with specification of the host- and remoteaddress
   */
  public T1Protocol(int stdHostAddress, int stdRemoteAddress, int timeout) {
    hostAddress = stdHostAddress;
    remoteAddress = stdRemoteAddress;
    setBlockWaitingTime(timeout);
  }

  /** <tt>open</tt>
   * should be called after creating a new object of this class.
   *
   * <tt>open</tt> sends S-BLOCK(RESYNCH REQUEST) and waits for S-BLOCK(RESYNCH RESPONSE).
   *
   * @exception T1Exception
   *            thrown when error occured.
   */
  public void open() throws T1Exception {
    initProtocol();
  }

  /** <tt>close</tt>
   *  should be called to deinitialize the object
   */
  public void close() {
  }

  /** <tt>getBlockWaitingTime</tt> returns the currently used block waiting time in milliseconds.
   */
  public int getBlockWaitingTime() {
    return blockWaitingTime;
  }

  /** <tt>setBlockWaitingTime</tt> sets the block waiting time in milliseconds.
   */
  public void setBlockWaitingTime(int timeout) {
    blockWaitingTime = timeout;
  }

  /** <tt>getHostAddress</tt>
   */
  public int getHostAddress() {
    return hostAddress;
  }

  /** <tt>getRemoteAddress</tt>
   */
  public int getRemoteAddress() {
    return remoteAddress;
  }

  /** <tt>getSendSequenceCounter</tt>
   */
  public int getSendSequenceCounter() {
    return sBlockCounter;
  }

  /** <tt>setSendSequenceCounter</tt>
   */
  public void setSendSequenceCounter(int val) {
    sBlockCounter = val;
  }

  /** <tt>incSendSequenceCounter</tt>
   */
  public void incSendSequenceCounter() {
    sBlockCounter++;
  }

  /** <tt>getRecvSequenceCounter</tt>
   */
  public int getRecvSequenceCounter() {
    return rBlockCounter;
  }

  /** <tt>setRecvSequenceCounter</tt>
   */
  public void setRecvSequenceCounter(int val) {
    rBlockCounter = val;
  }

  /** <tt>incRecvSequenceCounter</tt>
   */
  public void incRecvSequenceCounter() {
    rBlockCounter++;
  }


  /** <tt>transmit</tt>
   * sends the data to the terminal and waits for result until timeout is reached.
   * transmit handles a subset of T1 error-recognition and -recovering.
   * <tt>transmit</tt> uses the standard host- and remote-addresses (configured by constructor)
   *
   * @param     sendData
   *            data for the terminal
   * @exception T1IOException
   *            thrown when IO error occurs (send- or receivemethods)
   * @exception T1TimeoutException
   *            thrown when timeout limit reached for transmitting data
   */
  public synchronized byte[] transmit(byte[] sendData)
    throws T1IOException, T1TimeoutException, T1Exception {

    return transmit(getRemoteAddress(), sendData);
  }


  /** <tt>transmit</tt>
   * sends the data to the terminal and waits for result until timeout is reached.
   * transmit handles a subset of T1 error-recognition and -recovering.
   * <tt>transmit</tt> uses the standard host- and remote-addresses (configured by constructor)
   *
   * @param     remoteAddress
   * @param     sendData
   *            data for the terminal
   * @exception T1IOException
   *            thrown when IO error occurs (send- or receivemethods)
   * @exception T1TimeoutException
   *            thrown when timeout limit reached for transmitting data
   */
  public synchronized byte[] transmit(int remoteAddress, byte[] sendData)
    throws T1IOException, T1TimeoutException, T1Exception {

    return transmit(getHostAddress(), remoteAddress, sendData);
  }


  /** <tt>transmit</tt>
   * sends the data to the terminal and waits for result until timeout is reached.
   * transmit handles a subset of T1 error-recognition and -recovering.
   *
   * @param     hostAddress
   * @param     remoteAddress
   *            use another remote-address as previously given by constructor.
   * @param     sendData
   *            data for the terminal
   * @exception T1IOException
   *            thrown when IO error occurs (send- or receivemethods)
   * @exception T1TimeoutException
   *            thrown when timeout limit reached for transmitting data
   */
  public synchronized byte[] transmit(int hostAddress, int remoteAddress, byte[] sendData)
    throws T1IOException, T1TimeoutException, T1Exception {

    // bytes to send to the receiver
    int bytesLeft = sendData.length;

    // length of the current data field to send
    int dataLen = 0;

    byte[] data = null;

    T1Block recvBlock = null;

    while (bytesLeft > 0) {

      boolean chaining;

      // copy subpackage into new buffer - make decision about chaining
      if (bytesLeft > ifs) {
        //dataLen = ifs;
        //chaining = true;
        throw new T1IOException("block too long - chaining is not supported!");
      } else {
        dataLen = bytesLeft;
        chaining = false;
      }

      data = new byte[dataLen];
      System.arraycopy(sendData, sendData.length - bytesLeft, data, 0, dataLen);

      // subtract size of sent data
      bytesLeft -= dataLen;

      // pack sendData into T1 I-block
      T1Block sendBlock = T1BlockFactory.createIBlock(hostAddress,
                                                      remoteAddress,
                                                      T1Block.EDC_LDR,          // use XOR-algorithm
                                                      getSendSequenceCounter(),
                                                      chaining, // chaining used?
                                                      data);

      // set current I-Block
      blockSequence[getSendSequenceCounter() % 2] = sendBlock;

      // exchange the data
      recvBlock = internalTransmit(5, sendBlock);

      if (sendBlock.getBlockType() == T1Block.I_BLOCK)
        incSendSequenceCounter();



    } // while (bytesLeft > 0)

    // return the application data byte array
    return recvBlock.getDATA();
  }



  protected boolean isBlockComplete(byte[] rawBytes, int len) {
    boolean complete = false;

    if (rawBytes != null)
      if ((len >= 4) && (rawBytes.length >= 4))
      {
        int hdrlen = rawBytes[2]&0xFF;
        if (hdrlen + 4 == len) {
          complete = true;
        }
      }

    return complete;
  }

  /*******************************************
   *   P R I V A T E  &  P R O T E C T E D   *
   *******************************************/

  private int hostAddress = 0;
  private int remoteAddress = 0;

  private int blockWaitingTime = 0;

  private int sBlockCounter = 0;
  private int rBlockCounter = 0;

  private int ifs = 32;

  private T1Block[] blockSequence = new T1Block[2];

  /** <tt>initProtocol</tt>
   * initialize protocol.
   * <tt>initProtocol</tt> sends S-block (RESYNCH REQUEST) to the receiver and
   * waits for S-block (RESYNCH RESPONSE).
   * if successful, <tt>initProtocol</tt> resets the send- and receive
   * sequence counter. if not, an exception is thrown after 3 attempts.
   * see ISO7816-3 - 9.6.2.3.2 rule 6.4
   *
   * @exception T1Exeption
   *            thrown when error occurred.
   */
  private void initProtocol() throws T1Exception {

    // create S-block with RESYNCH REQUEST
    T1Block resyncRequest = T1BlockFactory.createSBlock(hostAddress,
                                                        remoteAddress,
                                                        T1Block.EDC_LDR,
                                                        T1Block.S_RESYNCH_REQUEST,
                                                        null);

    // create S-block with INFORMATION FIELD SIZE REQUEST (limited to 0x7F bytes per block)
    T1Block ifsRequest = T1BlockFactory.createSBlock(hostAddress,
                                                     remoteAddress,
                                                     T1Block.EDC_LDR,
                                                     T1Block.S_IFS_REQUEST,
                                                     new byte[] { (byte)ifs });

    // three attempts possible
    for (int i=0; i<3; i++) {
      // transmit S-block - answer must be S-block with RESYNCH_RESPONSE
      T1Block result = null;
      try {
        ctracer.debug("initProtocol", "Send S-BLOCK to reader for init-request.");
        result = internalTransmit(5, resyncRequest);

        if (result.getBlockType() == T1Block.S_BLOCK)
          if (result.getControlBits() == T1Block.S_RESYNCH_RESPONSE) {
            sBlockCounter = 0;
            rBlockCounter = 0;

            // send InformationFieldSize-request to terminal (new field size 0x7F bytes)
            result = internalTransmit(5, ifsRequest);

            if (result.getBlockType() == T1Block.S_BLOCK)
              if (result.getControlBits() == T1Block.S_IFS_RESPONSE) {
                if (result.getLEN() == 1) {

                  byte b = result.getDATA()[0];
                  ifs = (int)(b & 0x7F);
                  if ((b & 0x80) == 0x80)
                    ifs += 0x80;

                  ctracer.debug("initProtocol", "IFS set to " + ifs + " + bytes length");

                  return;
                } else
                  ctracer.error("initProtocol", "IFS response error");
              }
          }
      } catch(Exception e) {
        ctracer.critical("initProtocol", "Reader initialization failed.");
      }
    }

    throw new T1Exception("no correct answer on resync request - protocol init failed!");
  }

  /** <tt>internalTransmit</tt>
   *
   * exchanges blocks with T1 protocol handling.
   *
   * @param     retryCount
   *            number of retries left for transmitting data
   *            if retryCount = 0 reached, transfer failed
   *
   * @param     dataBlock
   *            application data block.
   * @exception T1Execption
   *            thrown when error occurred.
   */
  protected T1Block internalTransmit(int retryCount, T1Block dataBlock) throws T1Exception {
    T1Block recvBlock = null;

    // exchange data with receiver

    // for I-blocks increase the send-sequence-counter
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    if (retryCount == 0)
      throw new T1Exception("to many retries - transfer aborted");


    try {

      // exchange data with terminal layer
      recvBlock = exchangeData(dataBlock);

      if (recvBlock == null)
        throw new T1BlockEDCErrorException();

      // check the type of the recvBlock
      switch(recvBlock.getBlockType()) {

        // I-block returned - data exchange was successful
        case T1Block.I_BLOCK:

          // increase the receiver sequence count on every correctly received I-block
          incRecvSequenceCounter();

          // return the received I-block
          return recvBlock;

        // receiver detected error
        case T1Block.R_BLOCK:
          // resend the last block
          recvBlock = internalTransmit(retryCount - 1, blockSequence[recvBlock.getRequestedSequenceNumber() % 2]);
          break;

        case T1Block.S_BLOCK:

          // WTX request from ICC
          if (recvBlock.getControlBits() == T1Block.S_WTX_REQUEST) {
            // create WTX-responseblock
            T1Block wtxResponse = T1BlockFactory.createSBlock(dataBlock.getSourceAddress(),
                                                              dataBlock.getDestinationAddress(),
                                                              T1Block.EDC_LDR,
                                                              T1Block.S_WTX_RESPONSE,
                                                              recvBlock.getDATA());
            return internalTransmit(retryCount - 1, wtxResponse);
          }

          // check for
          // - last sent block == S_BLOCK?
          // - received S_Block == RESYNCH RESPONSE?
          if (dataBlock.getBlockType() == T1Block.S_BLOCK)
            if (dataBlock.getControlBits() == T1Block.S_RESYNCH_REQUEST)
              if (recvBlock.getControlBits() == T1Block.S_RESYNCH_RESPONSE)
                return recvBlock;

      }
    } catch(T1UnknownBlockException ube) {
    } catch(T1BlockEDCErrorException beee) {
      T1Block resendRequest = T1BlockFactory.createRBlock(hostAddress,
                                                          remoteAddress,
                                                          T1Block.EDC_LDR,
                                                          getRecvSequenceCounter(),
                                                          T1Block.ERROR_EDC);
      incSendSequenceCounter();
      blockSequence[getSendSequenceCounter() % 2] = resendRequest;
      recvBlock = internalTransmit(retryCount - 1, resendRequest);
    }

    // no usable result reached
    return recvBlock;
  }

  /** <tt>exchangeData</tt>
   *
   * responsible for the real data-transfer.
   *
   * @param     sendBlock
   *            the T1-block with the send-data inside.
   * @exception T1TimeoutException
   *            thrown when time is elapsed receiving a T1-block
   * @exception T1BlockLengthException
   *            thrown when difference detected between calculated and received block length
   * @exception T1UnknownBlockException
   *            thrown when blocktype could not be recognized
   * @exception T1BlockEDCErrorException
   *            thrown when error detection code differs from the calculated value
   */
  protected abstract T1Block exchangeData(T1Block sendBlock)
                             throws T1IOException,
                                    T1TimeoutException,
                                    T1BlockLengthException,
                                    T1UnknownBlockException,
                                    T1BlockEDCErrorException;

}
