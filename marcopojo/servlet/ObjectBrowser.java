package marcopojo.servlet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import marcopojo.utils.ObjectBrocker;

/**
 *
 * @author agbennet
 */
public class ObjectBrowser extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    private List<ObjectRef> callstack;
    private List<ObjectRef> childFields;
    private Object root;

    @Override
    public void init() throws ServletException {
        super.init();
        String modelName = "TestModel";
        callstack = new ArrayList<ObjectRef>();
        childFields = new ArrayList<ObjectRef>();
        root = ObjectBrocker.getPayload();

    }



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        printHeader(out);
        int objectId = getParamId(request.getParameter("objectid"));
        if(getRef(callstack, objectId)!=null) {
            ObjectRef ref = getRef(callstack, objectId);
            callstack=callstack.subList(0, callstack.lastIndexOf(ref)+1);
            constructParentLink(out);
            constructObjectPage(out, ref.object, ref.name);
        }
        else if(getRef(childFields, objectId)!=null) {
            ObjectRef ref = getRef(childFields, objectId);
            callstack.add(ref);
            constructParentLink(out);
            constructObjectPage(out, ref.object, ref.name);
        }
        else
        {
            constructParentLink(out);
            constructObjectPage(out, root, "root");
        }
    }


    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void printBr(PrintWriter out) {
        out.println("<br>");
    }

    private void printBr(PrintWriter out, int count) {
        for(int i=0; i< count; i++)
            printBr(out);
    }

    private void printHeader(PrintWriter out) {
        out.println("<html><body>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }

    private int getParamId(String parameter) {
        int objectId =0;
        if(parameter == null || parameter.equals(""))
            return objectId;
        try {
            objectId = Integer.parseInt(parameter);
        }catch(Exception e) {
        }
        return objectId;
    }

    private void constructObjectPage(PrintWriter out, Object currentObject, String objName) {
        Class dataClass = currentObject.getClass();
        out.println("Title: "+objName);
        printBr(out, 2);
        childFields = new ArrayList<ObjectRef>();
        if(currentObject instanceof List) {
            int count=0;
            for(Object value: (List)currentObject){
                String name = ""+count++;
                ObjectRef ref = printMember(out, value, name);
                if(ref!=null)
                    childFields.add(ref);
            }
        }else if(currentObject instanceof Map) {
            for(Object key: ((Map)currentObject).keySet()) {

            }
        }else{
            for(Field field: dataClass.getDeclaredFields()){
                String name = field.getName();
                Object value = getValue(currentObject, field);
                ObjectRef ref = printMember(out, value, name);
                if(ref!=null)
                    childFields.add(ref);
            }
            printBr(out);

        }

    }

    private ObjectRef printMember(PrintWriter out, Object value, String name) {
        out.print(value.getClass().getSimpleName()+" ");
        ObjectRef ref = null;
        if(isComplex(value.getClass().getName())) {
            ref = new ObjectRef(value, name);
            printLink(out, ref);
        }
        else
            out.println(name+": "+value);
        printBr(out);
        return ref;
    }

    private Object getValue(Object currentObject, Field field) {
        field.setAccessible(true);
        Object value = null;
        try {
            value = field.get(currentObject);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ObjectBrowser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ObjectBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(value == null)
            value = "";
        return value;
    }

    private boolean isComplex(String name) {
        if(name.equals("java.lang.String"))
            return false;
        if(name.equals("java.lang.Boolean"))
            return false;
        return true;
    }

    private void constructParentLink(PrintWriter out) {
        printRootLink(out);
        for(ObjectRef parent : callstack) {
           out.print(" -> ");
           printLink(out, parent);
        }
        printBr(out,2);
    }

    private void printLink(PrintWriter out, ObjectRef ref) {
       out.print("<a href=\"?objectid="+ref.object.hashCode()+"\">"+ref.name+"</a>");
    }

    private void printRootLink(PrintWriter out) {
       out.print("<a href=\"?objectid=0\">root</a>");
    }

    private ObjectRef getRef(List<ObjectRef> refList, int objectId) {
        for(ObjectRef ref: refList) {
            if(ref.object.hashCode()==objectId)
                return ref;
        }
        return null;
    }

    class ObjectRef {
        Object object;
        String name;
        ObjectRef(Object object, String name) {
            this.object = object;
            this.name = name;
        }

        public int hashcode() {
            return object.hashCode();
        }
    }

}