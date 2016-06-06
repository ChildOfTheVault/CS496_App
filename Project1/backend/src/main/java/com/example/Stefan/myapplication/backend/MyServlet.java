/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.Stefan.myapplication.backend;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.*;

public class MyServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Please use the form to POST to this url");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String op = req.getParameter("op");
        if (op == null)
            op = "list";
        PersistenceManager pm = PMF.getPMF().getPersistenceManager();
        try {
            switch (op) {
                case Constants.GetPollByKey:
                    handleGetPoll(req, out, pm);
                    break;
                case Constants.CreatePollKey:
                    handleCreatePoll(req, out, pm);
                    break;
                case Constants.GetAllPollsKey:
                    handleGetAllPoll(req, out, pm);
                    break;
                case Constants.GetPollByUserKey:
                    handleGetPolls(req, out, pm);
                    break;
                case Constants.PurgePollsKey:
                    break;
                default:
                    throw new IllegalArgumentException("Invalid op parameter");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                pm.currentTransaction().rollback();
            } catch (Exception ignoreThis) {
            }
            out.write("{\"error\":" + new Gson().toJson(ex.getMessage()) + "}");
        } finally {
            pm.close();
        }
    }

    private void handleCreatePoll(HttpServletRequest req, PrintWriter out, PersistenceManager pm) {
        try {
            String user = req.getParameter(Constants.UserNameKey);
            if (user == null || user.length() == 0){
                throw new IllegalArgumentException("Invalid user \""+ user+"\"");
            }

            String poll = req.getParameter(Constants.Poll);
            if (poll == null || poll.length() == 0){
                throw new IllegalArgumentException("Invalid poll Name \""+ poll+"\"");
            }

            // we could have multiple groups, each group locked separately,
            // but let's just have one for this project; create if not exists
            PollGroup group = PollGroup.create(Constants.AllPollsGroup, pm);

            Poll newPoll = new Poll();
            newPoll.setEntityGroup(group.getKey());
            newPoll.updateDate();
            newPoll.addUser(user);
            newPoll.setSerialPoll(poll);

            pm.makePersistent(newPoll);
            out.write(newPoll.getID().toString());//formatAsJson()
            //out.write(newPoll.getID());
        } catch (IllegalArgumentException iae) {
            out.write(formatAsJson(iae));
        } finally {
            pm.close();
        }
    }

    private void handleGetPolls(HttpServletRequest req, PrintWriter out, PersistenceManager pm) {
        try {
            String user = req.getParameter(Constants.UserNameKey);
            System.out.println("USER: "+user);
            if (user == null || user.length() == 0){
                throw new IllegalArgumentException("Invalid user \""+ user+"\"");
            }
            Query q = pm.newQuery(Poll.class, "activeUsers.contains(:user)");
            List<Poll> results = (List<Poll>) q.execute(user);
            results.size();
            if (results != null)
                out.write(formatAsJson(results));
            else{
                results = new ArrayList<Poll>();
                System.out.println("RESULTS WAS NULL");
                out.write(formatAsJson(results));
            }
        } catch (IllegalArgumentException iae) {
            out.write(formatAsJson(iae));
        } finally {
            pm.close();
        }
    }
    private void handleGetAllPoll(HttpServletRequest req, PrintWriter out, PersistenceManager pm) {
//        String modDate = req.getParameter(Constants.GetAllPollsKey);
//        System.out.println(modDate);
//        if (modDate == null || modDate.length() == 0){
//            new IllegalArgumentException("Invalid modDate \""+ modDate+"\"");
//        }
        Query q = pm.newQuery(Poll.class);
        q.setOrdering("id");

        try {
            List<Poll> results = (List<Poll>) q.execute();
            System.out.println(results.toString());
            results.size();
            if (!results.isEmpty()) {;
                out.write(formatAsJson(results));
                System.out.println(formatAsJson(results));
            } else {
                System.err.println("RESULTS WAS NULL");
                out.write("NULL");
            }
        } catch (IllegalArgumentException iae) {
            out.write(formatAsJson(iae));
        } finally {
            q.closeAll();
        }
    }
    private void handleGetPoll(HttpServletRequest req, PrintWriter out, PersistenceManager pm) {
        String pollID = req.getParameter(Constants.PollID);
        System.out.println(pollID);
        if (pollID == null || pollID.length() == 0){
            throw new IllegalArgumentException("Invalid Poll ID \""+ pollID+"\"");
        }
        Query q = pm.newQuery(Poll.class);
        q.setFilter("id == pollID");
        q.declareParameters("Long pollID");
//        Key k = KeyFactory.createKey(Poll.class.getSimpleName(), new Long(pollID));
//        Poll p = pm.getObjectById(Poll.class, k);
//        if (p != null){
//            out.write(formatAsJson(p));
//            System.out.println(formatAsJson(p));
//        }
//        else {
//            System.err.println("RESULTS WAS NULL");
//            out.write("NULL");
//        }

        try {
            List<Poll> results = (List<Poll>) q.execute(new Long(pollID));
            System.out.println(results.toString());
            results.size();
            if (!results.isEmpty()) {
                Poll p = results.get(0);
                out.write(formatAsJson(p));
                System.out.println(formatAsJson(p));
            } else {
                System.err.println("RESULTS WAS NULL");
                out.write("NULL");
            }
        } catch (IllegalArgumentException iae) {
            out.write(formatAsJson(iae));
        } finally {
            q.closeAll();
        }
    }

    public static String formatAsJson(Exception e) {
        HashMap<String, String> obj = new HashMap<String, String>();
        obj.put("errormsg", e.getMessage());

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String rv = gson.toJson(obj);
        return rv;
    }
    public static String formatAsJson(User user) {
        HashMap<String, String> obj = new HashMap<String, String>();
//        obj.put("id", Long.toString(user.getID()));
        obj.put("name", user.getName());
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String rv = gson.toJson(obj);
        return rv;
    }
    public static String formatAsJson(Poll poll) {
        HashMap<String, String> obj = new HashMap<String, String>();
        obj.put(Constants.PollID, poll.getID().toString());
        obj.put(Constants.PollGroup, poll.getEntityGroup().toString());
        obj.put(Constants.PollModDate, poll.getModifiedDate().toString());
        obj.put(Constants.ActiveUsers, poll.getActiveUsers().toString());
        obj.put(Constants.Poll, poll.getSerialPoll());

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        String rv = gson.toJson(obj);
        return rv;
    }
    public static String formatAsJson(List<Poll> polls){
        List<String> serializedpolls = new ArrayList<String>();
        for(Poll poll : polls){
//            String jsonPoll = formatAsJson();
            System.out.println(poll.getSerialPoll());
            serializedpolls.add(poll.getSerialPoll());
        }
        Type listOfStrings = new TypeToken<ArrayList<String>>(){}.getType();
        Gson gson = new Gson();
        String rv = gson.toJson(serializedpolls, listOfStrings);
        System.out.println(rv);
        return rv;
    }
//    public static String formatAsJson(List<Poll> results) {
//        HashMap<String, HashMap<String, String>> obj = new HashMap<String, HashMap<String, String>>();
//        for (Poll poll : results) {
//
//            HashMap<String, String> innerObj = new HashMap<String, String>();
//            innerObj.put(Constants.PollID, poll.getID().toString());
//            innerObj.put(Constants.PollGroup, poll.getEntityGroup().toString());
//            innerObj.put(Constants.PollModDate, poll.getModifiedDate().toString());
//            innerObj.put(Constants.ActiveUsers, poll.getActiveUsers().toString());
//            innerObj.put(Constants.Poll, poll.getSerialPoll());
//            obj.put(poll.getID().toString(), innerObj);
//        }
//
//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder.create();
//
//        String rv = gson.toJson(obj);
//        return rv;
//    }
}