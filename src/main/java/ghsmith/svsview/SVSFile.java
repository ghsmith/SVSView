/**
 * MIT License
 *
 * Copyright (c) 2022 Geoffrey H. Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ghsmith.svsview;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author geoffrey.smith@emory.edu
 */
public class SVSFile {

    static final Logger logger = Logger.getLogger(SVSFile.class.getName());

    public String name = null;
    
    public static final long FIRST_TIFF_DIRECTORY_OFFSET = 0x0000000000000008L;
    public static final long NO_MORE_TIFF_DIRECTORIES_OFFSET = 0x0000000000000000L;

    public int BUFFER_SIZE = 5000000;

    public FileChannel fc = null;
    public long bufferOffset = -1;
    public ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
    byte[] buffer = null;
    
    public long length = -1;
    public long firstHeaderOffset = -1;
    public long osFirstHeaderOffset = -1;
    public int longLength = 0x00000008;
    
    public List<TIFFDir> tiffDirList = new ArrayList<>();

    Map<String, Tile> tileMap = new HashMap<>();

    public void resetBuffer(int size) {
        BUFFER_SIZE = size;
        bb = ByteBuffer.allocate(BUFFER_SIZE);
        bufferOffset = -1;
    }
    
    public int getLongLength() {
        return longLength;
    }
    
    public SVSFile(FileChannel svsFile) throws IOException {
        fc = svsFile;
        // is this really a GT450 SVS file?
        {
            if(svsFile.size() < 8) { throw new IOException("not a GT450 SVS file (<8 bytes)"); }
            byte[] gt450TiffHeader = new byte[] { 0x49, 0x49, 0x2b, 0x00, 0x08, 0x00, 0x00, 0x00 };
            byte[] thisTiffHeader = getBytes(0, 8);
            if(!Arrays.equals(gt450TiffHeader, thisTiffHeader)) { throw new IOException("not a GT450 SVS file (bad header)"); }
        }
        long offset = getBytesAsLong(FIRST_TIFF_DIRECTORY_OFFSET);
        int x = 0;
        while(offset != NO_MORE_TIFF_DIRECTORIES_OFFSET) {
            logger.log(Level.INFO, String.format("========== parsing TIFF directory #%d tags (%d) ==========", tiffDirList.size(), offset));
            TIFFDir tiffDir = new TIFFDir(String.valueOf(x), this, offset);
            tiffDirList.add(tiffDir);
            logger.log(Level.INFO, String.format("width=%d height=%d tileWidth=%d tileHeight=%d mpp=%4.2f", tiffDir.width, tiffDir.height, tiffDir.tileWidth, tiffDir.tileHeight, tiffDir.mpp));
            offset = tiffDir.tagNextDirOffsetInSvs;
            x++;
        }
    }
       
    public void setBuffer(long indexStart, long indexEnd) throws IOException {
        if(indexEnd - indexStart > BUFFER_SIZE) {
            throw new IOException("request exceeds BUFFER_SIZE");
        }
        if(
            bufferOffset == -1
            ||
            !(
                indexStart >= bufferOffset
                && indexStart < (bufferOffset + BUFFER_SIZE)
                && indexEnd >= bufferOffset
                && indexEnd < (bufferOffset + BUFFER_SIZE)
             )
        ) {
            bufferOffset = indexStart;
            ((Buffer)bb).clear();
            int read = 0;
            int readAlready = 0;
            do {
                readAlready += read;
                read = fc.read(bb, indexStart + readAlready);
                logger.log(Level.SEVERE, String.format("read %s bytes from file channel", read));
            } while(read > 0 && bb.hasRemaining());
            buffer = bb.array();
        }
    }
        
    public byte getByte(long index) throws IOException {
        setBuffer(index, index + 1L);
        return buffer[(int)(index - bufferOffset)];
    }

    public byte[] getBytes(long indexStart, long indexEnd) throws IOException {
        setBuffer(indexStart, indexEnd);
        return Arrays.copyOfRange(buffer, (int)(indexStart - bufferOffset), (int)(indexEnd - bufferOffset));
    }
    
    public long getBytesAsLong(long index) throws IOException {
        setBuffer(index, index + 7L);
        byte[] bytes = Arrays.copyOfRange(buffer, (int)(index - bufferOffset), (int)(index + 7L - bufferOffset));
        return
              ((((long)bytes[0]) & 0x00000000000000ffL) <<  0)
            | ((((long)bytes[1]) & 0x00000000000000ffL) <<  8)
            | ((((long)bytes[2]) & 0x00000000000000ffL) << 16)
            | ((((long)bytes[3]) & 0x00000000000000ffL) << 24)
            | ((((long)bytes[4]) & 0x00000000000000ffL) << 32)
            | ((((long)bytes[5]) & 0x00000000000000ffL) << 40)
            | ((((long)bytes[6]) & 0x00000000000000ffL) << 48);
    }

    public int getBytesAsShort(long index) throws IOException {
        setBuffer(index, index + 2L);
        byte[] bytes = Arrays.copyOfRange(buffer, (int)(index - bufferOffset), (int)(index + 2L - bufferOffset));
        return
              ((((int)bytes[0]) & 0x000000ff) <<  0)
            | ((((int)bytes[1]) & 0x000000ff) <<  8);
    }

}
