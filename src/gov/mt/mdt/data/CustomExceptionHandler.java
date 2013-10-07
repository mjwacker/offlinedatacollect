/* Copyright (c) 2013 The Montana Department of Transportation
 * This class is borrowed from suggestion at: 
 * http://stackoverflow.com/questions/601503/how-do-i-obtain-crash-data-from-my-android-application
 * With some modifications.
 */
package gov.mt.mdt.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler defaultUEH;

    private String localPath;

    //private String url;

    /* 
     * if any of the parameters is null, the respective functionality 
     * will not be used 
     */
    // Changed constructor to deal with just a file path.
    //public CustomExceptionHandler(String localPath, String url) {
    public CustomExceptionHandler(String localPath){  
    	this.localPath = localPath;
        //this.url = url;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {
        //String timestamp = TimestampFormatter.getInstance().getTimestamp();
        SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timestamp = sdate.format(new Date());
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = "crash" + timestamp + ".txt";

        if (localPath != null) {
            writeToFile(stacktrace, filename);
        }
       // if (url != null) {
            //sendToServer(stacktrace, filename);
       // }

        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                    localPath + "/" + filename));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Method to send report to server, we won't implement this for now
     */
    /**
    private void sendToServer(String stacktrace, String filename) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("filename", filename));
        nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
        try {
            httpPost.setEntity(
                    new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   
}**/
}
