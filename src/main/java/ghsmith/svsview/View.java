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
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

/**
 *
 * @author geoffrey.smith@emory.edu
 */
@WebServlet(name = "View", urlPatterns = {"/*"})
public class View extends HttpServlet {

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

        response.setContentType("text/html;charset=UTF-8");

        SVSFile[] svsFileCache = (SVSFile[])request.getSession().getAttribute("svsFileCache");
        Integer index = (Integer)request.getSession().getAttribute("index");
        if(svsFileCache ==  null || index == null) {
            svsFileCache = new SVSFile[10];
            index = 0;
        }
        else {
            index = (index + 1) % 10;
        }
        request.getSession().setAttribute("svsFileCache", svsFileCache);
        request.getSession().setAttribute("index", index);
        
        SVSFile svsFile = svsFileCache[index];
        if(svsFile != null && svsFile.fc != null && svsFile.fc.isOpen()) {
            svsFile.fc.close();
        }
        svsFile = new SVSFile(FileChannel.open(Paths.get("//euh/ehc/apps/prd-sectraPathology/data/Scanners.qc_pending" + request.getPathInfo())));
        svsFile.name = request.getPathInfo();
        svsFileCache[index] = svsFile;
        
        int width = svsFile.tiffDirList.get(0).widthInTiles;
        int height = svsFile.tiffDirList.get(0).heightInTiles;
        int minLevel = 13;
        if(svsFile.tiffDirList.size() == 7) {
            minLevel = 12;
        }
        int maxLevel = 18;
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head>");
            out.println(String.format("<title>[%s] %s</title>", index, request.getPathInfo().replaceAll("^.*/(.*).svs$", "$1")));
            out.println("<script src='https://cdnjs.cloudflare.com/ajax/libs/openseadragon/5.0.1/openseadragon.min.js' integrity='sha512-gPZzE+sKmE0kvcjMxW431ef5b5T5QOADV9Gij0isPw2oLATd1IZW7dmDmKh7F2e5BfwjQyAfFp3/OF0fVMOF7Q==' crossorigin='anonymous' referrerpolicy='no-referrer'></script>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div id='view'></div>");
            out.println(              "<script>");
            out.println(              "  var viewer = OpenSeadragon({");
            out.println(              "    id: 'view',");
            out.println(              "    showNavigationControl: false,");
            out.println(              "    tileSources: {");
            out.println(String.format("      width:  %s*256,", width));
            out.println(String.format("      height: %s*256,", height));
            out.println(              "      tileSize: 256,");
            out.println(String.format("      minLevel: %s,", minLevel));
            out.println(              "      maxLevel: 18,");
            out.println(              "      getTileUrl: function(level, x, y) {");
            out.println(String.format("        return '%s/GetTile?index=%s&level=' + level + '&x=' + x + '&y=' + y + '&name=%s';", request.getContextPath(), index, request.getPathInfo()));
            out.println(              "      }");
            out.println(              "    }");
            out.println(              "  });");
            out.println(              "</script>");
            out.println("</body>");
            out.println("</html>");
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
