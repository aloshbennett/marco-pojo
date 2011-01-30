/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package marcopojo.utils;

import java.util.ArrayList;
import marcopojo.test.Tester;

/**
 *
 * @author alosh
 */
public class ObjectBrocker {

    private static Object payload;

    public static Object getPayload() {
        //return payload;
        Tester tester = new Tester();
        tester.setAge(25);
        tester.setName("Peter");

        ArrayList<String> books = new ArrayList<String>();
        books.add("Steppenwolf");
        books.add("Made in Japan");

        tester.setBooks(books);
        return tester;
    }

    public static void setPayload(Object obj) {
        payload = obj;
    }

}
