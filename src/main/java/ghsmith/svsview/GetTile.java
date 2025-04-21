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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
@WebServlet(name = "GetTile", urlPatterns = {"/GetTile"})
public class GetTile extends HttpServlet {

    static BufferedImage noTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        int index = Integer.parseInt(request.getParameter("index"));
        int x = Integer.parseInt(request.getParameter("x"));
        int y = Integer.parseInt(request.getParameter("y"));
        int level = Integer.parseInt(request.getParameter("level"));
        String name = request.getParameter("name");

        SVSFile svsFile = ((SVSFile[])request.getSession().getAttribute("svsFileCache"))[index];
        
        int tiffDir = -1;
        boolean downSample = false;

        switch(level) {
            case 18: tiffDir = 0;                    break;
            case 17: tiffDir = 0; downSample = true; break;
            case 16: tiffDir = 2;                    break;
            case 15: tiffDir = 2; downSample = true; break;
            case 14: tiffDir = 3;                    break;
            case 13: tiffDir = 3; downSample = true; break;
            case 12: tiffDir = 4;                    break;
        }

        response.setContentType("image/jpeg");

        synchronized(svsFile) {
            try(OutputStream out = response.getOutputStream()) {
                if(!name.equals(svsFile.name)) {
                    ImageIO.write(noTile, "jpg", out);
                }
                else {
                    if(!downSample) {
                        Tile tile = svsFile.tileMap.get(String.format("%s.%s.%s", tiffDir, x, y));
                        if(tile != null) {
                            out.write(svsFile.getBytes(tile.offsetInSVS, tile.offsetInSVS + tile.length));
                        }
                        else {
                            ImageIO.write(noTile, "jpg", out);
                        }
                    }
                    else {
                        BufferedImage biDown = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
                        for(int xd = 0; xd < 2; xd++) {
                            for(int yd = 0; yd < 2; yd++) {
                                Tile tile = svsFile.tileMap.get(String.format("%s.%s.%s", tiffDir, (x * 2) + xd, (y * 2) + yd));
                                Image image;
                                if(tile != null) {
                                    image = ImageIO.read(new ByteArrayInputStream(svsFile.getBytes(tile.offsetInSVS, tile.offsetInSVS + tile.length))).getScaledInstance(128, 128, Image.SCALE_DEFAULT);
                                }
                                else {
                                    image = noTile;
                                }
                                biDown.getGraphics().drawImage(image, xd * 128, yd * 128, null);
                            }
                        }
                        ImageIO.write(biDown, "jpg", out);
                    }
                }
            }
            catch (IOException ex) {
                throw new ServletException(ex);
            }
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
