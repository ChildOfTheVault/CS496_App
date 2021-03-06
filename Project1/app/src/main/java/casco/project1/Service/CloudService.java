package casco.project1.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import casco.project1.Utilities.HttpPost;
import casco.project1.dataBackend.Constants;
import casco.project1.dataBackend.LocalDataStore;
import casco.project1.dataBackend.Poll;

public class CloudService extends Service {
    private String userID;
    private int refreshRate = 1000* 60;

    public CloudService() {
    }

    LocalDataStore populator;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new CloudServiceBinder();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        populator = new LocalDataStore();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                removePolls();
            }
        }, 1, 10000);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                uploadPolls();
            }
        }, 1, 10000);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updatePolls();
            }
        }, 1, refreshRate);

        return START_STICKY;
    }

    public class CloudServiceBinder extends Binder {
        public CloudService getService() {
            return CloudService.this;
        }
    }

    public interface Callback {

    }

    private Callback callback;

    public void setListener(Callback callback) {
        this.callback = callback;
    }

    public void setUserID(String id) {
        userID = id;
    }

    private class PollRemover extends Thread {

        public void run() {
            List<String> polls = populator.removedPolls(getApplicationContext());
            for (String poll:polls) {
                Log.d("MELLO", "Poll: " + poll);

                // Todo: Send these to the cloud.
                System.err.println("Removing poll with Key: " + poll + "");
                String json;
                try {
                    HttpPost post = new HttpPost(Constants.BASE_URL, Constants.ENC);
                    post.addFormField("op", "removeUser");
                    post.addFormField("key", poll);
                    post.addFormField("username", userID);
                    json = post.finish();

                    File dir = getFilesDir();
                    File file = new File(dir, poll + ".remove");
                    file.delete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    json = null;
                }
            }
        }

        public void cleanup() {

        }
    }

    public void removePolls() {
        PollRemover remover = new PollRemover();
        remover.start();
    }

    private class PollUploader extends Thread {

        public void run() {
            Log.d("CHANG", "Uploader thread started.");
            List<Poll> polls = populator.newPolls(getApplicationContext());
            for (Poll poll:polls) {
                Log.d("CHANG", "New Poll to upload: " + poll.getLongCode());

                // Todo: send these to the cloud
                String key = "";
                System.err.println("Creating Poll: " + poll.getLongCode() + "");
                try {
                    Log.d("CHANG", "Before first HTTP request.");
                    HttpPost post = new HttpPost(Constants.BASE_URL, Constants.ENC);
                    post.addFormField("op", "createPoll");
                    post.addFormField("username", poll.getCreator().getName());
                    Gson gson = new Gson();
                    String json = gson.toJson(poll);
                    post.addFormField("poll", json);
                    key = post.finish();
                    Log.d("CHANG", "After first HTTP request.");
                    key = key.replaceAll("\\D+","");
                    poll.setLongCode(key);

                    Context c = getApplicationContext();
                    String baseFileName = poll.getTitle();
                    File dir = getFilesDir();
                    File file = new File(dir, baseFileName + ".new");
                    file.delete();
                    populator.savePoll(c, poll, true);

                    System.err.println("Getting Poll by Key: " + key + "");
                    try {
                        Log.d("CHANG", "Before second HTTP request.");
                        post = new HttpPost(Constants.BASE_URL, Constants.ENC);
                        post.addFormField("op", "updatePollKey");
                        post.addFormField("key", key);
                        post.addFormField("username", userID);
                        gson = new Gson();
                        String jsonOut = gson.toJson( poll );
                        post.addFormField("poll", jsonOut);
                        json = post.finish();
                        Log.d("CHANG", "After second HTTP request.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        json = null;
                    }
                    Log.d("CHANG", "First poll uploaded.");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.err.println("Created Poll "+poll.getLongCode()+ " on Server");
            }
        }

        public void cleanup() {

        }
    }

    public void uploadPolls() {
        PollUploader uploader = new PollUploader();
        uploader.start();
    }

    private class PollUpdater extends Thread {
         public void run() {
             // Todo: get updates from the cloud
             String json;
             try {
                 HttpPost post = new HttpPost(Constants.BASE_URL, Constants.ENC);
                 post.addFormField("op", "getPollByUser");
                 post.addFormField("username", userID);
                 json = post.finish();
//			System.out.println(json);
             } catch (Exception ex) {
                 ex.printStackTrace();
                 json = null;
             }
             if (json != null){
//			System.out.println(json);
                 Type listType = new TypeToken<ArrayList<String>>() {
                 }.getType();
                 List<String> SerializedPollList = new Gson().fromJson(json, listType);
                 List<Poll> pollList = new ArrayList<Poll>();
                 for (String jsPoll : SerializedPollList){
                     Poll p = new Gson().fromJson(jsPoll, Poll.class);
                     pollList.add(p);
                 }

                 for (Poll poll: pollList) {
                     populator.savePoll(getApplicationContext(), poll, true);
                 }
             }
         }

        public void cleanup() {

        }
    }

    public void updatePolls() {
        if (userID != "") {
            PollUpdater updater = new PollUpdater();
            updater.start();
        }
    }
}
