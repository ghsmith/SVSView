package ghsmith.svsview;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.READ;
import javax.imageio.ImageIO;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
public class SvsTileExtractor {

    public static void main(String[] args) throws IOException {

//        try (FileChannel fc = FileChannel.open(Paths.get("c:\\stuff\\3_17_S25-05277-A1-5_145124_L5.svs"), READ)) {
//          try(FileChannel fc = FileChannel.open(Paths.get("\\\\euh\\ehc\\apps\\prd-sectraPathology\\data\\Scanners.not_for_pacs\\GT450_EUH_SS12284\\2025-04-15\\S25-09346-A1-4.svs"), READ)) {
            Path path = Paths.get("\\\\euh\\ehc\\apps\\prd-sectraPathology\\data\\Scanners.not_for_pacs\\GT450DX_EUH_SS35007\\2025-02-25\\3_2_S25-05981-A1-4_074039.svs");
            SVSFile svsFile = new SVSFile(path);//new SVSFile(fc);
//            svsFile.resetBuffer(1000000);
            for(TIFFDir td : svsFile.tiffDirList) {
                System.out.println(td.id + ": " + td.mpp + "/" + td.widthInTiles + "/" + td.heightInTiles);
            }
            for(String x : svsFile.tileMap.keySet()) {
                if(x.startsWith("3.")) {
                    Tile t = svsFile.tileMap.get(x);
                    System.out.println(x + ": " + t.offsetInSVS + "/" + t.length + "/" + t.indexInSVS + "/" + t.indexInTiffDir);
                }
            }
            //for(long x : svsFile.tiffDirList.get(3).tagTileOffsetsInSvs) {
            //    System.out.println(x);
            //}
            //for(long x : svsFile.tiffDirList.get(3).tagTileLengths) {
            //    System.out.println(x);
            //}

            int x3 = 6;
            int y3 = 3;
            
            BufferedImage bi3down = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    Tile tile = svsFile.tileMap.get(String.format("%s.%s.%s", 3, (x3 * 2) + x, (y3 * 2) + y));
                    Image image = ImageIO.read(new ByteArrayInputStream(svsFile.getBytes(tile.offsetInSVS, tile.offsetInSVS + tile.length))).getScaledInstance(128, 128, Image.SCALE_DEFAULT);
                    bi3down.getGraphics().drawImage(image, x * 128, y * 128, null);
                }
            }
            ImageIO.write(bi3down, "jpg", new File("c:\\stuff\\t3_down.jpg"));
            
            Tile t3 = svsFile.tileMap.get(String.format("%s.%s.%s", 3, x3 * 2, y3 * 2));
            System.out.println(t3.offsetInSVS + "/" + t3.length);
            Files.write(Paths.get("c:\\stuff\\t3.jpg"), svsFile.getBytes(t3.offsetInSVS, t3.offsetInSVS + t3.length));

            BufferedImage bi2down = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    Tile tile = svsFile.tileMap.get(String.format("%s.%s.%s", 2, (x3 * 8) + x, (y3 * 8) + y));
                    Image image = ImageIO.read(new ByteArrayInputStream(svsFile.getBytes(tile.offsetInSVS, tile.offsetInSVS + tile.length))).getScaledInstance(128, 128, Image.SCALE_DEFAULT);
                    bi2down.getGraphics().drawImage(image, x * 128, y * 128, null);
                }
            }
            ImageIO.write(bi2down, "jpg", new File("c:\\stuff\\t2_down.jpg"));
            
            Tile t2 = svsFile.tileMap.get(String.format("%s.%s.%s", 2, x3 * 8, y3 * 8));
            Files.write(Paths.get("c:\\stuff\\t2.jpg"), svsFile.getBytes(t2.offsetInSVS, t2.offsetInSVS + t2.length));
            
            BufferedImage bi0down = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    Tile tile = svsFile.tileMap.get(String.format("%s.%s.%s", 0, (x3 * 32) + x, (y3 * 32) + y));
                    Image image = ImageIO.read(new ByteArrayInputStream(svsFile.getBytes(tile.offsetInSVS, tile.offsetInSVS + tile.length))).getScaledInstance(128, 128, Image.SCALE_DEFAULT);
                    bi0down.getGraphics().drawImage(image, x * 128, y * 128, null);
                }
            }
            ImageIO.write(bi0down, "jpg", new File("c:\\stuff\\t0_down.jpg"));
            
            Tile t0 = svsFile.tileMap.get(String.format("%s.%s.%s", 0, x3 * 32, y3 * 32));
            System.out.println(t0.offsetInSVS + "/" + t0.length);
            
        }
        
    //}
}
